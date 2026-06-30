package net.techneon.synapse.api;

/**
 * An isolated messaging scope for a single plugin or subsystem.
 *
 * <p>Namespaces prevent two independent plugins that both use Synapse on the same
 * server from interfering with one another: packet ids, listeners, and filters
 * registered in one namespace are invisible to another, and metrics and logs are
 * tagged with the namespace name.
 *
 * <pre>{@code
 * Namespace mine = Synapse.namespace("my-plugin");
 * mine.register(MyPacket.class);
 * mine.registerListener(new MyListener());
 * mine.send("survival", new MyPacket());
 * }</pre>
 *
 * <p>Obtain one with {@link Synapse#namespace(String)}. Calling it again with the
 * same name returns the same namespace instance.
 *
 * @since 1.0
 */
public interface Namespace extends SynapseScope {

    /**
     * The unique name of this namespace.
     *
     * @return the namespace name; never {@code null}
     */
    String getName();
}
