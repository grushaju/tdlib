package kit.penny.tdlib.updates;

import org.drinkless.tdlib.TdApi;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.springframework.util.StringUtils.hasText;

/**
 * Manages authorization state and authentication input for a TDLib client.
 *
 * <p>The manager is instance-scoped. No authorization data is stored in static state,
 * which allows multiple Telegram clients to be used independently.</p>
 *
 * @author Pavel Grushin
 */
public final class TelegramAuthorizationManager
        implements ITelegramAuthorizationManager {

    private final AtomicReference<CompletableFuture<String>> authenticationCode =
            new AtomicReference<>();

    private final AtomicReference<CompletableFuture<String>> authenticationPassword =
            new AtomicReference<>();

    private final AtomicReference<CompletableFuture<String>> emailAddress =
            new AtomicReference<>();

    private final AtomicReference<CompletableFuture<TdApi.Error>> authenticationError =
            new AtomicReference<>();

    private final AtomicBoolean waitAuthenticationCode = new AtomicBoolean();
    private final AtomicBoolean waitAuthenticationPassword = new AtomicBoolean();
    private final AtomicBoolean waitEmailAddress = new AtomicBoolean();

    private final AtomicBoolean authorized = new AtomicBoolean();
    private final AtomicBoolean stateClosed = new AtomicBoolean();

    private final CompletableFuture<Void> closedFuture =
            new CompletableFuture<>();

    @Override
    public synchronized void checkAuthenticationCode(String code) {
        complete(authenticationCode, waitAuthenticationCode, code);
    }

    @Override
    public synchronized void checkAuthenticationPassword(String password) {
        complete(authenticationPassword, waitAuthenticationPassword, password);
    }

    @Override
    public synchronized void checkEmailAddress(String email) {
        complete(emailAddress, waitEmailAddress, email);
    }

    @Override
    public boolean isWaitAuthenticationCode() {
        return waitAuthenticationCode.get();
    }

    @Override
    public boolean isWaitAuthenticationPassword() {
        return waitAuthenticationPassword.get();
    }

    @Override
    public boolean isWaitEmailAddress() {
        return waitEmailAddress.get();
    }

    @Override
    public boolean haveAuthorization() {
        return authorized.get();
    }

    @Override
    public boolean isStateClosed() {
        return stateClosed.get();
    }

    /**
     * Starts waiting for an authentication code.
     *
     * @return future completed when authentication code is supplied
     */
    synchronized CompletableFuture<String> awaitAuthenticationCode() {
        return await(authenticationCode, waitAuthenticationCode);
    }

    synchronized void retryAuthenticationCode() {
        CompletableFuture<String> current =
                authenticationCode.get();

        if (current == null || current.isDone()) {
            authenticationCode.set(new CompletableFuture<>());
        }

        waitAuthenticationCode.set(true);
    }

    /**
     * Starts waiting for an authentication password.
     *
     * @return future completed when password is supplied
     */
    synchronized CompletableFuture<String> awaitAuthenticationPassword() {
        return await(authenticationPassword, waitAuthenticationPassword);
    }

    synchronized void retryAuthenticationPassword() {
        CompletableFuture<String> current =
                authenticationPassword.get();

        if (current == null || current.isDone()) {
            authenticationPassword.set(new CompletableFuture<>());
        }

        waitAuthenticationPassword.set(true);
    }

    /**
     * Starts waiting for an email address.
     *
     * @return future completed when email address is supplied
     */
    synchronized CompletableFuture<String> awaitEmailAddress() {
        return await(emailAddress, waitEmailAddress);
    }

    /**
     * Starts waiting for an authorization request error.
     *
     * @return future completed when TDLib returns an authorization error
     */
    synchronized CompletableFuture<TdApi.Error> awaitAuthenticationError() {
        CompletableFuture<TdApi.Error> current =
                authenticationError.get();

        if (current == null || current.isDone()) {
            current = new CompletableFuture<>();
            authenticationError.set(current);
        }

        return current;
    }

    /**
     * Completes the current authorization error future.
     *
     * @param error TDLib authorization error
     */
    synchronized void failAuthentication(TdApi.Error error) {
        if (error == null) {
            return;
        }

        CompletableFuture<TdApi.Error> future =
                authenticationError.get();

        if (future != null && !future.isDone()) {
            future.complete(error);
        }
    }

    void setAuthorized(boolean value) {
        authorized.set(value);
    }

    void resetAuthorization() {
        authorized.set(false);
    }

    void setStateClosed(boolean value) {
        stateClosed.set(value);
    }

    /**
     * Completes all pending authentication requests when TDLib reaches its final state.
     */
    synchronized void close() {
        stateClosed.set(true);
        authorized.set(false);

        completeExceptionally(
                authenticationCode,
                "TDLib authorization state is closed"
        );

        completeExceptionally(
                authenticationPassword,
                "TDLib authorization state is closed"
        );

        completeExceptionally(
                emailAddress,
                "TDLib authorization state is closed"
        );

        completeExceptionally(
                authenticationError,
                "TDLib authorization state is closed"
        );

        waitAuthenticationCode.set(false);
        waitAuthenticationPassword.set(false);
        waitEmailAddress.set(false);

        closedFuture.complete(null);
    }

    private CompletableFuture<String> await(
            AtomicReference<CompletableFuture<String>> input,
            AtomicBoolean waiting) {

        CompletableFuture<String> current = input.get();

        if (current == null || current.isDone()) {
            current = new CompletableFuture<>();
            input.set(current);
        }

        waiting.set(true);

        return current;
    }

    private void complete(
            AtomicReference<CompletableFuture<String>> input,
            AtomicBoolean waiting,
            String value) {

        if (!waiting.get() || !hasText(value)) {
            return;
        }

        CompletableFuture<String> future = input.get();

        if (future != null && !future.isDone()) {
            waiting.set(false);
            future.complete(value);
        }
    }

    private <T> void completeExceptionally(
            AtomicReference<CompletableFuture<T>> input,
            String message) {

        CompletableFuture<T> future = input.get();

        if (future != null && !future.isDone()) {
            future.completeExceptionally(
                    new IllegalStateException(message)
            );
        }
    }

    public CompletableFuture<Void> closedFuture() {
        return closedFuture;
    }
}
