package kit.penny.tdlib.updates;

import kit.penny.tdlib.client.TelegramClient;
import kit.penny.tdlib.properties.TelegramProperties;
import org.drinkless.tdlib.TdApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UpdateAuthorizationStateTest {

    private TelegramClient telegramClient;
    private TelegramAuthorizationManager authorizationManager;
    private TelegramProperties properties;
    private UpdateAuthorizationState updateAuthorizationState;

    @BeforeEach
    void setUp() {
        telegramClient = mock(TelegramClient.class);
        authorizationManager = new TelegramAuthorizationManager();

        properties = new TelegramProperties(
                false,
                "/tmp/tdlib/database",
                "/tmp/tdlib/files",
                "encryption-key",
                true,
                true,
                true,
                false,
                123456,
                "api-hash",
                "+79990000000",
                "ru",
                "test-device",
                "Linux",
                "1.0.0",
                0,
                null
        );

        updateAuthorizationState = new UpdateAuthorizationState(
                properties,
                telegramClient,
                authorizationManager
        );
    }

    @Test
    void shouldSendTdlibParameters() {
        updateAuthorizationState.handleNotification(
                new TdApi.UpdateAuthorizationState(
                        new TdApi.AuthorizationStateWaitTdlibParameters()
                )
        );

        ArgumentCaptor<TdApi.SetTdlibParameters> captor =
                ArgumentCaptor.forClass(TdApi.SetTdlibParameters.class);

        verify(telegramClient).sendWithCallback(
                captor.capture(),
                any()
        );

        TdApi.SetTdlibParameters request = captor.getValue();

        assertThat(request.useTestDc).isFalse();
        assertThat(request.databaseDirectory)
                .isEqualTo("/tmp/tdlib/database");
        assertThat(request.filesDirectory)
                .isEqualTo("/tmp/tdlib/files");
        assertThat(request.apiId).isEqualTo(123456);
        assertThat(request.apiHash).isEqualTo("api-hash");
        assertThat(request.systemLanguageCode).isEqualTo("ru");
        assertThat(request.deviceModel).isEqualTo("test-device");
        assertThat(request.systemVersion).isEqualTo("Linux");
        assertThat(request.applicationVersion).isEqualTo("1.0.0");
    }

    @Test
    void shouldSendAuthenticationPhoneNumber() {
        updateAuthorizationState.handleNotification(
                new TdApi.UpdateAuthorizationState(
                        new TdApi.AuthorizationStateWaitPhoneNumber()
                )
        );

        ArgumentCaptor<TdApi.SetAuthenticationPhoneNumber> captor =
                ArgumentCaptor.forClass(
                        TdApi.SetAuthenticationPhoneNumber.class
                );

        verify(telegramClient).sendWithCallback(
                captor.capture(),
                any()
        );

        TdApi.SetAuthenticationPhoneNumber request = captor.getValue();

        assertThat(request.phoneNumber)
                .isEqualTo("+79990000000");
    }

    @Test
    void shouldWaitForAuthenticationCodeAndSendIt() {
        updateAuthorizationState.handleNotification(
                new TdApi.UpdateAuthorizationState(
                        new TdApi.AuthorizationStateWaitCode(
                                new TdApi.AuthenticationCodeInfo()
                        )
                )
        );

        verifyNoInteractions(telegramClient);

        assertThat(authorizationManager.isWaitAuthenticationCode())
                .isTrue();

        authorizationManager.checkAuthenticationCode("12345");

        ArgumentCaptor<TdApi.CheckAuthenticationCode> captor =
                ArgumentCaptor.forClass(
                        TdApi.CheckAuthenticationCode.class
                );

        verify(telegramClient).sendWithCallback(
                captor.capture(),
                any()
        );

        assertThat(captor.getValue().code)
                .isEqualTo("12345");
    }

    @Test
    void shouldWaitForAuthenticationPasswordAndSendIt() {
        updateAuthorizationState.handleNotification(
                new TdApi.UpdateAuthorizationState(
                        new TdApi.AuthorizationStateWaitPassword()
                )
        );

        verifyNoInteractions(telegramClient);

        assertThat(authorizationManager.isWaitAuthenticationPassword())
                .isTrue();

        authorizationManager.checkAuthenticationPassword("password");

        ArgumentCaptor<TdApi.CheckAuthenticationPassword> captor =
                ArgumentCaptor.forClass(
                        TdApi.CheckAuthenticationPassword.class
                );

        verify(telegramClient).sendWithCallback(
                captor.capture(),
                any()
        );

        assertThat(captor.getValue().password)
                .isEqualTo("password");
    }

    @Test
    void shouldWaitForEmailAddressAndSendIt() {
        updateAuthorizationState.handleNotification(
                new TdApi.UpdateAuthorizationState(
                        new TdApi.AuthorizationStateWaitEmailAddress()
                )
        );

        verifyNoInteractions(telegramClient);

        assertThat(authorizationManager.isWaitEmailAddress())
                .isTrue();

        authorizationManager.checkEmailAddress("user@example.com");

        ArgumentCaptor<TdApi.SetAuthenticationEmailAddress> captor =
                ArgumentCaptor.forClass(
                        TdApi.SetAuthenticationEmailAddress.class
                );

        verify(telegramClient).sendWithCallback(
                captor.capture(),
                any()
        );

        assertThat(captor.getValue().emailAddress)
                .isEqualTo("user@example.com");
    }

    @Test
    void shouldWaitForEmailAuthenticationCodeAndSendIt() {
        updateAuthorizationState.handleNotification(
                new TdApi.UpdateAuthorizationState(
                        new TdApi.AuthorizationStateWaitEmailCode()));

        assertThat(authorizationManager.isWaitAuthenticationCode())
                .isTrue();

        authorizationManager.checkAuthenticationCode("67890");

        var requestCaptor =
                ArgumentCaptor.forClass(
                        TdApi.CheckAuthenticationEmailCode.class);

        verify(telegramClient).sendWithCallback(
                requestCaptor.capture(),
                any());

        var request = requestCaptor.getValue();

        assertThat(request).isNotNull();

        if (request == null) {
            throw new AssertionError(
                    "CheckAuthenticationEmailCode request was not sent");
        }

        if (!(request.code
                instanceof TdApi.EmailAddressAuthenticationCode authenticationCode)) {

            throw new AssertionError(
                    "Expected EmailAddressAuthenticationCode, but got: "
                            + (request.code == null
                            ? "null"
                            : request.code.getClass().getName()));
        }

        assertThat(authenticationCode.code)
                .isEqualTo("67890");
    }

    @Test
    void shouldMarkAuthorizationAsCompletedWhenReady() {
        updateAuthorizationState.handleNotification(
                new TdApi.UpdateAuthorizationState(
                        new TdApi.AuthorizationStateReady()
                )
        );

        assertThat(authorizationManager.haveAuthorization())
                .isTrue();

        verifyNoInteractions(telegramClient);
    }

    @Test
    void shouldResetAuthorizationWhenLoggingOut() {
        authorizationManager.setAuthorized(true);

        updateAuthorizationState.handleNotification(
                new TdApi.UpdateAuthorizationState(
                        new TdApi.AuthorizationStateLoggingOut()
                )
        );

        assertThat(authorizationManager.haveAuthorization())
                .isFalse();

        verifyNoInteractions(telegramClient);
    }

    @Test
    void shouldResetAuthorizationWhenClosing() {
        authorizationManager.setAuthorized(true);

        updateAuthorizationState.handleNotification(
                new TdApi.UpdateAuthorizationState(
                        new TdApi.AuthorizationStateClosing()
                )
        );

        assertThat(authorizationManager.haveAuthorization())
                .isFalse();

        assertThat(authorizationManager.isStateClosed())
                .isFalse();

        verifyNoInteractions(telegramClient);
    }

    @Test
    void shouldCloseAuthorizationManagerWhenClosed() {
        authorizationManager.setAuthorized(true);

        updateAuthorizationState.handleNotification(
                new TdApi.UpdateAuthorizationState(
                        new TdApi.AuthorizationStateClosed()
                )
        );

        assertThat(authorizationManager.haveAuthorization())
                .isFalse();

        assertThat(authorizationManager.isStateClosed())
                .isTrue();

        verifyNoInteractions(telegramClient);
    }

    @Test
    void shouldIgnoreNullNotification() {
        updateAuthorizationState.handleNotification(null);

        verifyNoInteractions(telegramClient);

        assertThat(authorizationManager.haveAuthorization())
                .isFalse();
        assertThat(authorizationManager.isStateClosed())
                .isFalse();
    }

    @Test
    void shouldLogOtherDeviceConfirmationWithoutSendingRequest() {
        updateAuthorizationState.handleNotification(
                new TdApi.UpdateAuthorizationState(
                        new TdApi.AuthorizationStateWaitOtherDeviceConfirmation(
                                "https://t.me/confirm"
                        )
                )
        );

        verifyNoInteractions(telegramClient);

        assertThat(authorizationManager.haveAuthorization())
                .isFalse();
    }

    @Test
    void shouldConfigureMtProtoProxy() {
        TelegramProperties.Proxy proxy =
                new TelegramProperties.Proxy(
                        "proxy.example.com",
                        443,
                        null,
                        null,
                        new TelegramProperties.Proxy.ProxyMtProto(
                                "secret"
                        )
                );

        TelegramProperties proxyProperties = new TelegramProperties(
                false,
                "/tmp/tdlib/database",
                "/tmp/tdlib/files",
                "encryption-key",
                true,
                true,
                true,
                false,
                123456,
                "api-hash",
                "+79990000000",
                "ru",
                "test-device",
                "Linux",
                "1.0.0",
                0,
                proxy
        );

        UpdateAuthorizationState handler =
                new UpdateAuthorizationState(
                        proxyProperties,
                        telegramClient,
                        authorizationManager
                );

        handler.handleNotification(
                new TdApi.UpdateAuthorizationState(
                        new TdApi.AuthorizationStateWaitTdlibParameters()
                )
        );

        verify(telegramClient, times(2))
                .sendWithCallback(any(), any());

        ArgumentCaptor<TdApi.AddProxy> proxyCaptor =
                ArgumentCaptor.forClass(TdApi.AddProxy.class);

        verify(telegramClient).sendWithCallback(
                proxyCaptor.capture(),
                any()
        );

        TdApi.AddProxy request = proxyCaptor.getValue();

        assertThat(request.proxy.server)
                .isEqualTo("proxy.example.com");
        assertThat(request.proxy.port)
                .isEqualTo(443);
        assertThat(request.proxy.type)
                .isInstanceOf(TdApi.ProxyTypeMtproto.class);
    }
}
