package cz.fel.cvut.openk8s.migration.exception;

/**
 * Exception is thrown when no item was found
 */
public class ItemNotFoundException extends RuntimeException {
    public ItemNotFoundException() {
    }

    public ItemNotFoundException(String message) {
        super(message);
    }
}
