package cz.fel.cvut.openk8s.migration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
        "cz.fel.cvut.openk8s.migration.config",
        "cz.fel.cvut.openk8s.migration.controller",
        "cz.fel.cvut.openk8s.migration.service"
})
public class MigrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(MigrationApplication.class, args);
    }

}
