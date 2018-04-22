package org.cg.error;

/**
 * @author Chris.Ge
 */
public class IllegalParameterException extends AppBaseException {

    public IllegalParameterException() {
        super();
    }

    /**
     * Message+Cause constructor.
     *
     * @param message The message of this Exception.
     * @param cause   The cause of this Exception.
     */
    public IllegalParameterException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Message constructor.
     *
     * @param message Default constructor.
     */
    public IllegalParameterException(String message) {
        super(message);
    }

    /**
     * Cause constructor.
     *
     * @param cause The cause of this Exception.
     */
    public IllegalParameterException(Throwable cause) {
        super(cause);
    }

}
