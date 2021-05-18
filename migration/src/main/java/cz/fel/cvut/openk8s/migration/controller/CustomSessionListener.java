package cz.fel.cvut.openk8s.migration.controller;

import cz.fel.cvut.openk8s.migration.service.KubernetesService;
import cz.fel.cvut.openk8s.migration.service.OpenshiftService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * This class is used to close connections to Kubernetes and OpenShift clusters when user session closes.
 */
@WebListener
public class CustomSessionListener implements HttpSessionListener {

    @Autowired
    private KubernetesService kubernetesService;

    @Autowired
    private OpenshiftService openshiftService;

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
       if (kubernetesService != null) {
           kubernetesService.destoy();
       }
       if (openshiftService != null) {
           openshiftService.destroy();
       }
    }


}
