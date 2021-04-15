package cz.fel.cvut.openk8s.migration.service.migration;

import com.fasterxml.jackson.core.JsonProcessingException;
import cz.fel.cvut.openk8s.migration.controller.resources.KubernetesResource;
import cz.fel.cvut.openk8s.migration.exception.ItemNotFoundException;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.internal.SerializationUtils;
import io.fabric8.openshift.client.OpenShiftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(3)
public class DeploymentMigrationProvider implements MigrationProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeploymentMigrationProvider.class);

    @Override
    public String getKind() {
        return "Deployment";
    }

    @Override
    public void migrateResource(KubernetesResource item, KubernetesClient kubernetesClient, OpenShiftClient openShiftClient) {
        Deployment deployment= kubernetesClient.apps().deployments().inNamespace(item.getNamespace()).withName(item.getName()).get();
        if (deployment == null) {
            throw new ItemNotFoundException();
        }
        openShiftClient.apps().deployments().inNamespace(item.getNamespace()).create(deployment);
    }


    @Override
    public String getInfo(String name, String namespace, KubernetesClient kubernetesClient) throws JsonProcessingException {
        Deployment deployment = kubernetesClient.apps().deployments().inNamespace(namespace).withName(name).get();
        return SerializationUtils.dumpAsYaml(deployment);
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }
}