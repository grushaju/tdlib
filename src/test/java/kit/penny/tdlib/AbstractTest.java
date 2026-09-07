package kit.penny.tdlib;

import kit.penny.tdlib.client.TelegramClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(classes = {TdlibAutoConfiguration.class})
public abstract class AbstractTest {

    @MockitoBean
    public TelegramClient telegramClient;

}
