package cz.fel.cvut.openk8s.migration.service.migration;

import com.fasterxml.jackson.core.JsonProcessingException;
import cz.fel.cvut.openk8s.migration.controller.resources.KubernetesResource;
import cz.fel.cvut.openk8s.migration.exception.ItemNotFoundException;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.internal.SerializationUtils;
import io.fabric8.openshift.client.OpenShiftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages Secret objects
 */
@Component
public class SecretsMigrationProvider implements MigrationProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecretsMigrationProvider.class);

    @Override
    public String getKind() {
        return "Secret";
    }

    @Override
    public HasMetadata migrateResource(KubernetesResource item, KubernetesClient kubernetesClient, OpenShiftClient openShiftClient) {
        Secret secret = kubernetesClient.secrets().inNamespace(item.getNamespace()).withName(item.getName()).get();
        if (secret == null) {
            throw new ItemNotFoundException();
        }
        Map<String, String> anotations = new HashMap<>();
        secret.getMetadata().getAnnotations().forEach((k, v) -> {
            if (!"kubernetes.io/service-account.uid".equals(k)) {
                anotations.put(k, v);
            }
        });
        Secret destSecret = new SecretBuilder()
                .withApiVersion(secret.getApiVersion()).withKind(secret.getKind())
                .withNewMetadata().withName(secret.getMetadata().getName())
                .withNamespace(secret.getMetadata().getNamespace())
                .withLabels(secret.getMetadata().getLabels())
                .withAnnotations(anotations)
                .withFinalizers(secret.getMetadata().getFinalizers()).endMetadata()
                .withNewType(secret.getType())
                .withData(secret.getData())
                .withStringData(secret.getStringData())
                .withImmutable(secret.getImmutable()).build();
        openShiftClient.secrets().inNamespace(item.getNamespace()).withName(secret.getMetadata().getName()).create(destSecret);
        return destSecret;
    }

    @Override
    public String getInfo(String name, String namespace, KubernetesClient kubernetesClient) throws JsonProcessingException {
        Secret secret = kubernetesClient.secrets().inNamespace(namespace).withName(name).get();
        return SerializationUtils.dumpAsYaml(secret);
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    @Override
    public void rollbackResource(HasMetadata item, OpenShiftClient openShiftClient) {
        openShiftClient.secrets().inNamespace(item.getMetadata().getNamespace()).withName(item.getMetadata().getName()).delete();
    }
}
