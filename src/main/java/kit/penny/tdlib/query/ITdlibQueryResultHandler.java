package kit.penny.tdlib.query;

import org.drinkless.tdlib.TdApi;

/**
 * Interface for handler for results of queries with callback to TDLib.
 * @param <T> The object type that is returned by the function
 *
 * @author Pavel Grushin
 */
public interface ITdlibQueryResultHandler<T extends TdApi.Object> {

    /**
     * Callback called on result of query to TDLib.
     *
     * @param obj Response object from {@link TdApi.Function} query or null if {@link TdApi.Error} received
     * @param error Error result of query to TDLib or null
     */
    void onResult(T obj, TdApi.Error error);

}
