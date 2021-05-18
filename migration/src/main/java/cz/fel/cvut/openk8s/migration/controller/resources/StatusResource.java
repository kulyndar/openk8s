package cz.fel.cvut.openk8s.migration.controller.resources;

import io.fabric8.kubernetes.api.model.Status;

import java.io.Serializable;

/**
 * API resource.
 * Structure:
 *      cause - main message information
 *      message - additional information
 *      success - true if message represent successfully finished event
 */
public class StatusResource implements Serializable {
    private String cause;
    private String message;
    private boolean success;

    public StatusResource(String cause, String message) {
        this.cause = cause;
        this.message = message;
    }

    public StatusResource(String cause, String message, boolean success) {
        this.cause = cause;
        this.message = message;
        this.success = success;
    }

    /**
     * Creates error message from Kuberentes status response
     * @param status cluster response
     * @return created instance
     */
    public static StatusResource fromStatus(Status status) {
        return new StatusResource(status.getReason() + ". ", status.getMessage());
    }

    /**
     * Creates success message
     * @return created instance
     */
    public static StatusResource ok() {
        return new StatusResource("Successfully connected to cluster.", null, true);
    }

    /**
     * Creates error message when connection error occurred
     * @return created instance
     */
    public static StatusResource connectionError() {
        return new StatusResource("Connection exception. ", "Cannot connect to the cluster. Please, check cluster IP address.");
    }

    /**
     * Creates error message when unexpected error occurred
     * @return created instance
     */
    public static StatusResource unexpected() {
        return new StatusResource("Unexpected error occurred. ", "Try again later");
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
}
