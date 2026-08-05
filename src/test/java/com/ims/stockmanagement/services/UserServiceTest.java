package com.ims.stockmanagement.services;

import com.ims.stockmanagement.dtos.UserDTO;
import com.ims.stockmanagement.dtos.UserProfileUpdateRequest;
import com.ims.stockmanagement.enums.UserRole;
import com.ims.stockmanagement.exceptions.AlreadyExistsException;
import com.ims.stockmanagement.models.User;
import com.ims.stockmanagement.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Covers the one branch MockMvc cannot reach deterministically: losing the race
 * between the duplicate pre-check and the unique index.
 *
 * The integration test owns the rest of the contract.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final String EMAIL_IN_USE = "Email is already in use.";

    /** Anything that would describe the storage layer to a caller. */
    private static final List<String> FORBIDDEN_DISCLOSURES = List.of(
            "sql", "constraint", "jdbc", "hibernate", "unique", "duplicate entry", "violation");

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private UserService userService;

    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername("racer");
        existingUser.setEmail("before@example.com");
        existingUser.setFullName("Race Fixture");
        existingUser.setRole(UserRole.USER);
        existingUser.setEnabled(true);
    }

    private UserProfileUpdateRequest emailRequest(String email) {
        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        request.setEmail(email);
        return request;
    }

    @Test
    void emailConstraintRaceReturnsSafeConflict() {
        when(userRepository.findByUsername("racer")).thenReturn(Optional.of(existingUser));
        // The pre-check sees nothing: a concurrent transaction claimed the address
        // after this read and before the flush below.
        when(userRepository.findByEmailIgnoreCase("taken@example.com")).thenReturn(Optional.empty());
        when(userRepository.saveAndFlush(any(User.class)))
                .thenThrow(new DataIntegrityViolationException(
                        "could not execute statement; SQL [update users ...]; "
                                + "constraint [UK_users_email]; nested exception is "
                                + "org.hibernate.exception.ConstraintViolationException"));

        AlreadyExistsException exception = assertThrows(
                AlreadyExistsException.class,
                () -> userService.updateOwnProfile("racer", emailRequest("taken@example.com")));

        assertEquals(EMAIL_IN_USE, exception.getMessage());

        String lower = exception.getMessage().toLowerCase();
        for (String disclosure : FORBIDDEN_DISCLOSURES) {
            assertFalse(lower.contains(disclosure),
                    "the public message must not disclose '" + disclosure + "'");
        }
        assertFalse(lower.contains("taken@example.com"), "the submitted address must not be echoed");
    }

    @Test
    void integrityFailureUnrelatedToAChangedEmailIsNotReportedAsAnEmailConflict() {
        when(userRepository.findByUsername("racer")).thenReturn(Optional.of(existingUser));
        DataIntegrityViolationException original =
                new DataIntegrityViolationException("some other constraint");
        when(userRepository.saveAndFlush(any(User.class))).thenThrow(original);

        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        request.setFullName("Only A Name");

        // No email was changed, so the failure is left for the generic handler
        // rather than being misreported as a duplicate address.
        DataIntegrityViolationException thrown = assertThrows(
                DataIntegrityViolationException.class,
                () -> userService.updateOwnProfile("racer", request));

        assertSame(original, thrown);
        verify(userRepository, never()).findByEmailIgnoreCase(any());
    }

    @Test
    void duplicateEmailIsRejectedBeforeAnyWrite() {
        User owner = new User();
        owner.setId(2L);
        owner.setEmail("taken@example.com");

        when(userRepository.findByUsername("racer")).thenReturn(Optional.of(existingUser));
        when(userRepository.findByEmailIgnoreCase("taken@example.com")).thenReturn(Optional.of(owner));

        AlreadyExistsException exception = assertThrows(
                AlreadyExistsException.class,
                () -> userService.updateOwnProfile("racer", emailRequest("taken@example.com")));

        assertEquals(EMAIL_IN_USE, exception.getMessage());
        verify(userRepository, never()).saveAndFlush(any(User.class));
        assertEquals("before@example.com", existingUser.getEmail(), "nothing may be applied to the entity");
    }

    @Test
    void ownEmailInADifferentCaseIsNotTreatedAsAConflict() {
        when(userRepository.findByUsername("racer")).thenReturn(Optional.of(existingUser));
        // Case-insensitive lookup finds the caller themselves, which is not a conflict.
        when(userRepository.findByEmailIgnoreCase("BEFORE@example.com")).thenReturn(Optional.of(existingUser));
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(modelMapper.map(any(User.class), eq(UserDTO.class))).thenReturn(new UserDTO());

        userService.updateOwnProfile("racer", emailRequest("BEFORE@example.com"));

        assertEquals("BEFORE@example.com", existingUser.getEmail());
    }
}
