package cz.fel.cvut.openk8s.migration.controller.resources;

import io.fabric8.kubernetes.api.model.Status;

import java.io.Serializable;

public class MigrationResultResource implements Serializable {
    private String cause;
    private String message;
    private boolean success;
    private String kind;
    private String name;

    public MigrationResultResource(String cause, String message) {
        this.cause = cause;
        this.message = message;
    }

    private MigrationResultResource(KubernetesResource item) {
        this.kind = item.getKind();
        this.name = item.getName();
    }

    public MigrationResultResource(KubernetesResource item, String cause, String message) {
        this(item);
        this.cause = cause;
        this.message = message;
    }

    public MigrationResultResource(KubernetesResource item, String cause, String message, boolean success) {
        this(item, cause, message);
        this.success = success;
    }


    public static MigrationResultResource fromStatus(Status status, KubernetesResource item) {
        return new MigrationResultResource(item, status.getReason() + ". ", status.getMessage());
    }

    public static MigrationResultResource ok(KubernetesResource item) {
        return new MigrationResultResource(item, "Successfully migrated to cluster.", null, true);
    }

    public static MigrationResultResource connectionError(KubernetesResource item) {
        return new MigrationResultResource(item, "Connection exception. ", "Cannot connect to the cluster. Please, check cluster IP address.");
    }

    public static MigrationResultResource unexpected(KubernetesResource item) {
        return new MigrationResultResource(item, "Unexpected error occurred. ", "Try again later");
    }

    public static MigrationResultResource notFound(KubernetesResource item) {
        return new MigrationResultResource(item, "Not found. ", "Item was not found");
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
