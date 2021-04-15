package cz.fel.cvut.openk8s.migration.controller.resources;

import io.fabric8.kubernetes.api.model.Status;

import java.io.Serializable;

public class MigrationErrorResource implements Serializable {
    private String cause;
    private String message;
    private boolean success;
    private String kind;
    private String name;

    public MigrationErrorResource(String cause, String message) {
        this.cause = cause;
        this.message = message;
    }

    private MigrationErrorResource(KubernetesResource item) {
        this.kind = item.getKind();
        this.name = item.getName();
    }

    public MigrationErrorResource(KubernetesResource item, String cause, String message) {
        this(item);
        this.cause = cause;
        this.message = message;
    }

    public MigrationErrorResource(KubernetesResource item, String cause, String message, boolean success) {
        this(item, cause, message);
        this.success = success;
    }


    public static MigrationErrorResource fromStatus(Status status, KubernetesResource item) {
        return new MigrationErrorResource(item, status.getReason() + ". ", status.getMessage());
    }

    public static MigrationErrorResource ok(KubernetesResource item) {
        return new MigrationErrorResource(item, "Successfully connected to cluster.", null, true);
    }

    public static MigrationErrorResource connectionError(KubernetesResource item) {
        return new MigrationErrorResource(item, "Connection exception. ", "Cannot connect to the cluster. Please, check cluster IP address.");
    }

    public static MigrationErrorResource unexpected(KubernetesResource item) {
        return new MigrationErrorResource(item, "Unexpected error occurred. ", "Try again later");
    }

    public static MigrationErrorResource notFound(KubernetesResource item) {
        return new MigrationErrorResource(item, "Not found. ", "Item was not found");
    }

    public String getCause() {
        return cause;
    }

    public String getMessage() {
        return message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getKind() {
        return kind;
    }

    public String getName() {
        return name;
    }
}
