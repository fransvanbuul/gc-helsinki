# gc-helsinki
GiftCard Axon demo application,
fully written in Kotlin.

## Running this application

This application is designed to run in several modes:

### 1. Running as a stand-alone monolith

Just run the application; it's configuration 
as committed to the repository is like that. It
will run with a SimpleCommandBus, SimpleQueryBus
and an EmbeddedEventStore (backed by JPA+H2) as
EventBus.

### 2. Running locally with AxonHub and AxonDB

Edit pom.xml, comment in the dependency on 
io.axoniq:axonhub-spring-boot-autoconfigure. 
Download the free developer editions of AxonDB and AxonHub from
https://axoniq.io, and run them.

Now, you still have one instance of your application
but it will use an AxonHubCommandBus, AxonHubQueryBus
and AxonHubEventBus, and all communication will go through
AxonHub.

### 3. Running as a microservices system

You can create many instances of the application and run them in
parallel. There are a few properties in application.properties that
allow you to enable or disable the GUI, Command-Side and Query-Side
separately. 

The free Developer editions of AxonDB and AxonHub run a single nodes
only. If you want to experiment with more advanced configs like AxonDB
and AxonHub cluster, please reach out.

## Questions?

Feel free to reach out at frans dot vanbuul at axoniq dot io
 








