package net.techneon.synapse.api.send;

import net.techneon.synapse.api.packet.PacketPriority;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Immutable, per-send configuration controlling timeouts, retries,
 * acknowledgement, priority, time-to-live, and custom headers.
 *
 * <p>Build instances with the fluent {@link #builder()}; reuse them freely since
 * they are immutable:
 *
 * <pre>{@code
 * SendOptions options = SendOptions.builder()
 *     .timeout(2000)
 *     .retries(3)
 *     .requireAck(true)
 *     .priority(PacketPriority.HIGH)
 *     .ttl(5000)
 *     .header("trace-id", traceId)
 *     .build();
 *
 * Synapse.send("survival", packet, options);
 * }</pre>
 *
 * <p>{@link #defaults()} returns a shared instance with sensible defaults, which
 * is also what the no-options {@code send} overloads use.
 *
 * @since 1.0
 */
public final class SendOptions {

    private static final SendOptions DEFAULTS = builder().build();

    private final long timeoutMillis;
    private final int retries;
    private final boolean requireAck;
    private final PacketPriority priority;
    private final long ttlMillis;
    private final boolean persistent;
    private final Map<String, String> headers;

    private SendOptions(Builder b) {
        this.timeoutMillis = b.timeoutMillis;
        this.retries = b.retries;
        this.requireAck = b.requireAck;
        this.priority = b.priority;
        this.ttlMillis = b.ttlMillis;
        this.persistent = b.persistent;
        this.headers = Collections.unmodifiableMap(new LinkedHashMap<>(b.headers));
    }

    /**
     * A shared instance carrying default values: no acknowledgement, no retries,
     * {@link PacketPriority#NORMAL} priority, a 5-second timeout, and no TTL.
     *
     * @return the default options
     */
    public static SendOptions defaults() {
        return DEFAULTS;
    }

    /**
     * Creates a new builder pre-populated with default values.
     *
     * @return a fresh {@link Builder}
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * The maximum time to wait for an acknowledgement before the send is
     * considered timed out, in milliseconds. Only meaningful when
     * {@link #isRequireAck()} is {@code true}.
     *
     * @return the timeout in milliseconds
     */
    public long getTimeoutMillis() {
        return timeoutMillis;
    }

    /**
     * How many additional times Synapse will retransmit the packet if no
     * acknowledgement arrives within the timeout.
     *
     * @return the retry count ({@code 0} means send once, never retry)
     */
    public int getRetries() {
        return retries;
    }

    /**
     * Whether the receiver must acknowledge this packet, enabling
     * {@link SendResult#onAck} / {@link SendResult#onTimeout} callbacks.
     *
     * @return {@code true} if an acknowledgement is required
     */
    public boolean isRequireAck() {
        return requireAck;
    }

    /**
     * The scheduling priority of this packet on the outbound queue.
     *
     * @return the send priority; never {@code null}
     */
    public PacketPriority getPriority() {
        return priority;
    }

    /**
     * The time-to-live of this packet in milliseconds. A packet that has not been
     * delivered within its TTL is dropped rather than sent stale. {@code 0} means
     * no TTL.
     *
     * @return the time-to-live in milliseconds, or {@code 0} for none
     */
    public long getTtlMillis() {
        return ttlMillis;
    }

    /**
     * Whether this packet should be queued and delivered later if the target
     * server is currently offline (store-and-forward), instead of being dropped.
     * The queued packet is sent as soon as the server reconnects.
     *
     * @return {@code true} if store-and-forward is enabled for this send
     */
    public boolean isPersistent() {
        return persistent;
    }

    /**
     * Custom headers attached to this packet, readable on the receiving side via
     * {@link net.techneon.synapse.api.packet.PacketEvent#getHeaders()}.
     *
     * @return an immutable map of headers; never {@code null}
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * Fluent builder for {@link SendOptions}.
     *
     * @since 1.0
     */
    public static final class Builder {
        private long timeoutMillis = 5000L;
        private int retries = 0;
        private boolean requireAck = false;
        private PacketPriority priority = PacketPriority.NORMAL;
        private long ttlMillis = 0L;
        private boolean persistent = false;
        private final Map<String, String> headers = new LinkedHashMap<>();

        private Builder() {
        }

        /**
         * Sets the acknowledgement timeout.
         *
         * @param millis the timeout in milliseconds; must be positive
         * @return this builder
         */
        public Builder timeout(long millis) {
            if (millis <= 0) {
                throw new IllegalArgumentException("timeout must be > 0");
            }
            this.timeoutMillis = millis;
            return this;
        }

        /**
         * Sets the number of retransmission attempts after the first send.
         *
         * @param retries the retry count; must be {@code >= 0}
         * @return this builder
         */
        public Builder retries(int retries) {
            if (retries < 0) {
                throw new IllegalArgumentException("retries must be >= 0");
            }
            this.retries = retries;
            return this;
        }

        /**
         * Requires the receiver to acknowledge the packet. Implied when
         * {@link #retries(int)} is greater than zero.
         *
         * @param requireAck whether to require an acknowledgement
         * @return this builder
         */
        public Builder requireAck(boolean requireAck) {
            this.requireAck = requireAck;
            return this;
        }

        /**
         * Sets the outbound scheduling priority.
         *
         * @param priority the priority; must not be {@code null}
         * @return this builder
         */
        public Builder priority(PacketPriority priority) {
            if (priority == null) {
                throw new IllegalArgumentException("priority must not be null");
            }
            this.priority = priority;
            return this;
        }

        /**
         * Sets the packet time-to-live.
         *
         * @param millis the TTL in milliseconds; {@code 0} disables it
         * @return this builder
         */
        public Builder ttl(long millis) {
            if (millis < 0) {
                throw new IllegalArgumentException("ttl must be >= 0");
            }
            this.ttlMillis = millis;
            return this;
        }

        /**
         * Enables store-and-forward: if the target server is offline, the packet
         * is queued and delivered when it reconnects, rather than being dropped.
         *
         * @param persistent whether to queue for offline delivery
         * @return this builder
         */
        public Builder persistent(boolean persistent) {
            this.persistent = persistent;
            return this;
        }

        /**
         * Attaches a custom header readable by the receiver.
         *
         * @param key   the header name; must not be {@code null}
         * @param value the header value; must not be {@code null}
         * @return this builder
         */
        public Builder header(String key, String value) {
            if (key == null || value == null) {
                throw new IllegalArgumentException("header key/value must not be null");
            }
            this.headers.put(key, value);
            return this;
        }

        /**
         * Builds the immutable {@link SendOptions}. A non-zero retry count
         * implicitly enables acknowledgement.
         *
         * @return the configured options
         */
        public SendOptions build() {
            if (retries > 0) {
                requireAck = true;
            }
            return new SendOptions(this);
        }
    }
}
