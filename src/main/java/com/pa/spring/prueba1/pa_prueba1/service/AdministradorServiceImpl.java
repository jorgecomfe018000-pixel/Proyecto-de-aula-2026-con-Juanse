package com.pa.spring.prueba1.pa_prueba1.service;

import com.pa.spring.prueba1.pa_prueba1.model.Administrador;
import com.pa.spring.prueba1.pa_prueba1.repository.AdministradorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class AdministradorServiceImpl implements AdministradorService {

    @Autowired
    private AdministradorRepository administradorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // ✅ usar la interfaz en lugar de BCryptPasswordEncoder

    @Override
    public List<Administrador> obtenerTodos() {
        return administradorRepository.findAll();
    }

    @Override
    public Administrador obtenerPorId(String id) {
        Objects.requireNonNull(id, "El ID no puede ser nulo");
        Optional<Administrador> optAdmin = administradorRepository.findById(id);
        return optAdmin.orElse(null);
    }

    @Override
    public Administrador guardar(Administrador administrador) {
        if (administrador.getClave() != null) {
            administrador.setClave(passwordEncoder.encode(administrador.getClave()));
        }
        return administradorRepository.save(administrador);
    }

    @Override
    public void eliminar(String id) {
        Objects.requireNonNull(id, "El ID no puede ser nulo");
        administradorRepository.deleteById(id);
    }

    @Override
    public Administrador verificarCredenciales(String usuario, String password) {
        System.out.println("🔎 Verificando credenciales para usuario: " + usuario);

        Optional<Administrador> adminOpt = administradorRepository.findByCorreo(usuario);

        if (adminOpt.isPresent()) {
            Administrador admin = adminOpt.get();

            // Usar getClaveOPassword() para compatibilidad con datos antiguos
            if (passwordEncoder.matches(password, admin.getClaveOPassword())) {
                System.out.println("✅ Login correcto para administrador: " + admin.getNombre());
                return admin;
            } else {
                System.out.println("❌ Contraseña incorrecta para usuario: " + usuario);
            }
        } else {
            System.out.println("❌ No existe un administrador con usuario: " + usuario);
        }

        return null;
    }

    @Override
    public boolean existeAdministrador(String usuario) {
        return administradorRepository.findByCorreo(usuario).isPresent();
    }
}
