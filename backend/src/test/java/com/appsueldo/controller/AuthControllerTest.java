package com.appsueldo.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.appsueldo.dto.RegisterRequest;
import com.appsueldo.entity.User;
import com.appsueldo.exception.ApiExceptionHandler;
import com.appsueldo.exception.BadRequestException;
import com.appsueldo.exception.ConflictException;
import com.appsueldo.security.AuthCookieService;
import com.appsueldo.security.JwtService;
import com.appsueldo.service.AuthService;
import com.appsueldo.service.CurrentUserService;
import com.appsueldo.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletResponse;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

class AuthControllerTest {

    private FakeAuthService authService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        authService = new FakeAuthService();
        AuthController controller = new AuthController(
            authService,
            new FakeJwtService(),
            new FakeRefreshTokenService(),
            new FakeAuthCookieService(),
            new CurrentUserService(null, authService)
        );
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new ApiExceptionHandler())
            .setValidator(validator)
            .build();
    }

    @Test
    void registerCreatesUserAndSetsAuthCookies() throws Exception {
        authService.registerHandler = request -> localUser(request.name(), request.email());

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRegisterJson()))
            .andExpect(status().isOk())
            .andExpect(result -> assertThat(result.getResponse().getHeaders(HttpHeaders.SET_COOKIE))
                .anySatisfy(header -> assertThat(header).contains("access_token=access-token"))
                .anySatisfy(header -> assertThat(header).contains("refresh_token=refresh-token")))
            .andExpect(jsonPath("$.user.name").value("Test User"))
            .andExpect(jsonPath("$.user.email").value("test@appsueldo.local"))
            .andExpect(jsonPath("$.user.authProviders[0]").value("LOCAL"));
    }

    @Test
    void registerRejectsDuplicatedEmailWithClearMessage() throws Exception {
        authService.registerHandler = request -> {
            throw new ConflictException("Ese email ya esta registrado.");
        };

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRegisterJson()))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value("Ese email ya esta registrado."));
    }

    @Test
    void registerRejectsInvalidRequestWithClearMessage() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "",
                      "email": "not-an-email",
                      "password": "password123"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("La solicitud contiene datos invalidos."));
    }

    @Test
    void registerRejectsInvalidPasswordWithClearMessage() throws Exception {
        authService.registerHandler = request -> {
            throw new BadRequestException("La contrasena debe incluir letras y numeros.");
        };

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRegisterJson()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("La contrasena debe incluir letras y numeros."));
    }

    @Test
    void registerMapsDataIntegrityViolationToDuplicatedEmailMessage() throws Exception {
        authService.registerHandler = request -> {
            throw new DataIntegrityViolationException("duplicate key value violates unique constraint");
        };

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRegisterJson()))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value("Ese email ya esta registrado."));
    }

    @Test
    void registerMapsUnexpectedErrorsToJsonFallback() throws Exception {
        authService.registerHandler = request -> {
            throw new IllegalStateException("JWT encoder failed");
        };

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRegisterJson()))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.message").value("Error interno del servidor."));
    }

    private User localUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash("$2a$10$hashed");
        user.setEmailVerified(false);
        return user;
    }

    private String validRegisterJson() {
        return """
            {
              "name": "Test User",
              "email": "test@appsueldo.local",
              "password": "password123"
            }
            """;
    }

    private static class FakeAuthService extends AuthService {
        private Function<RegisterRequest, User> registerHandler = request -> {
            throw new IllegalStateException("Register handler was not configured.");
        };

        FakeAuthService() {
            super(null, null);
        }

        @Override
        public User register(RegisterRequest request) {
            return registerHandler.apply(request);
        }
    }

    private static class FakeJwtService extends JwtService {
        FakeJwtService() {
            super(null, 900000);
        }

        @Override
        public String createAccessToken(User user) {
            return "access-token";
        }
    }

    private static class FakeRefreshTokenService extends RefreshTokenService {
        FakeRefreshTokenService() {
            super(null, 2592000000L);
        }

        @Override
        public CreatedRefreshToken create(User user) {
            return new CreatedRefreshToken("refresh-token", null);
        }
    }

    private static class FakeAuthCookieService extends AuthCookieService {
        FakeAuthCookieService() {
            super(false, "Lax", 900000, 2592000000L);
        }

        @Override
        public void addAuthCookies(HttpServletResponse response, String accessToken, String refreshToken) {
            response.addHeader(HttpHeaders.SET_COOKIE, "access_token=" + accessToken + "; HttpOnly");
            response.addHeader(HttpHeaders.SET_COOKIE, "refresh_token=" + refreshToken + "; HttpOnly");
        }
    }
}
