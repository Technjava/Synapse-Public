package net.techneon.synapse.api.server;

import java.util.Collection;

/**
 * An immutable view of a named group of servers, as declared in
 * {@code config.yml}. Groups let developers route messages to a logical cluster
 * (for example {@code "survival-cluster"}) instead of enumerating individual
 * server names.
 *
 * @since 1.0
 */
public interface ServerGroup {

    /**
     * The unique name of this group (for example {@code "survival-cluster"}).
     *
     * @return the group name; never {@code null}
     */
    String getName();

    /**
     * The members of this group.
     *
     * @return an immutable collection of the servers in this group; never
     *         {@code null}, possibly empty
     */
    Collection<ServerInfo> getServers();

    /**
     * @return {@code true} if at least one member of this group is currently
     *         connected
     */
    default boolean hasConnectedMember() {
        return getServers().stream().anyMatch(ServerInfo::isConnected);
    }
}
