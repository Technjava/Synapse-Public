package net.techneon.synapse.api.server;

/**
 * Connection state of a remote server as seen from the local node.
 *
 * @since 1.0
 */
public enum ServerStatus {

    /** A TCP connection (and TLS handshake, if enabled) is established and ready. */
    CONNECTED,

    /** A connection attempt or handshake is currently in progress. */
    CONNECTING,

    /** Not currently connected; Synapse will keep retrying in the background. */
    DISCONNECTED;

    /**
     * @return {@code true} only when this status is {@link #CONNECTED}
     */
    public boolean isConnected() {
        return this == CONNECTED;
    }
}
