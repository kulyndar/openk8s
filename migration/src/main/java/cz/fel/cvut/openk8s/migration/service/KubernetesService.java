package cz.fel.cvut.openk8s.migration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import cz.fel.cvut.openk8s.migration.controller.resources.ClusterInfoResource;
import cz.fel.cvut.openk8s.migration.controller.resources.NamespaceResource;
import cz.fel.cvut.openk8s.migration.controller.resources.SimpleNamespaceResource;
import cz.fel.cvut.openk8s.migration.controller.resources.StatusResource;

import java.util.List;

public interface KubernetesService {
    StatusResource init(String kubeip, String authType, String tokenId, String tokenSecret, String token);

    List<NamespaceResource> getClusterInfo();

    ClusterInfoResource getClusterInfoSimple() throws JsonProcessingException;

    String getInfo(String namespace, String kind, String name) throws JsonProcessingException;
}
