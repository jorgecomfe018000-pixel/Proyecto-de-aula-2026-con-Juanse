package com.pa.spring.prueba1.pa_prueba1.repository;

import com.pa.spring.prueba1.pa_prueba1.model.CodigoVerificacion;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CodigoVerificacionRepository extends MongoRepository<CodigoVerificacion, String> {
    Optional<CodigoVerificacion> findFirstByIdentificadorOrderByCreadoEnDesc(String identificador);
    void deleteByIdentificador(String identificador);
}
