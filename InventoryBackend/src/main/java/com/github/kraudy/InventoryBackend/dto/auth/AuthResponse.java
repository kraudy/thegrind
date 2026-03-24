package com.github.kraudy.InventoryBackend.dto.auth;

import lombok.Data;
import java.util.List;

@Data
public class AuthResponse {
    private String token;
    private String usuario;
    private List<String> roles;
}