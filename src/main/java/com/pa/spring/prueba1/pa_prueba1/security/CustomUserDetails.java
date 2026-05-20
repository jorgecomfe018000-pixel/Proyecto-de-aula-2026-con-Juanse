package com.pa.spring.prueba1.pa_prueba1.security;

import com.pa.spring.prueba1.pa_prueba1.model.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class CustomUserDetails implements UserDetails {

    private final Usuario usuario;

    public CustomUserDetails(Usuario usuario) {
        this.usuario = usuario;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Proteger contra datos antiguos o nulos: si no hay roles, devolver conjunto vacío
        Set<com.pa.spring.prueba1.pa_prueba1.model.Rol> roles = usuario.getRoles();
        if (roles == null || roles.isEmpty()) {
            return Collections.emptySet();
        }

        return roles.stream()
                .map(rol -> new SimpleGrantedAuthority(rol.getNombre()))
                .collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        return usuario.getPassword();
    }

    @Override
    public String getUsername() {
        return usuario.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return usuario.isActivo(); }
}
