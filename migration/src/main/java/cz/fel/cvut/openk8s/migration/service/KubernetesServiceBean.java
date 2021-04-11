package cz.fel.cvut.openk8s.migration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import cz.fel.cvut.openk8s.migration.controller.resources.*;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentList;
import io.fabric8.kubernetes.api.model.apps.ReplicaSet;
import io.fabric8.kubernetes.api.model.apps.ReplicaSetList;
import io.fabric8.kubernetes.client.*;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.client.internal.SerializationUtils;
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
    @Deprecated
    public List<NamespaceResource> getClusterInfo() {
        List<NamespaceResource> namespaceResourceList = new ArrayList<>();
        NamespaceList list = kubernetesClient.namespaces().list();
        for (Namespace namespace : list.getItems()) {
            NamespaceResource namespaceResource = new NamespaceResource(namespace.getMetadata().getName());
            List<PodResource> singlePodResources = new ArrayList<>();
            Map<Owner, List<PodResource>> podsWithOwners = new HashMap<>();

            PodList podList = kubernetesClient.pods().inNamespace(namespace.getMetadata().getName()).list();
            for (Pod pod : podList.getItems()) {
                PodResource podResource = createResource(pod);
                detectSingleOrWithOwner(pod, singlePodResources, podsWithOwners, podResource);
            }
            namespaceResource.setPods(singlePodResources);

            ReplicaSetList replicaSets = kubernetesClient.apps().replicaSets().inNamespace(namespace.getMetadata().getName()).list();
            List<ReplicaSetResource> singleReplicaSets = new ArrayList<>();
            Map<Owner, List<ReplicaSetResource>> deploymentReplicaSets = new HashMap<>();
            for (ReplicaSet replicaSet : replicaSets.getItems()) {
                ReplicaSetResource replicaSetResource = createResource(replicaSet);
                replicaSetResource.setPods(new ArrayList<>(bindOwners(replicaSet, podsWithOwners)));
                detectSingleOrWithOwner(replicaSet, singleReplicaSets, deploymentReplicaSets, replicaSetResource);
            }
            namespaceResource.setReplicaSets(singleReplicaSets);

            DeploymentList deploymentList = kubernetesClient.apps().deployments().inNamespace(namespace.getMetadata().getName()).list();
            List<DeploymentResource> deployments = new ArrayList<>();
            for (Deployment deployment : deploymentList.getItems()) {
                DeploymentResource deploymentResource = createResource(deployment);
                deploymentResource.setReplicaSets(new ArrayList<>(bindOwners(deployment, deploymentReplicaSets)));
                deployments.add(deploymentResource);
            }
            namespaceResource.setDeployments(deployments);

            ServiceList serviceList = kubernetesClient.services().inNamespace(namespace.getMetadata().getName()).list();
            List<ServiceResource> services = new ArrayList<>();
            for (io.fabric8.kubernetes.api.model.Service service : serviceList.getItems()) {
                ServiceResource serviceResource = createResource(service);
                services.add(serviceResource);
            }
            namespaceResource.setServices(services);
            //TODO add other resource

            namespaceResourceList.add(namespaceResource);
        }
       return namespaceResourceList;
    }

    @Override
    public ClusterInfoResource getClusterInfoSimple() {
        List<SimpleNamespaceResource> namespaceResourceList = new ArrayList<>();
        NamespaceList list = kubernetesClient.namespaces().list();
        for (Namespace namespace : list.getItems()) {
            SimpleNamespaceResource namespaceResource = new SimpleNamespaceResource(namespace.getMetadata().getName());
            List<PodResource> singlePodResources = new ArrayList<>();
            Map<Owner, List<PodResource>> podsWithOwners = new HashMap<>();

            PodList podList = kubernetesClient.pods().inNamespace(namespace.getMetadata().getName()).list();
            for (Pod pod : podList.getItems()) {
                PodResource podResource = createResource(pod);
                detectSingleOrWithOwner(pod, singlePodResources, podsWithOwners, podResource);
            }
            namespaceResource.getChildren().addAll(singlePodResources);

            ReplicaSetList replicaSets = kubernetesClient.apps().replicaSets().inNamespace(namespace.getMetadata().getName()).list();
            List<ReplicaSetResource> singleReplicaSets = new ArrayList<>();
            Map<Owner, List<ReplicaSetResource>> deploymentReplicaSets = new HashMap<>();
            for (ReplicaSet replicaSet : replicaSets.getItems()) {
                ReplicaSetResource replicaSetResource = createResource(replicaSet);
                replicaSetResource.getChildren().addAll(new ArrayList<>(bindOwners(replicaSet, podsWithOwners)));
                detectSingleOrWithOwner(replicaSet, singleReplicaSets, deploymentReplicaSets, replicaSetResource);
            }
            namespaceResource.getChildren().addAll(singleReplicaSets);

            DeploymentList deploymentList = kubernetesClient.apps().deployments().inNamespace(namespace.getMetadata().getName()).list();
            List<DeploymentResource> deployments = new ArrayList<>();
            for (Deployment deployment : deploymentList.getItems()) {
                DeploymentResource deploymentResource = createResource(deployment);
                deploymentResource.getChildren().addAll(new ArrayList<>(bindOwners(deployment, deploymentReplicaSets)));
                deployments.add(deploymentResource);
            }
            namespaceResource.getChildren().addAll(deployments);

            ServiceList serviceList = kubernetesClient.services().inNamespace(namespace.getMetadata().getName()).list();
            List<ServiceResource> services = new ArrayList<>();
            for (io.fabric8.kubernetes.api.model.Service service : serviceList.getItems()) {
                ServiceResource serviceResource = createResource(service);
                services.add(serviceResource);
            }
            namespaceResource.getChildren().addAll(services);
            //TODO add other resource

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
                .append("<ul>")
                .append("<li>Pod</li>")
                .append("<li>ReplicaSet</li>")
                .append("<li>Deployment</li>")
                .append("<li>Service</li>")
                .append("</ul>");
        return MessageResource.info(message.toString());
    }

    @Override
    public String getInfo(String namespace, String kind, String name) throws JsonProcessingException {
        final String kindLower = kind.toLowerCase();
        switch (kindLower) {
            case "pod":
                Pod pod = kubernetesClient.pods().inNamespace(namespace).withName(name).get();
                return SerializationUtils.dumpAsYaml(pod);
            case "deployment":
                Deployment deployment = kubernetesClient.apps().deployments().inNamespace(namespace).withName(name).get();
                return SerializationUtils.dumpAsYaml(deployment);
            case "service":
                io.fabric8.kubernetes.api.model.Service service = kubernetesClient.services().inNamespace(namespace).withName(name).get();
                return SerializationUtils.dumpAsYaml(service);
            case "replicaset":
                ReplicaSet replicaSet = kubernetesClient.apps().replicaSets().inNamespace(namespace).withName(name).get();
                return SerializationUtils.dumpAsYaml(replicaSet);
            default:
                return "Not implemented yet";

        }
    }

    private ServiceResource createResource(io.fabric8.kubernetes.api.model.Service service) {
        ServiceResource serviceResource = new ServiceResource(service.getMetadata().getName(), service.getKind(),
                service.getMetadata().getNamespace(), service.getMetadata().getUid());
        return serviceResource;
    }

    private DeploymentResource createResource(Deployment deployment) {
        DeploymentResource deploymentResource = new DeploymentResource(deployment.getMetadata().getName(), deployment.getKind(),
                deployment.getMetadata().getNamespace(), deployment.getMetadata().getUid());
        return deploymentResource;
    }

    private <T> void detectSingleOrWithOwner(HasMetadata resource, List<T> singleList, Map<Owner, List<T>> withOwners, T newResource) {
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

    private ReplicaSetResource createResource(ReplicaSet replicaSet) {
        ReplicaSetResource resource = new ReplicaSetResource(replicaSet.getMetadata().getName(), replicaSet.getKind(),
                replicaSet.getMetadata().getNamespace(), replicaSet.getMetadata().getUid());
        return resource;
    }

    private PodResource createResource(Pod pod) {
        PodResource resource = new PodResource(pod.getMetadata().getName(), pod.getKind(),
                pod.getMetadata().getNamespace(), pod.getMetadata().getUid());
        return resource;
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
