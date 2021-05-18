package cz.fel.cvut.openk8s.migration.service.migration;

import com.fasterxml.jackson.core.JsonProcessingException;
import cz.fel.cvut.openk8s.migration.controller.resources.KubernetesResource;
import cz.fel.cvut.openk8s.migration.exception.ItemNotFoundException;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.internal.SerializationUtils;
import io.fabric8.openshift.client.OpenShiftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Manages ServiceAccount objects
 */
@Component
public class ServiceAccountMigrationProvider implements MigrationProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceAccountMigrationProvider.class);

    @Override
    public String getKind() {
        return "ServiceAccount";
    }

    @Override
    public HasMetadata migrateResource(KubernetesResource item, KubernetesClient kubernetesClient, OpenShiftClient openShiftClient) {
        ServiceAccount serviceAccount = kubernetesClient.serviceAccounts().inNamespace(item.getNamespace()).withName(item.getName()).get();
        if (serviceAccount == null) {
            throw new ItemNotFoundException();
        }
        ServiceAccount destServiceAccount = new ServiceAccountBuilder()
                .withApiVersion(serviceAccount.getApiVersion()).withKind(serviceAccount.getKind())
                .withNewMetadata().withName(serviceAccount.getMetadata().getName())
                .withNamespace(serviceAccount.getMetadata().getNamespace())
                .withLabels(serviceAccount.getMetadata().getLabels())
                .withAnnotations(serviceAccount.getMetadata().getAnnotations())
                .withFinalizers(serviceAccount.getMetadata().getFinalizers()).endMetadata()
                .withAutomountServiceAccountToken(serviceAccount.getAutomountServiceAccountToken())
                .withImagePullSecrets(serviceAccount.getImagePullSecrets())
                .withSecrets(serviceAccount.getSecrets().stream().map(secret -> new ObjectReferenceBuilder().withName(secret.getName()).build()).collect(Collectors.toList()))
                .build();
        openShiftClient.serviceAccounts().inNamespace(item.getNamespace()).create(destServiceAccount);
        return destServiceAccount;
    }

    @Override
    public String getInfo(String name, String namespace, KubernetesClient kubernetesClient) throws JsonProcessingException {
        ServiceAccount serviceAccount = kubernetesClient.serviceAccounts().inNamespace(namespace).withName(name).get();
        return SerializationUtils.dumpAsYaml(serviceAccount);
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    @Override
    public void rollbackResource(HasMetadata item, OpenShiftClient openShiftClient) {
        openShiftClient.serviceAccounts().inNamespace(item.getMetadata().getNamespace()).withName(item.getMetadata().getName()).delete();
    }
}
