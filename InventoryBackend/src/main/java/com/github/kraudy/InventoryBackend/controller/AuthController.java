package com.github.kraudy.InventoryBackend.controller;

import com.github.kraudy.InventoryBackend.dto.auth.AuthResponse;
import com.github.kraudy.InventoryBackend.dto.auth.LoginRequest;
import com.github.kraudy.InventoryBackend.repository.UsuarioRepository;
import com.github.kraudy.InventoryBackend.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          UsuarioRepository usuarioRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsuario(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // ← Now using native query
        List<String> roles = usuarioRepository.getRolesByUsuario(userDetails.getUsername());

        String token = jwtService.generateToken(userDetails.getUsername(), roles);

        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setUsuario(userDetails.getUsername());
        response.setRoles(roles);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/roles")
    public ResponseEntity<List<String>> getCurrentUserRoles() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = auth.getName();

        // ← Now using native query
        List<String> roles = usuarioRepository.getRolesByUsuario(username);

        return ResponseEntity.ok(roles);
    }
}