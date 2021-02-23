# e-Navigation VDES Controller Service
The VDES controller repository contains the implementation of a service
controlling VDES AIS stations. This entails reading messages through the
[Geomesa](https://www.geomesa.org/documentation/stable/index.html) library and
converting them to AIS messages before passing them down to the VDES stations
through UDP. The core micro-service is built using the Springboot framework.

## Development Setup
To start developing just open the repository with the IDE of your choice. The
original code has been generated using
[Intellij IDEA](https://www.jetbrains.com/idea). Just open it by going to:

    File -> New -> Project From Verson Control

Provide the URL of the current repository and the local directory you want.

You don't have to use it if you have another preference. Just make sure you
update the *.gitignore* file appropriately.

## Build Setup
The project is using the latest OpenJDK 8 to build, and only that should be
used. The main issue is that the current Geomesa library only supports Java 8
at the moment. We can only upgrade after later JDK versions are also supported
by Geomesa.

To build the project you will need Maven, which usually comes along-side the
IDE. Nothing exotic about the goals, just clean and install should do:

    mvn clean package

## Configuration
The configuration of the eureka server is based on the properties files found
in the *main/resources* directory.

The *boostrap.properties* contains the necessary properties to start the service
while the *application.properties* everything else i.e. the security
configuration.

Note that authentication is provided by Keycloak, so before you continue, you
need to make sure that a keycloak server is up and running, and that a client
is registered. Once that is done, the service will required the following
*application.properties* to be provided:

    keycloak.auth-server-url=<The keycloak address>
    keycloak.resource=<The client name>
    keycloak.credentials.secret=<The generated client sercet>

## Running the Service
To run the service, just like any other Springboot micro-service, all you need
to do is run the main class, i.e. VDESController. No further arguments are
required. Everything should be picked up through the properties files.

## Description
**The VDES controller is still under development and the actual VDES connection
has not yet been implemented.**

The service's core component is  the AtonGDSServices which boots up as a 
singleton component (only one per instance). This will create a consumer for 
a Geomesa Kafka Data Store and a number of listeners based on the configuration
provided in the *application.propeties* file:

    gla.rad.vdes-ctrl.aton.listeners[0].address: 10.0.0.1
    gla.rad.vdes-ctrl.aton.listeners[0].port: 9000
    gla.rad.vdes-ctrl.aton.listeners[0].polygon: 0.0,0.0,0.0,0.0
    gla.rad.vdes-ctrl.aton.listeners[1].address: 10.0.0.2
    gla.rad.vdes-ctrl.aton.listeners[1].port: 9001
    gla.rad.vdes-ctrl.aton.listeners[1].polygon: 1.0,1.0,1.0,1.0

In the above example, we get two listeners, controlling two different VDES 
stations. The crucial different is the polygon definition, which specified the
geographical area to which the listener will respond. All messages included in
that area will be sent to the VDES stations with the respective IP address and
port number.

Listeners will also propagate the received messages to a web-socket for 
debugging purposes.

## Contributing
Pull requests are welcome. For major changes, please open an issue first to
discuss what you would like to change.

Please make sure to update tests as appropriate.

## License
Distributed under the Apache License. See [LICENSE](./LICENSE) for more
information.

## Contact
Nikolaos Vastardis - Nikolaos.Vastardis@gla-rad.org



