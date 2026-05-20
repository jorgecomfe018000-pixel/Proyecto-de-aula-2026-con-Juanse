package com.pa.spring.prueba1.pa_prueba1.service;

import com.pa.spring.prueba1.pa_prueba1.model.Estilista;
import com.pa.spring.prueba1.pa_prueba1.model.Turno;
import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.List;

public interface TurnoService {
    List<Turno> obtenerTurnosPorEstilista(String idEstilista);
    List<Turno> obtenerTurnosDisponiblesPorEstilista(String idEstilista);
    List<Turno> obtenerTurnosNoDisponiblesPorEstilista(String idEstilista);
    List<Turno> obtenerTurnosPorEstilistaYEstado(String idEstilista, Turno.EstadoTurno estado);
    List<Turno> obtenerTurnosDisponibles();
    List<Turno> obtenerTurnosNoDisponibles();
    Turno guardarTurno(Turno turno);
    void eliminarTurno(String id);
    Turno obtenerPorId(String id);
    
    // Método para generar turnos disponibles para un estilista en un rango de fechas
    List<Turno> generarTurnosDisponibles(Estilista estilista, LocalDate fechaInicio, LocalDate fechaFin);
    
    // Método para verificar si un turno está disponible
    boolean esTurnoDisponible(String idTurno);
    
    // Método para marcar un turno como no disponible
    Turno marcarTurnoNoDisponible(String idTurno);
    
    // Método para marcar un turno como disponible
    Turno marcarTurnoDisponible(String idTurno);
    
    // Método para obtener todos los turnos
    List<Turno> obtenerTodos();
    
    // Método para completar un turno (usado cuando se completa una reserva)
    Turno completarTurno(String idTurno);
    
    // Método para cancelar una reserva (liberar el turno)
    Turno cancelarReserva(String idTurno);
    
    // Método para obtener turnos disponibles por estilista y fecha
    List<Turno> obtenerTurnosDisponiblesPorEstilistaYFecha(String idEstilista, LocalDate fecha);
    
    // Método para obtener turnos de una semana específica para un estilista
    List<Turno> obtenerTurnosSemana(String idEstilista, LocalDateTime inicio, LocalDateTime fin, Turno.EstadoTurno estado);
}
