package com.internship.management.controller;

import com.internship.management.dto.ApiResponse;
import com.internship.management.dto.AuthDTO;
import com.internship.management.dto.UserDTO;
import com.internship.management.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/check-email")
    public ResponseEntity<AuthDTO.CheckEmailResponse> checkEmail(
            @RequestBody AuthDTO.CheckEmailRequest request) {
        return ResponseEntity.ok(authService.checkEmail(request.getEmail()));
    }

    @PostMapping("/create-password")
    public ResponseEntity<AuthDTO.AuthResponse> createPassword(
            @RequestBody AuthDTO.CreatePasswordRequest request) {
        return ResponseEntity.ok(authService.createPassword(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthDTO.AuthResponse> login(
            @RequestBody AuthDTO.LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register/admin")
    public ResponseEntity<ApiResponse<UserDTO>> createAdmin(@RequestBody AuthDTO.CreateAdminRequest request) {
        try {
            UserDTO user = authService.createAdmin(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Admin créé avec succès", user));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/init-admin")
    public ResponseEntity<ApiResponse<UserDTO>> initializeAdmin() {
        try {
            UserDTO user = authService.initializeDefaultAdmin();
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Admin par défaut initialisé", user));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/register/encadreur")
    public ResponseEntity<ApiResponse<UserDTO>> createEncadreur(@RequestBody AuthDTO.CreateEncadreurRequest request) {
        try {
            UserDTO user = authService.createEncadreur(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Encadreur créé avec succès", user));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/register/stagiaire")
    public ResponseEntity<ApiResponse<UserDTO>> createStagiaire(@RequestBody AuthDTO.CreateStagiaireRequest request) {
        try {
            UserDTO user = authService.createStagiaire(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Stagiaire créé avec succès", user));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
