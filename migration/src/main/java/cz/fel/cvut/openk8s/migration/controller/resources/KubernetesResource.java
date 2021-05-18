package cz.fel.cvut.openk8s.migration.controller.resources;

import io.fabric8.kubernetes.api.model.HasMetadata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * API resource.
 * Represents Kubernetes object.
 * Structure:
 *      name - Kubernetes object name
 *      kind - Kubernetes object kind
 *      namespace - Kubernetes object namespace
 *      uid - Kubernetes object uid
 *      children - list of children
 */
public class KubernetesResource implements Serializable {
    private String name;
    private String kind;
    private String namespace;
    private String uid;

    private List<KubernetesResource> children;


    public KubernetesResource() {
    }

    public KubernetesResource(String name, String kind) {
        this.name = name;
        this.kind = kind;
    }

    public KubernetesResource(String name, String kind, String namespace, String uid) {
        this.name = name;
        this.kind = kind;
        this.namespace = namespace;
        this.uid = uid;
        this.children = new ArrayList<>();
    }

    public KubernetesResource(HasMetadata item) {
        this.name = item.getMetadata().getName();
        this.kind = item.getKind();
        this.namespace = item.getMetadata().getNamespace();
        this.uid = item.getMetadata().getUid();
        this.children = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public List<KubernetesResource> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        return "KubernetesResource{" +
                "name='" + name + '\'' +
                ", kind='" + kind + '\'' +
                ", namespace='" + namespace + '\'' +
                ", uid='" + uid + '\'' +
                '}';
    }
}
