package com.petshop.api.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petshop.api.TestcontainersConfiguration;
import com.petshop.api.auth.controller.RefreshCookie;
import com.petshop.api.auth.domain.Role;
import com.petshop.api.auth.domain.User;
import com.petshop.api.auth.repository.UserRepository;
import com.petshop.api.auth.service.LoginRateLimiter;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * The whole Phase 1 auth flow through the real security filter chain and a real PostgreSQL:
 * login, bearer access, refresh rotation and theft detection, logout, roles, forced password
 * change, disabled accounts and login throttling.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class AuthFlowIntegrationTest {

    // Random per run: a test fixture, not a secret. (A literal here is flagged by gitleaks'
    // generic-api-key rule, and reading it from an env var breaks local runs and the CI job.)
    private static final String PASSWORD = "Pw-" + UUID.randomUUID();
    private static final AtomicInteger IP = new AtomicInteger(1);

    @MockitoBean private ChatModel chatModel;

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper json;
    @Autowired private UserRepository users;
    @Autowired private PasswordEncoder encoder;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private LoginRateLimiter rateLimiter;

    /** A fresh client IP per test, so the in-memory login throttle does not leak between tests. */
    private String ip;

    @BeforeEach
    void setUp() {
        jdbc.update("DELETE FROM refresh_tokens");
        jdbc.update("DELETE FROM users");
        rateLimiter.reset();
        ip = "10.0.0." + IP.getAndIncrement();
    }

    // --- login / access ---

    @Test
    void login_returnsAccessToken_andHttpOnlyRefreshCookie() throws Exception {
        user("dona@pet.com", Role.OWNER, false);

        MvcResult result = mvc.perform(login("DONA@pet.com", PASSWORD))   // e-mail is case-insensitive
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.expiresIn").value(900))
                .andExpect(jsonPath("$.user.role").value("OWNER"))
                .andExpect(header().string("Set-Cookie", containsString("HttpOnly")))
                .andExpect(header().string("Set-Cookie", containsString("SameSite=Strict")))
                .andExpect(header().string("Set-Cookie", containsString("Path=/api/auth")))
                .andReturn();

        mvc.perform(get("/api/customers").header("Authorization", bearer(result)))
                .andExpect(status().isOk());
        mvc.perform(get("/api/me").header("Authorization", bearer(result)))
                .andExpect(jsonPath("$.email").value("dona@pet.com"));
    }

    @Test
    void wrongPassword_andUnknownEmail_getTheSameAnswer() throws Exception {
        user("dona@pet.com", Role.OWNER, false);

        mvc.perform(login("dona@pet.com", "errada-123"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("E-mail ou senha incorretos."));
        mvc.perform(login("ninguem@pet.com", PASSWORD))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("E-mail ou senha incorretos."));
    }

    @Test
    void apiWithoutToken_is401ProblemDetail() throws Exception {
        mvc.perform(get("/api/customers"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.requestId").isNotEmpty());
        mvc.perform(get("/api/customers").header("Authorization", "Bearer not-a-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void oldStyleAdminLogin_isGone() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"x\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    // --- refresh / logout ---

    @Test
    void refresh_rotatesTheCookie_andIssuesANewAccessToken() throws Exception {
        user("dona@pet.com", Role.OWNER, false);
        Cookie first = refreshCookie(mvc.perform(login("dona@pet.com", PASSWORD)).andReturn());

        MvcResult refreshed = mvc.perform(post("/api/auth/refresh").cookie(first))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andReturn();
        Cookie second = refreshCookie(refreshed);

        assertThat(second.getValue()).isNotEqualTo(first.getValue());
        mvc.perform(get("/api/customers").header("Authorization", bearer(refreshed))).andExpect(status().isOk());
        mvc.perform(post("/api/auth/refresh").cookie(second)).andExpect(status().isOk());
    }

    @Test
    void reusingAnOldRefreshToken_afterTheGrace_revokesEverySession() throws Exception {
        user("dona@pet.com", Role.OWNER, false);
        Cookie stolen = refreshCookie(mvc.perform(login("dona@pet.com", PASSWORD)).andReturn());
        Cookie legit = refreshCookie(mvc.perform(post("/api/auth/refresh").cookie(stolen)).andReturn());

        // Pretend the rotation happened a minute ago (beyond the 10 s grace for parallel tabs).
        jdbc.update("UPDATE refresh_tokens SET revoked_at = now() - interval '1 minute' WHERE revoked_at IS NOT NULL");

        mvc.perform(post("/api/auth/refresh").cookie(stolen))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Sessão expirada. Entre novamente."));
        // The legitimate session was revoked too: whoever holds the stolen token is locked out.
        mvc.perform(post("/api/auth/refresh").cookie(legit)).andExpect(status().isUnauthorized());
    }

    @Test
    void twoTabsRefreshingAtOnce_bothSucceed() throws Exception {
        user("dona@pet.com", Role.OWNER, false);
        Cookie shared = refreshCookie(mvc.perform(login("dona@pet.com", PASSWORD)).andReturn());

        mvc.perform(post("/api/auth/refresh").cookie(shared)).andExpect(status().isOk());
        mvc.perform(post("/api/auth/refresh").cookie(shared)).andExpect(status().isOk());   // within the grace
    }

    @Test
    void logout_revokesTheRefreshToken_andClearsTheCookie() throws Exception {
        user("dona@pet.com", Role.OWNER, false);
        Cookie cookie = refreshCookie(mvc.perform(login("dona@pet.com", PASSWORD)).andReturn());

        mvc.perform(post("/api/auth/logout").cookie(cookie))
                .andExpect(status().isNoContent())
                .andExpect(header().string("Set-Cookie", containsString("Max-Age=0")));
        mvc.perform(post("/api/auth/refresh").cookie(cookie)).andExpect(status().isUnauthorized());
    }

    @Test
    void refreshWithoutCookie_is401() throws Exception {
        mvc.perform(post("/api/auth/refresh")).andExpect(status().isUnauthorized());
    }

    // --- roles ---

    @Test
    void staff_worksWithCustomers_butCannotSeeReceiptsReportsOrUsers() throws Exception {
        user("groomer@pet.com", Role.STAFF, false);
        String token = bearer(mvc.perform(login("groomer@pet.com", PASSWORD)).andReturn());

        mvc.perform(get("/api/customers").header("Authorization", token)).andExpect(status().isOk());
        mvc.perform(post("/api/protocol").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Banho\"}"))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/receipts").header("Authorization", token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("Você não tem permissão para esta ação."));
        mvc.perform(get("/api/report/absent").header("Authorization", token)).andExpect(status().isForbidden());
        mvc.perform(get("/api/users").header("Authorization", token)).andExpect(status().isForbidden());
    }

    @Test
    void viewer_readsEverything_butWritesNothing() throws Exception {
        user("contador@pet.com", Role.VIEWER, false);
        String token = bearer(mvc.perform(login("contador@pet.com", PASSWORD)).andReturn());

        mvc.perform(get("/api/receipts").header("Authorization", token)).andExpect(status().isOk());
        mvc.perform(get("/api/customers").header("Authorization", token)).andExpect(status().isOk());
        mvc.perform(post("/api/protocol").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Banho\"}"))
                .andExpect(status().isForbidden());
    }

    // --- user management ---

    @Test
    void admin_createsStaff_whoMustChangeTheTemporaryPassword() throws Exception {
        user("gerente@pet.com", Role.ADMIN, false);
        String admin = bearer(mvc.perform(login("gerente@pet.com", PASSWORD)).andReturn());

        mvc.perform(post("/api/users").header("Authorization", admin).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Ana Tosadora","email":"ana@pet.com","role":"STAFF","temporaryPassword":"temporaria-1"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mustChangePassword").value(true));

        MvcResult anaLogin = mvc.perform(login("ana@pet.com", "temporaria-1"))
                .andExpect(jsonPath("$.user.mustChangePassword").value(true))
                .andReturn();
        String ana = bearer(anaLogin);

        mvc.perform(get("/api/customers").header("Authorization", ana))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("PASSWORD_CHANGE_REQUIRED"));
        mvc.perform(get("/api/me").header("Authorization", ana)).andExpect(status().isOk());

        MvcResult changed = mvc.perform(post("/api/me/password").header("Authorization", ana)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"temporaria-1\",\"newPassword\":\"minha-senha-nova\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.mustChangePassword").value(false))
                .andReturn();

        mvc.perform(get("/api/customers").header("Authorization", bearer(changed))).andExpect(status().isOk());
        // The token from before the change no longer works (token_version bumped).
        mvc.perform(get("/api/me").header("Authorization", ana)).andExpect(status().isUnauthorized());
    }

    @Test
    void admin_cannotCreateOrManageOwners() throws Exception {
        User owner = user("dona@pet.com", Role.OWNER, false);
        user("gerente@pet.com", Role.ADMIN, false);
        String admin = bearer(mvc.perform(login("gerente@pet.com", PASSWORD)).andReturn());

        mvc.perform(post("/api/users").header("Authorization", admin).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"X","email":"x@pet.com","role":"OWNER","temporaryPassword":"temporaria-1"}"""))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/users/" + owner.getPublicId() + "/reset-password").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"temporaryPassword\":\"temporaria-1\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void lastActiveOwner_cannotBeDisabledOrDemoted_norChangeOwnRole() throws Exception {
        User owner = user("dona@pet.com", Role.OWNER, false);
        String token = bearer(mvc.perform(login("dona@pet.com", PASSWORD)).andReturn());

        mvc.perform(put("/api/users/" + owner.getPublicId()).header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Dona\",\"role\":\"ADMIN\",\"status\":\"ACTIVE\"}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void disablingAUser_endsTheirSessionImmediately() throws Exception {
        user("dona@pet.com", Role.OWNER, false);
        User staff = user("groomer@pet.com", Role.STAFF, false);
        String owner = bearer(mvc.perform(login("dona@pet.com", PASSWORD)).andReturn());
        MvcResult staffLogin = mvc.perform(login("groomer@pet.com", PASSWORD)).andReturn();

        mvc.perform(put("/api/users/" + staff.getPublicId()).header("Authorization", owner)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Groomer\",\"role\":\"STAFF\",\"status\":\"DISABLED\"}"))
                .andExpect(status().isOk());

        mvc.perform(get("/api/customers").header("Authorization", bearer(staffLogin))).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/refresh").cookie(refreshCookie(staffLogin))).andExpect(status().isUnauthorized());
        mvc.perform(login("groomer@pet.com", PASSWORD)).andExpect(status().isUnauthorized());
    }

    // --- throttling ---

    @Test
    void fiveWrongPasswords_blockTheEmailForAWhile() throws Exception {
        user("dona@pet.com", Role.OWNER, false);
        for (int i = 0; i < 5; i++) {
            mvc.perform(login("dona@pet.com", "errada-" + i)).andExpect(status().isUnauthorized());
        }
        mvc.perform(login("dona@pet.com", PASSWORD))   // even the right password is refused now
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"));
    }

    // --- helpers ---

    private User user(String email, Role role, boolean mustChange) {
        User u = new User();
        u.setEmail(email);
        u.setName(email.substring(0, email.indexOf('@')));
        u.setRole(role);
        u.setPasswordHash(encoder.encode(PASSWORD));
        u.setMustChangePassword(mustChange);
        return users.save(u);
    }

    private MockHttpServletRequestBuilder login(String email, String password) throws Exception {
        return post("/api/auth/login")
                .with(request -> {
                    request.setRemoteAddr(ip);
                    return request;
                })
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(new java.util.HashMap<>(java.util.Map.of(
                        "email", email, "password", password))));
    }

    private String bearer(MvcResult result) throws Exception {
        JsonNode body = json.readTree(result.getResponse().getContentAsString());
        return "Bearer " + body.get("accessToken").asText();
    }

    private static Cookie refreshCookie(MvcResult result) {
        Cookie cookie = result.getResponse().getCookie(RefreshCookie.NAME);
        assertThat(cookie).as("refresh cookie").isNotNull();
        return new Cookie(RefreshCookie.NAME, cookie.getValue());
    }
}
