package cz.fel.cvut.openk8s.migration.config;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class KubernetesConfig {

    @Bean(name = "kubernetesClient")
    @Scope("session")
    public KubernetesClient getKubernetesClient() {
        Config config = new ConfigBuilder().build();
        return new DefaultKubernetesClient(config);
    }
}
