package com.pa.spring.prueba1.pa_prueba1.repository;

import com.pa.spring.prueba1.pa_prueba1.model.Cliente;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends MongoRepository<Cliente, String> {

    Optional<Cliente> findByCorreo(String correo);

    boolean existsByCorreo(String correo);

    Optional<Cliente> findByTelefono(String telefono);

    boolean existsByTelefono(String telefono);
}
