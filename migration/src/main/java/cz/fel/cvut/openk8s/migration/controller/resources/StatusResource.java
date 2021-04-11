package cz.fel.cvut.openk8s.migration.controller.resources;

import io.fabric8.kubernetes.api.model.Status;

import java.io.Serializable;

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


    public static StatusResource fromStatus(Status status) {
        return new StatusResource(status.getReason() + ". ", status.getMessage());
    }

    public static StatusResource ok() {
        return new StatusResource("Successfully connected to cluster.", null, true);
    }

    public static StatusResource connectionError() {
        return new StatusResource("Connection exception. ", "Cannot connect to the cluster. Please, check cluster IP address.");
    }

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
