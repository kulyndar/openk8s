package cz.fel.cvut.openk8s.migration.service;

import cz.fel.cvut.openk8s.migration.controller.resources.KubernetesResource;
import cz.fel.cvut.openk8s.migration.controller.resources.MigrationErrorResource;
import cz.fel.cvut.openk8s.migration.controller.resources.StatusResource;

import java.util.List;

public interface OpenshiftService {

    StatusResource init(String ocIp, String authType, String token, String username, String password);

    void destroy();

    List<MigrationErrorResource> migrate(List<KubernetesResource> itemsList);
}
