package com.pa.spring.prueba1.pa_prueba1.repository;

import com.pa.spring.prueba1.pa_prueba1.model.SolicitudAusencia;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SolicitudAusenciaRepository extends MongoRepository<SolicitudAusencia, String> {
    
    // antiguo nombre de campo antes de cambio a 'estilista'
    @Query("{ 'barbero.$id': ?0 }")
    List<SolicitudAusencia> findByBarberoIdBarbero(String idBarbero);
    
    @Query("{ 'barbero.$id': ?0, 'estado': ?1 }")
    List<SolicitudAusencia> findByBarberoIdBarberoAndEstado(String idBarbero, SolicitudAusencia.EstadoSolicitud estado);
    
    // nuevos métodos usando el campo estilista para la lógica actual
    @Query("{ 'estilista.$id': ?0 }")
    List<SolicitudAusencia> findByEstilistaIdEstilista(String idEstilista);
    
    @Query("{ 'estilista.$id': ?0, 'estado': ?1 }")
    List<SolicitudAusencia> findByEstilistaIdEstilistaAndEstado(String idEstilista, SolicitudAusencia.EstadoSolicitud estado);
    
    List<SolicitudAusencia> findByEstado(SolicitudAusencia.EstadoSolicitud estado);
    
    @Query("{ 'barbero.$id': ?0, 'estado': ?1, 'fechaInicio': { $gte: ?2, $lte: ?3 } }")
    List<SolicitudAusencia> findByBarberoIdBarberoAndEstadoAndFechaInicioBetween(
            String idBarbero, 
            SolicitudAusencia.EstadoSolicitud estado,
            LocalDate fechaInicio,
            LocalDate fechaFin
    );
    
    default List<SolicitudAusencia> findAllPendientes() {
        return findByEstado(SolicitudAusencia.EstadoSolicitud.PENDIENTE);
    }
}
