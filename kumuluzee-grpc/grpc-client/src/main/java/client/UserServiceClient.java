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
//  private final String JWT_TOKEN = "eyJhbGciOiJSUzI1NiIsImtpZCI6InRzMmN2NVZqcmY1MVZ1TEdKMzhEVldsMjNVYm93Y2VvcHpRb1ZfMHY3dU0iLCJ0eXAiOiJKV1QifQ.eyJleHAiOjE3NjEwNTcwMTksImlhdCI6MTcyOTUyODIxOSwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdCIsInJlc291cmNlX2FjY2VzcyI6eyJ1c2VyLWdycGMiOnsicm9sZXMiOlsiQWRtaW4iXX19LCJyb2xlcyI6WyJBZG1pbiIsIlVzZXIiXX0.G_qw81KWbe6GoxOfPYpfB7x_FKsLI5BdtevQAaSe901qR0ymgZTSLeNrN-AIqnKVHZ_GDE5F78CkhmqR7fhyIX2fXdzcBLKWZETPp6-mvN3bFSvvO4ldsBpqnsxMOm8AbUBBZGCbjBK2xpvTxRXEfpX8oOCdBHbgRnCFWRdFZPEDFblS5AP_aXK8xzb0KAqmCDO0SUDuBbOqDqTo9rcwCLWnk7fOPOejGY35Vw764l7NJT5lbiJJ0isihSnS2Z3NHKKdnb-8_i1lhgphezo_xTYQYvnVacLeVt6zHzLF_3wpjCz7PtxdircUFirfn7hrac7CwZ0WPzIOcAfOSAmyXw";
  // Token for PEM public key verification
  private final String JWT_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJyb2xlcyI6WyJBZG1pbiIsIlVzZXIiLCJHdWVzdCJdLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0IiwiaWF0IjoxNzEwNzYyMDE5fQ.QaHHcg8u2QIHy_oU9UopD7_sl7G7ided5LDT3wxqCvkHClpd3iwdtJ2R41qox7O4u64Ux7JSb-zl6UT7X5wAJHk0drliMzlXFmpmBRPVd9xVDEEUFUy0KHpEufjsatPdZZYMP4rLMzC4Eg6mETF1GqUi2b_5ggqLkArTbZ64KY9oJqKMU7A__cCJgG4Y6iGFXm4VIE4wL9qQr5RTyOSW6tLJWJv3NbSSUTIiZaY0KjiZfd1MF3Ld1LV_3sD8J9UgrQb58a2ZFa99siejB6veDpS_-_pXPIDp2jU1yYQLunA0zdK_E87TX1lpC58PNqiWc7zahg6H9EsELuQccJW66A";
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
        stub.getUsersClientStreaming(new StreamObserver<UserService.UserListResponse>() {
          @Override
          public void onNext(UserService.UserListResponse userListResponse) {
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
}
