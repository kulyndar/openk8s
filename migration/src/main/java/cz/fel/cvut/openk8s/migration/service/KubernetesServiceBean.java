package cz.fel.cvut.openk8s.migration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import cz.fel.cvut.openk8s.migration.controller.resources.KubernetesResource;
import cz.fel.cvut.openk8s.migration.controller.resources.*;
import cz.fel.cvut.openk8s.migration.service.migration.MigrationProvider;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentList;
import io.fabric8.kubernetes.api.model.apps.ReplicaSet;
import io.fabric8.kubernetes.api.model.apps.ReplicaSetList;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
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
public class KubernetesServiceBean implements KubernetesService {

    private static final Logger LOGGER = LoggerFactory.getLogger(KubernetesServiceBean.class);

    private static final String AUTH_TYPE_BOOTSTRAP = "boot";
    private static final String AUTH_TYPE_TOKEN = "token";

    private KubernetesClient kubernetesClient;

    @Autowired
    private List<MigrationProvider> migrationProviders;

    @Override
    public StatusResource init(String kubeip, String authType, String tokenId, String tokenSecret, String token) {
        if (kubernetesClient != null) {
            kubernetesClient.close();
        }
        ConfigBuilder builder = new ConfigBuilder().withMasterUrl(kubeip);
        if (AUTH_TYPE_BOOTSTRAP.equals(authType)) {
            builder.withOauthToken(tokenId + "." + tokenSecret);
        } else if (AUTH_TYPE_TOKEN.equals(authType)) {
            builder.withOauthToken(token);
        }

        this.kubernetesClient = new DefaultKubernetesClient(builder.build());
        /*Trying to connect*/
        try {
            kubernetesClient.pods().list();
        } catch (KubernetesClientException e) {
            LOGGER.error("Error occurred when trying to connect to K8s with IP: " + kubeip, e);
            if (e.getCause() != null && e.getCause() instanceof ConnectException) {
                return StatusResource.connectionError();
            } else if (e.getStatus() != null) {
                return StatusResource.fromStatus(e.getStatus());
            }
            return StatusResource.unexpected();
        } catch (Exception e) {
            LOGGER.error("Error occurred when trying to connect to K8s with IP: " + kubeip, e);
            return StatusResource.unexpected();
        }
        return StatusResource.ok();
    }

    @Override
    public ClusterInfoResource getClusterInfoSimple() {
        List<SimpleNamespaceResource> namespaceResourceList = new ArrayList<>();
        NamespaceList list = kubernetesClient.namespaces().list();
        for (Namespace namespace : list.getItems()) {
            SimpleNamespaceResource namespaceResource = new SimpleNamespaceResource(namespace.getMetadata().getName());
            List<KubernetesResource> singlePodResources = new ArrayList<>();
            Map<Owner, List<KubernetesResource>> podsWithOwners = new HashMap<>();

            PodList podList = kubernetesClient.pods().inNamespace(namespace.getMetadata().getName()).list();
            for (Pod pod : podList.getItems()) {
                KubernetesResource podResource = createResource(pod);
                detectSingleOrWithOwner(pod, singlePodResources, podsWithOwners, podResource);
            }
            namespaceResource.getChildren().addAll(singlePodResources);

            ReplicaSetList replicaSets = kubernetesClient.apps().replicaSets().inNamespace(namespace.getMetadata().getName()).list();
            List<KubernetesResource> singleReplicaSets = new ArrayList<>();
            Map<Owner, List<KubernetesResource>> deploymentReplicaSets = new HashMap<>();
            for (ReplicaSet replicaSet : replicaSets.getItems()) {
                KubernetesResource replicaSetResource = createResource(replicaSet);
                replicaSetResource.getChildren().addAll(new ArrayList<>(bindOwners(replicaSet, podsWithOwners)));
                detectSingleOrWithOwner(replicaSet, singleReplicaSets, deploymentReplicaSets, replicaSetResource);
            }
            namespaceResource.getChildren().addAll(singleReplicaSets);

            DeploymentList deploymentList = kubernetesClient.apps().deployments().inNamespace(namespace.getMetadata().getName()).list();
            List<KubernetesResource> deployments = new ArrayList<>();
            for (Deployment deployment : deploymentList.getItems()) {
                KubernetesResource deploymentResource = createResource(deployment);
                deploymentResource.getChildren().addAll(new ArrayList<>(bindOwners(deployment, deploymentReplicaSets)));
                deployments.add(deploymentResource);
            }
            namespaceResource.getChildren().addAll(deployments);

            ServiceList serviceList = kubernetesClient.services().inNamespace(namespace.getMetadata().getName()).list();
            List<KubernetesResource> services = new ArrayList<>();
            for (io.fabric8.kubernetes.api.model.Service service : serviceList.getItems()) {
                KubernetesResource serviceResource = createResource(service);
                services.add(serviceResource);
            }
            namespaceResource.getChildren().addAll(services);


            SecretList secretList = kubernetesClient.secrets().inNamespace(namespace.getMetadata().getName()).list();
            List<KubernetesResource> secrets = new ArrayList<>();
            for (Secret secret : secretList.getItems()) {
                KubernetesResource secretResource = createResource(secret);
                secrets.add(secretResource);
            }
            namespaceResource.getChildren().addAll(secrets);

            ConfigMapList configMapList = kubernetesClient.configMaps().inNamespace(namespace.getMetadata().getName()).list();
            List<KubernetesResource> configMaps = new ArrayList<>();
            for (ConfigMap configMap : configMapList.getItems()) {
                KubernetesResource configMapResource = createResource(configMap);
                configMaps.add(configMapResource);
            }
            namespaceResource.getChildren().addAll(configMaps);

            ServiceAccountList serviceAccountList = kubernetesClient.serviceAccounts().inNamespace(namespace.getMetadata().getName()).list();
            List<KubernetesResource> serviceAccounts = new ArrayList<>();
            for (ServiceAccount serviceAccount : serviceAccountList.getItems()) {
                KubernetesResource serviceAccountResource = createResource(serviceAccount);
                serviceAccounts.add(serviceAccountResource);
            }
            namespaceResource.getChildren().addAll(serviceAccounts);

            namespaceResourceList.add(namespaceResource);
        }
        List<MessageResource> messages = createMessages();
        return new ClusterInfoResource(namespaceResourceList, messages);
    }

    private List<MessageResource> createMessages() {
        List<MessageResource> messageResources = new ArrayList<>();
        messageResources.add(createDefaultInfoMessage());
        messageResources.add(createDefaultNamespaceMessage());
        return messageResources;
    }

    private MessageResource createDefaultNamespaceMessage() {
        String message = "Migration of resources from system namespaces (starting with \"kube-\") is not supported.";
        return MessageResource.warning(message);
    }

    private MessageResource createDefaultInfoMessage() {
        StringBuilder message = new StringBuilder();
        message.append("The following types of resources are currently being migrated:")
                .append("<ul>");
        for (MigrationProvider provider : migrationProviders) {
            message.append("<li>")
                    .append(provider.getKind())
                    .append("</li>");
        }
        message.append("</ul>");
        return MessageResource.info(message.toString());
    }

    @Override
    public String getInfo(String namespace, String kind, String name) throws JsonProcessingException {
        for (MigrationProvider provider : migrationProviders) {
            if (provider.isResponsibleFor(kind)) {
                return provider.getInfo(name, namespace, this.kubernetesClient);
            }
        }
        return "Not implemented yet";
    }


    @Override
    public void destoy() {
        LOGGER.info("Start destroying kubernetes client");
        if (this.kubernetesClient != null) {
            this.kubernetesClient.close();
            this.kubernetesClient = null;
        } else {
            LOGGER.warn("Kubernetes client is not initialized or is already destroyed");
        }
    }

    private  void detectSingleOrWithOwner(HasMetadata resource, List<KubernetesResource> singleList, Map<Owner, List<KubernetesResource>> withOwners, KubernetesResource newResource) {
        List<Owner> owners = resource.getMetadata().getOwnerReferences() == null ? new ArrayList<>() :
                resource.getMetadata().getOwnerReferences().stream().map(o -> new Owner(o.getKind(), o.getUid())).collect(Collectors.toList());
        if (owners.isEmpty()) {
            singleList.add(newResource);
        } else {
            for (Owner owner : owners) {
                if (!withOwners.containsKey(owner)) {
                    withOwners.put(owner, new ArrayList<>());
                }
                withOwners.get(owner).add(newResource);
            }
        }
    }

    private <T> List<T>  bindOwners(HasMetadata resource, Map<Owner, List<T>> resourcesMap) {
        Owner replicaOwner = new Owner(resource.getKind(), resource.getMetadata().getUid());
        if (resourcesMap.containsKey(replicaOwner)) {
            return resourcesMap.get(replicaOwner);
        }
        return new ArrayList<>();
    }

    private KubernetesResource createResource(HasMetadata hasMetadata) {
        return new KubernetesResource(hasMetadata.getMetadata().getName(), hasMetadata.getKind(),
                hasMetadata.getMetadata().getNamespace(), hasMetadata.getMetadata().getUid());
    }

    @Override
    public KubernetesClient getKubernetesClient() {
        return kubernetesClient;
    }

    private class Owner {
        private String type;
        private String uid;

        public Owner(String type, String uid) {
            this.type = type;
            this.uid = uid;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Owner owner = (Owner) o;
            return Objects.equals(type, owner.type) &&
                    Objects.equals(uid, owner.uid);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, uid);
        }
    }
}
