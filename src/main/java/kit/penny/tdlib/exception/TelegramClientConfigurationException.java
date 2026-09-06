package kit.penny.tdlib.exception;

/**
 * Telegram client configuration exception.
 * @author Pavel Vorobyev
 */
public class TelegramClientConfigurationException extends RuntimeException {

    public TelegramClientConfigurationException(String message) {
        super(message);
    }

}
