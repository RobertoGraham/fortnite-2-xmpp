package io.github.robertograham.fortnite2.xmpp.listener;

import io.github.robertograham.fortnite2.xmpp.resource.ChatResource;

/**
 * Registered using {@link io.github.robertograham.fortnite2.xmpp.implementation.DefaultFortniteXmpp.Builder#setOnChatMessageReceivedListener(OnChatMessageReceivedListener)}
 *
 * @since 1.0.0
 */
@FunctionalInterface
public interface OnChatMessageReceivedListener {

    /**
     * called when an incoming chat message is intercepted
     *
     * @param accountId   ID of the account that sent this message
     * @param messageBody the body of this message
     * @param chat        {@link ChatResource} instance that can be used to reply to the message
     * @since 2.0.0
     */
    void onChatMessageReceived(final String accountId, final String messageBody, final ChatResource chat);
}
