package cz.fel.cvut.openk8s.migration.controller.resources;

public class InitKubernetesResource {

    private String kubeip;
    private String authType;

    private String tokenId;
    private String tokeSecret;
    private String token;

    public String getKubeip() {
        return kubeip;
    }

    public String getAuthType() {
        return authType;
    }

    public String getTokenId() {
        return tokenId;
    }

    public String getTokeSecret() {
        return tokeSecret;
    }

    public String getToken() {
        return token;
    }
}
