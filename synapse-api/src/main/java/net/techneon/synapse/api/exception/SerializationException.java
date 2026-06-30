package net.techneon.synapse.api.exception;

/**
 * Thrown when a packet cannot be serialized before sending or deserialized after
 * receiving.
 *
 * @since 1.0
 */
public class SerializationException extends SynapseException {

    private static final long serialVersionUID = 1L;

    /**
     * @param message a description of the failure
     * @param cause   the underlying cause
     */
    public SerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
