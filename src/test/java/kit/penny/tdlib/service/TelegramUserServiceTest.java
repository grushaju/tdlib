package kit.penny.tdlib.service;

import kit.penny.tdlib.client.TelegramClient;
import kit.penny.tdlib.query.TdlibResponse;
import org.drinkless.tdlib.TdApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TelegramUserServiceTest {

    @Mock
    private TelegramClient telegramClient;

    private TelegramUserService service;

    @BeforeEach
    void setUp() {
        service = new TelegramUserService(telegramClient);
    }

    @Test
    void getUser_shouldSendGetUser() {
        long userId = 123L;
        TdApi.User user = new TdApi.User();

        when(telegramClient.sendAsync(any(TdApi.GetUser.class)))
                .thenReturn(success(user));

        TdlibResponse<TdApi.User> response =
                service.getUser(userId).join();

        assertThat(response.getObject()).containsSame(user);
        assertThat(response.getError()).isEmpty();

        verify(telegramClient)
                .sendAsync(any(TdApi.GetUser.class));
    }

    @Test
    void getUserFullInfo_shouldSendGetUserFullInfo() {
        long userId = 123L;
        TdApi.UserFullInfo info = new TdApi.UserFullInfo();

        when(telegramClient.sendAsync(any(TdApi.GetUserFullInfo.class)))
                .thenReturn(success(info));

        TdlibResponse<TdApi.UserFullInfo> response =
                service.getUserFullInfo(userId).join();

        assertThat(response.getObject()).containsSame(info);

        verify(telegramClient)
                .sendAsync(any(TdApi.GetUserFullInfo.class));
    }

    @Test
    void getUserLink_shouldSendGetUserLink() {
        TdApi.UserLink userLink = new TdApi.UserLink();

        when(telegramClient.sendAsync(any(TdApi.GetUserLink.class)))
                .thenReturn(success(userLink));

        TdlibResponse<TdApi.UserLink> response =
                service.getUserLink().join();

        assertThat(response.getObject()).containsSame(userLink);

        verify(telegramClient)
                .sendAsync(any(TdApi.GetUserLink.class));
    }

    @Test
    void getMe_shouldSendGetMe() {
        TdApi.User user = new TdApi.User();

        when(telegramClient.sendAsync(any(TdApi.GetMe.class)))
                .thenReturn(success(user));

        TdlibResponse<TdApi.User> response =
                service.getMe().join();

        assertThat(response.getObject()).containsSame(user);

        verify(telegramClient)
                .sendAsync(any(TdApi.GetMe.class));
    }

    @Test
    void getProfilePhoto_shouldExtractProfilePhoto() {
        TdApi.ProfilePhoto photo = new TdApi.ProfilePhoto();

        TdApi.User user = new TdApi.User();
        user.profilePhoto = photo;

        when(telegramClient.sendAsync(any(TdApi.GetUser.class)))
                .thenReturn(success(user));

        TdlibResponse<TdApi.ProfilePhoto> response =
                service.getProfilePhoto(123L).join();

        assertThat(response.getObject())
                .containsSame(photo);

        assertThat(response.getError())
                .isEmpty();
    }

    @Test
    void getProfilePhoto_shouldPropagateError() {
        TdApi.Error error =
                new TdApi.Error(400, "User not found");

        when(telegramClient.sendAsync(any(TdApi.GetUser.class)))
                .thenReturn(error(error));

        TdlibResponse<TdApi.ProfilePhoto> response =
                service.getProfilePhoto(123L).join();

        assertThat(response.getObject()).isEmpty();
        assertThat(response.getError()).containsSame(error);
    }

    @Test
    void getPublicPhoto_shouldExtractPublicPhoto() {
        TdApi.ChatPhoto photo = new TdApi.ChatPhoto();

        TdApi.UserFullInfo info = new TdApi.UserFullInfo();
        info.publicPhoto = photo;

        when(telegramClient.sendAsync(any(TdApi.GetUserFullInfo.class)))
                .thenReturn(success(info));

        TdlibResponse<TdApi.ChatPhoto> response =
                service.getPublicPhoto(123L).join();

        assertThat(response.getObject())
                .containsSame(photo);

        assertThat(response.getError())
                .isEmpty();
    }

    @Test
    void getPublicPhoto_shouldPropagateError() {
        TdApi.Error error =
                new TdApi.Error(400, "User not found");

        when(telegramClient.sendAsync(any(TdApi.GetUserFullInfo.class)))
                .thenReturn(error(error));

        TdlibResponse<TdApi.ChatPhoto> response =
                service.getPublicPhoto(123L).join();

        assertThat(response.getObject()).isEmpty();
        assertThat(response.getError()).containsSame(error);
    }

    @Test
    void getUserProfilePhotos_shouldSendGetUserProfilePhotos() {
        TdApi.ChatPhotos photos = new TdApi.ChatPhotos();

        when(telegramClient.sendAsync(any(TdApi.GetUserProfilePhotos.class)))
                .thenReturn(success(photos));

        TdlibResponse<TdApi.ChatPhotos> response =
                service.getUserProfilePhotos(123L, 10, 20).join();

        assertThat(response.getObject())
                .containsSame(photos);

        verify(telegramClient)
                .sendAsync(any(TdApi.GetUserProfilePhotos.class));
    }

    @Test
    void searchUserByPhoneNumber_shouldSendSearchUserByPhoneNumber() {
        TdApi.User user = new TdApi.User();

        when(telegramClient.sendAsync(any(TdApi.SearchUserByPhoneNumber.class)))
                .thenReturn(success(user));

        TdlibResponse<TdApi.User> response =
                service.searchUserByPhoneNumber("+79991234567").join();

        assertThat(response.getObject())
                .containsSame(user);

        verify(telegramClient)
                .sendAsync(any(TdApi.SearchUserByPhoneNumber.class));
    }

    @Test
    void searchUserByPhoneNumber_shouldRejectNullPhoneNumber() {
        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> service.searchUserByPhoneNumber(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void searchUserByUsername_shouldReturnUserFromPrivateChat() {
        TdApi.User user = new TdApi.User();

        TdApi.Chat chat = new TdApi.Chat();
        chat.type = new TdApi.ChatTypePrivate(123L);

        when(telegramClient.sendAsync(any(TdApi.SearchPublicChat.class)))
                .thenReturn(success(chat));

        when(telegramClient.sendAsync(any(TdApi.GetUser.class)))
                .thenReturn(success(user));

        TdlibResponse<TdApi.User> response =
                service.searchUserByUsername("john").join();

        assertThat(response.getObject())
                .containsSame(user);

        assertThat(response.getError())
                .isEmpty();

        verify(telegramClient)
                .sendAsync(any(TdApi.SearchPublicChat.class));

        verify(telegramClient)
                .sendAsync(any(TdApi.GetUser.class));
    }

    @Test
    void searchUserByUsername_shouldPropagateSearchError() {
        TdApi.Error error =
                new TdApi.Error(400, "Chat not found");

        when(telegramClient.sendAsync(any(TdApi.SearchPublicChat.class)))
                .thenReturn(error(error));

        TdlibResponse<TdApi.User> response =
                service.searchUserByUsername("unknown").join();

        assertThat(response.getObject()).isEmpty();
        assertThat(response.getError()).containsSame(error);
    }

    @Test
    void searchUserByUsername_shouldReturnError_whenChatIsNotPrivate() {
        TdApi.Chat chat = new TdApi.Chat();
        chat.type = new TdApi.ChatTypeSupergroup(456L, false);

        when(telegramClient.sendAsync(any(TdApi.SearchPublicChat.class)))
                .thenReturn(success(chat));

        TdlibResponse<TdApi.User> response =
                service.searchUserByUsername("channel").join();

        assertThat(response.getObject()).isEmpty();
        assertThat(response.getError()).isPresent();

        verify(telegramClient)
                .sendAsync(any(TdApi.SearchPublicChat.class));
    }

    @Test
    void searchUserByUsername_shouldRejectNullUsername() {
        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> service.searchUserByUsername(null))
                .isInstanceOf(NullPointerException.class);
    }

    private static <T extends TdApi.Object> CompletableFuture<TdlibResponse<T>> success(T object) {
        return CompletableFuture.completedFuture(
                new TdlibResponse<>(object, null)
        );
    }

    private static <T extends TdApi.Object> CompletableFuture<TdlibResponse<T>> error(
            TdApi.Error error) {

        return CompletableFuture.completedFuture(
                new TdlibResponse<>(null, error)
        );
    }
}