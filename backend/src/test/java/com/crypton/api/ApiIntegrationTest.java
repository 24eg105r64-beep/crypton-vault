package com.crypton.api;

import com.crypton.repository.UserRepository;
import jakarta.persistence.EntityManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** End-to-end checks for the JSON API used by the React frontend. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    private void register(String name, String email) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"%s","email":"%s","phone":"9876543210",
                                 "password":"password123","confirmPassword":"password123"}
                                """.formatted(name, email)))
                .andExpect(status().isCreated());
    }

    private String login(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private JsonNode json(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("Health check is public")
    void health() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("Protected API returns 401 JSON without a token or with a fake token")
    void requiresToken() throws Exception {
        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        mockMvc.perform(get("/api/dashboard").header("Authorization", "Bearer not.a-real-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Register, sign in, and read the current user")
    void registerAndLogin() throws Exception {
        register("Grace Hopper", "Grace@Example.com");
        String token = login("grace@example.com", "password123");

        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Grace Hopper"))
                .andExpect(jsonPath("$.email").value("grace@example.com"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    @DisplayName("Registration rejects invalid fields, mismatched and duplicate emails")
    void registrationValidation() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"email\":\"bad\",\"phone\":\"12\",\"password\":\"short\",\"confirmPassword\":\"x\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.phone").exists())
                .andExpect(jsonPath("$.errors.password").exists());

        register("Ada", "ada@example.com");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ada\",\"email\":\"ada@example.com\",\"phone\":\"9876543210\",\"password\":\"password123\",\"confirmPassword\":\"password999\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.confirmPassword").exists());
    }

    @Test
    @DisplayName("Wrong password is rejected, and 5 failures lock the account")
    void lockout() throws Exception {
        register("Ada", "ada@example.com");
        for (int i = 0; i < 4; i++) {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"ada@example.com\",\"password\":\"wrong-password\"}"))
                    .andExpect(status().isUnauthorized());
        }
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ada@example.com\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isLocked());

        // Even the right password is refused while locked
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ada@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isLocked());
    }

    @Test
    @DisplayName("Full vault flow: create, add coin, deposit, withdraw, insufficient balance, rename, delete")
    void vaultFlow() throws Exception {
        register("Ada", "ada@example.com");
        String auth = "Bearer " + login("ada@example.com", "password123");

        long vaultId = json(mockMvc.perform(post("/api/vaults").header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Savings\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Savings"))
                .andReturn()).get("id").asLong();

        long assetId = json(mockMvc.perform(post("/api/vaults/" + vaultId + "/assets").header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"coinType\":\"BTC\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.balance").value("0"))
                .andReturn()).get("id").asLong();

        mockMvc.perform(post("/api/assets/" + assetId + "/deposit").header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"amount\":\"1.5\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        mockMvc.perform(post("/api/assets/" + assetId + "/withdraw").header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"amount\":\"0.5\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/assets/" + assetId + "/withdraw").header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"amount\":\"100\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "There isn't enough in this wallet for that withdrawal. Try a smaller amount."));

        mockMvc.perform(post("/api/assets/" + assetId + "/deposit").header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"amount\":\"abc\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/assets/" + assetId).header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.asset.balance").value("1"))
                .andExpect(jsonPath("$.vault.name").value("Savings"))
                .andExpect(jsonPath("$.transactions", hasSize(3)));

        mockMvc.perform(get("/api/dashboard").header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totals.BTC").value("1"))
                .andExpect(jsonPath("$.vaults", hasSize(1)));

        mockMvc.perform(put("/api/vaults/" + vaultId).header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Long term\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Long term"));

        // Each real request gets a fresh database session; simulate that here.
        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(delete("/api/vaults/" + vaultId).header("Authorization", auth))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/vaults").header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Users cannot see or change each other's vaults")
    void dataIsolation() throws Exception {
        register("Ada", "ada@example.com");
        register("Bob", "bob@example.com");
        String ada = "Bearer " + login("ada@example.com", "password123");
        String bob = "Bearer " + login("bob@example.com", "password123");

        long vaultId = json(mockMvc.perform(post("/api/vaults").header("Authorization", ada)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Private\"}"))
                .andReturn()).get("id").asLong();

        mockMvc.perform(get("/api/vaults/" + vaultId).header("Authorization", bob))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/vaults/" + vaultId).header("Authorization", bob))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/vaults").header("Authorization", bob))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("CORS allows the local React dev server")
    void corsPreflight() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    @Test
    @DisplayName("Original Thymeleaf pages still work")
    void originalPagesStillWork() throws Exception {
        mockMvc.perform(get("/login")).andExpect(status().isOk());
        mockMvc.perform(get("/dashboard")).andExpect(status().is3xxRedirection());
    }
}
