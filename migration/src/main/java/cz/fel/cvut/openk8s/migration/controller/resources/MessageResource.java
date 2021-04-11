package cz.fel.cvut.openk8s.migration.controller.resources;

public class MessageResource {
    private String message;
    private String type;

    public MessageResource() {
    }

    public MessageResource(String message, String type) {
        this.message = message;
        this.type = type;
    }

    public static MessageResource info(String message) {
        return new MessageResource(message, "info");
    }
    public static MessageResource success(String message) {
        return new MessageResource(message, "success");
    }
    public static MessageResource error(String message) {
        return new MessageResource(message, "error");
    }
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
