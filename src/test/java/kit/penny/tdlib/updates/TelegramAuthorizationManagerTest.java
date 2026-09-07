package kit.penny.tdlib.updates;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TelegramAuthorizationManagerTest {

    @Test
    void shouldHaveInitialState() {
        TelegramAuthorizationManager manager = new TelegramAuthorizationManager();

        assertThat(manager.isWaitAuthenticationCode()).isFalse();
        assertThat(manager.isWaitAuthenticationPassword()).isFalse();
        assertThat(manager.isWaitEmailAddress()).isFalse();
        assertThat(manager.haveAuthorization()).isFalse();
        assertThat(manager.isStateClosed()).isFalse();
    }

    @Test
    void shouldCompleteAuthenticationCode() {
        TelegramAuthorizationManager manager = new TelegramAuthorizationManager();

        CompletableFuture<String> future = manager.awaitAuthenticationCode();

        assertThat(manager.isWaitAuthenticationCode()).isTrue();

        manager.checkAuthenticationCode("12345");

        assertThat(future).isCompletedWithValue("12345");
        assertThat(manager.isWaitAuthenticationCode()).isFalse();
    }

    @Test
    void shouldCompleteAuthenticationPassword() {
        TelegramAuthorizationManager manager = new TelegramAuthorizationManager();

        CompletableFuture<String> future = manager.awaitAuthenticationPassword();

        assertThat(manager.isWaitAuthenticationPassword()).isTrue();

        manager.checkAuthenticationPassword("password");

        assertThat(future).isCompletedWithValue("password");
        assertThat(manager.isWaitAuthenticationPassword()).isFalse();
    }

    @Test
    void shouldCompleteEmailAddress() {
        TelegramAuthorizationManager manager = new TelegramAuthorizationManager();

        CompletableFuture<String> future = manager.awaitEmailAddress();

        assertThat(manager.isWaitEmailAddress()).isTrue();

        manager.checkEmailAddress("user@example.com");

        assertThat(future).isCompletedWithValue("user@example.com");
        assertThat(manager.isWaitEmailAddress()).isFalse();
    }

    @Test
    void shouldIgnoreAuthenticationCodeWhenNotWaiting() {
        TelegramAuthorizationManager manager = new TelegramAuthorizationManager();

        manager.checkAuthenticationCode("12345");

        assertThat(manager.isWaitAuthenticationCode()).isFalse();
    }

    @Test
    void shouldIgnoreAuthenticationPasswordWhenNotWaiting() {
        TelegramAuthorizationManager manager = new TelegramAuthorizationManager();

        manager.checkAuthenticationPassword("password");

        assertThat(manager.isWaitAuthenticationPassword()).isFalse();
    }

    @Test
    void shouldIgnoreEmailAddressWhenNotWaiting() {
        TelegramAuthorizationManager manager = new TelegramAuthorizationManager();

        manager.checkEmailAddress("user@example.com");

        assertThat(manager.isWaitEmailAddress()).isFalse();
    }

    @Test
    void shouldIgnoreBlankAuthenticationCode() {
        TelegramAuthorizationManager manager = new TelegramAuthorizationManager();

        CompletableFuture<String> future = manager.awaitAuthenticationCode();

        manager.checkAuthenticationCode(" ");

        assertThat(future).isNotCompleted();
        assertThat(manager.isWaitAuthenticationCode()).isTrue();
    }

    @Test
    void shouldIgnoreBlankAuthenticationPassword() {
        TelegramAuthorizationManager manager = new TelegramAuthorizationManager();

        CompletableFuture<String> future = manager.awaitAuthenticationPassword();

        manager.checkAuthenticationPassword("");

        assertThat(future).isNotCompleted();
        assertThat(manager.isWaitAuthenticationPassword()).isTrue();
    }

    @Test
    void shouldIgnoreBlankEmailAddress() {
        TelegramAuthorizationManager manager = new TelegramAuthorizationManager();

        CompletableFuture<String> future = manager.awaitEmailAddress();

        manager.checkEmailAddress("   ");

        assertThat(future).isNotCompleted();
        assertThat(manager.isWaitEmailAddress()).isTrue();
    }

    @Test
    void shouldSetAndResetAuthorization() {
        TelegramAuthorizationManager manager = new TelegramAuthorizationManager();

        manager.setAuthorized(true);

        assertThat(manager.haveAuthorization()).isTrue();

        manager.resetAuthorization();

        assertThat(manager.haveAuthorization()).isFalse();
    }

    @Test
    void shouldCloseManagerAndFailPendingAuthenticationCode() {
        TelegramAuthorizationManager manager = new TelegramAuthorizationManager();

        CompletableFuture<String> future = manager.awaitAuthenticationCode();

        manager.close();

        assertThat(manager.isStateClosed()).isTrue();
        assertThat(manager.haveAuthorization()).isFalse();
        assertThat(manager.isWaitAuthenticationCode()).isFalse();

        assertThatThrownBy(future::join)
                .isInstanceOf(CompletionException.class)
                .hasCauseInstanceOf(IllegalStateException.class)
                .hasRootCauseMessage("TDLib authorization state is closed");
    }

    @Test
    void shouldCloseManagerAndFailAllPendingRequests() {
        TelegramAuthorizationManager manager = new TelegramAuthorizationManager();

        CompletableFuture<String> codeFuture = manager.awaitAuthenticationCode();
        CompletableFuture<String> passwordFuture = manager.awaitAuthenticationPassword();
        CompletableFuture<String> emailFuture = manager.awaitEmailAddress();

        manager.close();

        assertThat(codeFuture).isCompletedExceptionally();
        assertThat(passwordFuture).isCompletedExceptionally();
        assertThat(emailFuture).isCompletedExceptionally();

        assertThat(manager.isWaitAuthenticationCode()).isFalse();
        assertThat(manager.isWaitAuthenticationPassword()).isFalse();
        assertThat(manager.isWaitEmailAddress()).isFalse();
        assertThat(manager.isStateClosed()).isTrue();
        assertThat(manager.haveAuthorization()).isFalse();
    }

    @Test
    void shouldKeepAuthorizationStateIndependentBetweenInstances() {
        TelegramAuthorizationManager first = new TelegramAuthorizationManager();
        TelegramAuthorizationManager second = new TelegramAuthorizationManager();

        first.setAuthorized(true);

        assertThat(first.haveAuthorization()).isTrue();
        assertThat(second.haveAuthorization()).isFalse();

        CompletableFuture<String> firstFuture = first.awaitAuthenticationCode();
        CompletableFuture<String> secondFuture = second.awaitAuthenticationCode();

        first.checkAuthenticationCode("11111");

        assertThat(firstFuture).isCompletedWithValue("11111");
        assertThat(secondFuture).isNotCompleted();
        assertThat(second.isWaitAuthenticationCode()).isTrue();
    }

    @Test
    void shouldReuseSameFutureWhileWaiting() {
        TelegramAuthorizationManager manager = new TelegramAuthorizationManager();

        CompletableFuture<String> first = manager.awaitAuthenticationCode();
        CompletableFuture<String> second = manager.awaitAuthenticationCode();

        assertThat(second).isSameAs(first);

        manager.checkAuthenticationCode("12345");

        assertThat(first).isCompletedWithValue("12345");
        assertThat(second).isCompletedWithValue("12345");
    }

    @Test
    void shouldCreateNewFutureAfterPreviousRequestCompleted() {
        TelegramAuthorizationManager manager = new TelegramAuthorizationManager();

        CompletableFuture<String> first = manager.awaitAuthenticationCode();

        manager.checkAuthenticationCode("12345");

        CompletableFuture<String> second = manager.awaitAuthenticationCode();

        assertThat(second).isNotSameAs(first);
        assertThat(second).isNotCompleted();
        assertThat(manager.isWaitAuthenticationCode()).isTrue();
    }
}
