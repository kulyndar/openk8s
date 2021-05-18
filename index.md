# Hello, cloud users!
## Welcome to opek8s migration app.

This small app was developed to help you migrate your work from Kubernetes to OpenShift platforms. In a few clicks you will be able to select object you want to move.

Please, notice that app will need to have appropriate roles in your clusters. For Kubernetes cluster, please ensure, that application account has capabilities to read all objects. For OpenShift, the app has to have ability to list namespaces and to create new objects.

To start tour journey, please download the app using manual on [GitHub](https://github.com/kulyndar/openk8s).

# Technical documentation
Javadoc is available [here](https://kulyndar.github.io/openk8s/docs). 

## Technology stack
Aplication BE was written in Java using following technologies:

1. [Spring boot](https://spring.io/projects/spring-boot)
2. [kubernetes-client](https://github.com/fabric8io/kubernetes-client)

Aplication FE was written in JS using:
1. [React framework](https://reactjs.org/)
2. [Ant Design](https://ant.design/docs/react/introduce)

