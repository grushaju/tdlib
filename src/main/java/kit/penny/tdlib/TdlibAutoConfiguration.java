package kit.penny.tdlib;

import kit.penny.tdlib.client.TelegramClient;
import kit.penny.tdlib.updates.internal.TdlibUpdateDispatcher;
import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;
import kit.penny.tdlib.service.TelegramChatService;
import kit.penny.tdlib.service.TelegramUserService;
import kit.penny.tdlib.updates.ITelegramAuthorizationManager;
import kit.penny.tdlib.updates.TelegramAuthorizationManager;
import kit.penny.tdlib.updates.UpdateAuthorizationState;
import kit.penny.tdlib.updates.ITdlibUpdateListener;
import kit.penny.tdlib.properties.TelegramProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.Collection;

/**
 * Telegram Spring Boot client AutoConfiguration.
 *
 * @author Pavel Grushin
 */
@Configuration
@EnableConfigurationProperties(TelegramProperties.class)
public class TdlibAutoConfiguration {

    private final static Logger log = LoggerFactory.getLogger(TdlibAutoConfiguration.class);

    /**
     * Autoconfigured telegram client.
     *
     * @param properties {@link TelegramProperties}
     * @param notificationHandlers collection of {@link ITdlibUpdateListener} beans
     * @param authorizationManager authorization state of the client
     * @return {@link TelegramClient}
     */
    @Bean
    public TelegramClient telegramClient(TelegramProperties properties,
                                         Collection<ITdlibUpdateListener<?>> notificationHandlers,
                                         ITelegramAuthorizationManager authorizationManager) {
        return new TelegramClient(properties, notificationHandlers, defaultHandler(), authorizationManager);
    }

    /**
     * Client authorization state.
     *
     * @return {@link ITelegramAuthorizationManager}
     */
    @Bean
    public TelegramAuthorizationManager authorizationManager() {

        return new TelegramAuthorizationManager();
    }

    /**
     * Notification listener for authorization sate change.
     *
     * @return {@link ITdlibUpdateListener <TdApi.UpdateAuthorizationState>}
     */
    @Bean
    public ITdlibUpdateListener<TdApi.UpdateAuthorizationState> updateAuthorizationState(TelegramProperties properties,
                                                                                         @Lazy TelegramClient telegramClient,
                                                                                         TelegramAuthorizationManager authorizationManager) {
        return new UpdateAuthorizationState(properties, telegramClient, authorizationManager);
    }

    /**
     * Template for {@link TdlibUpdateDispatcher} related objects.
     *
     * @param notifications collection of {@link ITdlibUpdateListener} beans
     * @return {@link TelegramUserService}.
     */
    @Bean
    public TdlibUpdateDispatcher tdlibUpdateDispatcher(Collection<ITdlibUpdateListener<?>> notifications) {
        return new TdlibUpdateDispatcher(notifications, defaultHandler());
    }

    /**
     * Template for {@link TdApi.User} related objects.
     *
     * @param telegramClient Telegram client.
     * @return {@link TelegramUserService}.
     */
    @Bean
    public TelegramUserService telegramUserService(@Lazy TelegramClient telegramClient) {
        return new TelegramUserService(telegramClient);
    }

    /**
     * Template for {@link TdApi.Chat} related objects.
     *
     * @param telegramClient Telegram client.
     * @return {@link TelegramChatService}.
     */
    @Bean
    public TelegramChatService telegramChatService(@Lazy TelegramClient telegramClient) {
        return new TelegramChatService(telegramClient);
    }

    /**
     * @return Default handler for incoming TDLib updates.
     */

    private Client.ResultHandler defaultHandler() {
        return (TdApi.Object object) ->
                log.debug("\nSTART DEFAULT HANDLER\n{}\nEND DEFAULT HANDLER", object.toString());
    }
}
