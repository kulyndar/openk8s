package cz.fel.cvut.openk8s.migration.service;

import cz.fel.cvut.openk8s.migration.controller.resources.KubernetesResource;
import cz.fel.cvut.openk8s.migration.controller.resources.MigrationResultResource;
import cz.fel.cvut.openk8s.migration.controller.resources.StatusResource;

import java.util.List;

/**
 * This service is used to manage connection to OpenShift cluster
 */
public interface OpenshiftService {

    /**
     * This method creates connection to OpenShift cluster. Connection is tested with listing namespaces command
     * @param ocIp  URL of OpenShift cluster master
     * @param authType  authentication type. Use 'basic' for Basic authentication, 'token' for ServiceAccount or User token
     * @param username  username, is used only with 'basic' authentication type
     * @param password  password, is used only with 'basic' authentication type
     * @param token  ServiceAccount or User token, is used only with 'token' authentication type
     * @return status of connection
     */
    StatusResource init(String ocIp, String authType, String token, String username, String password);

    /**
     * Does a migration rollback.
     * @return  status of rollback
     * @throws NullPointerException  if cluster connection was not initialized or was expired
     */
    List<MigrationResultResource> rollback();

    /**
     * Clears data for rollback
     */
    void clearRollback();

    /**
     * Destroys user session and connection to the cluster
     */
    void destroy();

    /**
     * Migrates selected items to OpenShift cluster. If errors occured during some item migration, it will not influence other items migration
     * @param itemsList  list of items to migrate (see {@link KubernetesResource})
     * @return list of migration results
     * @throws NullPointerException  if cluster connection was not initialized or was expired
     */
    List<MigrationResultResource> migrate(List<KubernetesResource> itemsList);
}
