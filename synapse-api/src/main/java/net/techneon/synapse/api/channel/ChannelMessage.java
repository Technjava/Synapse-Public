package net.techneon.synapse.api.channel;

/**
 * A message delivered on a named pub/sub channel, Synapse's Redis-like
 * alternative to strongly typed packets.
 *
 * <p>Channels are convenient when you prefer a loosely typed,
 * "publish a payload to a topic" model over registering a packet class:
 *
 * <pre>{@code
 * Synapse.subscribe("chat-broadcast", message ->
 *     getServer().broadcastMessage(message.getPayloadAs(String.class)));
 *
 * Synapse.publish("chat-broadcast", "Hello from " + serverName);
 * }</pre>
 *
 * @since 1.0
 */
public interface ChannelMessage {

    /**
     * The name of the channel this message was published on.
     *
     * @return the channel name; never {@code null}
     */
    String getChannel();

    /**
     * The logical name of the server that published this message.
     *
     * @return the source server name; never {@code null}
     */
    String getSource();

    /**
     * The deserialized payload object exactly as published.
     *
     * @return the payload; never {@code null}
     */
    Object getPayload();

    /**
     * The payload cast to the expected type.
     *
     * @param type the expected payload type
     * @param <T>  the payload type
     * @return the payload cast to {@code type}
     * @throws ClassCastException if the payload is not of the requested type
     */
    default <T> T getPayloadAs(Class<T> type) {
        return type.cast(getPayload());
    }
}
