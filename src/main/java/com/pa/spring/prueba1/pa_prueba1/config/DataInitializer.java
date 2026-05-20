package com.pa.spring.prueba1.pa_prueba1.config;

import com.pa.spring.prueba1.pa_prueba1.model.Rol;
import com.pa.spring.prueba1.pa_prueba1.model.Cliente;
import com.pa.spring.prueba1.pa_prueba1.model.Administrador;
import com.pa.spring.prueba1.pa_prueba1.repository.RolRepository;
import com.pa.spring.prueba1.pa_prueba1.repository.ClienteRepository;
import com.pa.spring.prueba1.pa_prueba1.repository.AdministradorRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(
            RolRepository rolRepository,
            ClienteRepository clienteRepository,
            AdministradorRepository administradorRepository,
            PasswordEncoder passwordEncoder) {
        
        return args -> {
            System.out.println("Inicializando datos en MongoDB...");
            
            // ================== ROLES ==================
            if (rolRepository.count() == 0) {
                System.out.println("Creando roles...");
                
                Rol roleAdmin = new Rol();
                roleAdmin.setNombre("ROLE_ADMIN");
                rolRepository.save(roleAdmin);
                System.out.println("✓ Rol ROLE_ADMIN creado");

                Rol roleUser = new Rol();
                roleUser.setNombre("ROLE_USER");
                rolRepository.save(roleUser);
                System.out.println("✓ Rol ROLE_USER creado");

                Rol roleBarbero = new Rol();
                roleBarbero.setNombre("ROLE_BARBERO");
                rolRepository.save(roleBarbero);
                System.out.println("✓ Rol ROLE_BARBERO creado");
            } else {
                System.out.println("Los roles ya existen en la base de datos");
            }

            // ================== ADMINISTRADOR POR DEFECTO ==================
            if (administradorRepository.findByCorreo("admin@peluqueria.com").isEmpty()) {
                System.out.println("Creando administrador por defecto...");
                
                Administrador admin = new Administrador();
                admin.setNombre("Administrador");
                admin.setCorreo("admin@peluqueria.com");
                admin.setClave(passwordEncoder.encode("admin123"));
                admin.setTelefono("3001234567");
                admin.setActivo(true);
                admin.setRol("ROLE_ADMIN");

                administradorRepository.save(admin);
                System.out.println("✓ Administrador creado: admin@peluqueria.com / admin123");
            } else {
                System.out.println("✓ Administrador ya existe en la base de datos");
            }

            // ================== USUARIO DE PRUEBA ==================
            if (clienteRepository.findByCorreo("usuario@test.com").isEmpty()) {
                System.out.println("Creando usuario de prueba...");
                
                Cliente usuario = new Cliente();
                usuario.setNombre("Usuario Prueba");
                usuario.setCorreo("usuario@test.com");
                usuario.setClave(passwordEncoder.encode("123456"));
                usuario.setTelefono("3007654321");
                usuario.setActivo(true);
                usuario.setRol("ROLE_USER");
                
                clienteRepository.save(usuario);
                System.out.println("✓ Usuario de prueba creado: usuario@test.com / 123456");
            } else {
                System.out.println("✓ Usuario de prueba ya existe en la base de datos");
            }

            System.out.println("✓ Inicialización completada");
        };
    }
}