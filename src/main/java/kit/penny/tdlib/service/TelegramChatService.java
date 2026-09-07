package kit.penny.tdlib.service;

import kit.penny.tdlib.query.TdlibResponse;
import kit.penny.tdlib.client.TelegramClient;
import org.drinkless.tdlib.TdApi;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * This class simplifies the use of {@link TelegramClient} for chat related objects.
 *
 * @author Pavel Grushin
 */
public class TelegramChatService {

    private final TelegramClient telegramClient;

    public TelegramChatService(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    /**
     * Returns information about a chat by its identifier; this is an offline request if the current user is not a bot.
     *
     * @param chatId Chat identifier.
     * @return {@link CompletableFuture<      TdlibResponse      <TdApi.Chat>>}.
     */
    public CompletableFuture<TdlibResponse<TdApi.Chat>> getChat(long chatId) {
        return telegramClient.sendAsync(new TdApi.GetChat(chatId));
    }

    public CompletableFuture<TdlibResponse<TdApi.Chats>> getChats(
            TdApi.ChatList chatList,
            int limit
    ) {
        return telegramClient.sendAsync(
                new TdApi.GetChats(
                        chatList,
                        limit
                )
        );
    }

    /**
     * Adds the current user as a new member to a chat. Private and secret chats can't be joined using this method.
     * May return an error with a message "INVITE_REQUEST_SENT" if only a join request was created.
     *
     * @param chatId Chat identifier.
     * @return {@link CompletableFuture< TdlibResponse <TdApi.ChatJoinResult>>} Response to action.
     */
    public CompletableFuture<TdlibResponse<TdApi.ChatJoinResult>> joinChat(long chatId) {
        return telegramClient.sendAsync(new TdApi.JoinChat(chatId));
    }

    /**
     * Searches for the specified query in the title and username of already known chats; this is an offline request.
     * Returns chats in the order seen in the main chat list.
     *
     * @param query Query to search for.
     * @param limit The maximum number of chats to be returned.
     * @return {@link CompletableFuture< TdlibResponse <TdApi.Chats>>}.
     */
    public CompletableFuture<TdlibResponse<TdApi.Chats>> searchChats(String query, int limit) {
        Objects.requireNonNull(query);
        return telegramClient.sendAsync(new TdApi.SearchChats(query, null, limit));
    }

    /**
     * Searches a public chat by its username. Currently, only private chats (with public usernames), supergroups and channels can be public.
     * Returns the chat if found; otherwise, an error is returned.
     *
     * @param username Username to be resolved.
     * @return {@link CompletableFuture< TdlibResponse <TdApi.Chat>>}.
     */
    public CompletableFuture<TdlibResponse<TdApi.Chat>> searchPublicChat(String username) {
        Objects.requireNonNull(username);
        return telegramClient.sendAsync(new TdApi.SearchPublicChat(username));
    }

    /**
     * Searches public chats by looking for specified query in their username and title. Currently, only private chats
     * (with public usernames), supergroups and channels can be public. Returns a meaningful number of results.
     * Excludes private chats with contacts and chats already in the chat list from the results.
     *
     * @param query Query to search for.
     * @return {@link CompletableFuture< TdlibResponse <TdApi.Chats>>}.
     */
    public CompletableFuture<TdlibResponse<TdApi.Chats>> searchPublicChats(String query) {
        Objects.requireNonNull(query);
        return telegramClient.sendAsync(new TdApi.SearchPublicChats(query, null));
    }

    /**
     * Removes the current user from chat members. Private and secret chats can't be left using this method.
     *
     * @param chatId Chat identifier.
     * @return {@link CompletableFuture< TdlibResponse <TdApi.Ok>>}.
     */
    public CompletableFuture<TdlibResponse<TdApi.Ok>> leaveChat(long chatId) {
        return telegramClient.sendAsync(new TdApi.LeaveChat(chatId));
    }

    /**
     * Deletes a chat along with all messages in the corresponding chat for all chat members.
     * For group chats this will release the usernames and remove all members.
     * Use the field chat.canBeDeletedForAllUsers to find whether the method can be applied to the chat.
     *
     * @param chatId Chat identifier.
     * @return {@link CompletableFuture< TdlibResponse <TdApi.Ok>>}.
     */
    public CompletableFuture<TdlibResponse<TdApi.Ok>> deleteChat(long chatId) {
        return telegramClient.sendAsync(new TdApi.DeleteChat(chatId));
    }

    /**
     * Returns information about a basic group by its identifier. This is an offline request if the current user is not a bot.
     *
     * @param basicGroupId Basic group identifier.
     * @return {@link CompletableFuture< TdlibResponse <TdApi.BasicGroup>>}.
     */
    public CompletableFuture<TdlibResponse<TdApi.BasicGroup>> getBasicGroup(long basicGroupId) {
        return telegramClient.sendAsync(new TdApi.GetBasicGroup(basicGroupId));
    }

    /**
     * Returns full information about a basic group by its identifier.
     *
     * @param basicGroupId Basic group identifier.
     * @return {@link CompletableFuture< TdlibResponse <TdApi.BasicGroupFullInfo>>}.
     */
    public CompletableFuture<TdlibResponse<TdApi.BasicGroupFullInfo>> getBasicGroupFullInfo(long basicGroupId) {
        return telegramClient.sendAsync(new TdApi.GetBasicGroupFullInfo(basicGroupId));
    }

    /**
     * Returns information about a supergroup or a channel by its identifier.
     * This is an offline request if the current user is not a bot.
     *
     * @param supergroupId Supergroup or channel identifier.
     * @return {@link CompletableFuture< TdlibResponse <TdApi.Supergroup>>}.
     */
    public CompletableFuture<TdlibResponse<TdApi.Supergroup>> getSupergroup(long supergroupId) {
        return telegramClient.sendAsync(new TdApi.GetSupergroup(supergroupId));
    }

    /**
     * Returns full information about a supergroup or a channel by its identifier, cached for up to 1 minute.
     *
     * @param supergroupId Supergroup or channel identifier.
     * @return {@link CompletableFuture< TdlibResponse <TdApi.SupergroupFullInfo>>}.
     */
    public CompletableFuture<TdlibResponse<TdApi.SupergroupFullInfo>> getSupergroupFullInfo(long supergroupId) {
        return telegramClient.sendAsync(new TdApi.GetSupergroupFullInfo(supergroupId));
    }

}
