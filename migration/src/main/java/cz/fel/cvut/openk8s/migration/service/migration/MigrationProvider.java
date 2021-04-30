package cz.fel.cvut.openk8s.migration.service.migration;

import com.fasterxml.jackson.core.JsonProcessingException;
import cz.fel.cvut.openk8s.migration.controller.resources.KubernetesResource;
import cz.fel.cvut.openk8s.migration.controller.resources.MigrationResultResource;
import cz.fel.cvut.openk8s.migration.exception.ItemNotFoundException;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.openshift.client.OpenShiftClient;
import org.slf4j.Logger;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public interface MigrationProvider {

    String getKind();

    default List<MigrationResultResource> migrate(List<KubernetesResource> items, KubernetesClient kubernetesClient,
                                                  OpenShiftClient openShiftClient, Consumer<HasMetadata> callback) {
        List<MigrationResultResource> errors = new ArrayList<>();
        for (KubernetesResource item : items) {
            try {
                HasMetadata migrated = migrateResource(item, kubernetesClient, openShiftClient);
                callback.accept(migrated);
            } catch (ItemNotFoundException e) {
                getLogger().error("Cannot find " + getKind() + " with data " + item, e);
                errors.add(MigrationResultResource.notFound(item));
            } catch (KubernetesClientException e) {
                if (e.getCause() != null && e.getCause() instanceof ConnectException) {
                    getLogger().error("ConnectionException: cannot connect to the cluster", e);
                    errors.add(MigrationResultResource.connectionError(item));
                } else if (e.getStatus() != null) {
                    getLogger().error("Error received in response from Kubernetes", e);
                    errors.add(MigrationResultResource.fromStatus(e.getStatus(), item));
                } else {
                    errors.add(MigrationResultResource.unexpected(item));
                }
            } catch (Exception e) {
                getLogger().error("Unexpected exception in " + getKind() + " migration: " + item, e);
                errors.add(MigrationResultResource.unexpected(item));
            }
        }
        return errors;
    }

    HasMetadata migrateResource(KubernetesResource item, KubernetesClient kubernetesClient, OpenShiftClient openShiftClient);

    String getInfo(String name, String namespace, KubernetesClient kubernetesClient) throws JsonProcessingException;

    default boolean isResponsibleFor(String kind) {
        return kind != null && kind.equalsIgnoreCase(getKind());
    }

    Logger getLogger();

    default List<MigrationResultResource> rollback(List<HasMetadata> itemsToRollback, OpenShiftClient openShiftClient) {
        List<MigrationResultResource> errors = new ArrayList<>();
        for (HasMetadata item : itemsToRollback) {
            try {
                rollbackResource(item, openShiftClient);
            } catch (ItemNotFoundException e) {
                getLogger().error("Cannot find " + getKind() + " with data " + item, e);
                errors.add(MigrationResultResource.notFound(new KubernetesResource(item)));
            } catch (KubernetesClientException e) {
                if (e.getCause() != null && e.getCause() instanceof ConnectException) {
                    getLogger().error("ConnectionException: cannot connect to the cluster", e);
                    errors.add(MigrationResultResource.connectionError(new KubernetesResource(item)));
                } else if (e.getStatus() != null) {
                    getLogger().error("Error received in response from Kubernetes", e);
                    errors.add(MigrationResultResource.fromStatus(e.getStatus(), new KubernetesResource(item)));
                } else {
                    errors.add(MigrationResultResource.unexpected(new KubernetesResource(item)));
                }
            } catch (Exception e) {
                getLogger().error("Unexpected exception in " + getKind() + " migration: " + item, e);
                errors.add(MigrationResultResource.unexpected(new KubernetesResource(item)));
            }
        }
        return errors;
    }

    void rollbackResource(HasMetadata item, OpenShiftClient openShiftClient);
}
