package kit.penny.tdlib.exception;

/**
 * Telegram client configuration exception.
 * @author Pavel Grushin
 */
public class TdlibConfigurationException extends RuntimeException {

    public TdlibConfigurationException(String message) {
        super(message);
    }

}
