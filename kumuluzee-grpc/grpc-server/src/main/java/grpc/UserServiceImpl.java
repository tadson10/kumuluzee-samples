package grpc;

import com.google.protobuf.Empty;
import com.kumuluz.ee.grpc.annotations.GrpcInterceptor;
import com.kumuluz.ee.grpc.annotations.GrpcService;
import com.kumuluz.ee.grpc.server.GrpcServer;
import io.grpc.Context;
import io.grpc.Status;
import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.protobuf.services.HealthStatusManager;
import io.grpc.stub.StreamObserver;
import beans.UserBean;
import entity.User;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.inject.spi.CDI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@GrpcService(interceptors = {
        @GrpcInterceptor(name = "grpc.interceptors.HeaderInterceptor2"),
        @GrpcInterceptor(name = "grpc.interceptors.HeaderInterceptor")},
        secured = true,
        resourceName = "user-grpc")
public class UserServiceImpl extends UserGrpc.UserImplBase {

  private static final Logger logger = Logger.getLogger(UserServiceImpl.class.getName());

  private UserBean userBean;

  public void testFunction() {
    System.out.println("test");
  }

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
          .setPhoneNumber(user.getPhoneNumber())
          .setSex(user.getSex() != null && user.getSex().equals("MALE") ? UserService.Sex.MALE : UserService.Sex.FEMALE)
          .putPreferences("key1", "value1")
          .build();

      responseObserver.onNext(response);
    }

    responseObserver.onCompleted();
  }

  @RolesAllowed({"Manager"})
  @Override
  public void getUsersServerStreaming(Empty request, StreamObserver<UserService.UserResponse> responseObserver) {
    HealthStatusManager healthStatusManager = GrpcServer.getInstance().getHealthStatusManager();
    healthStatusManager.setStatus("User", HealthCheckResponse.ServingStatus.SERVING);
    healthStatusManager.setStatus("Test", HealthCheckResponse.ServingStatus.NOT_SERVING);

    userBean = CDI.current().select(UserBean.class).get();
    List<User> users = userBean.getAllUsers();
    UserService.UserResponse response;

    // Check if the call is cancelled by the client
    if (Context.current().isCancelled()) {
      responseObserver.onError(Status.CANCELLED.withDescription("Cancelled by client").asRuntimeException());
      return;
    }

    if (users != null) {
      for (User user : users) {
        response = UserService.UserResponse.newBuilder()
            .setId(user.getId())
            .setName(user.getName())
            .setSurname(user.getSurname())
            .build();

        responseObserver.onNext(response);
      }
    }

    responseObserver.onCompleted();
  }

  @PermitAll
  @Override
  public StreamObserver<UserService.UserRequest> getUsersClientStreaming(StreamObserver<UserService.UserListResponse> responseObserver) {
    return new StreamObserver<UserService.UserRequest>() {
      List<UserService.UserResponse> users = new ArrayList<>();
      Integer count = 0;

      @Override
      public void onNext(UserService.UserRequest userRequest) {
        userBean = CDI.current().select(UserBean.class).get();
        User user = userBean.getUser(userRequest.getId());

        if (user != null) {
          logger.warning("onNext: " + user.getName());
          count++;

          UserService.UserResponse response = UserService.UserResponse
              .newBuilder()
              .setId(user.getId())
              .setName(user.getName())
              .setSurname(user.getSurname())
              .build();

          users.add(response);
        }
      }

      @Override
      public void onError(Throwable throwable) {
        logger.warning("Error retrieving user");
        throwable.printStackTrace();
      }

      @Override
      public void onCompleted() {
        logger.warning("onCompleted: " + users.size());

        responseObserver.onNext(UserService.UserListResponse
            .newBuilder()
            .addAllUsers(users)
            .build());
        responseObserver.onCompleted();
      }
    };
  }

  @DenyAll
  @Override
  public StreamObserver<UserService.UserRequest> getUsersBidirectionalStreaming(StreamObserver<UserService.UserResponse> responseObserver) {
    return new StreamObserver<UserService.UserRequest>() {
      List<User> users = new ArrayList<>();
      Integer count = 0;

      @Override
      public void onNext(UserService.UserRequest userRequest) {
        userBean = CDI.current().select(UserBean.class).get();
        User user = userBean.getUser(userRequest.getId());

        if (user != null) {
          logger.warning("onNext: " + user.getName());
          count++;
          users.add(user);
          UserService.UserResponse response = UserService.UserResponse
              .newBuilder()
              .setId(user.getId())
              .setName(user.getName())
              .setSurname(user.getSurname())
              .build();

          responseObserver.onNext(response);
        }
      }

      @Override
      public void onError(Throwable throwable) {
        logger.warning("Error retrieving user");
        throwable.printStackTrace();
      }

      @Override
      public void onCompleted() {
        logger.warning("onCompleted: " + users.size());
        responseObserver.onCompleted();
      }
    };
  }
}
