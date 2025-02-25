import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService; // Mocked service

    @InjectMocks
    private UserController userController; // Controller under test

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    // ✅ Test: Valid user ID should return 200 OK
    @Test
    void getUser_ValidId_ReturnsUser() throws Exception {
        Long userId = 1L;
        UserDto userDto = new UserDto(userId, "Alice", "alice@example.com");

        when(userService.getUserById(userId)).thenReturn(userDto); // Mock service response

        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.email").value("alice@example.com"));

        verify(userService, times(1)).getUserById(userId);
    }

    // ✅ Test: Invalid user ID (negative) should return 400 Bad Request
    @Test
    void getUser_InvalidId_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/users/{id}", -1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("400"))
                .andExpect(jsonPath("$.message").value("User ID must be greater than 0"))
                .andExpect(jsonPath("$.path").value("/api/users/-1"));
    }

    // ✅ Test: User not found should return 404
    @Test
    void getUser_UserNotFound_ReturnsNotFound() throws Exception {
        Long userId = 99L;
        when(userService.getUserById(userId)).thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("404"))
                .andExpect(jsonPath("$.message").value("User not found"))
                .andExpect(jsonPath("$.path").value("/api/users/99"));
    }
}
