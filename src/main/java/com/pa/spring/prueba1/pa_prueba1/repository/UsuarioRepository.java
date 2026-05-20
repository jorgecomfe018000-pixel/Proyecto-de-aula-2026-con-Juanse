package com.pa.spring.prueba1.pa_prueba1.repository;

import com.pa.spring.prueba1.pa_prueba1.model.Usuario;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface UsuarioRepository extends MongoRepository<Usuario, String> {
    Optional<Usuario> findByUsername(String username);
    Optional<Usuario> findByEmail(String email);
}
