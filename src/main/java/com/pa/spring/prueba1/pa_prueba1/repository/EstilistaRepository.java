package com.pa.spring.prueba1.pa_prueba1.repository;

import com.pa.spring.prueba1.pa_prueba1.model.Estilista;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EstilistaRepository extends MongoRepository<Estilista, String> {

    // ==================== MÉTODOS ORIGINALES ====================
        // findAll() ya está definido en MongoRepository y maneja correctamente la nulidad.
    
    // ==================== MÉTODOS PARA AUTENTICACIÓN ====================
    
    Optional<Estilista> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    Optional<Estilista> findByEmailAndActivoTrue(String email);

    Optional<Estilista> findByTelefono(String telefono);

    boolean existsByTelefono(String telefono);
    
    // ==================== MÉTODOS PARA GESTIÓN ====================
    
    List<Estilista> findByActivoTrue();
    
    List<Estilista> findByEspecialidad(String especialidad);
    
    @Query("{ $or: [ { 'nombre': { $regex: ?0, $options: 'i' } }, { 'apellido': { $regex: ?0, $options: 'i' } } ] }")
    List<Estilista> buscarPorNombre(String busqueda);
    
    @Query("{ 'activo': true, 'idEstilista': { $nin: ?#{@solicitudAusenciaRepository.findIdsEstilistasConAusenciaEnFecha(?0)} } }")
    List<Estilista> findEstilistasDisponiblesEnFecha(LocalDate fecha);
    
    long countByActivoTrue();
    
    @Query("{ 'idEstilista': { $in: ?#{@reservaRepository.findIdsEstilistaPorReservasEnPeriodo(?0, ?1)} } }")
    List<Estilista> obtenerEstilistasConMasReservas(LocalDateTime inicio, LocalDateTime fin);
    
    @org.springframework.data.mongodb.repository.Update("{'$set': {'activo': ?1}}")
    @Query("{'_id': ?0}")
    int actualizarEstado(String id, boolean activo);
}
