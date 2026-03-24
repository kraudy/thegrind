package com.github.kraudy.InventoryBackend.security;

import com.github.kraudy.InventoryBackend.model.Usuario;
import com.github.kraudy.InventoryBackend.repository.UsuarioRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String usuario) throws UsernameNotFoundException {
        Usuario user = usuarioRepository.findById(usuario)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + usuario));

        // ← Now using native query instead of relationship
        List<String> roleNames = usuarioRepository.getRolesByUsuario(usuario);

        Collection<GrantedAuthority> authorities = roleNames.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsuario())
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(!user.isActivo())
                .accountLocked(!user.isActivo())
                .credentialsExpired(false)
                .disabled(!user.isActivo())
                .build();
    }
}