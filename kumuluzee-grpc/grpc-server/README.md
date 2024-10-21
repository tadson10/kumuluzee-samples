# Kumuluzee-grpc sample

## Requirements

In order to run this example you will need the following:
1. Java 8 (or newer), you can use any implementation:
    * If you have installed Java, you can check the version by typing the
    following in command line:
    ```bash
   java -version   
    ```
2. Maven 3.2.1 (or newer):
    * If you have installed Maven, you can check the version by typing the
    following in a command line:
    ```bash
    mvn -version
    ```
3. Git:
    * If you have installed Git, you can check the version by typing the
    following in a command line:
    ```bash
    git --version
    ```

## Usage

The example uses maven to build and run the microservice.

1. Build the sample using maven:
    ```bash
    cd kumuluzee-grpc-sample/grpc-server
    mvn clean package
    ```
2. Start local PostgreSQL DB:
    ```bash
    docker run -d --name postgres -e POSTGRES_DB=users -e POSTGRES_PASSWORD=postgres -e POSTGRES_HOST_AUTH_METHOD=password -p 5432:5432 postgres:latest
    ```
3. Run the sample:
    * Uber-jar:
    ```bash
    java -jar target/${project.build.finalName}.jar
    ```
    
    * Exploded:
    ```bash
    java -cp target/classes:target/dependency/* com.kumuluz.ee.EeApplication
    ```

The grpc server should be running on port specified in
the config.yml file (default 8443). To test if server is responding use provided
client sample:
* [grpc-client](../grpc-client)

## Tutorial

This tutorial will guide you through the steps required to initialize gRPC server
in KumuluzEE microservice.

### Add Maven dependencies

Add the `kumuluzee-core`, `kumuluzee-servlet-jetty`, `kumuluzee-cdi-weld`,
 `kumuluzee-jpa-eclipselink`, `postgresql` and `kumuluzee-grpc` dependency to the sample:

```xml
<dependencies>
        <dependency>
            <groupId>com.kumuluz.ee</groupId>
            <artifactId>kumuluzee-core</artifactId>
        </dependency>
        <dependency>
            <groupId>com.kumuluz.ee</groupId>
            <artifactId>kumuluzee-servlet-jetty</artifactId>
        </dependency>
        <dependency>
            <groupId>com.kumuluz.ee</groupId>
            <artifactId>kumuluzee-cdi-weld</artifactId>
        </dependency>
        <dependency>
            <groupId>com.kumuluz.ee</groupId>
            <artifactId>kumuluzee-jpa-eclipselink</artifactId>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <version>${postgres.version}</version>
        </dependency>
        <dependency>
            <groupId>io.grpc</groupId>
            <artifactId>grpc-services</artifactId>
            <version>${grpc.version}</version>
        </dependency>

        <dependency>
            <groupId>com.kumuluz.ee.grpc</groupId>
            <artifactId>kumuluzee-grpc</artifactId>
            <version>${kumuluzee.grpc.version}</version>
        </dependency>

    </dependencies>
```

Add the `kumuluzee-maven-plugin` build plugin to package microservice as uber-jar,
`protobuf-maven-plugin` plugin to generate java classes from `.proto` files and 
`os-maven-plugin` extension to let maven discover your OS so it can download the appropriate
compiler for Protobuf files.

```xml
<build>
    <extensions>
        <extension>
            <groupId>kr.motd.maven</groupId>
            <artifactId>os-maven-plugin</artifactId>
            <version>1.5.0.Final</version>
        </extension>
    </extensions>
    <plugins>
        <plugin>
            <groupId>org.xolstice.maven.plugins</groupId>
            <artifactId>protobuf-maven-plugin</artifactId>
            <version>0.5.1</version>
            <configuration>
                <protocArtifact>com.google.protobuf:protoc:3.5.1-1:exe:${os.detected.classifier}</protocArtifact>
                <pluginId>grpc-java</pluginId>
                <pluginArtifact>io.grpc:protoc-gen-grpc-java:1.14.0:exe:${os.detected.classifier}</pluginArtifact>
            </configuration>
            <executions>
                <execution>
                    <goals>
                        <goal>compile</goal>
                        <goal>compile-custom</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
        <plugin>
            <groupId>com.kumuluz.ee</groupId>
            <artifactId>kumuluzee-maven-plugin</artifactId>
            <version>${kumuluzee.version}</version>
            <executions>
                <execution>
                    <id>package</id>
                    <goals>
                        <goal>repackage</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

If you prefer exploded version change `goal` in `kumuluzee-maven-plugin` to `copy-dependencies`.

### Define proto file

Define `.proto` file with service and messages definition. More about Protobuf files
can be found on [Google Developers](https://developers.google.com/protocol-buffers/). Place your proto files
in "proto" directory so the maven plugin can detect it and compile correspondent Java classes.

```proto
syntax = "proto3";
option java_package = "grpc";

import "google/protobuf/empty.proto";

service User {
    rpc getUser(UserRequest) returns (UserResponse) {};
    rpc getUsersServerStreaming(google.protobuf.Empty) returns (stream UserResponse) {};
    rpc getUsersClientStreaming(stream UserRequest) returns (UserListResponse) {};
    rpc getUsersBidirectionalStreaming(stream UserRequest) returns (stream UserResponse) {};
}

message UserRequest {
    int32 id = 1;
}

message UserResponse {
    int32 id = 1;
    string name = 2;
    string surname = 3;
}

message UserListResponse {
    repeated UserResponse users = 1;
}
```

### Implement entity and service bean

Implement entity object that holds data about `Account`:
```java
package entity;

import javax.persistence.*;

@Entity
@Table(name = "account")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    private String surname;

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
```

Service bean that returns details for account with provided id:

```java
package beans;

import entity.User;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.logging.Logger;

@ApplicationScoped
public class UserBean {

    private static final Logger logger = Logger.getLogger(UserBean.class.getName());

    @PersistenceContext(name = "sample-user-jpa")
    private EntityManager em;

    @PostConstruct
    private void init() {
        logger.info("UserBean initialized");
    }

    public User getUser(Integer id) {
        return em.find(User.class, id);
    }

}
```

Make sure that `META-INF/persistence.xml` and `META-INF/beans.xml` files are present.

persistence.xml
```xml
<?xml version="1.0" encoding="UTF-8" ?>
<persistence xmlns="http://xmlns.jcp.org/xml/ns/persistence"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/persistence
             http://xmlns.jcp.org/xml/ns/persistence/persistence_2_1.xsd"
             version="2.1">
    <persistence-unit name="sample-user-jpa" transaction-type="JTA">

        <jta-data-source>jdbc/UsersDB</jta-data-source>

        <class>entity.User</class>

        <properties>
            <property name="javax.persistence.schema-generation.database.action" value="drop-and-create"/>
            <property name="javax.persistence.schema-generation.create-source" value="metadata"/>
            <property name="javax.persistence.sql-load-script-source" value="sql-script/init-db.sql"/>
            <property name="javax.persistence.schema-generation.drop-source" value="metadata"/>
        </properties>
    </persistence-unit>
</persistence>
```

beans.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://xmlns.jcp.org/xml/ns/javaee" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/beans_1_2.xsd"
       bean-discovery-mode="annotated">
</beans>
```

Database initialization sql script:

`sql-script/init-db.sql`
```sql
INSERT INTO account (name, surname) VALUES ('Primoz', 'Hrovat');
```

### Implement services

You should implement services defined in your `.proto` files.

Example:
```java
@GrpcService(interceptors = {
        @GrpcInterceptor(name = "grpc.interceptors.HeaderInterceptor2"),
        @GrpcInterceptor(name = "grpc.interceptors.HeaderInterceptor")},
        secured = true,
        resourceName = "user-grpc")
public class UserServiceImpl extends UserGrpc.UserImplBase {

    private static final Logger logger = Logger.getLogger(UserServiceImpl.class.getName());

    private UserBean userBean;

    @RolesAllowed({"Admin"})
    @Override
    public void getUser(UserService.UserRequest request, StreamObserver<UserService.UserResponse> responseObserver) {

        userBean = CDI.current().select(UserBean.class).get();
        User user = userBean.getUser(request.getId());
        UserService.UserResponse response;

        if (user != null) {
            response = UserService.UserResponse.newBuilder()
                    .setId(user.getId())
                    .setName(user.getName())
                    .setSurname(user.getSurname())
                    .build();
            responseObserver.onNext(response);
        }

        responseObserver.onCompleted();
    }

  @RolesAllowed({"Manager"})
  @Override
  public void getUsersServerStreaming(Empty request, StreamObserver<UserService.UserResponse> responseObserver) {
      // CODE
  }
  
  @PermitAll
  @Override
  public StreamObserver<UserService.UserRequest> getUsersClientStreaming(StreamObserver<UserService.UserListResponse> responseObserver) {
        // CODE
  }

  @DenyAll
  @Override
  public StreamObserver<UserService.UserRequest> getUsersBidirectionalStreaming(StreamObserver<UserService.UserResponse> responseObserver) {
        // CODE
  }
}
```

And annotate it with `@GrpcService` so KumuluzEE can automatically discover and bind your
implementation to server. If using server interceptors they should be provided for 
each service with their full class name inside `@GrpcService` annotation using `@GrpcInterceptor`.
Each service can be additionally secured with JWT token with `secured` value set to true.
If we are using secured service with Keycloak generated JWT, we need to provide `resourceName`. 
Keycloak will add client roles in `resource_access` for each resource separately.

When implementing secured service, we need to add `@RolesAllowed`, `@PermitAll` or `@DenyAll` annotations
to each service method.

**NOTE:** CDI injection currenty doesn't work inside service implementations. Lookup must
be done manually using
```java
CDI.current().select(UserBean.class).get();
```

### Add configuration

Add required configuration (database, grpc):
```yaml
kumuluzee:
  name: "grpc-server"
  server:
    http:
      port: 8080
  datasources:
    - jndi-name: jdbc/UsersDB
      connection-url: jdbc:postgresql://192.168.99.100:5432/users
      username: postgres
      password: postgres
  grpc:
    server:
      url: localhost
      http:
        port: 8081
      auth:
        public-key: MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDW6Angsf0Ry+GFD5HPstdcuaHJU5KhpT+gkzCCx7zZAbKRaEQexaTA9nPXK2Uzk2JqWTbZXSQYX2kBzYeiiedMpW6wvTaZWL9QhGjEnA9o97oNV1G5wQHKL/8FsvLXqt/81BCeZzWPDGvLNuU9l0qK3/xXL3efaZYPsZkB2AyZiQIDAQAB
        issuer: http://localhost
```
The supplied public key can be in any of the following formats:
- PKCS#8 PEM
- JWK
- JWKS
- Base64 URL encoded JWK
- Base64 URL encoded JWKS


We can also use JWKS server to get public keys for JWT verification. In this case we need to provide JWKS server URL instead of public key.
```yaml
grpc:
    server:
        auth:
            jwks-uri: http://localhost:8080/jwks
            issuer: http://localhost
```

We can use even Keycloak server to get public keys for JWT verification. In this case we need to provide Keycloak server URL instead of public key.
```yaml
grpc:
    server:
        auth:
            keycloak-jwks-uri: http://localhost:8090/realms/master/protocol/openid-connect/certs
            issuer: http://localhost
```

All possible extensions are shown in the example below.
```yaml
kumuluzee:
  name: "grpc-server"
  grpc:
    server:
      http:
        port: 8081
      https:
        enable: true
        port: 8443
        certFile: /path/to/cert/file
        keyFile: /path/to/key/file
        chainFile: /path/to/chain/file
        mutualTLS: optional
      conf:
        permitKeepAliveTime: 60000
        permitKeepAliveWithoutCalls: true
        keepAliveTimeout: 60000
        keepAliveTime: 60000
        maxConnectionIdle: 10000
        maxConnectionAge: 20000
        maxConnectionAgeGrace: 10000
      health:
        healthCheckEnabled: true   
      reflection:
        reflectionEnabled: true        
      auth:
        maximum-leeway: 100        
        public-key: MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnOTgnGBISzm3pKuG8QXMVm6eEuTZx8Wqc8D9gy7vArzyE5QC/bVJNFwlz...
        issuer: http://localhost
```


### Build the microservice and run it

To build the microservice and run the example use the commands as described in previous section.


