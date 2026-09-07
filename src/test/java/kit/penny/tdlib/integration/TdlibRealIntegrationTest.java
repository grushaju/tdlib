package kit.penny.tdlib.integration;

import kit.penny.tdlib.client.TelegramClient;
import kit.penny.tdlib.service.TelegramChatService;
import kit.penny.tdlib.service.TelegramUserService;
import kit.penny.tdlib.updates.ITelegramAuthorizationManager;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.drinkless.tdlib.TdApi;

import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Tag("integration")
@EnabledIfSystemProperty(
        named = "tdlib.integration",
        matches = "true"
)
public final class TdlibRealIntegrationTest {

    private static final Duration AUTHORIZATION_TIMEOUT =
            Duration.ofMinutes(5);

    private static final Duration REQUEST_TIMEOUT =
            Duration.ofSeconds(30);

    private static final int API_ID = 94575;
    private static final String API_HASH =
            "a3406de8d171bb422bb6ddf3bbd800e2";

    private TdlibRealIntegrationTest() {
    }

    static void runIntegrationTest() throws Exception {

        ConsoleInput console = new ConsoleInput();

        System.out.println();
        System.out.println("========================================");
        System.out.println(" TDLib real integration test");
        System.out.println("========================================");
        System.out.println();

        System.out.println();
        System.out.println("Enter Telegram account A");
        String phoneA = console.readLine(
                "Phone number A: "
        );

        System.out.println();
        System.out.println("Enter Telegram account B");
        String phoneB = console.readLine(
                "Phone number B: "
        );

        Path root = Path.of(
                System.getProperty("java.io.tmpdir"),
                "tdlib-integration-test"
        );

        Path databaseA = root.resolve("account-a/database");
        Path filesA = root.resolve("account-a/files");

        Path databaseB = root.resolve("account-b/database");
        Path filesB = root.resolve("account-b/files");

        System.out.println();
        System.out.println("Creating Telegram context A...");

        try (
                AnnotationConfigApplicationContext contextA =
                        TdlibTestContextConfiguration.create(
                                API_ID,
                                API_HASH,
                                phoneA,
                                databaseA,
                                filesA
                        )
        ) {

            System.out.println("Creating Telegram context B...");

            try (
                    AnnotationConfigApplicationContext contextB =
                            TdlibTestContextConfiguration.create(
                                    API_ID,
                                    API_HASH,
                                    phoneB,
                                    databaseB,
                                    filesB
                            )
            ) {

                System.out.println();
                System.out.println("========================================");
                System.out.println(" Authorizing account A");
                System.out.println("========================================");

                authorize(contextA, console, "A");

                System.out.println();
                System.out.println("========================================");
                System.out.println(" Authorizing account B");
                System.out.println("========================================");

                authorize(contextB, console, "B");

                System.out.println();
                System.out.println("========================================");
                System.out.println(" Account A");
                System.out.println("========================================");

                inspectAccount(contextA);

                System.out.println();
                System.out.println("========================================");
                System.out.println(" Account B");
                System.out.println("========================================");

                inspectAccount(contextB);

                System.out.println();
                System.out.println("========================================");
                System.out.println(" Logging out account A");
                System.out.println("========================================");

                logout(contextA);

                System.out.println();
                System.out.println("========================================");
                System.out.println(" Logging out account B");
                System.out.println("========================================");

                logout(contextB);

                System.out.println();
                System.out.println("========================================");
                System.out.println(" Integration test completed");
                System.out.println("========================================");
            }
        }
    }

    private static void authorize(
            AnnotationConfigApplicationContext context,
            ConsoleInput console,
            String accountName
    ) throws InterruptedException {

        ITelegramAuthorizationManager authorizationManager =
                context.getBean(ITelegramAuthorizationManager.class);

        long deadline =
                System.nanoTime()
                        + AUTHORIZATION_TIMEOUT.toNanos();

        while (!authorizationManager.haveAuthorization()) {

            if (authorizationManager.isStateClosed()) {
                throw new IllegalStateException(
                        "Telegram authorization state was closed for account "
                                + accountName
                );
            }

            if (System.nanoTime() > deadline) {
                throw new IllegalStateException(
                        "Authorization timeout for account "
                                + accountName
                );
            }

            if (authorizationManager.isWaitAuthenticationCode()) {

                String code = console.readSecret(
                        "Enter Telegram authentication code for account "
                                + accountName
                );

                authorizationManager.checkAuthenticationCode(code);

                continue;
            }

            if (authorizationManager.isWaitAuthenticationPassword()) {

                String password = console.readSecret(
                        "Enter Telegram 2FA password for account "
                                + accountName
                );

                authorizationManager.checkAuthenticationPassword(password);

                continue;
            }

            if (authorizationManager.isWaitEmailAddress()) {

                String email = console.readLine(
                        "Enter Telegram email for account "
                                + accountName
                                + ": "
                );

                authorizationManager.checkEmailAddress(email);

                continue;
            }

            Thread.sleep(250);
        }

        System.out.println(
                "Account " + accountName + " authorized."
        );
    }

    private static void inspectAccount(
            AnnotationConfigApplicationContext context
    ) throws Exception {

        TelegramUserService userService =
                context.getBean(TelegramUserService.class);

        TelegramChatService chatService =
                context.getBean(TelegramChatService.class);

        TdApi.User me =
                userService.getMe()
                        .get(REQUEST_TIMEOUT.toSeconds(), TimeUnit.SECONDS)
                        .getObjectOrThrow();

        System.out.println();
        System.out.println("Current user:");
        System.out.println("  id       = " + me.id);
        System.out.println("  firstName = " + me.firstName);
        System.out.println("  lastName  = " + me.lastName);

        if (me.usernames != null) {
            System.out.println(
                    "  usernames  = "
                            + (me.usernames.activeUsernames == null
                            ? "<none>"
                            : String.join(
                            ", ",
                            me.usernames.activeUsernames
                    ))
            );
        }

        System.out.println();
        System.out.println("Searching chats...");

        TdApi.Chats chats =
                chatService.getChats(
                                new TdApi.ChatListMain(),
                                20
                        )
                        .get(
                                REQUEST_TIMEOUT.toSeconds(),
                                TimeUnit.SECONDS
                        )
                        .getObjectOrThrow();

        System.out.println(
                "Chats returned: " + chats.chatIds.length
        );

        int printed = 0;

        for (long chatId : chats.chatIds) {

            TdApi.Chat chat =
                    chatService.getChat(chatId)
                            .get(
                                    REQUEST_TIMEOUT.toSeconds(),
                                    TimeUnit.SECONDS
                            )
                            .getObjectOrThrow();

            System.out.println(
                    "  [" + chat.id + "] "
                            + chat.title
                            + " (" + chat.type.getClass().getSimpleName() + ")"
            );

            printed++;

            if (printed >= 10) {
                break;
            }
        }

        System.out.println();
        System.out.println("Searching current user by phone...");

        TdApi.User searchedUser =
                userService.searchUserByPhoneNumber(
                                me.phoneNumber
                        )
                        .get(
                                REQUEST_TIMEOUT.toSeconds(),
                                TimeUnit.SECONDS
                        )
                        .getObjectOrThrow();

        System.out.println(
                "Found user: "
                        + searchedUser.id
                        + " "
                        + searchedUser.firstName
                        + " "
                        + searchedUser.lastName
        );

        System.out.println();
        System.out.println("Getting current user's full info...");

        TdApi.UserFullInfo fullInfo =
                userService.getUserFullInfo(me.id)
                        .get(
                                REQUEST_TIMEOUT.toSeconds(),
                                TimeUnit.SECONDS
                        )
                        .getObjectOrThrow();

        System.out.println(
                "Bio: "
                        + (fullInfo.bio == null
                        ? "<none>"
                        : fullInfo.bio.text)
        );
    }

    private static void logout(
            AnnotationConfigApplicationContext context
    ) throws Exception {

        TelegramClient telegramClient =
                context.getBean(TelegramClient.class);

        ITelegramAuthorizationManager authorizationManager =
                context.getBean(ITelegramAuthorizationManager.class);

        telegramClient
                .sendAsync(new TdApi.LogOut())
                .get(
                        REQUEST_TIMEOUT.toSeconds(),
                        TimeUnit.SECONDS
                )
                .getObjectOrThrow();

        long deadline =
                System.nanoTime()
                        + Duration.ofSeconds(30).toNanos();

        while (!authorizationManager.isStateClosed()) {

            if (System.nanoTime() > deadline) {
                throw new IllegalStateException(
                        "Timeout waiting for TDLib Closed state."
                );
            }

            Thread.sleep(100);
        }

        System.out.println("Logged out successfully.");
    }
}