package com.ims.stockmanagement.controllers;

import com.ims.stockmanagement.dtos.Response;
import com.ims.stockmanagement.dtos.UserDTO;
import com.ims.stockmanagement.exceptions.NotFoundException;
import com.ims.stockmanagement.models.User;
import com.ims.stockmanagement.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Response> getUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("No authenticated user found");
        }
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found: " + username));

        UserDTO userDTO = modelMapper.map(user, UserDTO.class);

        Response response = Response.builder()
                .statusCode(200)
                .message("User profile retrieved successfully")
                .user(userDTO)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Response> updateUserProfile(@RequestBody UserDTO userDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("No authenticated user found");
        }
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found: " + username));

        // Only allow updating email and fullName (not username, role, or password)
        if (userDTO.getEmail() != null && !userDTO.getEmail().isBlank()) {
            // Check if email is already used by another user
            userRepository.findByEmail(userDTO.getEmail())
                    .filter(existingUser -> !existingUser.getId().equals(user.getId()))
                    .ifPresent(existingUser -> {
                        throw new IllegalArgumentException("Email already in use: " + userDTO.getEmail());
                    });
            user.setEmail(userDTO.getEmail());
        }

        if (userDTO.getFullName() != null && !userDTO.getFullName().isBlank()) {
            user.setFullName(userDTO.getFullName());
        }

        User updatedUser = userRepository.save(user);
        UserDTO updatedUserDTO = modelMapper.map(updatedUser, UserDTO.class);

        Response response = Response.builder()
                .statusCode(200)
                .message("User profile updated successfully")
                .user(updatedUserDTO)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserDTO> userDTOs = users.stream()
                .map(user -> modelMapper.map(user, UserDTO.class))
                .collect(Collectors.toList());

        Response response = Response.builder()
                .statusCode(200)
                .message("Users retrieved successfully")
                .userList(userDTOs)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response> getUserById(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));

        UserDTO userDTO = modelMapper.map(user, UserDTO.class);

        Response response = Response.builder()
                .statusCode(200)
                .message("User retrieved successfully")
                .user(userDTO)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("User not found with id: " + id);
        }

        userRepository.deleteById(id);

        Response response = Response.builder()
                .statusCode(200)
                .message("User deleted successfully")
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }
}
