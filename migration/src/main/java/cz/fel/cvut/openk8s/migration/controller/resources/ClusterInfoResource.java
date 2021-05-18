package cz.fel.cvut.openk8s.migration.controller.resources;

import java.util.List;

/**
 * API resource
 * Structure:
 *      messages - list of system messages
 *      simpleStructure - tree structure of the cluster
 */
public class ClusterInfoResource {
    private List<MessageResource> messages;
    private List<SimpleNamespaceResource> simpleStructure;

    public ClusterInfoResource(List<SimpleNamespaceResource> structure, List<MessageResource> messages) {
        this.simpleStructure = structure;
        this.messages = messages;
    }

    public List<MessageResource> getMessages() {
        return messages;
    }

    public List<SimpleNamespaceResource> getSimpleStructure() {
        return simpleStructure;
    }
}
