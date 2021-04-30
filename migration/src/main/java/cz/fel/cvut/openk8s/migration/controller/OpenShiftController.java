package cz.fel.cvut.openk8s.migration.controller;

import cz.fel.cvut.openk8s.migration.controller.resources.InitOpenshiftResource;
import cz.fel.cvut.openk8s.migration.controller.resources.KubernetesResource;
import cz.fel.cvut.openk8s.migration.controller.resources.MigrationErrorResource;
import cz.fel.cvut.openk8s.migration.controller.resources.StatusResource;
import cz.fel.cvut.openk8s.migration.service.OpenshiftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "${openk8s.migration.fe.uri}", allowCredentials = "true")
@RestController
@RequestMapping("/openshift")
public class OpenShiftController {

    @Autowired
    private OpenshiftService openshiftService;

    @PutMapping("/init")
    public ResponseEntity<StatusResource> initKubernetes(@RequestBody InitOpenshiftResource initOpenshiftResource) {
        StatusResource responseStatus = openshiftService.init(initOpenshiftResource.getOcIp(), initOpenshiftResource.getAuthType(),
                initOpenshiftResource.getToken(), initOpenshiftResource.getUsername(), initOpenshiftResource.getPassword());
        return ResponseEntity.ok(responseStatus);
    }

    @PostMapping("/migrate")
    public ResponseEntity<List<MigrationErrorResource>> migrate(@RequestBody List<KubernetesResource> itemsList) {
        List<MigrationErrorResource> response = openshiftService.migrate(itemsList);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/rollback")
    public ResponseEntity<List<MigrationErrorResource>> rollback() {
        List<MigrationErrorResource> response = openshiftService.rollback();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/clear-rollback")
    public ResponseEntity<Void> clearRollback() {
        openshiftService.clearRollback();
        return ResponseEntity.ok().build();
    }
}
