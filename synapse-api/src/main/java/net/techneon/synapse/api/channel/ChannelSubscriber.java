package net.techneon.synapse.api.channel;

/**
 * A callback invoked for every {@link ChannelMessage} received on a subscribed
 * pub/sub channel.
 *
 * @see net.techneon.synapse.api.SynapseAPI#subscribe(String, ChannelSubscriber)
 * @since 1.0
 */
@FunctionalInterface
public interface ChannelSubscriber {

    /**
     * Handles a message published on a channel this subscriber is registered for.
     *
     * @param message the received message; never {@code null}
     */
    void onMessage(ChannelMessage message);
}
