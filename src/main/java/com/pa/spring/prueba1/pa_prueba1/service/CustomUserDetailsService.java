package com.pa.spring.prueba1.pa_prueba1.service;

import com.pa.spring.prueba1.pa_prueba1.model.Administrador;
import com.pa.spring.prueba1.pa_prueba1.model.Estilista;
import com.pa.spring.prueba1.pa_prueba1.model.Cliente;
import com.pa.spring.prueba1.pa_prueba1.repository.AdministradorRepository;
import com.pa.spring.prueba1.pa_prueba1.repository.EstilistaRepository;
import com.pa.spring.prueba1.pa_prueba1.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Servicio personalizado para cargar usuarios desde múltiples fuentes.
 * Spring Security llama a loadUserByUsername() cuando un usuario intenta hacer login.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private AdministradorRepository administradorRepository;

    @Autowired
    private EstilistaRepository estilistaRepository;

    /**
     * Busca un usuario en las tres tablas: Cliente, Administrador y Estilista.
     * Retorna UserDetails con el username, password y authorities necesarios para Spring Security.
     * 
     * @param username El email/correo del usuario
     * @return UserDetails con la información del usuario
     * @throws UsernameNotFoundException si el usuario no existe en ninguna tabla
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        
        System.out.println("🔍 CustomUserDetailsService: Buscando usuario: " + username);

        // ==================== 1) BUSCAR EN CLIENTES ====================
        Optional<Cliente> clienteOpt = clienteRepository.findByCorreo(username);
        if (clienteOpt.isPresent()) {
            Cliente cliente = clienteOpt.get();
            
            System.out.println("✅ Usuario encontrado como CLIENTE");
            System.out.println("   - Email: " + cliente.getCorreo());
            System.out.println("   - Rol: " + cliente.getRol());
            System.out.println("   - Activo: " + cliente.isActivo());

            List<GrantedAuthority> authorities =
                    Collections.singletonList(new SimpleGrantedAuthority(cliente.getRol()));

            return new User(
                    cliente.getCorreo(),         // username
                    cliente.getClave(),          // contraseña (debe estar encriptada)
                    cliente.isActivo(),          // habilitado
                    true,                        // cuenta no expirada
                    true,                        // credenciales no expiradas
                    true,                        // cuenta no bloqueada
                    authorities
            );
        }

        // ==================== 2) BUSCAR EN ADMINISTRADORES ====================
        Optional<Administrador> adminOpt = administradorRepository.findByCorreo(username);
        if (adminOpt.isPresent()) {
            Administrador admin = adminOpt.get();
            
            System.out.println("✅ Usuario encontrado como ADMINISTRADOR");
            System.out.println("   - Email: " + admin.getCorreo());
            System.out.println("   - Rol: " + admin.getRol());
            System.out.println("   - Activo: " + admin.isActivo());

            List<GrantedAuthority> authorities =
                    Collections.singletonList(new SimpleGrantedAuthority(admin.getRol()));

            return new User(
                    admin.getCorreo(),           // username
                    admin.getClaveOPassword(),   // contraseña (intenta 'clave' primero, luego 'password' para compatibilidad)
                    admin.isActivo(),            // habilitado
                    true,                        // cuenta no expirada
                    true,                        // credenciales no expiradas
                    true,                        // cuenta no bloqueada
                    authorities
            );
        }

        // ==================== 3) BUSCAR EN ESTILISTA ====================
        Optional<Estilista> estilistaOpt = estilistaRepository.findByEmail(username);
        if (estilistaOpt.isPresent()) {
            Estilista estilista = estilistaOpt.get();
            
            System.out.println("✅ Usuario encontrado como ESTILISTA");
            System.out.println("   - Email: " + estilista.getEmail());
            System.out.println("   - Nombre: " + estilista.getNombreCompleto());
            System.out.println("   - Rol: " + estilista.getRol());
            System.out.println("   - Activo: " + estilista.isActivo());
            System.out.println("   - Password presente: " + (estilista.getPassword() != null ? "Sí" : "No"));
            
            // ✅ CRÍTICO: Validar que el estilista tenga rol asignado
            String rol = estilista.getRol();
            if (rol == null || rol.isEmpty()) {
                System.err.println("❌ ERROR: El estilista no tiene ROL asignado");
                throw new UsernameNotFoundException(
                    "El estilista existe pero no tiene rol asignado. Contacte al administrador."
                );
            }
            
            // ✅ CRÍTICO: Validar que el estilista tenga password
            if (estilista.getPassword() == null || estilista.getPassword().isEmpty()) {
                System.err.println("❌ ERROR: El estilista no tiene contraseña");
                throw new UsernameNotFoundException(
                    "El estilista existe pero no tiene contraseña configurada. Contacte al administrador."
                );
            }
            
            // ✅ Usar el rol de la base de datos (más flexible)
            List<GrantedAuthority> authorities =
                    Collections.singletonList(new SimpleGrantedAuthority(rol));

            // 📝 OPCIONAL: Actualizar última sesión
            try {
                estilista.setUltimaSesion(LocalDateTime.now());
                estilistaRepository.save(estilista);
                System.out.println("✅ Última sesión actualizada");
            } catch (Exception e) {
                System.err.println("⚠️ No se pudo actualizar última sesión: " + e.getMessage());
            }

            return new User(
                    estilista.getEmail(),          // username
                    estilista.getPassword(),       // contraseña (debe estar encriptada con BCrypt)
                    estilista.isActivo(),          // habilitado (solo estilistas activos pueden hacer login)
                    true,                        // cuenta no expirada
                    true,                        // credenciales no expiradas
                    true,                        // cuenta no bloqueada
                    authorities
            );
        }

        // ==================== 4) USUARIO NO ENCONTRADO ====================
        System.err.println("❌ Usuario NO encontrado en ninguna tabla: " + username);
        throw new UsernameNotFoundException(
            "No se encontró ningún usuario con el email: " + username
        );
    }
}
