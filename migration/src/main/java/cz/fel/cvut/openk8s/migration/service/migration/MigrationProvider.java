package cz.fel.cvut.openk8s.migration.service.migration;

import com.fasterxml.jackson.core.JsonProcessingException;
import cz.fel.cvut.openk8s.migration.controller.resources.KubernetesResource;
import cz.fel.cvut.openk8s.migration.controller.resources.MigrationErrorResource;
import cz.fel.cvut.openk8s.migration.exception.ItemNotFoundException;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.openshift.client.OpenShiftClient;
import org.slf4j.Logger;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

public interface MigrationProvider {

    String getKind();

    default List<MigrationErrorResource> migrate(List<KubernetesResource> items, KubernetesClient kubernetesClient, OpenShiftClient openShiftClient) {
        List<MigrationErrorResource> errors = new ArrayList<>();
        for (KubernetesResource item : items) {
            try {
                migrateResource(item, kubernetesClient, openShiftClient);
            } catch (ItemNotFoundException e) {
                getLogger().error("Cannot find " + getKind() + " with data " + item, e);
                errors.add(MigrationErrorResource.notFound(item));
            } catch (KubernetesClientException e) {
                if (e.getCause() != null && e.getCause() instanceof ConnectException) {
                    getLogger().error("ConnectionException: cannot connect to the cluster", e);
                    errors.add(MigrationErrorResource.connectionError(item));
                } else if (e.getStatus() != null) {
                    getLogger().error("Error received in response from Kubernetes", e);
                    errors.add(MigrationErrorResource.fromStatus(e.getStatus(), item));
                }
                errors.add(MigrationErrorResource.unexpected(item));
            } catch (Exception e) {
                getLogger().error("Unexpected exception in " + getKind() + " migration: " + item, e);
                errors.add(MigrationErrorResource.unexpected(item));
            }
        }
        return errors;
    }

    void migrateResource(KubernetesResource item, KubernetesClient kubernetesClient, OpenShiftClient openShiftClient);

    String getInfo(String name, String namespace, KubernetesClient kubernetesClient) throws JsonProcessingException;

    default boolean isResponsibleFor(String kind) {
        return kind != null && kind.equalsIgnoreCase(getKind());
    }

    Logger getLogger();
}
