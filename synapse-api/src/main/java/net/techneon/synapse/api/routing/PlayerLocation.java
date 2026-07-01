package net.techneon.synapse.api.routing;

import java.util.UUID;

/**
 * An immutable snapshot of where a player currently is on the network: which
 * server hosts them, plus their id and last-known name.
 *
 * <p>Obtained from the network-wide player registry, for example
 * {@code Synapse.findPlayer(uuid)} or {@code Synapse.getNetworkPlayers()}. The
 * registry is kept in sync automatically as players join, leave, and switch
 * servers.
 *
 * @since 1.1
 */
public final class PlayerLocation {

    private final UUID uniqueId;
    private final String name;
    private final String server;

    /**
     * @param uniqueId the player's UUID
     * @param name     the player's name
     * @param server   the logical name of the server hosting the player
     */
    public PlayerLocation(UUID uniqueId, String name, String server) {
        this.uniqueId = uniqueId;
        this.name = name;
        this.server = server;
    }

    /** @return the player's UUID */
    public UUID getUniqueId() {
        return uniqueId;
    }

    /** @return the player's last-known name */
    public String getName() {
        return name;
    }

    /** @return the logical name of the server currently hosting the player */
    public String getServer() {
        return server;
    }

    @Override
    public String toString() {
        return name + "(" + uniqueId + ") @ " + server;
    }
}
