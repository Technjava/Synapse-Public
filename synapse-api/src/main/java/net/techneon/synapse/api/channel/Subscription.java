package net.techneon.synapse.api.channel;

/**
 * A handle to an active channel subscription, used to cancel it later.
 *
 * <p>Closing a subscription stops the associated {@link ChannelSubscriber} from
 * receiving further messages. {@code Subscription} extends {@link AutoCloseable}
 * so it can be used with try-with-resources when the lifetime is scoped.
 *
 * @since 1.0
 */
public interface Subscription extends AutoCloseable {

    /**
     * The channel this subscription is bound to.
     *
     * @return the channel name; never {@code null}
     */
    String getChannel();

    /**
     * Whether this subscription is still active (not yet closed).
     *
     * @return {@code true} if still receiving messages
     */
    boolean isActive();

    /**
     * Cancels the subscription. Idempotent: calling it more than once has no
     * additional effect. Never throws.
     */
    @Override
    void close();
}
