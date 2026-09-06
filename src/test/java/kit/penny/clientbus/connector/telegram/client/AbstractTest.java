package kit.penny.clientbus.connector.telegram.client;

import kit.penny.clientbus.connector.telegram.client.TelegramClient;
import kit.penny.clientbus.connector.telegram.TelegramClientAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(classes = {TelegramClientAutoConfiguration.class})
public abstract class AbstractTest {

    @MockitoBean
    public TelegramClient telegramClient;

}
