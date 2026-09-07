package kit.penny.tdlib.query;

import kit.penny.tdlib.exception.TdlibException;
import org.drinkless.tdlib.TdApi;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Response wrapper for queries.
 *
 * @author Pavel Grushin
 */
public final class TdlibResponse<T extends TdApi.Object> {
    private final T object;
    private final TdApi.Error error;

    /**
     * @param object {@link TdApi.Object} Query response.
     * @param error  {@link TdApi.Error} Query error.
     */
    public TdlibResponse(T object, TdApi.Error error) {
        this.object = object;
        this.error = error;
    }

    /**
     * Map, or transform, the {@link TdApi.Object} if it exists inside {@link TdlibResponse}
     * otherwise return {@link TdlibResponse} with existing {@link TdApi.Error}.
     * @param mapFunction function to transform {@link TdApi.Object} in response
     * @param <R> type of new {@link TdApi.Object}
     * @return {@link TdlibResponse <R>}
     */
    public <R extends TdApi.Object> TdlibResponse<R> map(Function<T, R> mapFunction) {
        if (object != null) {
            return new TdlibResponse<>(mapFunction.apply(object), null);
        }
        return new TdlibResponse<>(null, error);
    }

    /**
     * Performs an action upon a successful function call to TDLib and returns current {@link TdlibResponse <T>}.
     * @param action callback called upon a successful function call of query to TDLib
     * @return {@link TdlibResponse <T>}
     */
    public TdlibResponse<T> onSuccess(Consumer<T> action) {
        if (object != null) {
            action.accept(object);
        }
        return this;
    }

    /**
     * Performs an action in case of an error and returns current {@link TdlibResponse <T>}.
     * @param action callback called in case of an error of query to TDLib
     * @return {@link TdlibResponse <T>}
     */
    public TdlibResponse<T> onError(Consumer<TdApi.Error> action) {
        if (error != null) {
            action.accept(error);
        }
        return this;
    }

    public Optional<T> getObject() {
        return Optional.ofNullable(object);
    }

    /**
     * Returns the TdApi.Object if it is not null, otherwise throws a TelegramClientTdApiException.
     *
     * @return TdApi.Object
     * @throws TdlibException if the object is null
     */
    public T getObjectOrThrow() {
        return Optional.ofNullable(object)
                .orElseThrow(() -> new TdlibException("TdApi.Object is null", error));
    }

    public Optional<TdApi.Error> getError() {
        return Optional.ofNullable(error);
    }
}
