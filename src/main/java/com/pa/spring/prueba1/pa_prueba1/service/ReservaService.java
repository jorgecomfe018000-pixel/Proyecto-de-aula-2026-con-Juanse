package com.pa.spring.prueba1.pa_prueba1.service;

import com.pa.spring.prueba1.pa_prueba1.model.Reserva;
import java.time.LocalDate;

import java.util.List;

public interface ReservaService {

    // Obtiene todas las reservas
    List<Reserva> obtenerTodas();

    // Obtiene una reserva por su ID
    Reserva obtenerPorId(String id);

    // Obtiene las reservas de un cliente específico
    List<Reserva> obtenerPorCliente(String idCliente);

    // Obtiene las reservas de un estilista específico
    List<Reserva> obtenerPorEstilista(String idEstilista);

    // Obtiene las reservas filtradas por su estado
    List<Reserva> obtenerPorEstado(Reserva.EstadoReserva estado);

    // Obtiene las reservas dentro de un rango de fechas
    List<Reserva> obtenerPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin);

    // Crea una nueva reserva, relacionando cliente, estilista, servicio, turno y comentarios
    Reserva crearReserva(String idCliente, String idEstilista, String idServicioBelleza, String idTurno, String comentarios);

    // Marca una reserva como completada
    Reserva completarReserva(String idReserva);

    // Cancela una reserva
    Reserva cancelarReserva(String idReserva);

    // Elimina una reserva por su ID
    void eliminarReserva(String idReserva);

    // Verifica si existe una reserva para un turno específico
    boolean existeReservaParaTurno(String idTurno);
}
