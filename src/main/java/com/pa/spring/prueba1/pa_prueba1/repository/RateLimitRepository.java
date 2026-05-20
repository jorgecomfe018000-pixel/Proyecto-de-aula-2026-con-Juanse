package com.pa.spring.prueba1.pa_prueba1.repository;

import com.pa.spring.prueba1.pa_prueba1.model.RateLimit;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RateLimitRepository extends MongoRepository<RateLimit, String> {
    Optional<RateLimit> findByClave(String clave);
}
