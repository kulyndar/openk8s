# openk8s
This small app was developed to help you migrate your work from Kubernetes to OpenShift platforms. In a few clicks you will be able to select object you want to move.
See documentation [here](https://kulyndar.github.io/openk8s/)

## Installation

App contains too parts - Spring boot backend and Rect frontend.
 1. Clone this repository
 2. Move to the folder ```openk8s/migration```
 4. Run ```mvn clean package``` to package apps JAR
 5. Run the jar using command ```java -jar ./target/migration-1.0.0.jar```
 6. Move to  ```migration-fe``` folder
 7. Run ```npm install``` and then ```npm start```
 8. App is available at http://localhost:3000

## Connecting to clusters

Please, notice that app will need to have appropriate users with roles in your clusters. 
For Kubernetes cluster, please ensure, that application account has capabilities to 
read all objects. For OpenShift, the app has to have ability to list namespaces 
and to create and delete objects.

## Supported Kubernetes objects

In current version the following Kubernetes objects are supported:
1. Deployment
2. Pod
3. Service
4. ReplicaSet
5. Secret
6. ServiceAccount
7. ConfigMap
