package kit.penny.tdlib.client;

import kit.penny.tdlib.updates.TelegramAuthorizationManager;
import kit.penny.tdlib.updates.internal.TdlibUpdateDispatcher;
import kit.penny.tdlib.query.ITdlibQueryResultHandler;
import kit.penny.tdlib.query.TdlibResponse;
import kit.penny.tdlib.exception.TdlibConfigurationException;
import kit.penny.tdlib.exception.TdlibException;
import kit.penny.tdlib.properties.TelegramProperties;
import jakarta.annotation.PreDestroy;
import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.springframework.util.StringUtils.hasText;

/**
 * Telegram client component. Wrapper of native {@link Client} with authorization logic and notification handlers.
 *
 * @author Pavel Grushin
 */
public class TelegramClient {

    private final Logger log = LoggerFactory.getLogger(TelegramClient.class);

    private final Client client;

    private final TelegramAuthorizationManager telegramAuthorizationManager;

    private final TdlibUpdateDispatcher updateDispatcher;


    public TelegramClient(TelegramProperties properties,
                          TdlibUpdateDispatcher updateDispatcher,
                          TelegramAuthorizationManager authorizationManager) {
        this(properties, updateDispatcher, authorizationManager, null);
    }

    /**
     * @param properties TDlib client properties
     * @param updateDispatcher registered update dispatcher
     * @param ITelegramAuthorizationManager authorization state of the client
     */
    TelegramClient(TelegramProperties properties,
                          TdlibUpdateDispatcher updateDispatcher,
                          TelegramAuthorizationManager ITelegramAuthorizationManager,
                          Client client) {
        this.updateDispatcher = updateDispatcher;
        this.telegramAuthorizationManager = ITelegramAuthorizationManager;
        checkProperties(properties);
        this.client = client != null
                ? client
                : initializeNativeClient(properties);
    }

    private void checkProperties(TelegramProperties properties) {
        if (properties.phone() == null) {
            throw new TdlibConfigurationException("The phone number of the user not filled. " +
                    "Specify property spring.telegram.client.phone");
        }
        if (properties.apiId() == 0) {
            throw new TdlibConfigurationException("Application identifier for Telegram API access is invalid. " +
                    "Specify property spring.telegram.client.api-id");
        }
        if (!hasText(properties.apiHash())) {
            throw new TdlibConfigurationException("Application identifier hash for Telegram API access is invalid. " +
                    "Specify property spring.telegram.client.api-hash");
        }
        if (!hasText(properties.databaseEncryptionKey())) {
            throw new TdlibConfigurationException("Encryption key for the database is invalid. " +
                    "Specify property spring.telegram.client.database-encryption-key");
        }
        if (!hasText(properties.systemLanguageCode())) {
            throw new TdlibConfigurationException("IETF language tag of the user's operating system language; must be non-empty. " +
                    "Specify property spring.telegram.client.system-language-code");
        }
        if (!hasText(properties.deviceModel())) {
            throw new TdlibConfigurationException("Model of the device the application is being run on; must be non-empty. " +
                    "Specify property spring.telegram.client.device-model");
        }
        TelegramProperties.Proxy proxy = properties.proxy();
        if (proxy != null) {
            checkProxyProperties(proxy);
        }
    }

    private static void checkProxyProperties(TelegramProperties.Proxy proxy) {
        if (!hasText(proxy.server()) || proxy.port() <= 0) {
            throw new TdlibConfigurationException("""
                    Proxy settings not filled. Specify properties:
                     spring.telegram.client.proxy.server
                     spring.telegram.client.proxy.port
                    """);
        }
        TelegramProperties.Proxy.ProxyHttp http = proxy.http();
        TelegramProperties.Proxy.ProxySocks5 socks5 = proxy.socks5();
        TelegramProperties.Proxy.ProxyMtProto mtProto = proxy.mtproto();
        if (http != null) {
            if (!hasText(http.password()) || !hasText(http.username())) {
                throw new TdlibConfigurationException("""
                        Http proxy settings not filled. Specify properties:
                         spring.telegram.client.proxy.http.username
                         spring.telegram.client.proxy.http.password
                         spring.telegram.client.proxy.http.http-only
                        """);
            }
        } else if (socks5 != null) {
            if (!hasText(socks5.username()) || !hasText(socks5.password())) {
                throw new TdlibConfigurationException("""
                        Socks5 proxy settings not filled. Specify properties:
                         spring.telegram.client.proxy.socks5.username
                         spring.telegram.client.proxy.socks5.password
                        """);
            }
        } else if (mtProto != null) {
            if (!hasText(mtProto.secret())) {
                throw new TdlibConfigurationException("MtProto proxy settings not filled. " +
                        "Specify property spring.telegram.client.proxy.mtProto.secret");
            }
        } else {
            throw new TdlibConfigurationException("ProxyType not filled. Available types - http, socks5, mtproto");
        }
    }

    private Client initializeNativeClient(TelegramProperties properties) {
        var logVerbosityLevel = new TdApi.SetLogVerbosityLevel(properties.logVerbosityLevel());
        try {
            Client.execute(logVerbosityLevel);
        } catch (Client.ExecutionException e) {
            logError(logVerbosityLevel, e.error);
            throw new TdlibException(
                    "Failed to configure TDLib log verbosity.",
                    e,
                    e.error,
                    logVerbosityLevel
            );
        }
        Client.LogMessageHandler logMessageHandler = (level, message) -> {
            switch (level) {
                case 0 -> log.error(message);
                case 1 -> log.warn(message);
                case 2 -> log.info(message);
                default -> log.debug(message);
            }
        };
        Client.setLogMessageHandler(properties.logVerbosityLevel(), logMessageHandler);

        return Client.create(updateDispatcher, null, null);
    }

    /**
     * {@link TelegramClient} shutdown hook.
     * Properly closing the client.
     */
    @PreDestroy
    void cleanUp() {

        if (!telegramAuthorizationManager.isStateClosed()) {
            var close = new TdApi.Close();

            try {
                sendWithCallback(close, (result, error) -> {
                    if (error != null) {
                        logError(close, error);
                    }
                });

                telegramAuthorizationManager
                        .closedFuture()
                        .get(30, TimeUnit.SECONDS);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("TDLib client shutdown was interrupted.", e);

            } catch (TimeoutException e) {
                log.warn(
                        "TDLib client did not reach CLOSED state within 30 seconds.",
                        e
                );

            } catch (ExecutionException e) {
                log.warn("TDLib client shutdown failed.", e);

            } catch (RuntimeException e) {
                log.warn("Failed to send TDLib Close request.", e);
            }
        } else {
            log.info("TDLib client is already closed.");
        }

        log.info("Goodbye!");
    }

    /**
     * Sends a request to the TDLib.
     *
     * @param query object representing a query to the TDLib.
     * @throws NullPointerException if query is null.
     * @return {@link TdlibResponse <T>} response.
     */
    public <T extends TdApi.Object> TdlibResponse<T> send(
            TdApi.Function<T> query) {

        Objects.requireNonNull(query);

        try {
            return sendAsync(query).get(30, TimeUnit.SECONDS);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TdlibException("TDLib request interrupted.", e);

        } catch (TimeoutException e) {
            var error = new TdApi.Error(0, "TDLib request timeout.");
            logError(query, error);
            return new TdlibResponse<>(null, error);

        } catch (ExecutionException e) {
            if (e.getCause() instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }

            throw new TdlibException(
                    "TDLib request failed.",
                    e.getCause()
            );
        }
    }

    /**
     * Sends a request to the TDLib asynchronously.
     * If this stage completes exceptionally you can handle cause {@link TdlibException}
     *
     * @throws NullPointerException if query is null.
     * @param query object representing a query to the TDLib.
     * @return {@link CompletableFuture<TdlibResponse>} response from TDLib.
     */
    public <T extends TdApi.Object> CompletableFuture<TdlibResponse<T>> sendAsync(
            TdApi.Function<T> query) {

        Objects.requireNonNull(query);
        var future = new CompletableFuture<TdlibResponse<T>>();
        try {
            sendWithCallback(query, (obj, error) -> {
                try {
                    if (error != null) {
                        logError(query, error);
                    }

                    future.complete(new TdlibResponse<>(obj, error));
                } catch (RuntimeException e) {
                    future.completeExceptionally(e);
                }
            });
        } catch (RuntimeException e) {
            future.completeExceptionally(e);
        }

        return future;
    }

    private void logError(TdApi.Function<?> query, TdApi.Error error) {
        String errorLogString = String.format("""
                TDLib error:
                [
                    code: %d,
                    message: %s
                    queryIdentifier: %d
                ]
                """, error.code, error.message, query.getConstructor());
        log.error(errorLogString);
    }

    /**
     * Sends a request to the TDLib with callback.
     *
     * @param query object representing a query to the TDLib
     * @param resultHandler Result handler for results of queries with callback to TDLib
     * @param <T> The object type that is returned by the function
     */
    @SuppressWarnings("unchecked")
    public <T extends TdApi.Object> void sendWithCallback(
            TdApi.Function<T> query,
            ITdlibQueryResultHandler<T> resultHandler) {

        Objects.requireNonNull(query);
        Objects.requireNonNull(resultHandler);

        client.send(query, object -> {
            if (object instanceof TdApi.Error err) {
                resultHandler.onResult(null, err);
            } else {
                resultHandler.onResult((T) object, null);
            }
        });
    }

}
