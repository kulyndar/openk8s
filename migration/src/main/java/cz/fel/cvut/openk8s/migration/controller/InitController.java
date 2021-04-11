package cz.fel.cvut.openk8s.migration.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import cz.fel.cvut.openk8s.migration.controller.resources.*;
import cz.fel.cvut.openk8s.migration.service.KubernetesService;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RestController
@RequestMapping("/init")
public class InitController {

    @Autowired
    private KubernetesService kubernetesService;

    @PutMapping("/kubernetes")
    public ResponseEntity<StatusResource> initKubernetes(@RequestBody InitKubernetesResource initKubernetesResource) {
        StatusResource errorStatus = kubernetesService.init(initKubernetesResource.getKubeip(), initKubernetesResource.getAuthType(),
                initKubernetesResource.getTokenId(), initKubernetesResource.getTokeSecret(),
                initKubernetesResource.getToken());

        return ResponseEntity.ok(errorStatus);
    }
    @GetMapping("/clusterinfo")
    public ResponseEntity<ClusterInfoResource> getClusterInfo() throws JsonProcessingException {
        ClusterInfoResource clusterInfo = kubernetesService.getClusterInfoSimple();
        return ResponseEntity.ok(clusterInfo);
    }

    @GetMapping("/info/{kind}/{name}")
    public ResponseEntity<String> getInfo(@PathVariable String kind, @PathVariable String name, @RequestParam(value = "namespace", required = false) String namespace) throws JsonProcessingException {
         String yaml= kubernetesService.getInfo(namespace, kind, name);
        return ResponseEntity.ok(yaml);
    }
}
