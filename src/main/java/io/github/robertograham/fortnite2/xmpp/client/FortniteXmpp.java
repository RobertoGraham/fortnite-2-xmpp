package io.github.robertograham.fortnite2.xmpp.client;

import io.github.robertograham.fortnite2.xmpp.resource.ChatResource;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;

/**
 * An object from which all XMPP-related actions can be performed
 *
 * @since 1.0.0
 */
public interface FortniteXmpp extends AutoCloseable {

    @Override
    void close();

    /**
     * @return an object from which chat-related actions can be performed
     * @since 1.0.0
     */
    ChatResource chat();

    /**
     * @return the xmpp connection used to communicate with Epic Games' prod xmpp service
     * @since 1.2.0
     */
    @Deprecated
    XMPPTCPConnection prodService();
}
