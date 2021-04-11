package cz.fel.cvut.openk8s.migration.controller.resources;

import java.util.List;

public class NamespaceResource {
    private String name;
    private List<DeploymentResource> deployments;
    private List<PodResource> pods;
    private List<ReplicaSetResource> replicaSets;
    private List<ServiceResource> services;

    public NamespaceResource() {
    }

    public NamespaceResource(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<DeploymentResource> getDeployments() {
        return deployments;
    }

    public void setDeployments(List<DeploymentResource> deployments) {
        this.deployments = deployments;
    }

    public List<PodResource> getPods() {
        return pods;
    }

    public void setPods(List<PodResource> pods) {
        this.pods = pods;
    }

    public List<ReplicaSetResource> getReplicaSets() {
        return replicaSets;
    }

    public void setReplicaSets(List<ReplicaSetResource> replicaSets) {
        this.replicaSets = replicaSets;
    }

    public List<ServiceResource> getServices() {
        return services;
    }

    public void setServices(List<ServiceResource> services) {
        this.services = services;
    }
}
