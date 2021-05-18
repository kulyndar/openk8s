package cz.fel.cvut.openk8s.migration.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import cz.fel.cvut.openk8s.migration.controller.resources.ClusterInfoResource;
import cz.fel.cvut.openk8s.migration.controller.resources.InitKubernetesResource;
import cz.fel.cvut.openk8s.migration.controller.resources.StatusResource;
import cz.fel.cvut.openk8s.migration.service.KubernetesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller to serve all requests to Kuberentes cluster
 */
@CrossOrigin(origins = "${openk8s.migration.fe.uri}", allowCredentials = "true")
@RestController
@RequestMapping("/kubernetes")
public class KubernetesController {

    @Autowired
    private KubernetesService kubernetesService;

    /** Creates connection to a Kubernetes cluster.
     * URL: /kubernetes/init
     * Method: PUT
     * @param initKubernetesResource  data for connection (see {@link InitKubernetesResource})
     * @return {@link StatusResource}
     */
    @PutMapping("/init")
    public ResponseEntity<StatusResource> initKubernetes(@RequestBody InitKubernetesResource initKubernetesResource) {
        StatusResource errorStatus = kubernetesService.init(initKubernetesResource.getKubeip(), initKubernetesResource.getAuthType(),
                initKubernetesResource.getTokenId(), initKubernetesResource.getTokeSecret(),
                initKubernetesResource.getToken());

        return ResponseEntity.ok(errorStatus);
    }

    /** Reads cluster information and returns cluster structure as a tree.
     * @return {@link ClusterInfoResource}
     * @throws NullPointerException  if cluster connection was not initialized or was expired
     */
    @GetMapping("/clusterinfo")
    public ResponseEntity<ClusterInfoResource> getClusterInfo(){
        ClusterInfoResource clusterInfo = kubernetesService.getClusterInfoSimple();
        return ResponseEntity.ok(clusterInfo);
    }

    /** Returns YAML configuration for the item
     * @param kind  item kind (case insensitive)
     * @param name  item name
     * @param namespace  item namespace, nullable
     * @return YAML representation of the item
     * @throws JsonProcessingException  if an error occurred during serialization
     */
    @GetMapping("/info/{kind}/{name}")
    public ResponseEntity<String> getInfo(@PathVariable String kind, @PathVariable String name, @RequestParam(value = "namespace", required = false) String namespace) throws JsonProcessingException {
         String yaml= kubernetesService.getInfo(namespace, kind, name);
        return ResponseEntity.ok(yaml);
    }
}
