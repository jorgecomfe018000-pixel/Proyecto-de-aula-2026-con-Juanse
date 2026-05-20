package com.pa.spring.prueba1.pa_prueba1.repository;

import com.pa.spring.prueba1.pa_prueba1.model.Estilista;
import com.pa.spring.prueba1.pa_prueba1.model.Cliente;
import com.pa.spring.prueba1.pa_prueba1.model.Turno;
import com.pa.spring.prueba1.pa_prueba1.model.Reserva;
import com.pa.spring.prueba1.pa_prueba1.model.ServicioBelleza;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservaRepository extends MongoRepository<Reserva, String> {

        // ==================== MÉTODOS ORIGINALES ====================

    // Consultas derivadas por referencia (usar objeto Cliente/Barbero/Turno)
    List<Reserva> findByCliente(Cliente cliente);

    List<Reserva> findByEstilista(Estilista estilista);
    
    List<Reserva> findByServicioBelleza(ServicioBelleza servicioBelleza);

        List<Reserva> findByEstado(Reserva.EstadoReserva estado);

        List<Reserva> findByFechaHoraTurnoBetween(LocalDateTime inicio, LocalDateTime fin);

        @Query("{ 'estilista.$id': ?0, 'estado': ?1 }")
        List<Reserva> findByEstilistaIdEstilistaAndEstado(String idEstilista, Reserva.EstadoReserva estado);

    List<Reserva> findByTurno(Turno turno);

    @Query(value = "{ 'cliente.$id': ?0 }", exists = true)
    boolean existsByCliente_IdCliente(String idCliente);

        long countByEstado(Reserva.EstadoReserva estado);

        // ==================== MÉTODOS NUEVOS PARA PANEL DEL ESTILISTA ====================

        List<Reserva> findByEstilistaAndFechaHoraTurnoBetweenOrderByFechaHoraTurno(
                        Estilista estilista,
                        LocalDateTime inicio,
                        LocalDateTime fin);

        List<Reserva> findByEstilistaOrderByFechaHoraTurnoDesc(Estilista estilista);

        List<Reserva> findByEstilistaAndEstadoOrderByFechaHoraTurnoDesc(
                        Estilista estilista,
                        Reserva.EstadoReserva estado);

        @Query("{ 'estilista': ?0, $or: [ { 'cliente.nombre': { $regex: ?1, $options: 'i' } }, { 'cliente.correo': { $regex: ?1, $options: 'i' } } ] }")
        List<Reserva> buscarPorEstilistaYCliente(Estilista estilista, String busqueda);

        long countByEstilistaAndFechaHoraTurnoBetween(
                        Estilista estilista,
                        LocalDateTime inicio,
                        LocalDateTime fin);

        @Query("{ 'estilista.$id': ?0, 'fechaHoraTurno': { $gte: ?1, $lt: ?2 } }")
        List<Reserva> findByEstilistaIdEstilistaAndFechaHoraTurnoBetween(
                        String idEstilista, LocalDateTime fechaInicio, LocalDateTime fechaFin);

        Reserva findFirstByEstilistaAndEstadoAndFechaHoraTurnoAfterOrderByFechaHoraTurno(
                        Estilista estilista,
                        Reserva.EstadoReserva estado,
                        LocalDateTime fechaHora);

        @Query(value = "{ 'estilista': ?0, 'estado': ?1, 'fechaHoraTurno': { $gte: ?2, $lt: ?3 } }", count = true)
        long contarReservasHoyPorEstado(Estilista estilista, Reserva.EstadoReserva estado, LocalDateTime inicioDelDia, LocalDateTime finDelDia);

        @Query("{ 'estilista': ?0, 'fechaHoraTurno': { $gte: ?1, $lt: ?2 } }")
        List<Reserva> obtenerReservasPorFecha(Estilista estilista, LocalDateTime inicioDelDia, LocalDateTime finDelDia);

        boolean existsByEstilistaAndFechaHoraTurno(
                        Estilista estilista,
                        LocalDateTime fechaHora);

        @Query(value = "{ 'estilista': ?0 }", fields = "{ 'estado': 1 }")
        List<Reserva> findByEstilistaForGrouping(Estilista estilista);

        @Query("{ 'estilista': ?0, 'estado': ?1, 'fechaHoraTurno': { $gte: ?2, $lte: ?3 } }")
        List<Reserva> findForIngresoCalculation(Estilista estilista, Reserva.EstadoReserva estado, LocalDateTime inicio, LocalDateTime fin);

        @Query(value = "{ 'estilista': ?0 }", sort = "{ 'fechaHoraTurno': -1 }")
        List<Reserva> findTop10ByEstilistaOrderByFechaHoraTurnoDesc(Estilista estilista);

        @Query("{ 'estilista': ?0, 'estado': 'PENDIENTE', 'fechaHoraTurno': { $gte: ?1 } }")
        List<Reserva> obtenerReservasPendientes(Estilista estilista, LocalDateTime ahora);

        @Query("{ 'estilista.$id': ?0, 'fechaHoraTurno': { $gte: ?1, $lt: ?2 } }")
        List<Reserva> obtenerReservasDeHoy(String idEstilista, LocalDateTime inicioDelDia, LocalDateTime finDelDia);

        default long countAllReservas() {
            return count();
        }
}
