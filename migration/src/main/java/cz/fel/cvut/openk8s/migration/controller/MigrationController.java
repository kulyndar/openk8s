package cz.fel.cvut.openk8s.migration.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "${openk8s.migration.fe.uri}", allowCredentials = "true")
@RestController
@RequestMapping("/migration")
public class MigrationController {
}
