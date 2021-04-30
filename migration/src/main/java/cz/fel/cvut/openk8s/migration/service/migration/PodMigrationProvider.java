package cz.fel.cvut.openk8s.migration.service.migration;

import com.fasterxml.jackson.core.JsonProcessingException;
import cz.fel.cvut.openk8s.migration.controller.resources.KubernetesResource;
import cz.fel.cvut.openk8s.migration.exception.ItemNotFoundException;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.internal.SerializationUtils;
import io.fabric8.openshift.client.OpenShiftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


@Component
@Order(1)
public class PodMigrationProvider implements MigrationProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(PodMigrationProvider.class);

    @Override
    public String getKind() {
        return "Pod";
    }

    @Override
    public Pod migrateResource(KubernetesResource item, KubernetesClient kubernetesClient, OpenShiftClient openShiftClient) {
        Pod pod = kubernetesClient.pods().inNamespace(item.getNamespace()).withName(item.getName()).get();
        if (pod == null) {
            throw new ItemNotFoundException();
        }
        Pod destPod = new PodBuilder()
                .withApiVersion(pod.getApiVersion()).withKind(pod.getKind())
                .withNewMetadata().withName(pod.getMetadata().getName())
                .withNamespace(pod.getMetadata().getNamespace())
                .withLabels(pod.getMetadata().getLabels())
                .withAnnotations(pod.getMetadata().getAnnotations())
                .withFinalizers(pod.getMetadata().getFinalizers()).endMetadata()
                .withSpec(pod.getSpec()).build();
        openShiftClient.pods().inNamespace(item.getNamespace()).create(destPod);
        return destPod;
    }

    @Override
    public String getInfo(String name, String namespace, KubernetesClient kubernetesClient) throws JsonProcessingException {
        Pod pod = kubernetesClient.pods().inNamespace(namespace).withName(name).get();
        return SerializationUtils.dumpAsYaml(pod);
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    @Override
    public void rollbackResource(HasMetadata item, OpenShiftClient openShiftClient) {
        openShiftClient.pods().inNamespace(item.getMetadata().getNamespace()).withName(item.getMetadata().getName()).delete();
    }
}
