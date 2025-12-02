package com.ims.stockmanagement.controllers;

import com.ims.stockmanagement.dtos.Response;
import com.ims.stockmanagement.dtos.UserDTO;
import com.ims.stockmanagement.models.User;
import com.ims.stockmanagement.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Response> getUserProfile() {
        Response response = new Response();
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            UserDTO userDTO = modelMapper.map(user, UserDTO.class);

            response.setStatusCode(200);
            response.setMessage("User profile retrieved successfully");
            response.setUser(userDTO);
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error: " + e.getMessage());
        }
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response> getAllUsers() {
        Response response = new Response();
        try {
            List<User> users = userRepository.findAll();
            List<UserDTO> userDTOs = users.stream()
                    .map(user -> modelMapper.map(user, UserDTO.class))
                    .collect(Collectors.toList());

            response.setStatusCode(200);
            response.setMessage("Users retrieved successfully");
            response.setUserList(userDTOs);
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error: " + e.getMessage());
        }
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response> getUserById(@PathVariable Long id) {
        Response response = new Response();
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            UserDTO userDTO = modelMapper.map(user, UserDTO.class);

            response.setStatusCode(200);
            response.setMessage("User retrieved successfully");
            response.setUser(userDTO);
        } catch (Exception e) {
            response.setStatusCode(404);
            response.setMessage("Error: " + e.getMessage());
        }
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response> deleteUser(@PathVariable Long id) {
        Response response = new Response();
        try {
            userRepository.deleteById(id);
            response.setStatusCode(200);
            response.setMessage("User deleted successfully");
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error: " + e.getMessage());
        }
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}

