package cz.fel.cvut.openk8s.migration.service;

import cz.fel.cvut.openk8s.migration.controller.resources.KubernetesResource;
import cz.fel.cvut.openk8s.migration.controller.resources.MigrationResultResource;
import cz.fel.cvut.openk8s.migration.controller.resources.StatusResource;

import java.util.List;

public interface OpenshiftService {

    StatusResource init(String ocIp, String authType, String token, String username, String password);

    List<MigrationResultResource> rollback();

    void clearRollback();

    void destroy();

    List<MigrationResultResource> migrate(List<KubernetesResource> itemsList);
}
