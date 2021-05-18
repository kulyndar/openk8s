package cz.fel.cvut.openk8s.migration.service.migration;

import com.fasterxml.jackson.core.JsonProcessingException;
import cz.fel.cvut.openk8s.migration.controller.resources.KubernetesResource;
import cz.fel.cvut.openk8s.migration.exception.ItemNotFoundException;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.apps.ReplicaSet;
import io.fabric8.kubernetes.api.model.apps.ReplicaSetBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.internal.SerializationUtils;
import io.fabric8.openshift.client.OpenShiftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Manages ReplicaSet objects
 */
@Component
@Order(2)
public class ReplicaSetMigrationProvider implements MigrationProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReplicaSetMigrationProvider.class);

    @Override
    public String getKind() {
        return "ReplicaSet";
    }


    @Override
    public ReplicaSet migrateResource(KubernetesResource item, KubernetesClient kubernetesClient, OpenShiftClient openShiftClient) {
        ReplicaSet replicaSet= kubernetesClient.apps().replicaSets().inNamespace(item.getNamespace()).withName(item.getName()).get();
        if (replicaSet == null) {
            throw new ItemNotFoundException();
        }
        ReplicaSet destRs = new ReplicaSetBuilder()
                .withApiVersion(replicaSet.getApiVersion()).withKind(replicaSet.getKind())
                .withNewMetadata().withName(replicaSet.getMetadata().getName())
                .withNamespace(replicaSet.getMetadata().getNamespace())
                .withLabels(replicaSet.getMetadata().getLabels())
                .withAnnotations(replicaSet.getMetadata().getAnnotations())
                .withFinalizers(replicaSet.getMetadata().getFinalizers()).endMetadata()
                .withNewSpecLike(replicaSet.getSpec()).endSpec().build();
        openShiftClient.apps().replicaSets().inNamespace(item.getNamespace()).create(destRs);
        return destRs;
    }

    @Override
    public String getInfo(String name, String namespace, KubernetesClient kubernetesClient) throws JsonProcessingException {
        ReplicaSet replicaSet = kubernetesClient.apps().replicaSets().inNamespace(namespace).withName(name).get();
        return SerializationUtils.dumpAsYaml(replicaSet);
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    @Override
    public void rollbackResource(HasMetadata item, OpenShiftClient openShiftClient) {
        openShiftClient.apps().replicaSets().inNamespace(item.getMetadata().getNamespace()).withName(item.getMetadata().getName()).delete();
    }
}
