package cz.fel.cvut.openk8s.migration.controller.resources;

public class ServiceResource extends KubernetesResource {

    public ServiceResource() {
    }

    public ServiceResource(String name, String kind, String namespace, String uid) {
        super(name, kind, namespace, uid);
    }
}
