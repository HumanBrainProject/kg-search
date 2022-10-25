# Setting up the development environment

To set up the development environment, you need to have the following elements:

- An elasticsearch instance (running either on your local machine, on a server of your choice - maybe with SSH port-forwarding)
- A development environment for Java (Spring Boot) and JavaScript (React)


## Running the services
The services are located at `service/services`. You can find two different maven projects you should be able to integrate 
into your IDE. Additionally, there is a `kg-common` library in the `service/libs` directory. The two main projects are 
stand-alone Spring Boot services and therefore can run alongside each other.

Please note, that - depending on what you want to do - you don't need both services to be running. If you want to work on the UI, it is often
enough to only run the kg-search service (unless you want to improve / debug the actual indexing process).


## Running the UI
To run the UI, you can simply execute
```shell
npm install
npm run start
```
to launch the UI in a development server. If you want to update the configuration (e.g. different server endpoint) please have
a look at the `setupProxy.js` in the `src` directory.