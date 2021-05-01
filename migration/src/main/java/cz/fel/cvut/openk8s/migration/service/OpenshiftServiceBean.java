package cz.fel.cvut.openk8s.migration.service;

import cz.fel.cvut.openk8s.migration.controller.resources.KubernetesResource;
import cz.fel.cvut.openk8s.migration.controller.resources.MigrationResultResource;
import cz.fel.cvut.openk8s.migration.controller.resources.StatusResource;
import cz.fel.cvut.openk8s.migration.service.migration.MigrationProvider;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Namespace;
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

    //items are stores here to rollback migration if something goes wrong
    private List<HasMetadata> createdItems = new ArrayList<>();

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
    public List<MigrationResultResource> migrate(List<KubernetesResource> itemsList) {
        createdItems.clear();
        if (itemsList.isEmpty()) {
            return Collections.singletonList(new MigrationResultResource("Empty content", "Please, select content for migration"));
        }
        List<MigrationResultResource> errors = new ArrayList<>();
        Set<String> namespaces = itemsList.stream().filter(item -> Objects.nonNull(item.getNamespace())).map(KubernetesResource::getNamespace)
                .collect(Collectors.toSet());
        for (String namespace : namespaces) {
            try {
                Namespace ns = new NamespaceBuilder()
                        .withNewMetadata()
                        .withName(namespace)
                        .endMetadata()
                        .build();
                openShiftClient.namespaces().create(ns);
                createdItems.add(ns);
            } catch (KubernetesClientException e) {
                if (e.getCause() != null && e.getCause() instanceof ConnectException) {
                    LOGGER.error("ConnectionException: cannot connect to the cluster", e);
                    errors.add(MigrationResultResource.connectionError(new KubernetesResource(namespace, "Namespace")));
                } else if (e.getStatus() != null) {
                    LOGGER.error("Error received in response from Kubernetes", e);
                    errors.add(MigrationResultResource.fromStatus(e.getStatus(), new KubernetesResource(namespace, "Namespace")));
                } else {
                    errors.add(MigrationResultResource.unexpected(new KubernetesResource(namespace, "Namespace")));
                }
            } catch (Exception e) {
                LOGGER.error("Unexpected exception in namespace migration: " + new KubernetesResource(namespace, "Namespace"), e);
                errors.add(MigrationResultResource.unexpected(new KubernetesResource(namespace, "Namespace")));
            }

            for (MigrationProvider provider : migrationProviders) {
                List<KubernetesResource> itemsToMigrate = itemsList.stream()
                        .filter(item -> namespace.equals(item.getNamespace()))
                        .filter(item -> provider.isResponsibleFor(item.getKind()))
                        .collect(Collectors.toList());
                errors.addAll(provider.migrate(itemsToMigrate, kubernetesService.getKubernetesClient(), openShiftClient, (item) -> this.createdItems.add(item)));
            }
        }
        //todo migrate out-of-namespaces items
        createdItems.stream().map(item -> MigrationResultResource.ok(new KubernetesResource(item))).forEach(errors::add);
        return errors;
    }

    @Override
    public List<MigrationResultResource> rollback() {
        List<MigrationResultResource> errors = new ArrayList<>();
        if (this.createdItems == null || this.createdItems.isEmpty()) {
            return errors;
        }
        for (MigrationProvider provider : migrationProviders) {
            List<HasMetadata> itemsToRollback = createdItems.stream().filter(item -> provider.isResponsibleFor(item.getKind()))
                    .collect(Collectors.toList());
            errors.addAll(provider.rollback(itemsToRollback, openShiftClient));
        }
        return errors;
    }

    @Override
    public void clearRollback() {
        this.createdItems.clear();
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
