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

/**
 * This interface should be implemented if you want to extend Kubernetes items list, for that migration is supported.
 */
public interface MigrationProvider {

    /**
     * @return Kubernetes object kind, that can be managed by this implementation
     */
    String getKind();

    /**
     * Migrates list of items from Kubernetes to OpenShift cluster
     * @param items  items to migrate
     * @param kubernetesClient -kubernetes client, connected to the cluster
     * @param openShiftClient  openShift client, connected to the cluster
     * @param callback  action, that will be done after item was migrated
     * @return  list of migration results.
     * @throws NullPointerException  if cluster connection was not initialized or was expired
     */
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

    /**
     * This method migrates a single resource. There is no need to catch exceptions. Parent method {@link MigrationProvider#migrate(List, KubernetesClient, OpenShiftClient, Consumer)}
     * manages exceptions.
     * @param item  item to migrate
     * @param kubernetesClient  kubernetes client, connected to the cluster
     * @param openShiftClient  openShift client, connected to the cluster
     * @return item created in OpenShift cluster
     * @throws NullPointerException  if cluster connection was not initialized or was expired
     */
    HasMetadata migrateResource(KubernetesResource item, KubernetesClient kubernetesClient, OpenShiftClient openShiftClient);

    /**
     * This method return YAML configuration for a single item.
     * @param name  item name
     * @param namespace  item namepsace, nullable
     * @param kubernetesClient  kubernetes client, connected to the cluster
     * @return YAML configuration
     * @throws JsonProcessingException  if item cannot be serialised
     * @throws NullPointerException  if cluster connection was not initialized or was expired
     */
    String getInfo(String name, String namespace, KubernetesClient kubernetesClient) throws JsonProcessingException;

    /**
     * Detects, if current implementation can manage items with defined kind.
     * @param kind  kind of the item, case insensitive
     * @return true is current implementation can migrate, rollback etc. item with defined kind, otherwise false
     */
    default boolean isResponsibleFor(String kind) {
        return kind != null && kind.equalsIgnoreCase(getKind());
    }

    /**
     * @return logger for particular implementation
     */
    Logger getLogger();

    /**
     * Rolls back migration for defined items.
     * @param itemsToRollback  list of item to rollback
     * @param openShiftClient  openShift client, connected to the cluster
     * @return list of rollback results
     * @throws NullPointerException  if cluster connection was not initialized or was expired
     */
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

    /**
     * This method rolls back a single resource. There is no need to catch exceptions. Parent method {@link MigrationProvider#rollback(List, OpenShiftClient)}
     * manages exceptions.
     * @param item  item to rollback
     * @param openShiftClient  openShift client, connected to the cluster
     * @throws NullPointerException  if cluster connection was not initialized or was expired
     */
    void rollbackResource(HasMetadata item, OpenShiftClient openShiftClient);
}
