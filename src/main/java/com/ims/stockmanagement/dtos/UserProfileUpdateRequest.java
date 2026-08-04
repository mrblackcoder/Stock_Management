package com.ims.stockmanagement.dtos;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Request body for PUT /api/users/profile.
 *
 * Deliberately separate from UserDTO. UserDTO is a response shape carrying id,
 * username, role, enabled and createdAt; binding it to an edit made the endpoint
 * advertise five fields it silently discarded, and it would have become a real
 * privilege-escalation hole the moment anyone mapped the DTO back onto the entity.
 * Here those fields are simply not bindable, and any attempt to send one is refused.
 *
 * A profile edit is partial: an omitted field is left unchanged. A field that is
 * present must be usable, so null means "leave alone" while blank means "invalid".
 */
@Data
@NoArgsConstructor
public class UserProfileUpdateRequest {

    @Email(message = "Please provide a valid email address.")
    @Size(max = 255, message = "Email cannot exceed 255 characters.")
    private String email;

    private String fullName;

    /**
     * Every JSON property that is not an editable profile field.
     *
     * Collected rather than thrown from, so the rejection surfaces as an ordinary
     * validation failure with a stable message instead of Jackson's malformed-body
     * response. Names are kept only for the server-side decision - they are never
     * echoed - and FAIL_ON_UNKNOWN_PROPERTIES stays off globally.
     */
    @JsonIgnore
    private final Set<String> unsupportedFields = new LinkedHashSet<>();

    @JsonAnySetter
    public void captureUnsupportedField(String name, Object ignoredValue) {
        unsupportedFields.add(name);
    }

    /** Trimmed at bind time so validation and persistence see the same value. */
    public void setEmail(String email) {
        this.email = email == null ? null : email.trim();
    }

    public void setFullName(String fullName) {
        this.fullName = fullName == null ? null : fullName.trim();
    }

    // ==================== Validation ====================
    //
    // Each rule yields at most one violation for the input it owns, so a rejected
    // request maps to exactly one deterministic public message.

    @JsonIgnore
    @AssertTrue(message = "Profile update contains unsupported fields.")
    public boolean isOnlySupportedFieldsPresent() {
        return unsupportedFields.isEmpty();
    }

    /**
     * An edit has to change something. Skipped when unsupported fields are present:
     * that request already has its own answer, and reporting both would make the
     * message depend on validator ordering.
     */
    @JsonIgnore
    @AssertTrue(message = "At least one profile field must be provided.")
    public boolean isAtLeastOneFieldProvided() {
        if (!unsupportedFields.isEmpty()) {
            return true;
        }
        return email != null || fullName != null;
    }

    @JsonIgnore
    @AssertTrue(message = "Email must not be blank.")
    public boolean isEmailNotBlank() {
        return email == null || !email.isEmpty();
    }

    @JsonIgnore
    @AssertTrue(message = "Full name must not be blank.")
    public boolean isFullNameNotBlank() {
        return fullName == null || !fullName.isEmpty();
    }

    /** Blank is the blank rule's business, so this stays silent for an empty value. */
    @JsonIgnore
    @AssertTrue(message = "Full name must be between 2 and 100 characters.")
    public boolean isFullNameLengthValid() {
        if (fullName == null || fullName.isEmpty()) {
            return true;
        }
        return fullName.length() >= 2 && fullName.length() <= 100;
    }
}
