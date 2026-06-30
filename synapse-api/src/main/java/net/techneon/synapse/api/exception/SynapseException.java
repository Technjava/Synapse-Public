package net.techneon.synapse.api.exception;

/**
 * Base type for all unchecked exceptions thrown by the Synapse API.
 *
 * @since 1.0
 */
public class SynapseException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * @param message a description of the failure
     */
    public SynapseException(String message) {
        super(message);
    }

    /**
     * @param message a description of the failure
     * @param cause   the underlying cause
     */
    public SynapseException(String message, Throwable cause) {
        super(message, cause);
    }
}
