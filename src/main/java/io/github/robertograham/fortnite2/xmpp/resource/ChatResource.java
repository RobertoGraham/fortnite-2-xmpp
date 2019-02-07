package io.github.robertograham.fortnite2.xmpp.resource;

import io.github.robertograham.fortnite2.domain.Account;
import io.github.robertograham.fortnite2.xmpp.client.FortniteXmpp;
import io.github.robertograham.fortnite2.xmpp.domain.enumeration.Status;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * an object from which chat-related actions can be performed
 *
 * @since 1.0.0
 */
public interface ChatResource {

    /**
     * @param account     the account to send this chat message to
     * @param messageBody the body of this chat message
     * @throws IOException          if an error occurs when sending the chat message
     * @throws NullPointerException if the {@code account} is {@code null}
     * @throws NullPointerException if the {@code messageBody} is {@code null}
     * @since 1.0.0
     */
    default void sendMessageToAccount(final Account account, final String messageBody) throws IOException {
        Objects.requireNonNull(account, "account cannot be null");
        sendMessageToAccountId(account.accountId(), messageBody);
    }

    /**
     * @param accountId   ID of the account to send this chat message to
     * @param messageBody the body of this chat message
     * @throws IOException          if an error occurs when sending the chat message
     * @throws NullPointerException if the {@code accountId} is {@code null}
     * @throws NullPointerException if the {@code messageBody} is {@code null}
     * @since 1.0.0
     */
    void sendMessageToAccountId(final String accountId, final String messageBody) throws IOException;

    /**
     * @param status How you will appear to friends
     * @throws IOException          if an error occurs sending a presence update
     * @throws NullPointerException is {@code status} is {@code null}
     * @since 1.2.0
     */
    void updateStatus(final Status status) throws IOException;

    /**
     * @param accountId ID of the account that the messages have been sent to
     * @return a {@link List} of the bodies of the messages sent to this user
     * @throws NullPointerException if {@code accountId} is {@code null}
     * @since 2.0.0
     */
    List<String> findAllMessagesSentToAccountId(final String accountId);

    /**
     * @param account the account that the messages have been sent to
     * @return a {@link List} of the bodies of the messages sent to this user
     * @throws NullPointerException if {@code account} is {@code null}
     * @since 2.0.0
     */
    default List<String> findAllMessagesSentToAccount(final Account account) {
        Objects.requireNonNull(account, "account cannot be null");
        return findAllMessagesSentToAccountId(account.accountId());
    }

    /**
     * @param accountId ID of the account that the messages have received from
     * @return a {@link List} of the bodies of the messages received from this user
     * @throws NullPointerException if {@code accountId} is {@code null}
     * @since 2.0.0
     */
    List<String> findAllMessagesReceivedFromAccountId(final String accountId);

    /**
     * @param account the account that the messages have received from
     * @return a {@link List} of the bodies of the messages received from this user
     * @throws NullPointerException if {@code account} is {@code null}
     * @since 2.0.0
     */
    default List<String> findAllMessagesReceivedFromAccount(final Account account) {
        Objects.requireNonNull(account, "account cannot be null");
        return findAllMessagesReceivedFromAccountId(account.accountId());
    }

    /**
     * @return the instance of {@link FortniteXmpp} this object belongs to
     * @since 2.0.0
     */
    FortniteXmpp fortniteXmpp();
}
