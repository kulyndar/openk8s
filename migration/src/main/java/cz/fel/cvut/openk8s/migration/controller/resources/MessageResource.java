package cz.fel.cvut.openk8s.migration.controller.resources;

/**
 * API resource.
 * Represents system message object.
 * Structure:
 *      message - Plain text message
 *      type - message type ('info', 'success', 'warning' or 'error')
 */
public class MessageResource {
    private String message;
    private String type;

    public MessageResource() {
    }

    public MessageResource(String message, String type) {
        this.message = message;
        this.type = type;
    }

    /**
     * Creates information message
     * @param message message text
     * @return created instance
     */
    public static MessageResource info(String message) {
        return new MessageResource(message, "info");
    }
    /**
     * Creates success message
     * @param message message text
     * @return created instance
     */
    public static MessageResource success(String message) {
        return new MessageResource(message, "success");
    }
    /**
     * Creates error message
     * @param message message text
     * @return created instance
     */
    public static MessageResource error(String message) {
        return new MessageResource(message, "error");
    }
    /**
     * Creates warning message
     * @param message message text
     * @return created instance
     */
    public static MessageResource warning(String message) {
        return new MessageResource(message, "warning");
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }
}
