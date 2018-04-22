package org.cg.error;

/**
 * @author Chris.Ge
 */
public class CommandFailedToRunException extends AppBaseException {

    public CommandFailedToRunException() {
        super();
    }

    /**
     * Message+Cause constructor.
     *
     * @param message The message of this Exception.
     * @param cause   The cause of this Exception.
     */
    public CommandFailedToRunException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Message constructor.
     *
     * @param message Default constructor.
     */
    public CommandFailedToRunException(String message) {
        super(message);
    }

    /**
     * Cause constructor.
     *
     * @param cause The cause of this Exception.
     */
    public CommandFailedToRunException(Throwable cause) {
        super(cause);
    }


}
