package kit.penny.clientbus.connector.telegram.client;

import kit.penny.tdlib.TelegramTdlibAutoConfiguration;
import kit.penny.tdlib.client.TelegramClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(classes = {TelegramTdlibAutoConfiguration.class})
public abstract class AbstractTest {

    @MockitoBean
    public TelegramClient telegramClient;

}
