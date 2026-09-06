package kit.penny.clientbus.connector.telegram.client;

import kit.penny.tdlib.CoreUpdateHandler;
import kit.penny.tdlib.updates.ITdlibUpdateListener;
import org.drinkless.tdlib.TdApi;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CoreHandlerTest {

    @Test
    void onResult() {
        var actualConstructorIdentifier = new AtomicInteger();
        var updateNewChatListener = new ITdlibUpdateListener<TdApi.UpdateNewChat>() {
            @Override
            public void handleNotification(TdApi.UpdateNewChat notification) {
                actualConstructorIdentifier.set(notification.getConstructor());
            }

            @Override
            public Class<TdApi.UpdateNewChat> notificationType() {
                return TdApi.UpdateNewChat.class;
            }
        };

        var coreHandler = new CoreUpdateHandler(List.of(updateNewChatListener), obj -> {});
        var updateNewChat = new TdApi.UpdateNewChat();
        int expectedConstructorIdentifier = updateNewChat.getConstructor();
        coreHandler.onResult(updateNewChat);

        assertEquals(expectedConstructorIdentifier, actualConstructorIdentifier.get());
    }

}