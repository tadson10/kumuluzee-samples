package client;

import com.google.protobuf.Empty;
import com.kumuluz.ee.grpc.client.GrpcChannelConfig;
import com.kumuluz.ee.grpc.client.GrpcChannels;
import com.kumuluz.ee.grpc.client.GrpcClient;
import com.kumuluz.ee.grpc.client.JWTClientCredentials;
import io.grpc.StatusRuntimeException;
import io.grpc.health.v1.HealthCheckRequest;
import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.health.v1.HealthGrpc;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.StreamObserver;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.net.ssl.SSLException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.IntStream;

@ApplicationScoped
public class UserServiceClient {
  private final static Logger logger = Logger.getLogger(UserServiceClient.class.getName());
  // Token for JWT token verification with JWKS
  private final String JWT_TOKEN = "eyJhbGciOiJSUzI1NiIsImtpZCI6ImNkUElURFJyZjk0Sm9TVTZPVEk3WHZ2Y1FZMUgxVHBibXJCU3BQd2hsWWsiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjE3NTYxMTc2OTMsImlhdCI6MTcxOTMxODQ5MywiaXNzIjoiaHR0cDovL2xvY2FsaG9zdCIsInJvbGVzIjpbIkFkbWluIiwiVXNlciJdfQ.mD2UXfsAg3cWn2YifB-YdgYNKFAhEQTE-FO_i44f1gSXlr0lKeOUlZ8Hx-HOVpttPafXkF-AC9bfCZO8mw1P1R-w53OcMPh5O5UqPonBFNs3lYGfBenb_NWtRJ5D7CnMtDScbbVBxZ1Bch52606sFM9bv-iTFnKCdjfYccipAm0lnmEvjlu1fQAR4x08KRpKZ6pyVlUQiFg9eO2iwR7ReRaxY2bgehzFfBCpqzwHztQX40bZSs7M6HgEkezM_4X0MjnoZZfPe_4HTm4R73jnd7glAa3PIF_SGQeN0JKn_MxVwi-ZTE_n_Xjjy69UDchYcINSG8685Z_DKnaig9NXUw";
  // Token for PEM public key verification
//  private final String JWT_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJyb2xlcyI6WyJBZG1pbiIsIlVzZXIiLCJHdWVzdCJdLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0IiwiaWF0IjoxNzEwNzYyMDE5fQ.QaHHcg8u2QIHy_oU9UopD7_sl7G7ided5LDT3wxqCvkHClpd3iwdtJ2R41qox7O4u64Ux7JSb-zl6UT7X5wAJHk0drliMzlXFmpmBRPVd9xVDEEUFUy0KHpEufjsatPdZZYMP4rLMzC4Eg6mETF1GqUi2b_5ggqLkArTbZ64KY9oJqKMU7A__cCJgG4Y6iGFXm4VIE4wL9qQr5RTyOSW6tLJWJv3NbSSUTIiZaY0KjiZfd1MF3Ld1LV_3sD8J9UgrQb58a2ZFa99siejB6veDpS_-_pXPIDp2jU1yYQLunA0zdK_E87TX1lpC58PNqiWc7zahg6H9EsELuQccJW66A";
  private UserGrpc.UserStub stub;
  private HealthGrpc.HealthBlockingStub healthBlockingStub;
  private HealthCheckRequest healthRequest;
  @PostConstruct
  public void init() {
    try {
      GrpcChannels clientPool = GrpcChannels.getInstance();
      GrpcChannelConfig config = clientPool.getGrpcClientConfig("client1");
      GrpcClient client = new GrpcClient(config);

      stub = UserGrpc.newStub(client.getChannel()).withCallCredentials(new JWTClientCredentials(JWT_TOKEN));
      healthBlockingStub = HealthGrpc.newBlockingStub(client.getChannel());
    } catch (SSLException e) {
      logger.warning(e.getMessage());
    }
  }

  public HealthCheckResponse.ServingStatus checkHealth(String service) {
    try {
      healthRequest = HealthCheckRequest.newBuilder().setService(service).build();
      HealthCheckResponse response =
          healthBlockingStub.check(healthRequest);
      logger.info(service + ", current health is: " + response.getStatus());
      return response.getStatus();
    } catch (StatusRuntimeException e) {
      logger.warning("Error checking health");
      e.printStackTrace();
      return HealthCheckResponse.ServingStatus.SERVICE_UNKNOWN;
    }
  }

  public void getUser(Integer id) {
    UserService.UserRequest request = UserService.UserRequest.newBuilder().setId(id).build();

    stub.getUser(request, new StreamObserver<UserService.UserResponse>() {
      @Override
      public void onNext(UserService.UserResponse userResponse) {
        logger.info(userResponse.getName() + " " + userResponse.getSurname());
      }

      @Override
      public void onError(Throwable throwable) {
        logger.warning("Error retrieving user");
        throwable.printStackTrace();
      }

      @Override
      public void onCompleted() {
        logger.info("Completed");
      }
    });
  }

  public void getUsersServerStreaming() {
    Empty request = Empty.newBuilder().build();

    stub.getUsersServerStreaming(request, new StreamObserver<UserService.UserResponse>() {
      @Override
      public void onNext(UserService.UserResponse userResponse) {
        logger.info(userResponse.getName() + " " + userResponse.getSurname());
      }

      @Override
      public void onError(Throwable throwable) {
        logger.warning("Error retrieving user");
        throwable.printStackTrace();
      }

      @Override
      public void onCompleted() {
        logger.info("Completed");
        HealthCheckResponse.ServingStatus status = checkHealth("User");
      }
    });
  }

  public void getUsersClientStreaming() {
    StreamObserver<UserService.UserRequest> requestStreamObserver =
        stub.getUsersClientStreaming(new StreamObserver<UserService.UserList>() {
          @Override
          public void onNext(UserService.UserList userListResponse) {
            logger.info("onNext: " + userListResponse.getUsersCount());
            for (UserService.UserResponse user : userListResponse.getUsersList()) {
              System.out.println("User ID: " + user.getId() + ", Name: " + user.getName());
            }
          }

          @Override
          public void onError(Throwable throwable) {
            logger.warning("Error retrieving user");
            throwable.printStackTrace();
          }

          @Override
          public void onCompleted() {
            logger.info("Completed");
          }
        });

    logger.info("Sending requests");

    IntStream.range(1, 5).mapToObj(n -> UserService.UserRequest
            .newBuilder()
            .setId(n)
            .build())
        .forEach(requestStreamObserver::onNext);

    requestStreamObserver.onCompleted();

  }

  public void getUsersBidirectionalStreaming() {
    StreamObserver<UserService.UserRequest> requestStreamObserver =
        stub
            .withDeadlineAfter(10, TimeUnit.SECONDS)
            .getUsersBidirectionalStreaming(new StreamObserver<UserService.UserResponse>() {
              @Override
              public void onNext(UserService.UserResponse userResponse) {
                logger.info("onNext: " + userResponse.getName() + " " + userResponse.getSurname());
              }

              @Override
              public void onError(Throwable throwable) {
                logger.warning("Error retrieving user");
                throwable.printStackTrace();
              }

              @Override
              public void onCompleted() {
                logger.info("Completed");
              }
            });

    logger.info("Sending requests");

    IntStream.range(1, 5).mapToObj(n -> UserService.UserRequest
            .newBuilder()
            .setId(n)
            .build())
        .forEach(requestStreamObserver::onNext);

    requestStreamObserver.onCompleted();

  }

  public void getUsersBidirectionalStreamingCancellation() {
    ClientCallStreamObserver<UserService.UserRequest> requestStreamObserver =
        (ClientCallStreamObserver<UserService.UserRequest>) stub.withCompression("gzip")
//            .withDeadlineAfter(10, TimeUnit.SECONDS)
            .getUsersBidirectionalStreamingCancellation(new StreamObserver<UserService.UserResponse>() {
              @Override
              public void onNext(UserService.UserResponse userResponse) {
                logger.info("onNext: " + userResponse.getName() + " " + userResponse.getSurname());
              }

              @Override
              public void onError(Throwable throwable) {
                logger.warning("Error retrieving user");
                throwable.printStackTrace();
              }

              @Override
              public void onCompleted() {
                logger.info("Completed");
              }
            });

    logger.info("Sending requests");

    IntStream.range(1, 5).mapToObj(n -> UserService.UserRequest
            .newBuilder()
            .setId(n)
            .build())
        .forEach(id -> {
              logger.info("Sending request: " + id.getId());
              if (id.getId() == 3) {
                requestStreamObserver.cancel("Call cancelled", new StatusRuntimeException(io.grpc.Status.CANCELLED));
                requestStreamObserver.onError(new StatusRuntimeException(io.grpc.Status.CANCELLED));
              }
              requestStreamObserver.onNext(id);
            }
        );

    requestStreamObserver.onCompleted();

  }

}
