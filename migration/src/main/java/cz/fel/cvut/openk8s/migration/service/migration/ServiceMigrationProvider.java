package cz.fel.cvut.openk8s.migration.service.migration;

import com.fasterxml.jackson.core.JsonProcessingException;
import cz.fel.cvut.openk8s.migration.controller.resources.KubernetesResource;
import cz.fel.cvut.openk8s.migration.exception.ItemNotFoundException;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.internal.SerializationUtils;
import io.fabric8.openshift.client.OpenShiftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class ServiceMigrationProvider implements MigrationProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceMigrationProvider.class);

    @Override
    public String getKind() {
        return "Service";
    }

    @Override
    public void migrateResource(KubernetesResource item, KubernetesClient kubernetesClient, OpenShiftClient openShiftClient) {
        Service service = kubernetesClient.services().inNamespace(item.getNamespace()).withName(item.getName()).get();
        if (service == null) {
            throw new ItemNotFoundException();
        }
        openShiftClient.services().inNamespace(item.getNamespace()).create(service);
    }

    @Override
    public String getInfo(String name, String namespace, KubernetesClient kubernetesClient) throws JsonProcessingException {
        Service service = kubernetesClient.services().inNamespace(namespace).withName(name).get();
        return SerializationUtils.dumpAsYaml(service);
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }
}
