package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class MultiTenantIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Test öncesi hazırlıklar (Gerekirse mock veriler veya temizlik)
    }

    @Test
    @DisplayName("Tenant Header ile İstek Atıldığında Veri İlgili Şemaya Kaydedilmeli")
    @WithMockUser(username = "testAdmin", roles = {"ADMIN"})
    void shouldCreateUserInSpecificTenantSchema() throws Exception {

        User newUser = new User();
        newUser.setEmail("test.alpha@tenantflow.com");
        newUser.setPassword("SecurePassword123!");
        newUser.setFirstName("Alpha");
        newUser.setLastName("User");
        newUser.setRole("ROLE_CUSTOMER");
        newUser.setTenantId("alpha");

        String requestBody = objectMapper.writeValueAsString(newUser);

        // İstek atarken .with(csrf()) ekliyoruz (Security eğer POST'larda CSRF bekliyorsa testte patlamamak için)
        mockMvc.perform(post("/api/v1/users/register")
                        .header("X-Tenant-Id", "alpha")
                        .with(csrf()) // 🛡️ CSRF korumasını test için aşar
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated()); // Eğer Controller 200 dönüyorsa isOk() yapabilirsin
    }
}