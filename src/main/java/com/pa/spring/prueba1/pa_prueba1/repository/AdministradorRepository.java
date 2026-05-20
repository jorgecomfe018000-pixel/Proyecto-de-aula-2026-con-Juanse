package com.pa.spring.prueba1.pa_prueba1.repository;

import com.pa.spring.prueba1.pa_prueba1.model.Administrador;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface AdministradorRepository extends MongoRepository<Administrador, String> {

    Optional<Administrador> findByCorreo(String correo);

    boolean existsByCorreo(String correo);
}