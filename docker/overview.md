# The GLA e-Navigation Service Architecture - VDES Controller Service

## Quick Reference
* Maintained by:<br/>
  [GRAD](https://www.gla-rad.org/)
* Where to get help:<br/>
  [Unix & Linux](https://unix.stackexchange.com/help/on-topic),
  [Stack Overflow](https://stackoverflow.com/help/on-topic),
  [GRAD Wiki](https://rnavlab.gla-rad.org/wiki/E-Navigation_Service_Architecture)
  (for GRAD members only)

## What is e-Navigation
The maritime domain is facing a number for challenges, mainly due to the
increasing demand, that may increase the risk of an accident or loss of life.
These challenges require technological solutions and e-Navigation is one such
solution. The International Maritime Organization ([IMO](https://www.imo.org/))
adopted a ‘Strategy for the development and implementation of e‐Navigation’
(MSC85/26, Annexes 20 and 21), providing the following definition of
e‐Navigation:

<div style="padding: 4px;
    background:lightgreen;
    border:2px;
    border-style:solid;
    border-radius:20px;
    color:black">
E-Navigation, as defined by the IMO, is the harmonised collection, integration,
exchange, presentation and analysis of maritime information on-board and ashore
by electronic means to enhance berth-to-berth navigation and related services,
for safety and security at sea and protection of the marine environment.
</div>

In response, the International Association of Lighthouse Authorities
([IALA](https://www.iala-aism.org/)) published a number of guidelines such as
[G1113](https://www.iala-aism.org/product/g1113/) and
[G1114](https://www.iala-aism.org/product/g1114/), which establish the relevant
principles for the design and implementation of harmonised shore-based technical
system architectures and propose a set of best practices to be followed. In
these, the terms Common Shore‐Based System (CSS) and Common Shore‐based System
Architecture (CSSA) were introduced to describe the shore‐based technical system
of the IMO’s overarching architecture.

To ensure the secure communication between ship and CSSA, the International
Electrotechnical Commission (IEC), in coordination with IALA, compiled a set of
system architecture and operational requirements for e-Navigation into a
standard better known as [SECOM](https://webstore.iec.ch/publication/64543).
This provides mechanisms for secure data exchange, as well as a TS interface
design that is in accordance with the service guidelines and templates defined
by IALA. Although SECOM is just a conceptual standard, the Maritime Connectivity
Platform ([MCP](https://maritimeconnectivity.net/)) provides an actual
implementation of a decentralised framework that supports SECOM.

## What is the GRAD e-Navigation Service Architecture

The GLA follow the developments on e-Navigation closely, contributing through
their role as an IALA member whenever possible. As part of their efforts, a
prototype GLA e-Navigation Service Architecture is being developed by the GLA
Research and Development Directorate (GRAD), to be used as the basis for the
provision of the future GLA e-Navigation services.

As a concept, the CSSA is based on the Service Oriented Architecture (SOA). A
pure-SOA approach however was found to be a bit cumbersome for the GLA
operations, as it usually requires the entire IT landscape being compatible,
resulting in high investment costs. In the context of e-Navigation, this could
become a serious problem, since different components of the system are designed
by independent teams/manufacturers. Instead, a more flexible microservice
architecture was opted for. This is based on a break-down of the larger
functional blocks into small independent services, each responsible for
performing its own orchestration, maintaining its own data and communicating
through lightweight mechanisms such as HTTP/HTTPS. It should be pointed out that
SOA and the microservice architecture are not necessarily that different.
Sometimes, microservices are even considered as an extension or a more
fine-grained version of SOA.

## The e-Navigation VDES Controller Service

the GLA identified the provision of VAtoN as one of the top priority use-cases
for their future e-Navigation applications. The use-case requirements stated
that the transmission must be performed over AIS/VDES, therefore the
architecture should include a component that provides this capability.
Consequently, the **VDES Controller** microservice was introduced, which is
capable of interfacing with VDES modules like the CML Microcircuits VDES1000,
using a set of predefined UDP/IP ports. The application is therefore able to
transmit messages using the TSA/VDM (Transmit Slot Assignment/VHF Data-link
Message) and the BBM (Broadcast Binary Message) sentence protocols. **VDES
Controller** can receive the current VAtoN information by either polling or
subscribing to the **AtoN Service** microservice, and translates it to the
appropriate transmission format. For testing purposes, the service is also able
to send AIS messages using the Ettus E320 software-defined radio USRP platform
in a similar manner.

## How to use this image

This image can be used in two ways (based on the use or not of the Spring Cloud
Config server).
* Enabling the cloud config client and using the configurations located in an
  online repository.
* Disabling the cloud config client and using the configuration provided
  locally.

### Cloud Config Configuration

In order to run the image in a **Cloud Config** configuration, you just need
to provide the environment variables that allow is to connect to the cloud
config server. This is assumed to be provided the GRAD e-Navigation Service
Architecture
[Eureka Service](https://hub.docker.com/repository/docker/glarad/enav-eureka/).

The available environment variables are:

    ENAV_CLOUD_CONFIG_URI=<The URL of the eureka cloud configuration server>
    ENAV_CLOUD_CONFIG_BRANCH=<The cloud configuration repository branch to be used>
    ENAV_CLOUD_CONFIG_USERNAME=<The cloud configration server username>
    ENAV_CLOUD_CONFIG_PASSWORD=<The cloud configration server password>

The parameters will be picked up and used to populate the default
**bootstrap.properties** of the service that look as follows:

    server.port=8762
    spring.application.name=vdes-ctrl
    spring.application.version=0.0.3
   
    # The Spring Cloud Discovery Config
    spring.cloud.config.uri=${ENAV_CLOUD_CONFIG_URI}
    spring.cloud.config.username=${ENAV_CLOUD_CONFIG_USERNAME}
    spring.cloud.config.password=${ENAV_CLOUD_CONFIG_PASSWORD}
    spring.cloud.config.label=${ENAV_CLOUD_CONFIG_BRANCH}
    spring.cloud.config.fail-fast=false

As you can see, the service is called **vdes-ctrl** and uses the **8762**
port when running.

To run the image, along with the aforementioned environment variables, you can
use the following command:

    docker run -t -i --rm \
        -p 8762:8762 \
        -e ENAV_CLOUD_CONFIG_URI='<cloud config server url>' \
        -e ENAV_CLOUD_CONFIG_BRANCH='<cloud config config repository branch>' \
        -e ENAV_CLOUD_CONFIG_USERNAME='<config config repository username>' \
        -e ENAV_CLOUD_CONFIG_PASSWORD='<config config repository passord>' \
        <image-id>

### Local Config Configuration

In order to run the image in a **Local Config** configuration, you just need
to mount a local configuration directory that contains the necessary
**.properties** files (including bootstrap) into the **/conf** directory of the
image.

This can be done in the following way:

    docker run -t -i --rm \
        -p 8762:8762 \
        -v /path/to/config-directory/on/machine:/conf \
        <image-id>

Examples of the required properties files can be seen below.

For bootstrapping, we need to disable the cloud config client, and clear our the
environment variable inputs:

    server.port=8762
    spring.application.name=vdes-ctrl
    spring.application.version=<application.version>
    
    # Disable the cloud config
    spring.cloud.config.enabled=false
    
    # Clear out the environment variables
    spring.cloud.config.uri=
    spring.cloud.config.username=
    spring.cloud.config.password=
    spring.cloud.config.label=

While the application properties need to provide the service with an OAuth2.0
server like keycloak, logging configuration, the eureka client connection etc.:

    # Configuration Variables
    service.variable.eureka.server.name=<eureka.server.name>
    service.variable.eureka.server.port=<eureka.server.port>
    service.variable.keycloak.server.name=<keycloak.server.name>
    service.variable.keycloak.server.port=<keycloak.server.port>
    service.variable.keycloak.server.realm=<keycloak.realm>
    service.variable.database.server.name=<database.server.name>
    service.variable.database.server.port=<database.server.port>
    
    # Eureka Client Configuration
    eureka.client.service-url.defaultZone=http://${service.variable.eureka.server.name}:${service.variable.eureka.server.port}/eureka/
    eureka.client.registryFetchIntervalSeconds=5
    eureka.instance.preferIpAddress=true
    eureka.instance.leaseRenewalIntervalInSeconds=10
    eureka.instance.metadata-map.startup=${random.int}
    
    # Spring-boot Admin Configuration
    spring.boot.admin.client.url=http://${service.variable.server.eureka.name}:${service.variable.server.eureka.port}/admin
    
    # Logging Configuration
    logging.file.name=/var/log/${spring.application.name}.log
    logging.file.max-size=10MB
    logging.pattern.rolling-file-name=${spring.application.name}-%d{yyyy-MM-dd}.%i.log
    
    # Management Endpoints
    management.endpoint.logfile.external-file=/var/log/${spring.application.name}.log
    management.endpoints.web.exposure.include=*
    management.endpoint.health.show-details=always
    management.endpoint.httpexchanges.enabled=true
    management.endpoint.health.probes.enabled: true
    
    # Springdoc configuration
    springdoc.swagger-ui.path=/swagger-ui.html
    springdoc.packagesToScan=org.grad.eNav.vdesCtrl.controllers
    
    # Spring JPA Configuration - PostgreSQL
    spring.jpa.generate-ddl=true
    spring.jpa.hibernate.ddl-auto=update
    spring.jpa.hibernate.show-sql=true
    spring.jpa.properties.hibernate.search.backend.directory.root=./lucene/
    spring.jpa.properties.hibernate.search.schema_management.strategy=create-or-update
    spring.jpa.properties.hibernate.search.backend.analysis.configurer=class:org.grad.eNav.vdesCtrl.config.CustomLuceneAnalysisConfigurer
    
    # Datasource Configuration
    spring.datasource.url=jdbc:postgresql://${service.variable.database.server.name}:${service.variable.database.server.port}/vdes_controller
    spring.datasource.username=<changeit>
    spring.datasource.password=<changeit>
    
    ## Keycloak Configuration
    spring.security.oauth2.client.registration.keycloak.client-id=vdes-ctrl
    spring.security.oauth2.client.registration.keycloak.client-secret=<changeit>
    spring.security.oauth2.client.registration.keycloak.client-name=Keycloak
    spring.security.oauth2.client.registration.keycloak.provider=keycloak
    spring.security.oauth2.client.registration.keycloak.authorization-grant-type=authorization_code
    spring.security.oauth2.client.registration.keycloak.scope=openid
    spring.security.oauth2.client.registration.keycloak.redirect-uri={baseUrl}/login/oauth2/code/{registrationId}
    spring.security.oauth2.client.provider.keycloak.issuer-uri=http://${service.variable.keycloak.server.name}:${service.variable.keycloak.server.port}/realms/${service.variable.keycloak.server.realm}
    spring.security.oauth2.client.provider.keycloak.user-name-attribute=preferred_username
    spring.security.oauth2.resourceserver.jwt.issuer-uri=http://${service.variable.keycloak.server.name}:${service.variable.keycloak.server.port}/realms/${service.variable.keycloak.server.realm}
    
    # Feign
    feign.autoconfiguration.jackson.enabled=true
    feign.client.config.default.connectTimeout=60000
    feign.client.config.default.readTimeout=20000
    
    # Web Socket Configuration
    gla.rad.vdes-ctrl.web-socket.name=vdes-ctrl-websocket
    gla.rad.vdes-ctrl.web-socket.prefix=topic
    
    # VDES-1000 AIS Message Advertiser Configuration
    gla.rad.vdes-ctrl.vdes-1000-advertiser.enableSignatures=true
    
    # GRURadio AIS Message Advertiser Configuration
    gla.rad.vdes-ctrl.gr-ais-advertiser.ais-interval=1000
    gla.rad.vdes-ctrl.gr-ais-advertiser.enableSignatures=true
    gla.rad.vdes-ctrl.gr-ais-advertiser.destMmsi=111111111
    
    # Front-end Information
    gla.rad.vdes-ctrl.info.name=VDES Controller
    gla.rad.vdes-ctrl.info.version=${spring.application.version}
    gla.rad.vdes-ctrl.info.operatorName=Research and Development Directorate of GLA of UK and Ireland
    gla.rad.vdes-ctrl.info.operatorContact=Nikolaos.Vastardis@gla-rad.org
    gla.rad.vdes-ctrl.info.operatorUrl=https://www.gla-rad.org/
    gla.rad.vdes-ctrl.info.copyright=\u00A9 2023 GLA Research & Development

## Operation

Initially an AIS/VDES station like VDES1000 needs to be registered with the *
*VDES Controller** using the service’s own configuration web-interface (GUI).
The configuration parameters for each station are briefly presented in the
following table. In addition, each station needs to be allocated to a specific
area in order to activate its transmission capabilities over it. For each of the
defined stations, the **VDES Controller** will poll the **AtoN Service**
periodically (currently fixed to once every minute) and only the Virtual AtoNs
applicable to each station will be picked up and broadcasted over AIS/VDES.

| Parameter          | Description                                      | Mandatory |
|--------------------|--------------------------------------------------|-----------|
| Name               | The name of the station                          | Yes       |
| IP Address         | The IP address to contact the station            | Yes       |
| Port               | The TCP/UDP port to contact the station          | Yes       |
| Type               | The type of the station                          | Yes       |
| Channel            | The transmission channel to be used              | Yes       |
| Broadcast Port     | The broadcast port of the station (if present)   | No        |
| Forward IP Address | The IP to forward incoming messages to           | No        |
| Forward Port       | The TCP/UDP port to forward incoming messages to | No        |
| MMSI               | The MMSI of the station                          | Yes       |

Most parameters presented in the previous table are self-explanatory. For
example the “Name” parameter is a user defined identifier for that station,
while the “IP Address” and “Port” define how the service will contact a given
station. The “Type” parameter specifies the vendor-specific operation used to
communicate with the station and currently only supports the VDES1000 and USRP
equipment. The “Channel” and “MMSI” parameters are utilised to configure the
transmissions so that they make use of the appropriate AIS channel and Maritime
Mobile Service Identity (MMSI) respectively. The “Broadcast Port” and “Forward
IP Address”/” Forward Port” parameters on the other hand, do required some
additional clarifications. VDES1000 stations provide an additional UDP port to
allow monitoring of the transmitted and received messages. The “VDES Controller”
can allow access to that information through its GUI, if the “Broadcast Port” is
defined. In addition, the messages received by the service can be further
promulgated to other third-party applications through TCP/UDP, if the “Forward
IP Address” and “Forward Port” parameters are defined. This functionality can be
used for example to display the received AtoN messages to a chart plotting
facility like a software-based ECDIS or OpenCPN.

## Contributing
For contributing in this project, please have a look at the Github repository
[eNav-VDESController](https://github.com/gla-rad/eNav-VDESController). Pull
requests are welcome. For major changes, please open an issue first to discuss
what you would like to change.

Please make sure to update tests as appropriate.

## License
Distributed under the Apache License, Version 2.0.

## Contact
Nikolaos Vastardis -
[Nikolaos.Vastardis@gla-rad.org](mailto:Nikolaos.Vastardis@gla-rad.org)
