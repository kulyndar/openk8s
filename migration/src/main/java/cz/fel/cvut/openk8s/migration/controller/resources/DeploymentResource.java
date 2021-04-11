package cz.fel.cvut.openk8s.migration.controller.resources;

import java.util.List;

public class DeploymentResource extends KubernetesResource{
    private List<ReplicaSetResource> replicaSets;

    public DeploymentResource() {

    }

    public DeploymentResource(String name, String kind, String namespace, String uid) {
        super(name, kind, namespace, uid);
    }

    public List<ReplicaSetResource> getReplicaSets() {
        return replicaSets;
    }

    public void setReplicaSets(List<ReplicaSetResource> replicaSets) {
        this.replicaSets = replicaSets;
    }
}
