package net.techneon.synapse.api;

import net.techneon.synapse.api.event.LifecycleListener;
import net.techneon.synapse.api.interceptor.Interceptor;
import net.techneon.synapse.api.routing.PlayerLocation;
import net.techneon.synapse.api.server.ServerGroup;
import net.techneon.synapse.api.server.ServerInfo;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * The root Synapse service: the default messaging scope plus network-wide
 * capabilities such as topology inspection, namespaces, interceptors, lifecycle
 * hooks, and a low-level transport escape hatch.
 *
 * <p>Most code uses the static {@link Synapse} facade, which simply forwards to
 * the single {@code SynapseAPI} instance the running platform plugin installs.
 * Obtain the instance directly with {@link Synapse#get()} if you prefer to hold a
 * reference (for testing, dependency injection, or to avoid the static calls).
 *
 * @see Synapse
 * @since 1.0
 */
public interface SynapseAPI extends SynapseScope {

    /**
     * Whether the local node has finished starting up and is ready to send and
     * receive packets. The {@link LifecycleListener#onReady()} callback fires when
     * this first becomes {@code true}.
     *
     * @return {@code true} once Synapse is ready
     */
    boolean isReady();

    /**
     * Information about the local server itself, including its configured name and
     * group.
     *
     * @return the local server's info; never {@code null}
     */
    ServerInfo getLocalServer();

    // ------------------------------------------------------------------
    // Topology
    // ------------------------------------------------------------------

    /**
     * All servers this node knows about, connected or not.
     *
     * @return an immutable snapshot of known servers; never {@code null}
     */
    Collection<ServerInfo> getServers();

    /**
     * Looks up a single server by its logical name.
     *
     * @param name the server name
     * @return the server info, or {@link Optional#empty()} if not configured
     */
    Optional<ServerInfo> getServer(String name);

    /**
     * All configured server groups.
     *
     * @return an immutable snapshot of groups; never {@code null}
     */
    Collection<ServerGroup> getGroups();

    /**
     * Looks up a single group by name.
     *
     * @param name the group name
     * @return the group, or {@link Optional#empty()} if not configured
     */
    Optional<ServerGroup> getGroup(String name);

    // ------------------------------------------------------------------
    // Namespaces, interceptors, lifecycle
    // ------------------------------------------------------------------

    /**
     * Returns the isolated {@link Namespace} with the given name, creating it on
     * first use. Repeated calls with the same name return the same instance.
     *
     * @param name the namespace name
     * @return the namespace scope
     */
    Namespace namespace(String name);

    /**
     * Registers a network-wide {@link Interceptor} that observes or vetoes every
     * inbound and outbound packet, across all namespaces.
     *
     * @param interceptor the interceptor to add
     */
    void addInterceptor(Interceptor interceptor);

    /**
     * Registers a {@link LifecycleListener} for ready/shutdown/connect/disconnect
     * and dropped-packet notifications.
     *
     * @param listener the lifecycle listener to add
     */
    void addLifecycleListener(LifecycleListener listener);

    // ------------------------------------------------------------------
    // Low-level escape hatch
    // ------------------------------------------------------------------

    /**
     * Returns the underlying Netty {@code Channel} for the connection to a server,
     * for power users who need raw control (custom pipeline handlers, write
     * back-pressure, native options).
     *
     * <p>The returned object is an {@code io.netty.channel.Channel}. It is typed as
     * {@link Object} here because the public API deliberately carries no Netty
     * dependency and Netty is shaded/relocated inside the plugin. Cast it to the
     * relocated channel type only if you ship against the same relocation, and
     * never close it yourself &mdash; Synapse owns its lifecycle.
     *
     * @param serverName the server whose connection channel is wanted
     * @return the underlying channel, or {@link Optional#empty()} if not currently
     *         connected
     */
    Optional<Object> getUnderlyingChannel(String serverName);

    // ------------------------------------------------------------------
    // Network-wide player registry
    // ------------------------------------------------------------------

    /**
     * Finds where a player currently is on the network.
     *
     * @param playerId the player's UUID
     * @return the player's location, or {@link Optional#empty()} if not online
     *         anywhere on the network
     */
    Optional<PlayerLocation> findPlayer(UUID playerId);

    /**
     * Finds a player by (case-insensitive) name.
     *
     * @param name the player's name
     * @return the player's location, or {@link Optional#empty()} if not found
     */
    Optional<PlayerLocation> findPlayerByName(String name);

    /**
     * @param playerId the player's UUID
     * @return whether the player is online anywhere on the network
     */
    default boolean isPlayerOnline(UUID playerId) {
        return findPlayer(playerId).isPresent();
    }

    /**
     * @return an immutable snapshot of every player known to be online across the
     *         whole network
     */
    Collection<PlayerLocation> getNetworkPlayers();

    /**
     * @return the total number of players online across the whole network
     */
    int getNetworkPlayerCount();
}
