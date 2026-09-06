package kit.penny.tdlib.exception;

import org.drinkless.tdlib.TdApi;

/**
 * Telegram TDLib exceptions.
 * @author Pavel Vorobyev
 */
public class TdlibException extends RuntimeException {

    private final TdApi.Error error;

    private final TdApi.Function<? extends TdApi.Object> query;

    /**
     * @param message exception information message
     */
    public TdlibException(String message) {
        this(message, null, null, null);
    }

    /**
     * @param message exception information message
     * @param cause {@link Throwable}
     */
    public TdlibException(String message, Throwable cause) {
        this(message, cause, null, null);
    }

    /**
     * @param message exception information message
     * @param error error from TDLib
     */
    public TdlibException(String message, TdApi.Error error) {
        this(message, null, error, null);
    }

    /**
     * @param message exception information message
     * @param cause {@link Throwable}
     * @param error error from TDLib
     */
    public TdlibException(String message, Throwable cause, TdApi.Error error) {
        this(message, cause, error, null);
    }

    /**
     * @param message exception information message
     * @param error error from TDLib
     * @param query {@link TdApi.Function} the original function query that was causing the error
     */
    public TdlibException(String message, TdApi.Error error, TdApi.Function<? extends TdApi.Object> query) {
        this(message, null, error, query);
    }

    /**
     * @param message exception information message
     * @param cause {@link Throwable}
     * @param error error from TDLib
     * @param query {@link TdApi.Function} the original function query that was causing the error
     */
    public TdlibException(String message, Throwable cause, TdApi.Error error, TdApi.Function<? extends TdApi.Object> query) {
        super(message, cause);
        this.error = error;
        this.query = query;
    }

    /**
     * @return {@link TdApi.Error} error from TDLib or null.
     */
    public TdApi.Error getError() {
        return error;
    }

    /**
     * @return {@link TdApi.Function} the original function query that was causing the error or null.
     */
    public TdApi.Function<? extends TdApi.Object> getQuery() {
        return query;
    }
}
