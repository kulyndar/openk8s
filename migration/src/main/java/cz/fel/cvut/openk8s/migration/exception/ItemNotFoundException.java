package cz.fel.cvut.openk8s.migration.exception;

public class ItemNotFoundException extends RuntimeException {
    public ItemNotFoundException() {
    }

    public ItemNotFoundException(String message) {
        super(message);
    }
}
