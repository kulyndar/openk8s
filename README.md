# openk8s
This small app was developed to help you migrate your work from Kubernetes to OpenShift platforms. In a few clicks you will be able to select object you want to move.

## Installation

App contains too parts - Spring boot backend and Rect frontend.
 1. Clone this repository
 2. Move to the root folder
 3. Move to ```migration``` folder
 4. Run ```mvn package``` to package apps JAR
 5. Run the jar
 6. Move to  ```migration-fe``` folder
 7. Run ```npm install``` and then ```npm start```
 8. App is available at http://localhost:3000

## Connecting to clusters

Please, notice that app will need to have appropriate roles in your clusters. For Kubernetes cluster, please ensure, that application account has capabilities to read all objects. For OpenShift, the app has to have ability to list namespaces and to create new objects.