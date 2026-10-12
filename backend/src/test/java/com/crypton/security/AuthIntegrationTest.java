package com.crypton.security;

import com.crypton.model.User;
import com.crypton.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.logout;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Verify public routes are accessible without authentication")
    void testPublicRoutes() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));

        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"));

        mockMvc.perform(get("/robots.txt"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/sitemap.xml"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Verify protected route redirects unauthenticated user to /login")
    void testProtectedRouteRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    @DisplayName("Verify successful registration hashes password with BCrypt and creates user")
    void testSuccessfulRegistration() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("name", "John Satoshi")
                        .param("email", "john@crypton.local")
                        .param("phone", "9876543210")
                        .param("password", "SecureP@ss123")
                        .param("confirmPassword", "SecureP@ss123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered=true"));

        User saved = userRepository.findByEmail("john@crypton.local").orElse(null);
        assertThat(saved).isNotNull();
        assertThat(saved.getName()).isEqualTo("John Satoshi");
        assertThat(saved.getPhone()).isEqualTo("9876543210");
        assertThat(passwordEncoder.matches("SecureP@ss123", saved.getPasswordHash())).isTrue();
    }

    @Test
    @DisplayName("Verify registration validation: invalid phone, short password, mismatched passwords")
    void testRegistrationValidationFailures() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("name", "Bob")
                        .param("email", "not-an-email")
                        .param("phone", "123") // Not 10 digits
                        .param("password", "short") // < 8 chars
                        .param("confirmPassword", "different"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeHasFieldErrors("registerDto", "email", "phone", "password", "confirmPassword"));
    }

    @Test
    @DisplayName("Verify friendly error message when registering an existing email")
    void testDuplicateEmailRegistration() throws Exception {
        User existing = User.builder()
                .name("Existing User")
                .email("duplicate@crypton.local")
                .phone("1122334455")
                .passwordHash(passwordEncoder.encode("Password123"))
                .build();
        userRepository.save(existing);

        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("name", "New Person")
                        .param("email", "duplicate@crypton.local")
                        .param("phone", "9988776655")
                        .param("password", "NewPassword123")
                        .param("confirmPassword", "NewPassword123"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeHasFieldErrors("registerDto", "email"));
    }

    @Test
    @DisplayName("Verify successful login and authenticated dashboard access")
    void testSuccessfulLoginAndDashboardAccess() throws Exception {
        User user = User.builder()
                .name("Grace Hopper")
                .email("grace@crypton.local")
                .phone("1234567890")
                .passwordHash(passwordEncoder.encode("Password123"))
                .build();
        userRepository.save(user);

        // Test form login
        mockMvc.perform(formLogin("/login")
                        .user("username", "grace@crypton.local")
                        .password("password", "Password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    @DisplayName("Verify authenticated user sees Welcome, [user's name] on /dashboard")
    @WithMockUser(username = "grace@crypton.local")
    void testDashboardDisplaysUserName() throws Exception {
        User user = User.builder()
                .name("Grace Hopper")
                .email("grace@crypton.local")
                .phone("1234567890")
                .passwordHash(passwordEncoder.encode("Password123"))
                .build();
        userRepository.save(user);

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(content().string(containsString("Welcome, Grace Hopper")));
    }

    @Test
    @DisplayName("Verify account locks for 15 minutes after 5 failed login attempts")
    void testAccountLockoutAfter5FailedAttempts() throws Exception {
        User user = User.builder()
                .name("Target User")
                .email("target@crypton.local")
                .phone("1234567890")
                .passwordHash(passwordEncoder.encode("CorrectPassword123"))
                .failedAttempts(0)
                .build();
        userRepository.save(user);

        // Attempt 1 to 4: Failures increment attempts
        for (int i = 1; i <= 4; i++) {
            mockMvc.perform(formLogin("/login")
                            .user("username", "target@crypton.local")
                            .password("password", "WrongPassword"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/login?error=bad_credentials"));
        }

        User checkAttempts = userRepository.findByEmail("target@crypton.local").orElseThrow();
        assertThat(checkAttempts.getFailedAttempts()).isEqualTo(4);
        assertThat(checkAttempts.getLockedUntil()).isNull();

        // 5th Failed Attempt: triggers lockout
        mockMvc.perform(formLogin("/login")
                        .user("username", "target@crypton.local")
                        .password("password", "WrongPassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=locked"));

        User lockedUser = userRepository.findByEmail("target@crypton.local").orElseThrow();
        assertThat(lockedUser.getLockedUntil()).isNotNull();
        assertThat(lockedUser.getLockedUntil()).isAfter(LocalDateTime.now().plusMinutes(14));
        assertThat(lockedUser.isAccountNonLocked()).isFalse();

        // Subsequent attempt with correct password while locked is still blocked
        mockMvc.perform(formLogin("/login")
                        .user("username", "target@crypton.local")
                        .password("password", "CorrectPassword123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=locked"));
    }

    @Test
    @DisplayName("Verify logout redirects to /login?logout")
    void testLogoutRedirect() throws Exception {
        mockMvc.perform(logout("/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout"));
    }

    @Test
    @DisplayName("Verify Security Headers: nosniff, DENY, CSP")
    void testSecurityHeaders() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().exists("Content-Security-Policy"));
    }
}
