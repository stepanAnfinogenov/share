import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;  // Mocking the UserService

    @InjectMocks
    private UserController userController;  // Injecting the mock into the controller

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler()) // Ensure exception handling works
                .build();
    }

    // ✅ 1. Test: Should return 200 OK with active users
    @Test
    void getActiveUsers_ReturnsListOfUsers() throws Exception {
        List<UserDto> activeUsers = Arrays.asList(
                new UserDto(1L, "Alice", "alice@example.com"),
                new UserDto(2L, "Bob", "bob@example.com")
        );

        when(userService.getActiveUsers()).thenReturn(activeUsers);

        mockMvc.perform(get("/api/users/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Alice"))
                .andExpect(jsonPath("$[1].name").value("Bob"));

        verify(userService, times(1)).getActiveUsers();
    }

    // ✅ 2. Test: Should return 404 when no active users exist
    @Test
    void getActiveUsers_NoUsersFound_ReturnsNotFound() throws Exception {
        when(userService.getActiveUsers()).thenThrow(new UserNotFoundException("No active users found"));

        mockMvc.perform(get("/api/users/active"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("404"))
                .andExpect(jsonPath("$.message").value("No active users found"))
                .andExpect(jsonPath("$.path").value("/api/users/active"));

        verify(userService, times(1)).getActiveUsers();
    }

    // ✅ 3. Test: Should return 500 Internal Server Error when NPE occurs
    @Test
    void getActiveUsers_NullPointerException_ReturnsInternalServerError() throws Exception {
        when(userService.getActiveUsers()).thenThrow(new NullPointerException());

        mockMvc.perform(get("/api/users/active"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("500"))
                .andExpect(jsonPath("$.message").contains("Unexpected null value encountered"))
                .andExpect(jsonPath("$.path").value("/api/users/active"));

        verify(userService, times(1)).getActiveUsers();
    }
}
