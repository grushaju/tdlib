package kit.penny.tdlib.updates;

import kit.penny.tdlib.client.TelegramClient;
import kit.penny.tdlib.integration.ConsoleInput;
import kit.penny.tdlib.integration.TdlibTestContextConfiguration;
import org.drinkless.tdlib.TdApi;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class TdlibAuthorizationIntegrationTest {

    private static final int API_ID = 94575;
    private static final String API_HASH =
            "a3406de8d171bb422bb6ddf3bbd800e2";
    private static final String PHONE = "+79159326001";

    private static final Duration AUTHORIZATION_TIMEOUT =
            Duration.ofMinutes(5);

    private static final Duration LOGOUT_TIMEOUT =
            Duration.ofSeconds(30);

    public static void main(String[] args) {
        new TdlibAuthorizationIntegrationTest()
                .runIntegrationTest();
    }

    void runIntegrationTest() {
        Path testDirectory = createTestDirectory();

        Path databaseDirectory =
                testDirectory.resolve("database");

        Path filesDirectory =
                testDirectory.resolve("files");

        AnnotationConfigApplicationContext context =
                TdlibTestContextConfiguration.create(
                        API_ID,
                        API_HASH,
                        PHONE,
                        databaseDirectory,
                        filesDirectory
                );

        try {
            TelegramAuthorizationManager authorizationManager =
                    context.getBean(TelegramAuthorizationManager.class);

            TelegramClient telegramClient =
                    context.getBean(TelegramClient.class);

            ConsoleInput console = new ConsoleInput();

            authorize(
                    authorizationManager,
                    console
            );

            System.out.println();
            System.out.println("Telegram authorization completed.");

            logout(
                    telegramClient,
                    authorizationManager
            );

        } finally {
            context.close();
        }
    }

    private void authorize(
            TelegramAuthorizationManager authorizationManager,
            ConsoleInput console
    ) {
        Instant deadline =
                Instant.now().plus(AUTHORIZATION_TIMEOUT);

        System.out.println();
        System.out.println("========================================");
        System.out.println(" Telegram authorization");
        System.out.println("========================================");

        while (!authorizationManager.haveAuthorization()) {

            if (authorizationManager.isStateClosed()) {
                throw new IllegalStateException(
                        "TDLib authorization state was closed."
                );
            }

            if (Instant.now().isAfter(deadline)) {
                throw new IllegalStateException(
                        "Telegram authorization timed out."
                );
            }

            if (authorizationManager.isWaitAuthenticationCode()) {
                String code = console.readLine(
                        "Enter Telegram authentication code: "
                );

                authorizationManager.checkAuthenticationCode(code);

                continue;
            }

            if (authorizationManager.isWaitAuthenticationPassword()) {
                String password = console.readSecret(
                        "Enter Telegram 2FA password"
                );

                authorizationManager.checkAuthenticationPassword(password);

                continue;
            }

            if (authorizationManager.isWaitEmailAddress()) {
                String email = console.readLine(
                        "Enter Telegram email address: "
                );

                authorizationManager.checkEmailAddress(email);

                continue;
            }

            sleep(250);
        }
    }

    private void logout(
            TelegramClient telegramClient,
            TelegramAuthorizationManager authorizationManager
    ) {
        System.out.println();
        System.out.println("========================================");
        System.out.println(" Telegram logout");
        System.out.println("========================================");

        CompletableFuture<TdApi.Ok> logoutFuture =
                new CompletableFuture<>();

        telegramClient.sendWithCallback(
                new TdApi.LogOut(),
                (result, error) -> {
                    if (error != null) {
                        logoutFuture.completeExceptionally(
                                new IllegalStateException(
                                        "Telegram logout failed: " + error
                                )
                        );
                        return;
                    }

                    logoutFuture.complete(result);
                }
        );

        try {
            logoutFuture.get(
                    LOGOUT_TIMEOUT.toSeconds(),
                    TimeUnit.SECONDS
            );
        } catch (TimeoutException e) {
            throw new IllegalStateException(
                    "Telegram logout timed out.",
                    e
            );
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Telegram logout failed.",
                    e
            );
        }

        waitUntilLoggedOut(authorizationManager);

        if (authorizationManager.haveAuthorization()) {
            throw new IllegalStateException(
                    "Telegram authorization is still active after logout."
            );
        }

        System.out.println("Telegram logout completed.");
    }

    private void waitUntilLoggedOut(
            TelegramAuthorizationManager authorizationManager
    ) {
        Instant deadline =
                Instant.now().plus(LOGOUT_TIMEOUT);

        while (authorizationManager.haveAuthorization()) {

            if (authorizationManager.isStateClosed()) {
                throw new IllegalStateException(
                        "TDLib authorization state was closed during logout."
                );
            }

            if (Instant.now().isAfter(deadline)) {
                throw new IllegalStateException(
                        "Telegram logout timed out waiting for "
                                + "authorization state to reset."
                );
            }

            sleep(100);
        }
    }

    private Path createTestDirectory() {
        return Paths.get(".", "tdlib-authorization-test");
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Authorization test was interrupted.",
                    e
            );
        }
    }
}