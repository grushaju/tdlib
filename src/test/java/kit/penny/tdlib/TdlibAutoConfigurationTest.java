package kit.penny.tdlib;

import kit.penny.tdlib.client.TelegramClient;
import kit.penny.tdlib.service.TelegramChatService;
import kit.penny.tdlib.service.TelegramUserService;
import kit.penny.tdlib.updates.TelegramAuthorizationManager;
import kit.penny.tdlib.updates.internal.TdlibUpdateDispatcher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = TdlibAutoConfiguration.class)
@ActiveProfiles("test")
class TdlibAutoConfigurationTest {

    @Autowired
    private TelegramAuthorizationManager authorizationManager;

    @Autowired
    private TdlibUpdateDispatcher updateDispatcher;

    @Autowired
    private TelegramClient telegramClient;

    @Autowired
    private TelegramUserService telegramUserService;

    @Autowired
    private TelegramChatService telegramChatService;

    @Test
    void context_shouldCreateAllExpectedBeans() {
        assertThat(authorizationManager).isNotNull();
        assertThat(updateDispatcher).isNotNull();
        assertThat(telegramClient).isNotNull();
        assertThat(telegramUserService).isNotNull();
        assertThat(telegramChatService).isNotNull();
    }
}