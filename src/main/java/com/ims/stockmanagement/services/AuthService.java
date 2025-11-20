package com.ims.stockmanagement.services;

import com.ims.stockmanagement.dtos.LoginRequest;
import com.ims.stockmanagement.dtos.RegisterRequest;
import com.ims.stockmanagement.dtos.Response;
import com.ims.stockmanagement.dtos.UserDTO;
import com.ims.stockmanagement.enums.UserRole;
import com.ims.stockmanagement.exceptions.AlreadyExistsException;
import com.ims.stockmanagement.exceptions.InvalidCredentialsException;
import com.ims.stockmanagement.models.User;
import com.ims.stockmanagement.repositories.UserRepository;
import com.ims.stockmanagement.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final ModelMapper modelMapper;

    /**
     * Yeni kullanıcı kaydı
     */
    public Response register(RegisterRequest request) {
        // Kullanıcı adı kontrolü
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AlreadyExistsException("Username already exists: " + request.getUsername());
        }

        // Email kontrolü
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AlreadyExistsException("Email already exists: " + request.getEmail());
        }

        // Yeni kullanıcı oluştur
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.USER); // Varsayılan rol
        user.setEnabled(true);

        User savedUser = userRepository.save(user);

        // JWT token oluştur
        String token = jwtService.generateToken(savedUser);

        UserDTO userDTO = modelMapper.map(savedUser, UserDTO.class);

        return Response.builder()
                .statusCode(201)
                .message("User registered successfully")
                .token(token)
                .user(userDTO)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Kullanıcı girişi
     */
    public Response login(LoginRequest request) {
        try {
            // Kimlik doğrulama
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            // Kullanıcıyı bul
            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

            // JWT token oluştur
            String token = jwtService.generateToken(user);

            UserDTO userDTO = modelMapper.map(user, UserDTO.class);

            return Response.builder()
                    .statusCode(200)
                    .message("Login successful")
                    .token(token)
                    .user(userDTO)
                    .timestamp(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            throw new InvalidCredentialsException("Invalid username or password");
        }
    }

    /**
     * Token'dan kullanıcı bilgisi al
     */
    public Response getUserInfo(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));

        UserDTO userDTO = modelMapper.map(user, UserDTO.class);

        return Response.builder()
                .statusCode(200)
                .message("User info retrieved successfully")
                .user(userDTO)
                .timestamp(LocalDateTime.now())
                .build();
    }
}

