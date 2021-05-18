package cz.fel.cvut.openk8s.migration.controller;

import cz.fel.cvut.openk8s.migration.controller.resources.*;
import cz.fel.cvut.openk8s.migration.service.OpenshiftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller to serve all requests to OpenShift cluster
 */
@CrossOrigin(origins = "${openk8s.migration.fe.uri}", allowCredentials = "true")
@RestController
@RequestMapping("/openshift")
public class OpenShiftController {

    @Autowired
    private OpenshiftService openshiftService;

    /** Creates connection to a Kubernetes cluster.
     * @param initOpenshiftResource  data for connection (see {@link InitOpenshiftResource})
     * @return {@link StatusResource}
     */
    @PutMapping("/init")
    public ResponseEntity<StatusResource> initKubernetes(@RequestBody InitOpenshiftResource initOpenshiftResource) {
        StatusResource responseStatus = openshiftService.init(initOpenshiftResource.getOcIp(), initOpenshiftResource.getAuthType(),
                initOpenshiftResource.getToken(), initOpenshiftResource.getUsername(), initOpenshiftResource.getPassword());
        return ResponseEntity.ok(responseStatus);
    }

    /** Migrates selected items to OpenShift cluster. If errors occured during some item migration, it will not influence other items migration
     * @param itemsList  list of items to migrate (see {@link KubernetesResource})
     * @return list of migration results (see {@link MigrationResultResource})
     * @throws NullPointerException  if cluster connection was not initialized or was expired
     */
    @PostMapping("/migrate")
    public ResponseEntity<List<MigrationResultResource>> migrate(@RequestBody List<KubernetesResource> itemsList) {
        List<MigrationResultResource> response = openshiftService.migrate(itemsList);
        return ResponseEntity.ok(response);
    }

    /** Rollbacks full migration
     * @return list of rollback results (see {@link MigrationResultResource})
     * @throws NullPointerException  if cluster connection was not initialized or was expired
     */
    @PostMapping("/rollback")
    public ResponseEntity<List<MigrationResultResource>> rollback() {
        List<MigrationResultResource> response = openshiftService.rollback();
        return ResponseEntity.ok(response);
    }

    /**
     * Clears data for rollback, so no rollback could be done after that
     * @return HTTP status 200
     */
    @PostMapping("/clear-rollback")
    public ResponseEntity<Void> clearRollback() {
        openshiftService.clearRollback();
        return ResponseEntity.ok().build();
    }
}
