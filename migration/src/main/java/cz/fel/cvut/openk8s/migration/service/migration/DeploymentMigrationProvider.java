package cz.fel.cvut.openk8s.migration.service.migration;

import com.fasterxml.jackson.core.JsonProcessingException;
import cz.fel.cvut.openk8s.migration.controller.resources.KubernetesResource;
import cz.fel.cvut.openk8s.migration.exception.ItemNotFoundException;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.internal.SerializationUtils;
import io.fabric8.openshift.client.OpenShiftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Manages Deployment objects
 */
@Component
@Order(3)
public class DeploymentMigrationProvider implements MigrationProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeploymentMigrationProvider.class);

    @Override
    public String getKind() {
        return "Deployment";
    }

    @Override
    public Deployment migrateResource(KubernetesResource item, KubernetesClient kubernetesClient, OpenShiftClient openShiftClient) {
        Deployment deployment= kubernetesClient.apps().deployments().inNamespace(item.getNamespace()).withName(item.getName()).get();
        if (deployment == null) {
            throw new ItemNotFoundException();
        }
        Deployment destDepl = new DeploymentBuilder()
                .withApiVersion(deployment.getApiVersion()).withKind(deployment.getKind())
                .withNewMetadata().withName(deployment.getMetadata().getName())
                .withNamespace(deployment.getMetadata().getNamespace())
                .withLabels(deployment.getMetadata().getLabels())
                .withAnnotations(deployment.getMetadata().getAnnotations())
                .withFinalizers(deployment.getMetadata().getFinalizers()).endMetadata()
                .withNewSpecLike(deployment.getSpec()).endSpec().build();
        openShiftClient.apps().deployments().inNamespace(item.getNamespace()).create(destDepl);
        return destDepl;
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

    @Override
    public void rollbackResource(HasMetadata item, OpenShiftClient openShiftClient) {
        openShiftClient.apps().deployments().inNamespace(item.getMetadata().getNamespace()).withName(item.getMetadata().getName()).delete();
    }
}
