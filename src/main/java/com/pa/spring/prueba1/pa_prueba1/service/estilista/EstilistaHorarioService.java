package com.pa.spring.prueba1.pa_prueba1.service.estilista;

import com.pa.spring.prueba1.pa_prueba1.model.Estilista;
import com.pa.spring.prueba1.pa_prueba1.model.Reserva;
import com.pa.spring.prueba1.pa_prueba1.model.SolicitudAusencia;
import com.pa.spring.prueba1.pa_prueba1.repository.ReservaRepository;
import com.pa.spring.prueba1.pa_prueba1.repository.SolicitudAusenciaRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EstilistaHorarioService {

    @Autowired
    private ReservaRepository reservaRepository;
    
    @Autowired
    private SolicitudAusenciaRepository solicitudAusenciaRepository;

    /**
     * Obtiene todas las reservas de un estilista para un mes específico.
     *
     * @param estilista         El estilista autenticado
     * @param fechaSeleccionada Fecha del mes deseado
     * @return Lista de reservas de ese mes
     */
    public List<Reserva> obtenerReservasPorMes(Estilista estilista, LocalDate fechaSeleccionada) {
        // Primer día del mes
        LocalDate inicioMes = fechaSeleccionada.withDayOfMonth(1);

        // Último día del mes
        LocalDate finMes = fechaSeleccionada.with(TemporalAdjusters.lastDayOfMonth());

        // Consulta al repositorio usando el rango de fechas
        return reservaRepository.findByEstilistaIdEstilistaAndFechaHoraTurnoBetween(
                estilista.getIdEstilista(),
                inicioMes.atStartOfDay(),
                finMes.atTime(23, 59, 59)
        );
    }
    
    /**
     * Obtiene las reservas de un estilista en un rango de fechas específico
     */
    public List<Reserva> obtenerReservasPorRango(String idEstilista, LocalDateTime inicio, LocalDateTime fin) {
        return reservaRepository.findByEstilistaIdEstilistaAndFechaHoraTurnoBetween(idEstilista, inicio, fin);
    }

    /**
     * Calcula las horas trabajadas en una semana
     */
    public int calcularHorasSemanales(Estilista estilista) {
        if (estilista.getHoraInicio() == null || estilista.getHoraFin() == null) {
            return 0;
        }
        
        int horasDiarias = estilista.getHoraFin().getHour() - estilista.getHoraInicio().getHour();
        
        // Restar tiempo de almuerzo si existe
        if (estilista.getHoraInicioAlmuerzo() != null && estilista.getHoraFinAlmuerzo() != null) {
            horasDiarias -= (estilista.getHoraFinAlmuerzo().getHour() - estilista.getHoraInicioAlmuerzo().getHour());
        }
        
        // Multiplicar por 6 días (asumiendo que trabaja 6 días a la semana)
        return horasDiarias * 6;
    }

    /**
     * Calcula el porcentaje de disponibilidad basado en reservas
     */
    public double calcularDisponibilidad(String idEstilista, LocalDate inicio, LocalDate fin, Estilista estilista) {
        LocalDateTime inicioMes = inicio.atStartOfDay();
        LocalDateTime finMes = fin.atTime(23, 59, 59);
        
        List<Reserva> reservas = reservaRepository.findByEstilistaIdEstilistaAndFechaHoraTurnoBetween(
                idEstilista, inicioMes, finMes);
        
        long reservasActivas = reservas.stream()
                .filter(r -> r.getEstado() != Reserva.EstadoReserva.CANCELADA)
                .count();
        
        if (reservasActivas == 0 || estilista.getHoraInicio() == null || estilista.getHoraFin() == null) {
            return 0.0;
        }
        
        long diasTrabajo = inicio.datesUntil(fin.plusDays(1))
                .filter(fecha -> fecha.getDayOfWeek() != estilista.getDiaLibre())
                .count();
        
        if (diasTrabajo == 0) {
            return 0.0;
        }
        
        int slotsDisponiblesPorDia = estilista.getHoraFin().getHour() - estilista.getHoraInicio().getHour();
        int totalSlotsDisponibles = (int) (slotsDisponiblesPorDia * diasTrabajo * 2); // 2 slots por hora
        
        if (totalSlotsDisponibles == 0) {
            return 0.0;
        }
        
        return Math.min(100.0, ((double) reservasActivas / totalSlotsDisponibles) * 100);
    }

    /**
     * Obtiene los días con ausencia aprobada en un rango de fechas
     */
    public List<LocalDate> obtenerDiasConAusencia(String idEstilista, LocalDate inicio, LocalDate fin) {
        List<SolicitudAusencia> ausencias = solicitudAusenciaRepository
                .findByEstilistaIdEstilistaAndEstado(idEstilista, SolicitudAusencia.EstadoSolicitud.APROBADA);
        
        List<LocalDate> diasConAusencia = new ArrayList<>();
        
        for (SolicitudAusencia ausencia : ausencias) {
            if (ausencia.getTipoAusencia() == SolicitudAusencia.TipoAusencia.DIA_COMPLETO) {
                LocalDate fechaAusencia = ausencia.getFechaInicio();
                while (!fechaAusencia.isAfter(ausencia.getFechaFin())) {
                    if (!fechaAusencia.isBefore(inicio) && !fechaAusencia.isAfter(fin)) {
                        diasConAusencia.add(fechaAusencia);
                    }
                    fechaAusencia = fechaAusencia.plusDays(1);
                }
            }
        }
        
        return diasConAusencia;
    }

    /**
     * Genera slots de tiempo disponibles para un día específico
     */
    public List<LocalTime> generarSlotsDisponibles(Estilista estilista, LocalDate fecha) {
        List<LocalTime> slots = new ArrayList<>();
        
        if (estilista.getHoraInicio() == null || estilista.getHoraFin() == null) {
            return slots;
        }
        
        // Verificar que no sea día libre
        if (fecha.getDayOfWeek() == estilista.getDiaLibre()) {
            return slots;
        }
        
        // Obtener reservas del día
        LocalDateTime inicioDia = fecha.atStartOfDay();
        LocalDateTime finDia = fecha.atTime(23, 59, 59);
        
        List<Reserva> reservasDia = reservaRepository.findByEstilistaIdEstilistaAndFechaHoraTurnoBetween(
                estilista.getIdEstilista(), inicioDia, finDia);
        
        LocalTime horaActual = estilista.getHoraInicio();
        int duracionSlot = estilista.getDuracionTurno() != null ? estilista.getDuracionTurno() : 30;
        
        while (horaActual.isBefore(estilista.getHoraFin())) {
            // Verificar si está en horario de almuerzo
            boolean esHoraAlmuerzo = false;
            if (estilista.getHoraInicioAlmuerzo() != null && estilista.getHoraFinAlmuerzo() != null) {
                esHoraAlmuerzo = !horaActual.isBefore(estilista.getHoraInicioAlmuerzo()) && 
                               horaActual.isBefore(estilista.getHoraFinAlmuerzo());
            }
            
            if (!esHoraAlmuerzo) {
                // Verificar si hay reserva en este horario
                LocalDateTime fechaHoraSlot = LocalDateTime.of(fecha, horaActual);
                boolean ocupado = reservasDia.stream()
                        .anyMatch(r -> r.getFechaHoraTurno().equals(fechaHoraSlot) && 
                                     r.getEstado() != Reserva.EstadoReserva.CANCELADA);
                
                if (!ocupado) {
                    slots.add(horaActual);
                }
            }
            
            horaActual = horaActual.plusMinutes(duracionSlot);
        }
        
        return slots;
    }
    
    /**
     * Verifica si un estilista está disponible en una fecha específica
     */
    public boolean estaEstilistaDisponible(Estilista estilista, LocalDate fecha) {
        // Verificar día libre
        if (fecha.getDayOfWeek() == estilista.getDiaLibre()) {
            return false;
        }
        
        // Verificar ausencias aprobadas
        List<SolicitudAusencia> ausencias = solicitudAusenciaRepository
                .findByEstilistaIdEstilistaAndEstado(estilista.getIdEstilista(), 
                        SolicitudAusencia.EstadoSolicitud.APROBADA);
        
        for (SolicitudAusencia ausencia : ausencias) {
            if (ausencia.getTipoAusencia() == SolicitudAusencia.TipoAusencia.DIA_COMPLETO) {
                if (!fecha.isBefore(ausencia.getFechaInicio()) && 
                    !fecha.isAfter(ausencia.getFechaFin())) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * Cuenta el total de reservas activas en un rango de fechas
     */
    public long contarReservasActivas(String idEstilista, LocalDateTime inicio, LocalDateTime fin) {
        return reservaRepository.findByEstilistaIdEstilistaAndFechaHoraTurnoBetween(idEstilista, inicio, fin)
                .stream()
                .filter(r -> r.getEstado() != Reserva.EstadoReserva.CANCELADA)
                .count();
    }
    
    /**
     * Obtiene las reservas de un día específico
     */
    public List<Reserva> obtenerReservasPorDia(String idEstilista, LocalDate fecha) {
        LocalDateTime inicioDia = fecha.atStartOfDay();
        LocalDateTime finDia = fecha.atTime(23, 59, 59);
        
        return reservaRepository.findByEstilistaIdEstilistaAndFechaHoraTurnoBetween(
                idEstilista, inicioDia, finDia)
                .stream()
                .sorted((r1, r2) -> r1.getFechaHoraTurno().compareTo(r2.getFechaHoraTurno()))
                .collect(Collectors.toList());
    }
}
