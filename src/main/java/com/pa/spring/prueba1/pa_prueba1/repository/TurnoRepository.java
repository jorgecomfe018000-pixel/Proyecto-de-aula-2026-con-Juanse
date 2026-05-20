package com.pa.spring.prueba1.pa_prueba1.repository;

import com.pa.spring.prueba1.pa_prueba1.model.Turno;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TurnoRepository extends MongoRepository<Turno, String> {

    // --- EXISTENTES ---
    @Query("{ 'estilista.$id': { $oid: ?0 } }")
    List<Turno> findByEstilistaIdEstilista(String estilistaId);

    @Query("{ 'estilista.$id': { $oid: ?0 }, 'estado': ?1 }")
    List<Turno> findByEstilistaIdEstilistaAndEstado(String estilistaId, Turno.EstadoTurno estado);

    @Query("{ 'estilista.$id': { $oid: ?0 }, 'fechaHora': ?1 }")
    List<Turno> findByEstilistaIdEstilistaAndFechaHora(String estilistaId, LocalDateTime fechaHora);

    @Query("{ 'estilista.$id': { $oid: ?0 }, 'estado': ?1, 'fechaHora': { $gte: ?2, $lte: ?3 } }")
    List<Turno> findByEstilistaIdEstilistaAndEstadoAndFechaHoraBetween(
            String estilistaId,
            Turno.EstadoTurno estado,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin
    );

    List<Turno> findByEstado(Turno.EstadoTurno estado);

    List<Turno> findByFechaHoraBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    // --- NUEVOS MÉTODOS ---
    @Override
    @NonNull
    List<Turno> findAll();

    @Query("{ 'estilista.$id': { $oid: ?0 } }")
    List<Turno> findByEstilistaConReservasYClientes(String estilistaId);

    // Método para consultar turnos por estilista, rango de fechas y estado
    @Query("{ 'estilista.$id': { $oid: ?0 }, 'fechaHora': { $gte: ?1, $lte: ?2 }, 'estado': ?3 }")
    List<Turno> findByEstilistaIdAndFechaHoraBetweenAndEstado(
            String estilistaId,
            LocalDateTime start,
            LocalDateTime end,
            Turno.EstadoTurno estado
    );
}
