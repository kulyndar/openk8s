package cz.fel.cvut.openk8s.migration.controller.resources;

/**
 * API resource
 * Structure:
 *      ocIp - OpenShift master node URL
 *      authType - authentication type ('basic' or 'token')
 *      username - username for authentication
 *      password - password for authentication
 *      token - user token
 */
public class InitOpenshiftResource {
    private String ocIp;
    private String authType;
    private String token;
    private String username;
    private String password;

    public String getOcIp() {
        return ocIp;
    }

    public String getToken() {
        return token;
    }

    public String getAuthType() {
        return authType;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
