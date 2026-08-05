package com.ims.stockmanagement.services;

import com.ims.stockmanagement.dtos.Response;
import com.ims.stockmanagement.dtos.UserDTO;
import com.ims.stockmanagement.dtos.UserProfileUpdateRequest;
import com.ims.stockmanagement.exceptions.AlreadyExistsException;
import com.ims.stockmanagement.exceptions.NotFoundException;
import com.ims.stockmanagement.models.User;
import com.ims.stockmanagement.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    /** Deliberately free of the submitted address: the caller already knows what they sent. */
    private static final String EMAIL_IN_USE_MESSAGE = "Email is already in use.";

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    /**
     * Applies a partial profile edit to the caller's own account.
     *
     * The account is chosen by the authenticated username the caller was issued -
     * never by anything in the request body, which carries no identity field at all.
     * That holds for ADMIN too: this operation edits the caller's own profile only.
     */
    @Transactional
    public Response updateOwnProfile(String authenticatedUsername, UserProfileUpdateRequest request) {
        User user = userRepository.findByUsername(authenticatedUsername)
                .orElseThrow(() -> new NotFoundException("User not found: " + authenticatedUsername));

        boolean emailChanged = false;

        // Values arrive already trimmed from the request DTO.
        if (request.getEmail() != null) {
            rejectEmailOwnedByAnotherUser(request.getEmail(), user);
            emailChanged = !request.getEmail().equalsIgnoreCase(user.getEmail());
            user.setEmail(request.getEmail());
        }

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }

        User updatedUser = persist(user, emailChanged);

        return Response.builder()
                .statusCode(200)
                .message("User profile updated successfully")
                .user(modelMapper.map(updatedUser, UserDTO.class))
                .timestamp(LocalDateTime.now())
                .build();
    }

    private void rejectEmailOwnedByAnotherUser(String email, User user) {
        userRepository.findByEmailIgnoreCase(email)
                .filter(owner -> !owner.getId().equals(user.getId()))
                .ifPresent(owner -> {
                    throw new AlreadyExistsException(EMAIL_IN_USE_MESSAGE);
                });
    }

    /**
     * Flushes inside the transaction so the unique index is exercised here rather
     * than at an unobservable commit. The lookup above is a check-then-act and can
     * lose a race with a concurrent edit; when that happens on an email this call
     * actually changed, the database's answer means exactly what the pre-check would
     * have said. Any other integrity failure is rethrown for the generic handler, so
     * an unrelated fault is never reported as an email conflict.
     */
    private User persist(User user, boolean emailChanged) {
        try {
            return userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException ex) {
            if (emailChanged) {
                throw new AlreadyExistsException(EMAIL_IN_USE_MESSAGE);
            }
            throw ex;
        }
    }
}
