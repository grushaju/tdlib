package kit.penny.tdlib.updates;

import org.drinkless.tdlib.TdApi;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TelegramAuthorizationManagerTest {

    @Test
    void shouldHaveInitialState() {
        TelegramAuthorizationManager manager =
                new TelegramAuthorizationManager();

        assertThat(manager.getStatus())
                .isEqualTo(TelegramAuthorizationStatus.WAIT_PHONE_NUMBER);

        assertThat(manager.isWaitAuthenticationCode())
                .isFalse();

        assertThat(manager.isWaitAuthenticationPassword())
                .isFalse();

        assertThat(manager.isWaitEmailAddress())
                .isFalse();

        assertThat(manager.haveAuthorization())
                .isFalse();

        assertThat(manager.isStateClosed())
                .isFalse();
    }

    @Test
    void shouldCompleteAuthenticationCode() {
        TelegramAuthorizationManager manager =
                new TelegramAuthorizationManager();

        CompletableFuture<String> future =
                manager.awaitAuthenticationCode();

        assertThat(manager.isWaitAuthenticationCode())
                .isTrue();

        manager.checkAuthenticationCode("12345");

        assertThat(future)
                .isCompletedWithValue("12345");

        assertThat(manager.isWaitAuthenticationCode())
                .isFalse();
    }

    @Test
    void shouldCompleteAuthenticationPassword() {
        TelegramAuthorizationManager manager =
                new TelegramAuthorizationManager();

        CompletableFuture<String> future =
                manager.awaitAuthenticationPassword();

        assertThat(manager.isWaitAuthenticationPassword())
                .isTrue();

        manager.checkAuthenticationPassword("password");

        assertThat(future)
                .isCompletedWithValue("password");

        assertThat(manager.isWaitAuthenticationPassword())
                .isFalse();
    }

    @Test
    void shouldCompleteEmailAddress() {
        TelegramAuthorizationManager manager =
                new TelegramAuthorizationManager();

        CompletableFuture<String> future =
                manager.awaitEmailAddress();

        assertThat(manager.isWaitEmailAddress())
                .isTrue();

        manager.checkEmailAddress("user@example.com");

        assertThat(future)
                .isCompletedWithValue("user@example.com");

        assertThat(manager.isWaitEmailAddress())
                .isFalse();
    }

    @Test
    void shouldIgnoreAuthenticationCodeWhenNotWaiting() {
        TelegramAuthorizationManager manager =
                new TelegramAuthorizationManager();

        manager.checkAuthenticationCode("12345");

        assertThat(manager.isWaitAuthenticationCode())
                .isFalse();

        assertThat(manager.getStatus())
                .isEqualTo(TelegramAuthorizationStatus.WAIT_PHONE_NUMBER);
    }

    @Test
    void shouldIgnoreAuthenticationPasswordWhenNotWaiting() {
        TelegramAuthorizationManager manager =
                new TelegramAuthorizationManager();

        manager.checkAuthenticationPassword("password");

        assertThat(manager.isWaitAuthenticationPassword())
                .isFalse();

        assertThat(manager.getStatus())
                .isEqualTo(TelegramAuthorizationStatus.WAIT_PHONE_NUMBER);
    }

    @Test
    void shouldIgnoreEmailAddressWhenNotWaiting() {
        TelegramAuthorizationManager manager =
                new TelegramAuthorizationManager();

        manager.checkEmailAddress("user@example.com");

        assertThat(manager.isWaitEmailAddress())
                .isFalse();

        assertThat(manager.getStatus())
                .isEqualTo(TelegramAuthorizationStatus.WAIT_PHONE_NUMBER);
    }

    @Test
    void shouldIgnoreBlankAuthenticationCode() {
        TelegramAuthorizationManager manager =
                new TelegramAuthorizationManager();

        CompletableFuture<String> future =
                manager.awaitAuthenticationCode();

        manager.checkAuthenticationCode(" ");

        assertThat(future)
                .isNotCompleted();

        assertThat(manager.isWaitAuthenticationCode())
                .isTrue();
    }

    @Test
    void shouldIgnoreBlankAuthenticationPassword() {
        TelegramAuthorizationManager manager =
                new TelegramAuthorizationManager();

        CompletableFuture<String> future =
                manager.awaitAuthenticationPassword();

        manager.checkAuthenticationPassword("");

        assertThat(future)
                .isNotCompleted();

        assertThat(manager.isWaitAuthenticationPassword())
                .isTrue();
    }

    @Test
    void shouldIgnoreBlankEmailAddress() {
        TelegramAuthorizationManager manager =
                new TelegramAuthorizationManager();

        CompletableFuture<String> future =
                manager.awaitEmailAddress();

        manager.checkEmailAddress("   ");

        assertThat(future)
                .isNotCompleted();

        assertThat(manager.isWaitEmailAddress())
                .isTrue();
    }

    @Test
    void shouldSetReadyStatusWhenAuthorized() {
        TelegramAuthorizationManager manager =
                new TelegramAuthorizationManager();

        manager.setAuthorized(true);

        assertThat(manager.getStatus())
                .isEqualTo(TelegramAuthorizationStatus.READY);

        assertThat(manager.haveAuthorization())
                .isTrue();

        assertThat(manager.isStateClosed())
                .isFalse();
    }

    @Test
    void shouldResetAuthorizationToWaitPhoneNumber() {
        TelegramAuthorizationManager manager =
                new TelegramAuthorizationManager();

        manager.setAuthorized(true);

        assertThat(manager.getStatus())
                .isEqualTo(TelegramAuthorizationStatus.READY);

        manager.resetAuthorization();

        assertThat(manager.getStatus())
                .isEqualTo(TelegramAuthorizationStatus.WAIT_PHONE_NUMBER);

        assertThat(manager.haveAuthorization())
                .isFalse();

        assertThat(manager.isStateClosed())
                .isFalse();
    }

    @Test
    void shouldSetErrorStatusWhenAuthenticationFails() {
        TelegramAuthorizationManager manager =
                new TelegramAuthorizationManager();

        CompletableFuture<TdApi.Error> future =
                manager.awaitAuthenticationError();

        TdApi.Error error =
                new TdApi.Error(
                        400,
                        "PHONE_CODE_INVALID"
                );

        manager.failAuthentication(error);

        assertThat(manager.getStatus())
                .isEqualTo(TelegramAuthorizationStatus.ERROR);

        assertThat(future)
                .isCompletedWithValue(error);

        assertThat(manager.haveAuthorization())
                .isFalse();

        assertThat(manager.isStateClosed())
                .isFalse();
    }

    @Test
    void shouldSetWaitCodeStatusWhenAuthenticationCodeIsRetried() {
        TelegramAuthorizationManager manager =
                new TelegramAuthorizationManager();

        manager.retryAuthenticationCode();

        assertThat(manager.getStatus())
                .isEqualTo(TelegramAuthorizationStatus.WAIT_CODE);

        assertThat(manager.isWaitAuthenticationCode())
                .isTrue();

        assertThat(manager.haveAuthorization())
                .isFalse();
    }

    @Test
    void shouldSetWaitPasswordStatusWhenAuthenticationPasswordIsRetried() {
        TelegramAuthorizationManager manager =
                new TelegramAuthorizationManager();

        manager.retryAuthenticationPassword();

        assertThat(manager.getStatus())
                .isEqualTo(TelegramAuthorizationStatus.WAIT_PASSWORD);

        assertThat(manager.isWaitAuthenticationPassword())
                .isTrue();

        assertThat(manager.haveAuthorization())
                .isFalse();
    }

    @Test
    void shouldKeepAuthenticationCodeMechanismIndependentFromStatus() {
        TelegramAuthorizationManager manager =
                new TelegramAuthorizationManager();

        manager.setStatus(
                TelegramAuthorizationStatus.WAIT_EMAIL
        );

        CompletableFuture<String> future =
                manager.awaitAuthenticationCode();

        assertThat(manager.getStatus())
                .isEqualTo(TelegramAuthorizationStatus.WAIT_EMAIL);

        assertThat(manager.isWaitAuthenticationCode())
                .isTrue();

        manager.checkAuthenticationCode("12345");

        assertThat(future)
                .isCompletedWithValue("12345");

        assertThat(manager.getStatus())
                .isEqualTo(TelegramAuthorizationStatus.WAIT_EMAIL);

        assertThat(manager.isWaitAuthenticationCode())
                .isFalse();
    }

    @Test
    void shouldKeepAuthorizationStateIndependentBetweenInstances() {
        TelegramAuthorizationManager first =
                new TelegramAuthorizationManager();

        TelegramAuthorizationManager second =
                new TelegramAuthorizationManager();

        first.setAuthorized(true);

        assertThat(first.getStatus())
                .isEqualTo(TelegramAuthorizationStatus.READY);

        assertThat(second.getStatus())
                .isEqualTo(TelegramAuthorizationStatus.WAIT_PHONE_NUMBER);

        assertThat(first.haveAuthorization())
                .isTrue();

        assertThat(second.haveAuthorization())
                .isFalse();
    }

    @Test
    void shouldReuseSameFutureWhileWaiting() {
        TelegramAuthorizationManager manager =
                new TelegramAuthorizationManager();

        CompletableFuture<String> first =
                manager.awaitAuthenticationCode();

        CompletableFuture<String> second =
                manager.awaitAuthenticationCode();

        assertThat(second)
                .isSameAs(first);

        manager.checkAuthenticationCode("12345");

        assertThat(first)
                .isCompletedWithValue("12345");

        assertThat(second)
                .isCompletedWithValue("12345");
    }

    @Test
    void shouldCreateNewFutureAfterPreviousRequestCompleted() {
        TelegramAuthorizationManager manager =
                new TelegramAuthorizationManager();

        CompletableFuture<String> first =
                manager.awaitAuthenticationCode();

        manager.checkAuthenticationCode("12345");

        CompletableFuture<String> second =
                manager.awaitAuthenticationCode();

        assertThat(second)
                .isNotSameAs(first);

        assertThat(second)
                .isNotCompleted();

        assertThat(manager.isWaitAuthenticationCode())
                .isTrue();
    }

    @Test
    void shouldCloseManagerAndFailPendingAuthenticationCode() {
        TelegramAuthorizationManager manager =
                new TelegramAuthorizationManager();

        CompletableFuture<String> future =
                manager.awaitAuthenticationCode();

        manager.close();

        assertThat(manager.getStatus())
                .isEqualTo(TelegramAuthorizationStatus.CLOSED);

        assertThat(manager.isStateClosed())
                .isTrue();

        assertThat(manager.haveAuthorization())
                .isFalse();

        assertThat(manager.isWaitAuthenticationCode())
                .isFalse();

        assertThatThrownBy(future::join)
                .isInstanceOf(CompletionException.class)
                .hasCauseInstanceOf(IllegalStateException.class)
                .hasRootCauseMessage(
                        "TDLib authorization state is closed"
                );
    }

    @Test
    void shouldCloseManagerAndFailAllPendingRequests() {
        TelegramAuthorizationManager manager =
                new TelegramAuthorizationManager();

        CompletableFuture<String> codeFuture =
                manager.awaitAuthenticationCode();

        CompletableFuture<String> passwordFuture =
                manager.awaitAuthenticationPassword();

        CompletableFuture<String> emailFuture =
                manager.awaitEmailAddress();

        manager.close();

        assertThat(codeFuture)
                .isCompletedExceptionally();

        assertThat(passwordFuture)
                .isCompletedExceptionally();

        assertThat(emailFuture)
                .isCompletedExceptionally();

        assertThat(manager.getStatus())
                .isEqualTo(TelegramAuthorizationStatus.CLOSED);

        assertThat(manager.isWaitAuthenticationCode())
                .isFalse();

        assertThat(manager.isWaitAuthenticationPassword())
                .isFalse();

        assertThat(manager.isWaitEmailAddress())
                .isFalse();

        assertThat(manager.isStateClosed())
                .isTrue();

        assertThat(manager.haveAuthorization())
                .isFalse();
    }

    @Test
    void shouldNotChangeClosedStatusWhenResettingAuthorization() {
        TelegramAuthorizationManager manager =
                new TelegramAuthorizationManager();

        manager.close();

        manager.resetAuthorization();

        assertThat(manager.getStatus())
                .isEqualTo(TelegramAuthorizationStatus.CLOSED);

        assertThat(manager.isStateClosed())
                .isTrue();

        assertThat(manager.haveAuthorization())
                .isFalse();
    }
}