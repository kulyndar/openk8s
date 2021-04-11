package cz.fel.cvut.openk8s.migration.controller.resources;

import java.util.List;

public class ReplicaSetResource extends KubernetesResource{
    private List<PodResource> pods;

    public ReplicaSetResource() {
    }

    public ReplicaSetResource(String name, String kind, String namespace, String uid) {
        super(name, kind, namespace, uid);
    }

    public List<PodResource> getPods() {
        return pods;
    }

    public void setPods(List<PodResource> pods) {
        this.pods = pods;
    }
}
