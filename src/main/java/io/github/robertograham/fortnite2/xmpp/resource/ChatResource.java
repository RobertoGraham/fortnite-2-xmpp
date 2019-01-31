package io.github.robertograham.fortnite2.xmpp.resource;

import io.github.robertograham.fortnite2.domain.Account;

import java.io.IOException;
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
}
