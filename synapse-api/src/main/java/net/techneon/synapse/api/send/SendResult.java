package net.techneon.synapse.api.send;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * The handle returned by every {@code send} call, offering both a chainable
 * callback style and a {@link CompletableFuture} for acknowledgement-aware
 * sends.
 *
 * <p>For fire-and-forget sends the result simply reports the assigned
 * correlation id. When the packet was sent with
 * {@link SendOptions#isRequireAck() requireAck(true)}, the callbacks fire on
 * acknowledgement, timeout, or transport error:
 *
 * <pre>{@code
 * Synapse.send("survival", packet, options)
 *     .onAck(ack -> getLogger().info("acked in " + ack.getRoundTripMillis() + "ms"))
 *     .onTimeout(() -> getLogger().warning("no ack received"));
 * }</pre>
 *
 * <p>Callbacks are invoked on Synapse's internal event threads; dispatch to a
 * platform scheduler if you need to touch server state from them.
 *
 * @since 1.0
 */
public interface SendResult {

    /**
     * The unique id correlating this send with its eventual acknowledgement.
     *
     * @return the correlation id
     */
    long getCorrelationId();

    /**
     * Registers a callback invoked when the packet is acknowledged. Has no effect
     * unless the send required an acknowledgement.
     *
     * @param callback receives the {@link AckResult}; must not be {@code null}
     * @return this result, for chaining
     */
    SendResult onAck(Consumer<AckResult> callback);

    /**
     * Registers a callback invoked if no acknowledgement arrives within the
     * configured timeout, after all retries are exhausted.
     *
     * @param callback the timeout handler; must not be {@code null}
     * @return this result, for chaining
     */
    SendResult onTimeout(Runnable callback);

    /**
     * Registers a callback invoked if the packet could not be handed to the
     * transport at all (for example the target server is unknown or
     * serialization failed).
     *
     * @param callback receives the failure cause; must not be {@code null}
     * @return this result, for chaining
     */
    SendResult onError(Consumer<Throwable> callback);

    /**
     * Exposes the same outcome as a {@link CompletableFuture}, completing with
     * the {@link AckResult} on acknowledgement, or completing exceptionally on
     * timeout or transport error.
     *
     * @return a future view of this send's acknowledgement
     */
    CompletableFuture<AckResult> asFuture();
}
