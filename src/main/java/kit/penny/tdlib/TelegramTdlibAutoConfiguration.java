package kit.penny.tdlib;

import kit.penny.tdlib.client.TelegramClient;
import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;
import kit.penny.clientbus.connector.telegram.client.runner.TelegramRunnersConsumer;
import kit.penny.clientbus.connector.telegram.client.runner.TelegramRunnersConsumerImpl;
import kit.penny.tdlib.templates.TelegramChatService;
import kit.penny.tdlib.templates.TelegramUserService;
import kit.penny.tdlib.updates.ITelegramAuthorizationManager;
import kit.penny.tdlib.updates.ClientAuthorizationStateImpl;
import kit.penny.tdlib.updates.UpdateAuthorizationState;
import kit.penny.tdlib.updates.ITdlibUpdateListener;
import kit.penny.tdlib.properties.TelegramProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.nio.file.Paths;
import java.util.Collection;

/**
 * Telegram Spring Boot client AutoConfiguration.
 *
 * @author Pavel Vorobyev
 */
@Configuration
@ConfigurationPropertiesScan(basePackages = "kit.penny.clientbus.connector.telegram.properties")
public class TelegramTdlibAutoConfiguration {

    private final static Logger log = LoggerFactory.getLogger(TelegramTdlibAutoConfiguration.class);

    //Loading TDLib library
    static {
        try {
            log.error("Absolute PATH: " + Paths.get(".").toAbsolutePath().toString());
            String os = System.getProperty("os.name");
            if (os != null && os.toLowerCase().startsWith("windows")) {
                System.loadLibrary("libcrypto-3-x64");
                System.loadLibrary("libssl-3-x64");
                System.loadLibrary("zlib1");
            }
            System.loadLibrary("tdjni");
        } catch (UnsatisfiedLinkError e) {
            log.error(e.getMessage());
        }
    }

    /**
     * Autoconfigured telegram client.
     *
     * @param properties {@link TelegramProperties}
     * @param notificationHandlers collection of {@link ITdlibUpdateListener} beans
     * @param defaultHandler default handler for incoming updates
     * @param ITelegramAuthorizationManager authorization state of the client
     * @return {@link TelegramClient}
     */
    @Bean
    public TelegramClient telegramClient(TelegramProperties properties,
                                         Collection<ITdlibUpdateListener<?>> notificationHandlers,
                                         Client.ResultHandler defaultHandler,
                                         ITelegramAuthorizationManager ITelegramAuthorizationManager) {
        return new TelegramClient(properties, notificationHandlers, defaultHandler, ITelegramAuthorizationManager);
    }

    /**
     * Client authorization state.
     *
     * @return {@link ITelegramAuthorizationManager}
     */
    @Bean
    public ITelegramAuthorizationManager clientAuthorizationState() {
        return new ClientAuthorizationStateImpl();
    }

    /**
     * Notification listener for authorization sate change.
     *
     * @return {@link ITdlibUpdateListener <TdApi.UpdateAuthorizationState>}
     */
    @Bean
    public ITdlibUpdateListener<TdApi.UpdateAuthorizationState> updateAuthorizationNotification(TelegramProperties properties,
                                                                                                @Lazy TelegramClient telegramClient) {
        return new UpdateAuthorizationState(properties, telegramClient);
    }

    /**
     * Template for {@link TdApi.User} related objects.
     *
     * @param telegramClient Telegram client.
     * @return {@link TelegramUserService}.
     */
    @Bean
    public TelegramUserService userTemplate(@Lazy TelegramClient telegramClient) {
        return new TelegramUserService(telegramClient);
    }

    /**
     * Template for {@link TdApi.Chat} related objects.
     *
     * @param telegramClient Telegram client.
     * @return {@link TelegramChatService}.
     */
    @Bean
    public TelegramChatService chatTemplate(@Lazy TelegramClient telegramClient) {
        return new TelegramChatService(telegramClient);
    }

    /**
     * @return Default handler for incoming TDLib updates.
     * Could be overwritten by another bean
     */
    @Bean
    public Client.ResultHandler defaultHandler() {
        return (TdApi.Object object) ->
                log.debug("\nSTART DEFAULT HANDLER\n" +
                        object.toString() + "\n" +
                        "END DEFAULT HANDLER");
    }

    /**
     * Creates a consumer of {@link TelegramRunner} implementations.
     *
     * @param authorizationState authorization state of the client
     * @param applicationArguments the arguments that were used to run a {@link SpringApplication}
     * @param applicationContext interface to provide configuration for an spring application
     * @return {@link TelegramRunnersConsumer}
     */
    @Bean
    public TelegramRunnersConsumer telegramRunnersConsumer(ITelegramAuthorizationManager authorizationState,
                                                           ApplicationArguments applicationArguments,
                                                           ApplicationContext applicationContext) {
        return new TelegramRunnersConsumerImpl(authorizationState, applicationArguments, applicationContext);
    }

    /**
     * @param telegramRunners collection of {@link TelegramRunner} implementations
     * @param telegramRunnersConsumer consumer of {@link TelegramRunner} implementations
     * @return {@link ApplicationRunner}
     */
    @Bean
    public ApplicationRunner telegramClientRunner(Collection<TelegramRunner> telegramRunners,
                                                  TelegramRunnersConsumer telegramRunnersConsumer) {
        return args -> telegramRunnersConsumer.accept(telegramRunners);
    }

}
