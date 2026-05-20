package com.pa.spring.prueba1.pa_prueba1.repository;

import com.pa.spring.prueba1.pa_prueba1.model.ServicioBelleza;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServicioRepository extends MongoRepository<ServicioBelleza, String> {
    
    // MongoRepository ya proporciona métodos CRUD como:
    // - findById(String id)
    // - findAll()
    // - save(ServicioBelleza servicioBelleza)
    // - deleteById(String id)
}
