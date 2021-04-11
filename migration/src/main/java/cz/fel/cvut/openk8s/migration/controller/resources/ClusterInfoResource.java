package cz.fel.cvut.openk8s.migration.controller.resources;

import java.util.List;

public class ClusterInfoResource {
    private List<NamespaceResource> structure;
    private List<MessageResource> messages;
    private List<SimpleNamespaceResource> simpleStructure;

    public ClusterInfoResource(List<NamespaceResource> structure) {
        this.structure = structure;
    }

    public ClusterInfoResource(List<SimpleNamespaceResource> structure, List<MessageResource> messages) {
        this.simpleStructure = structure;
        this.messages = messages;
    }


    public List<NamespaceResource> getStructure() {
        return structure;
    }

    public List<MessageResource> getMessages() {
        return messages;
    }

    public List<SimpleNamespaceResource> getSimpleStructure() {
        return simpleStructure;
    }
}
