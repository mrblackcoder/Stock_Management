package com.ims.stockmanagement.controllers;

import com.ims.stockmanagement.enums.UserRole;
import com.ims.stockmanagement.models.User;
import com.ims.stockmanagement.repositories.UserRepository;
import com.ims.stockmanagement.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The full contract of PUT /api/users/profile, against the real controller,
 * service, repository and security filter chain.
 *
 * Nothing is mocked: the request travels through the JWT filter, method security,
 * bean validation and the database, so what is asserted here is what a client sees.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserProfileUpdateIntegrationTest {

    private static final String UNSUPPORTED_FIELDS = "Profile update contains unsupported fields.";
    private static final String NO_FIELDS = "At least one profile field must be provided.";
    private static final String BLANK_EMAIL = "Email must not be blank.";
    private static final String BLANK_FULL_NAME = "Full name must not be blank.";
    private static final String INVALID_EMAIL = "Please provide a valid email address.";
    private static final String EMAIL_TOO_LONG = "Email cannot exceed 255 characters.";
    private static final String FULL_NAME_LENGTH = "Full name must be between 2 and 100 characters.";
    private static final String EMAIL_IN_USE = "Email is already in use.";
    private static final String AUTHENTICATION_REQUIRED = "Authentication is required to access this resource.";

    /** Nothing about how the data is stored may reach the caller. */
    private static final List<String> FORBIDDEN_DISCLOSURES = List.of(
            "sql", "constraint", "jdbc", "hibernate", "users", "unique", "duplicate entry", "com.ims");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    private User caller;
    private User otherUser;
    private String callerToken;

    private String callerEmailBefore;
    private String callerFullNameBefore;
    private UserRole callerRoleBefore;
    private boolean callerEnabledBefore;
    private String otherEmailBefore;
    private String otherFullNameBefore;

    @BeforeEach
    void setUp() {
        caller = saveUser("profile_caller", UserRole.USER);
        otherUser = saveUser("profile_other", UserRole.USER);
        callerToken = jwtService.generateToken(caller);

        // Captured as values, so a later assertion cannot be satisfied by the same object.
        callerEmailBefore = caller.getEmail();
        callerFullNameBefore = caller.getFullName();
        callerRoleBefore = caller.getRole();
        callerEnabledBefore = caller.isEnabled();
        otherEmailBefore = otherUser.getEmail();
        otherFullNameBefore = otherUser.getFullName();
    }

    // ==================== Valid updates ====================

    @Test
    void validEmailUpdateSucceeds() throws Exception {
        updateProfile(callerToken, "{\"email\":\"changed@example.com\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("User profile updated successfully"))
                .andExpect(jsonPath("$.user.email").value("changed@example.com"));

        assertEquals("changed@example.com", reload(caller).getEmail());
    }

    @Test
    void validFullNameUpdateSucceeds() throws Exception {
        updateProfile(callerToken, "{\"fullName\":\"Renamed Person\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.fullName").value("Renamed Person"));

        assertEquals("Renamed Person", reload(caller).getFullName());
    }

    @Test
    void validCombinedUpdateSucceeds() throws Exception {
        updateProfile(callerToken, "{\"email\":\"both@example.com\",\"fullName\":\"Both Changed\"}")
                .andExpect(status().isOk());

        User updated = reload(caller);
        assertEquals("both@example.com", updated.getEmail());
        assertEquals("Both Changed", updated.getFullName());
    }

    @Test
    void partialUpdateLeavesOmittedFieldsUnchanged() throws Exception {
        updateProfile(callerToken, "{\"email\":\"only-email@example.com\"}")
                .andExpect(status().isOk());

        User updated = reload(caller);
        assertEquals("only-email@example.com", updated.getEmail());
        assertEquals(callerFullNameBefore, updated.getFullName(), "an omitted field must be left alone");

        updateProfile(callerToken, "{\"fullName\":\"Only Name\"}")
                .andExpect(status().isOk());

        User updatedAgain = reload(caller);
        assertEquals("only-email@example.com", updatedAgain.getEmail(), "an omitted field must be left alone");
        assertEquals("Only Name", updatedAgain.getFullName());
    }

    @Test
    void authenticatedIdentitySelectsTheUpdatedUser() throws Exception {
        updateProfile(callerToken, "{\"email\":\"mine@example.com\",\"fullName\":\"Mine Only\"}")
                .andExpect(status().isOk());

        assertEquals("mine@example.com", reload(caller).getEmail());
        assertOtherUserUntouched();
    }

    // ==================== Empty, null and blank ====================

    @Test
    void emptyRequestReturns400() throws Exception {
        assertRejected("{}", NO_FIELDS);
    }

    @Test
    void nullOnlyRequestReturns400() throws Exception {
        assertRejected("{\"email\":null,\"fullName\":null}", NO_FIELDS);
    }

    @Test
    void blankEmailReturns400() throws Exception {
        assertRejected("{\"email\":\"\"}", BLANK_EMAIL);
    }

    @Test
    void whitespaceOnlyEmailReturns400() throws Exception {
        assertRejected("{\"email\":\"   \"}", BLANK_EMAIL);
    }

    @Test
    void blankFullNameReturns400() throws Exception {
        assertRejected("{\"fullName\":\"\"}", BLANK_FULL_NAME);
    }

    @Test
    void whitespaceOnlyFullNameReturns400() throws Exception {
        assertRejected("{\"fullName\":\"   \"}", BLANK_FULL_NAME);
    }

    // ==================== Format and length ====================

    @Test
    void malformedEmailReturns400() throws Exception {
        assertRejected("{\"email\":\"not-an-email\"}", INVALID_EMAIL);
    }

    @Test
    void emailExceedingColumnLimitReturns400() throws Exception {
        // A genuinely well-formed address that is simply too long for the column:
        // the local part stays within its 64-character limit and every domain label
        // within 63, so only the length rule has anything to object to.
        String tooLong = "a".repeat(64) + "@"
                + "b".repeat(60) + "." + "c".repeat(60) + "."
                + "d".repeat(60) + "." + "e".repeat(60) + ".com";
        assertEquals(312, tooLong.length(), "fixture must exceed the 255-character limit");

        assertRejected("{\"email\":\"" + tooLong + "\"}", EMAIL_TOO_LONG);
    }

    @Test
    void fullNameBelowMinimumReturns400() throws Exception {
        assertRejected("{\"fullName\":\"A\"}", FULL_NAME_LENGTH);
    }

    @Test
    void fullNameExceedingMaximumReturns400() throws Exception {
        assertRejected("{\"fullName\":\"" + "x".repeat(101) + "\"}", FULL_NAME_LENGTH);
    }

    // ==================== Unsupported fields ====================

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"id\":99}",
            "{\"username\":\"someone_else\"}",
            "{\"role\":\"ADMIN\"}",
            "{\"enabled\":false}",
            "{\"createdAt\":\"2020-01-01T00:00:00\"}",
            "{\"password\":\"NewPassword1!\"}",
            "{\"unexpectedField\":\"anything\"}"
    })
    void unsupportedProfileFieldReturns400AndChangesNothing(String body) throws Exception {
        assertRejected(body, UNSUPPORTED_FIELDS);
        assertCallerUntouched();
        assertOtherUserUntouched();
    }

    @Test
    void multipleUnsupportedFieldsReturnASingleRejection() throws Exception {
        assertRejected("{\"id\":99,\"role\":\"ADMIN\",\"enabled\":false,\"unexpectedField\":1}", UNSUPPORTED_FIELDS);
        assertCallerUntouched();
        assertOtherUserUntouched();
    }

    @Test
    void unsupportedFieldAlongsideAValidEditIsStillRejected() throws Exception {
        assertRejected("{\"email\":\"valid@example.com\",\"role\":\"ADMIN\"}", UNSUPPORTED_FIELDS);
        assertCallerUntouched();
    }

    /**
     * The request DTO's own internals must be as unacceptable as any other field.
     *
     * `unsupportedFields` is the collector itself; the rest are the names Bean
     * Validation derives from the @AssertTrue rules. None of them is a settable
     * property, so Jackson treats them as unknown and routes them to the collector -
     * but that depends on their staying getter-only, which is exactly what these
     * cases pin down. Adding a setter to any of them would turn it into a known
     * ignored property that slips past the collector, and these tests would fail.
     */
    @ParameterizedTest
    @ValueSource(strings = {
            "unsupportedFields",
            "onlySupportedFieldsPresent",
            "atLeastOneFieldProvided",
            "emailNotBlank",
            "fullNameNotBlank",
            "fullNameLengthValid"
    })
    void internalBeanPropertyAloneReturnsUnsupportedFieldError(String property) throws Exception {
        assertRejected("{\"" + property + "\":true}", UNSUPPORTED_FIELDS);

        assertCallerUntouched();
        assertOtherUserUntouched();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "unsupportedFields",
            "onlySupportedFieldsPresent",
            "atLeastOneFieldProvided",
            "emailNotBlank",
            "fullNameNotBlank",
            "fullNameLengthValid"
    })
    void internalBeanPropertyAlongsideValidEditReturnsUnsupportedFieldError(String property) throws Exception {
        assertRejected("{\"fullName\":\"Attempted Update\",\"" + property + "\":true}", UNSUPPORTED_FIELDS);

        // The otherwise-valid edit travelling with it must not be applied.
        assertEquals(callerFullNameBefore, reload(caller).getFullName(),
                "a rejected request must not persist the editable field it carried");
        assertCallerUntouched();
        assertOtherUserUntouched();
    }

    // ==================== Duplicate email ====================

    @Test
    void duplicateEmailReturnsSafe409() throws Exception {
        assertConflict("{\"email\":\"" + otherEmailBefore + "\"}");
        assertCallerUntouched();
        assertOtherUserUntouched();
    }

    @Test
    void duplicateEmailWithDifferentCaseReturnsSafe409() throws Exception {
        assertConflict("{\"email\":\"" + otherEmailBefore.toUpperCase() + "\"}");
        assertCallerUntouched();
        assertOtherUserUntouched();
    }

    @Test
    void ownExistingEmailDoesNotConflict() throws Exception {
        updateProfile(callerToken, "{\"email\":\"" + callerEmailBefore + "\"}")
                .andExpect(status().isOk());

        assertEquals(callerEmailBefore, reload(caller).getEmail());
    }

    @Test
    void ownExistingEmailWithDifferentCaseDoesNotConflict() throws Exception {
        String upperCased = callerEmailBefore.toUpperCase();

        updateProfile(callerToken, "{\"email\":\"" + upperCased + "\"}")
                .andExpect(status().isOk());

        assertEquals(upperCased, reload(caller).getEmail());
    }

    @Test
    void emailIsTrimmedBeforeDuplicateCheckAndPersistence() throws Exception {
        // An accepted address is stored without its padding...
        updateProfile(callerToken, "{\"email\":\"  padded@example.com  \"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value("padded@example.com"));

        assertEquals("padded@example.com", reload(caller).getEmail());

        // ...and padding cannot smuggle a duplicate past the check. Asserted last:
        // the rejection marks the surrounding transaction rollback-only.
        assertConflict("{\"email\":\"   " + otherEmailBefore + "   \"}");
        assertOtherUserUntouched();
    }

    // ==================== Security ====================

    @Test
    void unauthenticatedProfileUpdateReturns401() throws Exception {
        mockMvc.perform(put("/api/users/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Nobody\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(401))
                .andExpect(jsonPath("$.message").value(AUTHENTICATION_REQUIRED));

        assertCallerUntouched();
    }

    @Test
    void normalUserUpdatesOnlyTheirOwnProfile() throws Exception {
        updateProfile(callerToken, "{\"fullName\":\"Self Edited\"}")
                .andExpect(status().isOk());

        assertEquals("Self Edited", reload(caller).getFullName());
        assertOtherUserUntouched();
    }

    @Test
    void adminUpdatesOnlyTheirOwnProfile() throws Exception {
        User admin = saveUser("profile_admin", UserRole.ADMIN);
        String adminEmailBefore = admin.getEmail();

        updateProfile(jwtService.generateToken(admin), "{\"email\":\"admin-self@example.com\"}")
                .andExpect(status().isOk());

        assertEquals("admin-self@example.com", reload(admin).getEmail());
        assertFalse(adminEmailBefore.equals(reload(admin).getEmail()));
        // An ADMIN token still edits only the account it belongs to.
        assertCallerUntouched();
        assertOtherUserUntouched();
    }

    // ==================== Helpers ====================

    private org.springframework.test.web.servlet.ResultActions updateProfile(String token, String body)
            throws Exception {
        return mockMvc.perform(put("/api/users/profile")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private void assertRejected(String body, String expectedMessage) throws Exception {
        updateProfile(callerToken, body)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.message").value(expectedMessage));
    }

    private void assertConflict(String body) throws Exception {
        String responseBody = updateProfile(callerToken, body)
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(409))
                .andExpect(jsonPath("$.message").value(EMAIL_IN_USE))
                .andReturn().getResponse().getContentAsString();

        String lower = responseBody.toLowerCase();
        for (String disclosure : FORBIDDEN_DISCLOSURES) {
            assertFalse(lower.contains(disclosure),
                    "conflict response must not disclose '" + disclosure + "', body was: " + responseBody);
        }
        // The submitted address is never echoed back either.
        assertFalse(lower.contains(otherEmailBefore.toLowerCase()),
                "conflict response must not echo the submitted email, body was: " + responseBody);
    }

    private void assertCallerUntouched() {
        User reloaded = reload(caller);
        assertEquals(callerEmailBefore, reloaded.getEmail());
        assertEquals(callerFullNameBefore, reloaded.getFullName());
        assertEquals(callerRoleBefore, reloaded.getRole(), "a profile edit must never change a role");
        assertEquals(callerEnabledBefore, reloaded.isEnabled(), "a profile edit must never change account state");
    }

    private void assertOtherUserUntouched() {
        User reloaded = reload(otherUser);
        assertEquals(otherEmailBefore, reloaded.getEmail());
        assertEquals(otherFullNameBefore, reloaded.getFullName());
    }

    private User reload(User user) {
        return userRepository.findById(user.getId()).orElseThrow();
    }

    private User saveUser(String prefix, UserRole role) {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        User user = new User();
        user.setUsername(prefix + "_" + unique);
        user.setEmail(prefix + "_" + unique + "@example.com");
        user.setPassword("not-used-for-jwt-auth");
        user.setFullName("Profile Fixture " + prefix);
        user.setRole(role);
        user.setEnabled(true);
        return userRepository.save(user);
    }
}
