package cz.fel.cvut.openk8s.migration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import cz.fel.cvut.openk8s.migration.controller.resources.ClusterInfoResource;
import cz.fel.cvut.openk8s.migration.controller.resources.StatusResource;
import io.fabric8.kubernetes.client.KubernetesClient;

/**
 * Service is used to manipulate with Kubernetes cluster
 */
public interface KubernetesService {

    /**
     * This method creates connection to Kubernetes cluster. Connection is tested with listing pods command
     * @param kubeip  URL of kubernetes cluster master
     * @param authType  authentication type. Use 'boot' for Bootstrap token, 'token' for ServiceAccount or User token
     * @param tokenId  Bootstrap token ID, is used only with 'boot' authentication type
     * @param tokenSecret  Bootstrap token secret, is used only with 'boot' authentication type
     * @param token  ServiceAccount or User token, is used only with 'token' authentication type
     * @return status of connection
     */
    StatusResource init(String kubeip, String authType, String tokenId, String tokenSecret, String token);

    /**
     * Returns Kubernetes cluster structure as tree.
     * @return cluster structure
     * @throws NullPointerException  if cluster connection was not initialized or was expired
     */
    ClusterInfoResource getClusterInfoSimple();

    /**
     * This method returns information for Kubernetes object in YAML format. Output is the same as "kubectl -n namespace kinf item -o yaml".
     * @param namespace  item namespace, nullable
     * @param kind  item kind, case insensitive
     * @param name  item name
     * @return YAML konfiguration
     * @throws JsonProcessingException  if item cannot be serialized
     * @throws NullPointerException  if cluster connection was not initialized or was expired
     */
    String getInfo(String namespace, String kind, String name) throws JsonProcessingException;

    /**
     * Destroys user session and connection to the cluster
     */
    void destoy();

    /**
     * @return kubernetes client, connected to the cluster
     */
    KubernetesClient getKubernetesClient();
}
