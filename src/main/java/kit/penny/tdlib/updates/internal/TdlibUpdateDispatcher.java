package kit.penny.tdlib.updates.internal;

import kit.penny.tdlib.exception.TdlibException;
import kit.penny.tdlib.updates.ITdlibUpdateListener;
import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * The main handler for incoming updates from TDLib.
 */
public final class TdlibUpdateDispatcher implements Client.ResultHandler {

    private final Map<Integer, Consumer<TdApi.Object>> tdUpdateHandlers = new HashMap<>();

    private final Client.ResultHandler defaultHandler;

    public TdlibUpdateDispatcher(Collection<ITdlibUpdateListener<?>> notifications, Client.ResultHandler defaultHandler) {
        this.defaultHandler = defaultHandler;
        notifications.forEach(ntf -> {
            var handler = new UpdateNotificationConsumer(ntf, ntf.notificationType());
            tdUpdateHandlers.putIfAbsent(getConstructorNumberOfType(ntf), handler);
        });
    }

    private int getConstructorNumberOfType(ITdlibUpdateListener<?> updateNotification) {
        try {
            TdApi.Update tmp = updateNotification.notificationType().getConstructor().newInstance();
            return tmp.getConstructor();
        } catch (ReflectiveOperationException e) {
            throw new TdlibException(e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResult(TdApi.Object object) {
        tdUpdateHandlers.getOrDefault(object.getConstructor(), defaultHandler::onResult).
                accept(object);
    }

}
