package kit.penny.tdlib.updates;

import kit.penny.tdlib.client.TelegramClient;
import kit.penny.tdlib.exception.TdlibConfigurationException;
import kit.penny.tdlib.properties.TelegramProperties;
import kit.penny.tdlib.query.ITdlibQueryResultHandler;
import org.drinkless.tdlib.TdApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.springframework.util.StringUtils.hasText;

/**
 * Handler of {@link TdApi.AuthorizationState} updates.
 *
 * @author Pavel Grushin
 */
public final class UpdateAuthorizationState
        implements ITdlibUpdateListener<TdApi.UpdateAuthorizationState> {

    private static final Logger log =
            LoggerFactory.getLogger(UpdateAuthorizationState.class);

    private final TelegramProperties properties;

    private final TelegramClient telegramClient;

    private final TelegramAuthorizationManager authorizationManager;

    private final AuthorizationRequestHandler authorizationRequestHandler;

    private TdApi.AuthorizationState authorizationState;

    public UpdateAuthorizationState(
            TelegramProperties properties,
            TelegramClient telegramClient,
            TelegramAuthorizationManager authorizationManager) {

        this.properties = properties;
        this.telegramClient = telegramClient;
        this.authorizationManager = authorizationManager;
        this.authorizationRequestHandler = new AuthorizationRequestHandler();
    }

    @Override
    public void handleNotification(TdApi.UpdateAuthorizationState notification) {
        Optional.ofNullable(notification)
                .ifPresent(this::processNotification);
    }

    private void processNotification(
            TdApi.UpdateAuthorizationState notification) {

        TdApi.AuthorizationState newAuthorizationState =
                notification.authorizationState;

        if (newAuthorizationState != null) {
            this.authorizationState = newAuthorizationState;
        }

        if (this.authorizationState == null) {
            log.warn("Received authorization state update without authorization state");
            return;
        }

        switch (this.authorizationState.getConstructor()) {

            case TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR ->
                    setTdlibParameters();

            case TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR ->
                    sendAuthenticationPhoneNumber();

            case TdApi.AuthorizationStateWaitOtherDeviceConfirmation.CONSTRUCTOR ->
                    logOtherDeviceConfirmationLink();

            case TdApi.AuthorizationStateWaitCode.CONSTRUCTOR ->
                    waitForAuthenticationCode();

            case TdApi.AuthorizationStateWaitPassword.CONSTRUCTOR ->
                    waitForAuthenticationPassword();

            case TdApi.AuthorizationStateWaitEmailAddress.CONSTRUCTOR ->
                    waitForEmailAddress();

            case TdApi.AuthorizationStateWaitEmailCode.CONSTRUCTOR ->
                    waitForAuthenticationEmailCode();

            case TdApi.AuthorizationStateReady.CONSTRUCTOR ->
                    handleReady();

            case TdApi.AuthorizationStateLoggingOut.CONSTRUCTOR ->
                    resetAuthorization("Logging out");

            case TdApi.AuthorizationStateClosing.CONSTRUCTOR ->
                    resetAuthorization("Closing");

            case TdApi.AuthorizationStateClosed.CONSTRUCTOR ->
                    closeState();

            default ->
                    log.error(
                            "Unsupported authorization state:\n{}",
                            this.authorizationState);
        }
    }

    private void setTdlibParameters() {
        TdApi.SetTdlibParameters tdLibParameters = tdLibParameters();

        log.info(
                "TDLib application version: {}",
                tdLibParameters.applicationVersion);

        telegramClient.sendWithCallback(
                tdLibParameters,
                authorizationRequestHandler);

        TelegramProperties.Proxy proxy = properties.proxy();

        if (proxy != null) {
            addProxy(proxy);
        }
    }

    private void sendAuthenticationPhoneNumber() {
        var request =
                new TdApi.SetAuthenticationPhoneNumber(
                        properties.phone(),
                        null);

        telegramClient.sendWithCallback(
                request,
                authorizationRequestHandler);
    }

    private void logOtherDeviceConfirmationLink() {
        String link =
                ((TdApi.AuthorizationStateWaitOtherDeviceConfirmation)
                        authorizationState).link;

        log.info(
                "Please confirm this login link on another device: {}",
                link);
    }

    /**
     * Waits asynchronously for the authentication code.
     *
     * <p>No TDLib callback thread is blocked while waiting for user input.</p>
     */
    private void waitForAuthenticationCode() {
        log.info("Please enter authentication code");

        CompletableFuture<String> future =
                authorizationManager.awaitAuthenticationCode();

        future.thenAccept(this::sendAuthenticationCode);
    }

    private void sendAuthenticationCode(String code) {
        if (!hasText(code)) {
            return;
        }

        var request =
                new TdApi.CheckAuthenticationCode(code);

        telegramClient.sendWithCallback(
                request,
                authorizationRequestHandler);
    }

    /**
     * Waits asynchronously for the two-step verification password.
     */
    private void waitForAuthenticationPassword() {
        log.info("Please enter password");

        CompletableFuture<String> future =
                authorizationManager.awaitAuthenticationPassword();

        future.thenAccept(this::sendAuthenticationPassword);
    }

    private void sendAuthenticationPassword(String password) {
        if (!hasText(password)) {
            return;
        }

        var request =
                new TdApi.CheckAuthenticationPassword(password);

        telegramClient.sendWithCallback(
                request,
                authorizationRequestHandler);
    }

    /**
     * Waits asynchronously for the authentication email address.
     */
    private void waitForEmailAddress() {
        log.info("Please enter email");

        CompletableFuture<String> future =
                authorizationManager.awaitEmailAddress();

        future.thenAccept(this::sendAuthenticationEmailAddress);
    }

    private void sendAuthenticationEmailAddress(String email) {
        if (!hasText(email)) {
            return;
        }

        var request =
                new TdApi.SetAuthenticationEmailAddress(email);

        telegramClient.sendWithCallback(
                request,
                authorizationRequestHandler);
    }

    /**
     * Waits asynchronously for the authentication code received by email.
     */
    private void waitForAuthenticationEmailCode() {
        log.info("Please enter authentication code from email");

        CompletableFuture<String> future =
                authorizationManager.awaitAuthenticationCode();

        future.thenAccept(this::sendAuthenticationEmailCode);
    }

    private void sendAuthenticationEmailCode(String code) {
        if (!hasText(code)) {
            return;
        }

        var emailAuth =
                new TdApi.EmailAddressAuthenticationCode(code);

        var request =
                new TdApi.CheckAuthenticationEmailCode(emailAuth);

        telegramClient.sendWithCallback(
                request,
                authorizationRequestHandler);
    }

    private void handleReady() {
        authorizationManager.setAuthorized(true);
        log.info("Telegram authorization completed");
    }

    private void resetAuthorization(String logMessage) {
        authorizationManager.resetAuthorization();
        log.info(logMessage);
    }

    private void closeState() {
        authorizationManager.close();
        log.info("Closed");
    }

    @Override
    public Class<TdApi.UpdateAuthorizationState> notificationType() {
        return TdApi.UpdateAuthorizationState.class;
    }

    /**
     * Configure TDLib parameters.
     *
     * @return {@link TdApi.SetTdlibParameters}
     */
    private TdApi.SetTdlibParameters tdLibParameters() {
        boolean useTestDc = properties.useTestDc();

        String databaseDirectory =
                checkStringOrEmpty(properties.databaseDirectory());

        String filesDirectory =
                checkStringOrEmpty(properties.filesDirectory());

        byte[] databaseEncryptionKey =
                properties.databaseEncryptionKey()
                        .getBytes(StandardCharsets.UTF_8);

        boolean useFileDatabase =
                properties.useFileDatabase();

        boolean useChatInfoDatabase =
                properties.useChatInfoDatabase();

        boolean useMessageDatabase =
                properties.useMessageDatabase();

        boolean useSecretChats =
                properties.useSecretChats();

        int apiId = properties.apiId();

        String apiHash = properties.apiHash();

        String systemLanguageCode =
                properties.systemLanguageCode();

        String deviceModel =
                properties.deviceModel();

        String systemVersion =
                checkStringOrEmpty(properties.systemVersion());

        String applicationVersion =
                checkStringOrEmpty(properties.applicationVersion());

        return new TdApi.SetTdlibParameters(
                useTestDc,
                databaseDirectory,
                filesDirectory,
                databaseEncryptionKey,
                useFileDatabase,
                useChatInfoDatabase,
                useMessageDatabase,
                useSecretChats,
                apiId,
                apiHash,
                systemLanguageCode,
                deviceModel,
                systemVersion,
                applicationVersion
        );
    }

    /**
     * Configure and send proxy settings for TDLib.
     *
     * @param proxyProperties proxy properties
     */
    private void addProxy(TelegramProperties.Proxy proxyProperties) {
        TdApi.ProxyType proxyType =
                getProxyType(proxyProperties);

        var proxy =
                new TdApi.Proxy(
                        proxyProperties.server(),
                        proxyProperties.port(),
                        proxyType);

        var proxyTypeName =
                proxyType.getClass().getSimpleName();

        var addProxy =
                new TdApi.AddProxy(
                        proxy,
                        true,
                        proxyTypeName);

        telegramClient.sendWithCallback(
                addProxy,
                (obj, error) -> {
                    if (error == null) {
                        log.info(
                                "Proxy server: [server: {}, port: {}, type: {}]",
                                proxy.server,
                                proxy.port,
                                proxyTypeName);
                    } else {
                        log.error(
                                "Failed to configure proxy: {}",
                                error);
                    }
                });
    }

    private static TdApi.ProxyType getProxyType(
            TelegramProperties.Proxy proxy) {

        var http = proxy.http();
        var socks5 = proxy.socks5();
        var mtProto = proxy.mtproto();

        if (http != null) {
            return new TdApi.ProxyTypeHttp(
                    http.username(),
                    http.password(),
                    http.httpOnly());
        }

        if (socks5 != null) {
            return new TdApi.ProxyTypeSocks5(
                    socks5.username(),
                    socks5.password());
        }

        if (mtProto != null) {
            return new TdApi.ProxyTypeMtproto(
                    mtProto.secret());
        }

        throw new TdlibConfigurationException(
                "ProxyType not filled. Available types - http, socks5, mtProto");
    }

    private String checkStringOrEmpty(String value) {
        return hasText(value) ? value : "";
    }

    private final class AuthorizationRequestHandler
            implements ITdlibQueryResultHandler<TdApi.Ok> {

        @Override
        public void onResult(TdApi.Ok obj, TdApi.Error error) {
            if (error != null) {
                log.error(
                        "TDLib authorization request failed:\n{}",
                        error);
            }
        }
    }
}