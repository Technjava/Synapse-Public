package net.techneon.synapse.api.server;

import java.util.Optional;

/**
 * An immutable, read-only view of a server that this node knows about, including
 * its current connection {@link ServerStatus} and most recent measured latency.
 *
 * <p>Instances are obtained from the topology API, for example
 * {@code Synapse.getServers()} or {@code Synapse.getServer("survival")}, and
 * reflect the state at the moment they were retrieved.
 *
 * @since 1.0
 */
public interface ServerInfo {

    /**
     * The unique logical name of this server, as declared in {@code config.yml}
     * (for example {@code "survival"} or {@code "lobby-1"}).
     *
     * @return the server's logical name; never {@code null}
     */
    String getName();

    /**
     * The host (IP or DNS name) Synapse uses to reach this server.
     *
     * @return the configured host
     */
    String getHost();

    /**
     * The TCP port Synapse uses to reach this server.
     *
     * @return the configured port
     */
    int getPort();

    /**
     * The group this server belongs to, if any. Groups let messages target a
     * logical cluster (for example {@code "survival-cluster"}) rather than a
     * single server name.
     *
     * @return the group name, or {@link Optional#empty()} if ungrouped
     */
    Optional<String> getGroup();

    /**
     * The live connection state of this server.
     *
     * @return the current status; never {@code null}
     */
    ServerStatus getStatus();

    /**
     * Convenience shortcut for {@code getStatus().isConnected()}.
     *
     * @return {@code true} if a usable connection currently exists
     */
    default boolean isConnected() {
        return getStatus().isConnected();
    }

    /**
     * The most recently measured round-trip latency to this server, in
     * milliseconds, as produced by Synapse's periodic heartbeat. A value of
     * {@code -1} means latency is not yet known (for example right after start-up
     * or while disconnected).
     *
     * @return the latency in milliseconds, or {@code -1} if unknown
     */
    long getLatencyMillis();
}
