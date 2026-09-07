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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TelegramChatServiceTest {

    @Mock
    private TelegramClient telegramClient;

    private TelegramChatService service;

    @BeforeEach
    void setUp() {
        service = new TelegramChatService(telegramClient);
    }

    @Test
    void getChat_shouldSendGetChat() {
        TdApi.Chat chat = new TdApi.Chat();

        when(telegramClient.sendAsync(any(TdApi.GetChat.class)))
                .thenReturn(success(chat));

        TdlibResponse<TdApi.Chat> response =
                service.getChat(123L).join();

        assertThat(response.getObject()).containsSame(chat);
        assertThat(response.getError()).isEmpty();

        verify(telegramClient)
                .sendAsync(any(TdApi.GetChat.class));
    }

    @Test
    void joinChat_shouldSendJoinChat() {
        when(telegramClient.sendAsync(any(TdApi.JoinChat.class)))
                .thenReturn(CompletableFuture.completedFuture(
                        new TdlibResponse<>(null, null)
                ));

        TdlibResponse<TdApi.ChatJoinResult> response =
                service.joinChat(123L).join();

        assertThat(response.getObject()).isEmpty();
        assertThat(response.getError()).isEmpty();

        verify(telegramClient)
                .sendAsync(any(TdApi.JoinChat.class));
    }

    @Test
    void searchChats_shouldSendSearchChats() {
        TdApi.Chats chats = new TdApi.Chats();

        when(telegramClient.sendAsync(any(TdApi.SearchChats.class)))
                .thenReturn(success(chats));

        TdlibResponse<TdApi.Chats> response =
                service.searchChats("john", 20).join();

        assertThat(response.getObject()).containsSame(chats);

        verify(telegramClient)
                .sendAsync(any(TdApi.SearchChats.class));
    }

    @Test
    void searchChats_shouldRejectNullQuery() {
        assertThatThrownBy(
                () -> service.searchChats(null, 20))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void searchPublicChat_shouldSendSearchPublicChat() {
        TdApi.Chat chat = new TdApi.Chat();

        when(telegramClient.sendAsync(any(TdApi.SearchPublicChat.class)))
                .thenReturn(success(chat));

        TdlibResponse<TdApi.Chat> response =
                service.searchPublicChat("telegram").join();

        assertThat(response.getObject()).containsSame(chat);

        verify(telegramClient)
                .sendAsync(any(TdApi.SearchPublicChat.class));
    }

    @Test
    void searchPublicChat_shouldRejectNullUsername() {
        assertThatThrownBy(
                () -> service.searchPublicChat(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void searchPublicChats_shouldSendSearchPublicChats() {
        TdApi.Chats chats = new TdApi.Chats();

        when(telegramClient.sendAsync(any(TdApi.SearchPublicChats.class)))
                .thenReturn(success(chats));

        TdlibResponse<TdApi.Chats> response =
                service.searchPublicChats("telegram").join();

        assertThat(response.getObject()).containsSame(chats);

        verify(telegramClient)
                .sendAsync(any(TdApi.SearchPublicChats.class));
    }

    @Test
    void searchPublicChats_shouldRejectNullQuery() {
        assertThatThrownBy(
                () -> service.searchPublicChats(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void leaveChat_shouldSendLeaveChat() {
        TdApi.Ok result = new TdApi.Ok();

        when(telegramClient.sendAsync(any(TdApi.LeaveChat.class)))
                .thenReturn(success(result));

        TdlibResponse<TdApi.Ok> response =
                service.leaveChat(123L).join();

        assertThat(response.getObject()).containsSame(result);

        verify(telegramClient)
                .sendAsync(any(TdApi.LeaveChat.class));
    }

    @Test
    void deleteChat_shouldSendDeleteChat() {
        TdApi.Ok result = new TdApi.Ok();

        when(telegramClient.sendAsync(any(TdApi.DeleteChat.class)))
                .thenReturn(success(result));

        TdlibResponse<TdApi.Ok> response =
                service.deleteChat(123L).join();

        assertThat(response.getObject()).containsSame(result);

        verify(telegramClient)
                .sendAsync(any(TdApi.DeleteChat.class));
    }

    @Test
    void getBasicGroup_shouldSendGetBasicGroup() {
        TdApi.BasicGroup group = new TdApi.BasicGroup();

        when(telegramClient.sendAsync(any(TdApi.GetBasicGroup.class)))
                .thenReturn(success(group));

        TdlibResponse<TdApi.BasicGroup> response =
                service.getBasicGroup(123L).join();

        assertThat(response.getObject()).containsSame(group);

        verify(telegramClient)
                .sendAsync(any(TdApi.GetBasicGroup.class));
    }

    @Test
    void getBasicGroupFullInfo_shouldSendGetBasicGroupFullInfo() {
        TdApi.BasicGroupFullInfo info = new TdApi.BasicGroupFullInfo();

        when(telegramClient.sendAsync(any(TdApi.GetBasicGroupFullInfo.class)))
                .thenReturn(success(info));

        TdlibResponse<TdApi.BasicGroupFullInfo> response =
                service.getBasicGroupFullInfo(123L).join();

        assertThat(response.getObject()).containsSame(info);

        verify(telegramClient)
                .sendAsync(any(TdApi.GetBasicGroupFullInfo.class));
    }

    @Test
    void getSupergroup_shouldSendGetSupergroup() {
        TdApi.Supergroup group = new TdApi.Supergroup();

        when(telegramClient.sendAsync(any(TdApi.GetSupergroup.class)))
                .thenReturn(success(group));

        TdlibResponse<TdApi.Supergroup> response =
                service.getSupergroup(123L).join();

        assertThat(response.getObject()).containsSame(group);

        verify(telegramClient)
                .sendAsync(any(TdApi.GetSupergroup.class));
    }

    @Test
    void getSupergroupFullInfo_shouldSendGetSupergroupFullInfo() {
        TdApi.SupergroupFullInfo info = new TdApi.SupergroupFullInfo();

        when(telegramClient.sendAsync(any(TdApi.GetSupergroupFullInfo.class)))
                .thenReturn(success(info));

        TdlibResponse<TdApi.SupergroupFullInfo> response =
                service.getSupergroupFullInfo(123L).join();

        assertThat(response.getObject()).containsSame(info);

        verify(telegramClient)
                .sendAsync(any(TdApi.GetSupergroupFullInfo.class));
    }

    @Test
    void getChat_shouldPropagateError() {
        TdApi.Error error =
                new TdApi.Error(400, "Chat not found");

        when(telegramClient.sendAsync(any(TdApi.GetChat.class)))
                .thenReturn(error(error));

        TdlibResponse<TdApi.Chat> response =
                service.getChat(123L).join();

        assertThat(response.getObject()).isEmpty();
        assertThat(response.getError()).containsSame(error);
    }

    private static <T extends TdApi.Object> CompletableFuture<TdlibResponse<T>> success(
            T object) {

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