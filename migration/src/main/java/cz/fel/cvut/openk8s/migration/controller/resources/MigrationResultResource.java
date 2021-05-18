package cz.fel.cvut.openk8s.migration.controller.resources;

import io.fabric8.kubernetes.api.model.Status;

import java.io.Serializable;
/**
 * API resource.
 * Represents migration result object.
 * Structure:
 *      cause - main message information
 *      message - additional information
 *      success - true if message represent successfully finished event
 *      kind - owner Kubernetes object kind
 *      name - owner Kubernetes object name
 *      namespace - owner Kubernetes object namespace
 */
public class MigrationResultResource implements Serializable {
    private String cause;
    private String message;
    private boolean success;
    private String kind;
    private String name;
    private String namespace;

    public MigrationResultResource(String cause, String message) {
        this.cause = cause;
        this.message = message;
    }

    private MigrationResultResource(KubernetesResource item) {
        this.kind = item.getKind();
        this.name = item.getName();
        this.namespace = item.getNamespace();
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


    /**
     * Creates error message from Kuberentes status response
     * @param status cluster response
     * @param item owner Kubernetes object
     * @return created instance
     */
    public static MigrationResultResource fromStatus(Status status, KubernetesResource item) {
        return new MigrationResultResource(item, status.getReason() + ". ", status.getMessage());
    }

    /**
     * Creates success message
     * @param item owner Kubernetes object
     * @return created instance
     */
    public static MigrationResultResource ok(KubernetesResource item) {
        return new MigrationResultResource(item, "Successfully migrated to cluster.", null, true);
    }

    /**
     * Creates error message when connection error occurred
     * @param item owner Kubernetes object
     * @return created instance
     */
    public static MigrationResultResource connectionError(KubernetesResource item) {
        return new MigrationResultResource(item, "Connection exception. ", "Cannot connect to the cluster. Please, check cluster IP address.");
    }

    /**
     * Creates error message when unexpected error occurred
     * @param item owner Kubernetes object
     * @return created instance
     */
    public static MigrationResultResource unexpected(KubernetesResource item) {
        return new MigrationResultResource(item, "Unexpected error occurred. ", "Try again later");
    }

    /**
     * Creates error message when object was not found
     * @param item owner Kubernetes object
     * @return created instance
     */
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

    public String getNamespace() {
        return namespace;
    }
}

