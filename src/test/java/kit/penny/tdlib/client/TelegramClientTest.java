package kit.penny.tdlib.client;

import kit.penny.tdlib.properties.TelegramProperties;
import kit.penny.tdlib.query.TdlibResponse;
import kit.penny.tdlib.updates.TelegramAuthorizationManager;
import kit.penny.tdlib.updates.internal.TdlibUpdateDispatcher;
import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TelegramClientTest {

    @Mock
    private Client client;

    @Mock
    private TdlibUpdateDispatcher updateDispatcher;

    private TelegramAuthorizationManager authorizationManager;

    private TelegramClient telegramClient;

    @BeforeEach
    void setUp() {
        authorizationManager = new TelegramAuthorizationManager();

        telegramClient = new TelegramClient(
                properties(),
                updateDispatcher,
                authorizationManager,
                client
        );
    }

    @Test
    void sendWithCallback_shouldRejectNullQuery() {
        assertThatThrownBy(() ->
                telegramClient.sendWithCallback(
                        null,
                        (result, error) -> {
                        }
                )
        )
                .isInstanceOf(NullPointerException.class);

        verify(client, never()).send(any(), any());
    }

    @Test
    void sendWithCallback_shouldRejectNullResultHandler() {
        TdApi.GetAuthorizationState query =
                new TdApi.GetAuthorizationState();

        assertThatThrownBy(() ->
                telegramClient.sendWithCallback(query, null)
        )
                .isInstanceOf(NullPointerException.class);

        verify(client, never()).send(any(), any());
    }

    @Test
    void sendWithCallback_shouldReturnSuccessfulResult() {
        TdApi.GetAuthorizationState query =
                new TdApi.GetAuthorizationState();

        TdApi.AuthorizationStateReady expected =
                new TdApi.AuthorizationStateReady();

        doAnswer(invocation -> {
            Client.ResultHandler handler =
                    invocation.getArgument(1);

            handler.onResult(expected);

            return null;
        }).when(client).send(any(), any());

        var resultHolder = new Object() {
            TdApi.AuthorizationState result;
            TdApi.Error error;
        };

        telegramClient.sendWithCallback(
                query,
                (result, error) -> {
                    resultHolder.result = result;
                    resultHolder.error = error;
                }
        );

        assertThat(resultHolder.result)
                .isSameAs(expected);

        assertThat(resultHolder.error)
                .isNull();

        verify(client).send(any(), any());
    }

    @Test
    void sendWithCallback_shouldReturnError() {
        TdApi.GetAuthorizationState query =
                new TdApi.GetAuthorizationState();

        TdApi.Error expected =
                new TdApi.Error(401, "Unauthorized");

        doAnswer(invocation -> {
            Client.ResultHandler handler =
                    invocation.getArgument(1);

            handler.onResult(expected);

            return null;
        }).when(client).send(any(), any());

        var resultHolder = new Object() {
            TdApi.Object result;
            TdApi.Error error;
        };

        telegramClient.sendWithCallback(
                query,
                (result, error) -> {
                    resultHolder.result = result;
                    resultHolder.error = error;
                }
        );

        assertThat(resultHolder.result)
                .isNull();

        assertThat(resultHolder.error)
                .isSameAs(expected);

        verify(client).send(any(), any());
    }

    @Test
    void sendAsync_shouldRejectNullQuery() {
        assertThatThrownBy(() ->
                telegramClient.sendAsync(null)
        )
                .isInstanceOf(NullPointerException.class);

        verify(client, never()).send(any(), any());
    }

    @Test
    void sendAsync_shouldCompleteSuccessfully() throws Exception {
        TdApi.GetAuthorizationState query =
                new TdApi.GetAuthorizationState();

        TdApi.AuthorizationStateReady expected =
                new TdApi.AuthorizationStateReady();

        doAnswer(invocation -> {
            Client.ResultHandler handler =
                    invocation.getArgument(1);

            handler.onResult(expected);

            return null;
        }).when(client).send(any(), any());

        CompletableFuture<TdlibResponse<TdApi.AuthorizationState>> future =
                telegramClient.sendAsync(query);

        TdlibResponse<TdApi.AuthorizationState> response =
                future.get(1, TimeUnit.SECONDS);

        assertThat(response)
                .isNotNull();

        assertThat(response.getObject())
                .contains(expected);

        assertThat(response.getError())
                .isEmpty();

        verify(client).send(any(), any());
    }

    @Test
    void sendAsync_shouldCompleteWithTdlibError() throws Exception {
        TdApi.GetAuthorizationState query =
                new TdApi.GetAuthorizationState();

        TdApi.Error expected =
                new TdApi.Error(401, "Unauthorized");

        doAnswer(invocation -> {
            Client.ResultHandler handler =
                    invocation.getArgument(1);

            handler.onResult(expected);

            return null;
        }).when(client).send(any(), any());

        CompletableFuture<TdlibResponse<TdApi.AuthorizationState>> future =
                telegramClient.sendAsync(query);

        TdlibResponse<TdApi.AuthorizationState> response =
                future.get(1, TimeUnit.SECONDS);

        assertThat(response)
                .isNotNull();

        assertThat(response.getObject())
                .isEmpty();

        assertThat(response.getError())
                .contains(expected);

        verify(client).send(any(), any());
    }

    @Test
    void sendAsync_shouldCompleteExceptionallyWhenClientSendFails() {
        TdApi.GetAuthorizationState query =
                new TdApi.GetAuthorizationState();

        RuntimeException expected =
                new IllegalStateException("Client failure");

        doThrow(expected)
                .when(client)
                .send(any(), any());

        CompletableFuture<TdlibResponse<TdApi.AuthorizationState>> future =
                telegramClient.sendAsync(query);

        assertThat(future)
                .isCompletedExceptionally();

        assertThatThrownBy(future::join)
                .hasCause(expected);

        verify(client).send(any(), any());
    }

    @Test
    void send_shouldRejectNullQuery() {
        assertThatThrownBy(() ->
                telegramClient.send(null)
        )
                .isInstanceOf(NullPointerException.class);

        verify(client, never()).send(any(), any());
    }

    @Test
    void send_shouldReturnSuccessfulResponse() {
        TdApi.GetAuthorizationState query =
                new TdApi.GetAuthorizationState();

        TdApi.AuthorizationStateReady expected =
                new TdApi.AuthorizationStateReady();

        doAnswer(invocation -> {
            Client.ResultHandler handler =
                    invocation.getArgument(1);

            handler.onResult(expected);

            return null;
        }).when(client).send(any(), any());

        TdlibResponse<TdApi.AuthorizationState> response =
                telegramClient.send(query);

        assertThat(response)
                .isNotNull();

        assertThat(response.getObject())
                .contains(expected);

        assertThat(response.getError())
                .isEmpty();

        verify(client).send(any(), any());
    }

    @Test
    void send_shouldReturnTdlibErrorResponse() {
        TdApi.GetAuthorizationState query =
                new TdApi.GetAuthorizationState();

        TdApi.Error expected =
                new TdApi.Error(401, "Unauthorized");

        doAnswer(invocation -> {
            Client.ResultHandler handler =
                    invocation.getArgument(1);

            handler.onResult(expected);

            return null;
        }).when(client).send(any(), any());

        TdlibResponse<TdApi.AuthorizationState> response =
                telegramClient.send(query);

        assertThat(response)
                .isNotNull();

        assertThat(response.getObject())
                .isEmpty();

        assertThat(response.getError())
                .contains(expected);

        verify(client).send(any(), any());
    }

    @Test
    void send_shouldPropagateRuntimeExceptionFromAsyncOperation() {
        TdApi.GetAuthorizationState query =
                new TdApi.GetAuthorizationState();

        RuntimeException expected =
                new IllegalStateException("Client failure");

        doThrow(expected)
                .when(client)
                .send(any(), any());

        assertThatThrownBy(() ->
                telegramClient.send(query)
        )
                .isSameAs(expected);

        verify(client).send(any(), any());
    }

    private TelegramProperties properties() {
        return new TelegramProperties(
                false,
                "tdlib/database",
                "tdlib/files",
                "test-encryption-key",
                true,
                true,
                true,
                false,
                123456,
                "test-api-hash",
                "+79991234567",
                "en",
                "test-device",
                "Windows 11",
                "test",
                1,
                null
        );
    }
}