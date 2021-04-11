package cz.fel.cvut.openk8s.migration.controller.resources;

public class PodResource extends KubernetesResource {

    public PodResource() {
    }

    public PodResource(String name, String kind, String namespace, String uid) {
        super(name, kind, namespace, uid);
    }
}
