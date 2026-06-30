package net.techneon.synapse.api.exception;

/**
 * Thrown when a packet type cannot be registered &mdash; for example because its
 * id collides with an already-registered type, or it is registered twice within
 * the same namespace.
 *
 * @since 1.0
 */
public class PacketRegistrationException extends SynapseException {

    private static final long serialVersionUID = 1L;

    /**
     * @param message a description of the registration problem
     */
    public PacketRegistrationException(String message) {
        super(message);
    }
}
