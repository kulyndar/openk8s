package cz.fel.cvut.openk8s.migration.controller.resources;

import java.util.ArrayList;
import java.util.List;

/**
 * API resource.
 * Represents Kubernetes namespace.
 * Structure:
 *      name - Kubernetes namespace name
 *      children - list of children
 */
public class SimpleNamespaceResource {
    private String name;
    private List<KubernetesResource> children;

    public SimpleNamespaceResource(String name) {
        this.name = name;
        this.children = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<KubernetesResource> getChildren() {
        return children;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setChildren(List<KubernetesResource> children) {
        this.children = children;
    }
}
