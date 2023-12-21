package com.dolph.blog.unit.services;

    import com.dolph.blog.dto.user.NewUserRequest;
    import com.dolph.blog.models.User;
    import com.dolph.blog.repository.UserRepo;
    import com.dolph.blog.services.UserService;
    import com.dolph.blog.utils.EmailSender;
    import org.junit.jupiter.api.BeforeEach;
    import org.junit.jupiter.api.Test;
    import org.mockito.InjectMocks;
    import org.mockito.Mock;
    import org.mockito.MockitoAnnotations;

    import static org.assertj.core.api.Assertions.assertThat;
    import static org.mockito.Mockito.*;

public class UserServiceTest {

  @Mock
  private UserRepo userRepo;

  @Mock
  private EmailSender emailSender;

  @InjectMocks
  private UserService userService;

  @BeforeEach
  public void setUp(){
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void testCreateUser(){
    NewUserRequest newUserRequest = new NewUserRequest();
    newUserRequest.setOtp("80191");
    newUserRequest.setFullname("John Doe");
    newUserRequest.setOtpExpiry(String.valueOf(123456789L));
    newUserRequest.setEmail("john@example.com");
    newUserRequest.setBio("a new bio");
    newUserRequest.setPassword("randomPassword2");
    when(userRepo.save(any(User.class))).thenReturn(new User());

    String userId = userService.createUser(newUserRequest);
    assertThat(userId).isNotNull();
    verify(userRepo, times(1)).save(any(User.class));
  }
}
