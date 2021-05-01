package cz.fel.cvut.openk8s.migration.service.migration;

import com.fasterxml.jackson.core.JsonProcessingException;
import cz.fel.cvut.openk8s.migration.controller.resources.KubernetesResource;
import cz.fel.cvut.openk8s.migration.exception.ItemNotFoundException;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.ReplicaSet;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.internal.SerializationUtils;
import io.fabric8.openshift.client.OpenShiftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ConfigMapMigrationProvider implements MigrationProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigMapMigrationProvider.class);

    @Override
    public String getKind() {
        return "ConfigMap";
    }

    @Override
    public HasMetadata migrateResource(KubernetesResource item, KubernetesClient kubernetesClient, OpenShiftClient openShiftClient) {
        ConfigMap configMap = kubernetesClient.configMaps().inNamespace(item.getNamespace()).withName(item.getName()).get();
        if (configMap == null) {
            throw new ItemNotFoundException();
        }
        ConfigMap destConfig = new ConfigMapBuilder()
                .withApiVersion(configMap.getApiVersion()).withKind(configMap.getKind())
                .withNewMetadata().withName(configMap.getMetadata().getName())
                .withNamespace(configMap.getMetadata().getNamespace())
                .withLabels(configMap.getMetadata().getLabels())
                .withAnnotations(configMap.getMetadata().getAnnotations())
                .withFinalizers(configMap.getMetadata().getFinalizers()).endMetadata()
                .withData(configMap.getData()).withBinaryData(configMap.getBinaryData())
                .withImmutable(configMap.getImmutable()).build();
        openShiftClient.configMaps().inNamespace(item.getNamespace()).create(destConfig);
        return destConfig;
    }

    @Override
    public String getInfo(String name, String namespace, KubernetesClient kubernetesClient) throws JsonProcessingException {
        ConfigMap configMap = kubernetesClient.configMaps().inNamespace(namespace).withName(name).get();
        return SerializationUtils.dumpAsYaml(configMap);
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    @Override
    public void rollbackResource(HasMetadata item, OpenShiftClient openShiftClient) {
        openShiftClient.configMaps().inNamespace(item.getMetadata().getNamespace()).withName(item.getMetadata().getName()).delete();
    }
}
