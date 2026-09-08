package kit.penny.tdlib.updates;

import kit.penny.tdlib.client.TelegramClient;
import kit.penny.tdlib.integration.ConsoleInput;
import kit.penny.tdlib.integration.TdlibTestContextConfiguration;
import kit.penny.tdlib.service.TelegramChatService;
import kit.penny.tdlib.service.TelegramUserService;
import org.drinkless.tdlib.TdApi;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.nio.file.Path;
import java.nio.file.Paths;
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

    private static final Duration CLOSE_TIMEOUT =
            Duration.ofSeconds(30);

    private static final int API_ID = 94575;

    private static final String API_HASH =
            "a3406de8d171bb422bb6ddf3bbd800e2";

    private TdlibRealIntegrationTest() {
    }

    public static void main(String[] args) throws Exception {
        new TdlibRealIntegrationTest()
                .runIntegrationTest();
    }

    public void runIntegrationTest() throws Exception {

        ConsoleInput console = new ConsoleInput();

        System.out.println();
        System.out.println("========================================");
        System.out.println(" TDLib real integration test");
        System.out.println("========================================");
        System.out.println();

        System.out.println("Enter Telegram account A");
        String phoneA = console.readLine(
                "Phone number A: "
        );

        System.out.println();
        System.out.println("========================================");
        System.out.println(" Account A");
        System.out.println("========================================");

        runAccount(
                console,
                "A",
                phoneA,
                "account-a"
        );

        System.out.println();
        System.out.println("========================================");
        System.out.println(" Account A completed");
        System.out.println(" Context A closed");
        System.out.println("========================================");

        System.out.println();
        System.out.println("Enter Telegram account B");
        String phoneB = console.readLine(
                "Phone number B: "
        );

        System.out.println();
        System.out.println("========================================");
        System.out.println(" Account B");
        System.out.println("========================================");

        runAccount(
                console,
                "B",
                phoneB,
                "account-b"
        );

        System.out.println();
        System.out.println("========================================");
        System.out.println(" Integration test completed");
        System.out.println("========================================");
    }

    private static void runAccount(
            ConsoleInput console,
            String accountName,
            String phone,
            String directoryName
    ) throws Exception {

        Path root = Paths.get(".", "tdlib-integration-test");;

        Path databaseDirectory =
                root.resolve(directoryName + "/database");

        Path filesDirectory =
                root.resolve(directoryName + "/files");

        System.out.println();
        System.out.println(
                "Creating Telegram context "
                        + accountName
                        + "..."
        );

        try (
                AnnotationConfigApplicationContext context =
                        TdlibTestContextConfiguration.create(
                                API_ID,
                                API_HASH,
                                phone,
                                databaseDirectory,
                                filesDirectory
                        )
        ) {

            System.out.println();
            System.out.println(
                    "Authorizing account "
                            + accountName
                            + "..."
            );

            authorize(
                    context,
                    console,
                    accountName
            );

            System.out.println();
            System.out.println(
                    "Authorization completed for account "
                            + accountName
                            + "."
            );

            inspectAccount(
                    context,
                    accountName
            );

            logout(
                    context,
                    accountName
            );

            System.out.println();
            System.out.println(
                    "Closing context "
                            + accountName
                            + "..."
            );
        }
    }

    private static void authorize(
            AnnotationConfigApplicationContext context,
            ConsoleInput console,
            String accountName
    ) throws Exception {

        TelegramAuthorizationManager authorizationManager =
                context.getBean(
                        TelegramAuthorizationManager.class
                );

        long deadline =
                System.nanoTime()
                        + AUTHORIZATION_TIMEOUT.toNanos();

        while (!authorizationManager.haveAuthorization()) {

            checkAuthorizationTimeout(
                    accountName,
                    deadline
            );

            if (authorizationManager.isStateClosed()) {
                throw new IllegalStateException(
                        "Telegram authorization state was closed "
                                + "for account "
                                + accountName
                );
            }

            if (authorizationManager.isWaitAuthenticationCode()) {

                System.out.println();
                System.out.println(
                        "TDLib is waiting for authentication code "
                                + "for account "
                                + accountName
                );

                String code = console.readSecret(
                        "Enter Telegram authentication code for account "
                                + accountName
                );

                authorizationManager.checkAuthenticationCode(code);

                continue;
            }

            if (authorizationManager.isWaitAuthenticationPassword()) {

                System.out.println();
                System.out.println(
                        "TDLib is waiting for 2FA password "
                                + "for account "
                                + accountName
                );

                String password = console.readSecret(
                        "Enter Telegram 2FA password for account "
                                + accountName
                );

                authorizationManager.checkAuthenticationPassword(
                        password
                );

                continue;
            }

            if (authorizationManager.isWaitEmailAddress()) {

                System.out.println();
                System.out.println(
                        "TDLib is waiting for email address "
                                + "for account "
                                + accountName
                );

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
                "Account "
                        + accountName
                        + " authorized."
        );
    }

    private static void checkAuthorizationTimeout(
            String accountName,
            long deadline
    ) {

        if (System.nanoTime() > deadline) {
            throw new IllegalStateException(
                    "Authorization timeout for account "
                            + accountName
            );
        }
    }

    private static void inspectAccount(
            AnnotationConfigApplicationContext context,
            String accountName
    ) throws Exception {

        TelegramUserService userService =
                context.getBean(
                        TelegramUserService.class
                );

        TelegramChatService chatService =
                context.getBean(
                        TelegramChatService.class
                );

        System.out.println();
        System.out.println(
                "Getting current user for account "
                        + accountName
                        + "..."
        );

        TdApi.User me =
                userService.getMe()
                        .get(
                                REQUEST_TIMEOUT.toSeconds(),
                                TimeUnit.SECONDS
                        )
                        .getObjectOrThrow();

        System.out.println();
        System.out.println("Current user:");
        System.out.println("  id        = " + me.id);
        System.out.println("  firstName = " + me.firstName);
        System.out.println("  lastName  = " + me.lastName);

        if (me.usernames != null) {
            System.out.println(
                    "  usernames  = "
                            + (
                            me.usernames.activeUsernames == null
                                    ? "<none>"
                                    : String.join(
                                    ", ",
                                    me.usernames.activeUsernames
                            )
                    )
            );
        }

        System.out.println();
        System.out.println(
                "Searching chats for account "
                        + accountName
                        + "..."
        );

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
                "Chats returned: "
                        + chats.chatIds.length
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
                    "  ["
                            + chat.id
                            + "] "
                            + chat.title
                            + " ("
                            + chat.type
                            .getClass()
                            .getSimpleName()
                            + ")"
            );

            printed++;

            if (printed >= 10) {
                break;
            }
        }

        System.out.println();
        System.out.println(
                "Searching current user by phone..."
        );

        if (me.phoneNumber == null
                || me.phoneNumber.isBlank()) {

            throw new IllegalStateException(
                    "Current Telegram user has no phone number."
            );
        }

        TdApi.User searchedUser =
                userService.searchUserByPhoneNumber(
                                me.phoneNumber
                        )
                        .get(
                                REQUEST_TIMEOUT.toSeconds(),
                                TimeUnit.SECONDS
                        )
                        .getObjectOrThrow();

        if (searchedUser.id != me.id) {
            throw new IllegalStateException(
                    "Phone search returned unexpected user. "
                            + "Expected id="
                            + me.id
                            + ", actual id="
                            + searchedUser.id
            );
        }

        System.out.println(
                "Found user: "
                        + searchedUser.id
                        + " "
                        + searchedUser.firstName
                        + " "
                        + searchedUser.lastName
        );

        System.out.println();
        System.out.println(
                "Getting current user's full info..."
        );

        TdApi.UserFullInfo fullInfo =
                userService.getUserFullInfo(me.id)
                        .get(
                                REQUEST_TIMEOUT.toSeconds(),
                                TimeUnit.SECONDS
                        )
                        .getObjectOrThrow();

        System.out.println(
                "Bio: "
                        + (
                        fullInfo.bio == null
                                ? "<none>"
                                : fullInfo.bio.text
                )
        );

        System.out.println();
        System.out.println(
                "Account "
                        + accountName
                        + " functional checks completed."
        );
    }

    private static void logout(
            AnnotationConfigApplicationContext context,
            String accountName
    ) throws Exception {

        TelegramClient telegramClient =
                context.getBean(
                        TelegramClient.class
                );

        TelegramAuthorizationManager authorizationManager =
                context.getBean(
                        TelegramAuthorizationManager.class
                );

        System.out.println();
        System.out.println(
                "Logging out account "
                        + accountName
                        + "..."
        );

        telegramClient
                .sendAsync(new TdApi.LogOut())
                .get(
                        REQUEST_TIMEOUT.toSeconds(),
                        TimeUnit.SECONDS
                )
                .getObjectOrThrow();

        long deadline =
                System.nanoTime()
                        + CLOSE_TIMEOUT.toNanos();

        while (!authorizationManager.isStateClosed()) {

            if (System.nanoTime() > deadline) {
                throw new IllegalStateException(
                        "Timeout waiting for TDLib Closed state "
                                + "for account "
                                + accountName
                );
            }

            Thread.sleep(100);
        }

        if (authorizationManager.haveAuthorization()) {
            throw new IllegalStateException(
                    "Authorization flag is still true after logout "
                            + "for account "
                            + accountName
            );
        }

        System.out.println(
                "Account "
                        + accountName
                        + " logged out successfully."
        );
    }
}