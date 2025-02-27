import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

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

import com.example.controller.UserController;
import com.example.dto.UserDto;
import com.example.exception.UserNotFoundException;
import com.example.handler.GlobalExceptionHandler;
import com.example.service.UserService;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;  // Mocking UserService

    @InjectMocks
    private UserController userController;  // Injecting mocks into UserController

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler()) // Handle exceptions
                .build();
    }

    // ✅ 1. Test: Should return 200 OK when all parameters are provided
    @Test
    void getActiveUsers_WithAllParams_ReturnsList() throws Exception {
        List<UserDto> userConfigs = Arrays.asList(
                new UserDto(1L, "Alice", "alice@example.com"),
                new UserDto(2L, "Bob", "bob@example.com")
        );

        when(userService.findArchiverConfigByNameIdActive("dataset1", "123", "Y"))
                .thenReturn(userConfigs);

        mockMvc.perform(get("/api/users/config")
                .param("datasetName", "dataset1")
                .param("datasetId", "123")
                .param("active", "Y"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Alice"))
                .andExpect(jsonPath("$[1].name").value("Bob"));

        verify(userService, times(1)).findArchiverConfigByNameIdActive("dataset1", "123", "Y");
    }

    // ✅ 2. Test: Should return 200 OK when parameters are missing (service handles defaults)
    @Test
    void getActiveUsers_MissingParams_UsesDefaultsAndReturnsOk() throws Exception {
        List<UserDto> userConfigs = Arrays.asList(
                new UserDto(1L, "Default", "default@example.com")
        );

        when(userService.findArchiverConfigByNameIdActive(null, null, null))
                .thenReturn(userConfigs);

        mockMvc.perform(get("/api/users/config"))  // No params provided
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].name").value("Default"));

        verify(userService, times(1)).findArchiverConfigByNameIdActive(null, null, null);
    }

    // ✅ 3. Test: Should return 404 when no configuration is found
    @Test
    void getActiveUsers_NoMatchingConfig_ReturnsNotFound() throws Exception {
        when(userService.findArchiverConfigByNameIdActive("dataset1", "123", "Y"))
                .thenThrow(new UserNotFoundException("No matching config found"));

        mockMvc.perform(get("/api/users/config")
                .param("datasetName", "dataset1")
                .param("datasetId", "123")
                .param("active", "Y"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("404"))
                .andExpect(jsonPath("$.message").value("No matching config found"))
                .andExpect(jsonPath("$.path").value("/api/users/config"));

        verify(userService, times(1)).findArchiverConfigByNameIdActive("dataset1", "123", "Y");
    }

    // ✅ 4. Test: Should return 500 Internal Server Error when an exception occurs
    @Test
    void getActiveUsers_NullPointerException_ReturnsInternalServerError() throws Exception {
        when(userService.findArchiverConfigByNameIdActive(any(), any(), any()))
                .thenThrow(new NullPointerException());

        mockMvc.perform(get("/api/users/config")
                .param("datasetName", "dataset1")
                .param("datasetId", "123")
                .param("active", "Y"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("500"))
                .andExpect(jsonPath("$.message").value(containsString("Unexpected null value encountered")))
                .andExpect(jsonPath("$.path").value("/api/users/config"));

        verify(userService, times(1)).findArchiverConfigByNameIdActive(any(), any(), any());
    }
}
