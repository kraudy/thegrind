package com.github.kraudy.InventoryBackend.dto.auth;

import lombok.Data;

@Data
public class LoginRequest {
    private String usuario;
    private String password;
}