# KG Service

This service is a Play framework application which requires the latest SBT installation.

## Production deployement

To create a binary for production; in the root folder run.

```
sbt stage
```

The binary can be found in the `target/universal/stage/bin` folder.

To run the application you havbe to specify a secret and a file for the running PID for example :
```
target/universal/stage/bin/kg_service -Dpidfile.path=/var/run/kg-service.pid -Dplay.http.secret.key=myapplicationsecret
```
You can specify the port with the `-Dhttp.port` option (e.g. `target/universal/stage/bin/kg_service  -Dhttp.port=8080`).

## Modules

### Common

This modules contains code used by other modules in this project.

### Authentication

This modules allows authentication through OIDC.
The authentication modules also check for accessbile index in the nexus ElasticSearch instance.
This is used for example by the proxy module in order to query the indices authorized by a OIDC group.

### Nexus

All the helpers and API wrapper for the Nexus API can be found in this project.

### Proxy

This service handles the call to our search ui project.

### Editor

This service provides an API for our editor ui.

### Data_import

Anything related to data import is found in this modules.
