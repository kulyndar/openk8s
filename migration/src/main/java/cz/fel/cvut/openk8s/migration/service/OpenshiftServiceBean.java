package cz.fel.cvut.openk8s.migration.service;

import cz.fel.cvut.openk8s.migration.controller.resources.KubernetesResource;
import cz.fel.cvut.openk8s.migration.controller.resources.MigrationErrorResource;
import cz.fel.cvut.openk8s.migration.controller.resources.StatusResource;
import cz.fel.cvut.openk8s.migration.service.migration.MigrationProvider;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

import java.net.ConnectException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Scope(value = "session", proxyMode = ScopedProxyMode.INTERFACES)
public class OpenshiftServiceBean implements OpenshiftService {
    private static final String AUTH_TYPE_TOKEN = "token";
    private static final String AUTH_TYPE_BASIC = "basic";

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenshiftServiceBean.class);

    @Autowired
    private List<MigrationProvider> migrationProviders;

    @Autowired
    private KubernetesService kubernetesService;

    private OpenShiftClient openShiftClient;

    @Override
    public StatusResource init(final String ocIp, final String authType, final String token, final String username, final String password) {
        if (openShiftClient != null) {
            openShiftClient.close();
        }
        ConfigBuilder builder = new ConfigBuilder().withMasterUrl(ocIp).withOauthToken(token);
        if (AUTH_TYPE_BASIC.equals(authType)) {
            builder.withUsername(username).withPassword(password);
        } else if (AUTH_TYPE_TOKEN.equals(authType)) {
            builder.withOauthToken(token);
        }

        this.openShiftClient = new DefaultOpenShiftClient(builder.build());
        /*Trying to connect*/
        try {
            openShiftClient.namespaces().list();
        } catch (KubernetesClientException e) {
            LOGGER.error("Error occurred when trying to connect to K8s with IP: " + ocIp, e);
            if (e.getCause() != null && e.getCause() instanceof ConnectException) {
                return StatusResource.connectionError();
            } else if (e.getStatus() != null) {
                return StatusResource.fromStatus(e.getStatus());
            }
            return StatusResource.unexpected();
        } catch (Exception e) {
            LOGGER.error("Error occurred when trying to connect to K8s with IP: " + ocIp, e);
            return StatusResource.unexpected();
        }
        return StatusResource.ok();
    }

    @Override
    public List<MigrationErrorResource> migrate(List<KubernetesResource> itemsList) {
        if (itemsList.isEmpty()) {
            return Collections.singletonList(new MigrationErrorResource("Empty content", "Please, select content for migration"));
        }
        List<MigrationErrorResource> errors = new ArrayList<>();
        Set<String> namespaces = itemsList.stream().filter(item -> Objects.nonNull(item.getNamespace())).map(KubernetesResource::getNamespace)
                .collect(Collectors.toSet());
        for (String namespace : namespaces) {
            try {
               openShiftClient.namespaces().create(new NamespaceBuilder()
                        .withNewMetadata()
                        .withName(namespace)
                        .endMetadata()
                        .build());
            } catch (KubernetesClientException e) {
                if (e.getCause() != null && e.getCause() instanceof ConnectException) {
                    LOGGER.error("ConnectionException: cannot connect to the cluster", e);
                    errors.add(MigrationErrorResource.connectionError(new KubernetesResource(namespace, "Namespace")));
                } else if (e.getStatus() != null) {
                    LOGGER.error("Error received in response from Kubernetes", e);
                    errors.add(MigrationErrorResource.fromStatus(e.getStatus(), new KubernetesResource(namespace, "Namespace")));
                } else {
                    errors.add(MigrationErrorResource.unexpected(new KubernetesResource(namespace, "Namespace")));
                }
            } catch (Exception e) {
                LOGGER.error("Unexpected exception in namespace migration: " + new KubernetesResource(namespace, "Namespace"), e);
                errors.add(MigrationErrorResource.unexpected(new KubernetesResource(namespace, "Namespace")));
            }

            for (MigrationProvider provider : migrationProviders) {
                List<KubernetesResource> itemsToMigrate = itemsList.stream().filter(item -> provider.isResponsibleFor(item.getKind()))
                        .collect(Collectors.toList());
                errors.addAll(provider.migrate(itemsToMigrate, kubernetesService.getKubernetesClient(), openShiftClient));
            }
        }
        //todo migrate out-of-namespaces items

        return errors;
    }

    @Override
    public void destroy() {
        LOGGER.info("Start destroying openshift client");
        if (this.openShiftClient != null) {
            this.openShiftClient.close();
            this.openShiftClient = null;
        } else {
            LOGGER.warn("Openshift client is not initialized or is already destroyed");
        }
    }


}
