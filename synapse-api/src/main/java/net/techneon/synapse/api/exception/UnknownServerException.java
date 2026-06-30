package net.techneon.synapse.api.exception;

/**
 * Thrown when a send targets a server name that is not declared in the local
 * configuration.
 *
 * @since 1.0
 */
public class UnknownServerException extends SynapseException {

    private static final long serialVersionUID = 1L;

    /**
     * @param serverName the unknown server name
     */
    public UnknownServerException(String serverName) {
        super("No server named '" + serverName + "' is configured");
    }
}
