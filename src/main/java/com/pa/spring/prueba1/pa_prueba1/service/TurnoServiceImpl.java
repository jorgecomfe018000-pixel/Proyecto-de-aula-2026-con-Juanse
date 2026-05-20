package com.pa.spring.prueba1.pa_prueba1.service;

import com.pa.spring.prueba1.pa_prueba1.model.Estilista;
import com.pa.spring.prueba1.pa_prueba1.model.Turno;
import com.pa.spring.prueba1.pa_prueba1.repository.TurnoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Objects;

@Service
public class TurnoServiceImpl implements TurnoService {

    @Autowired
    private TurnoRepository turnoRepository;
    
    @Autowired
    private ReservaService reservaService;

    public List<Turno> obtenerPorEstilista(String idEstilista) {
        return turnoRepository.findByEstilistaIdEstilista(Objects.requireNonNull(idEstilista));
    }

    @Override
    public List<Turno> obtenerTurnosPorEstilista(String idEstilista) {
        return turnoRepository.findByEstilistaIdEstilista(
                Objects.requireNonNull(idEstilista, "El ID del estilista no puede ser nulo"));
    }

    @Override
    public List<Turno> obtenerTurnosDisponiblesPorEstilista(String idEstilista) {
        return turnoRepository.findByEstilistaIdEstilistaAndEstado(
            Objects.requireNonNull(idEstilista, "El ID del estilista no puede ser nulo"),
            Turno.EstadoTurno.DISPONIBLE);
    }

    @Override
    public List<Turno> obtenerTurnosNoDisponiblesPorEstilista(String idEstilista) {
        return turnoRepository.findByEstilistaIdEstilistaAndEstado(
            Objects.requireNonNull(idEstilista, "El ID del estilista no puede ser nulo"),
            Turno.EstadoTurno.NO_DISPONIBLE);
    }

    @Override
    public List<Turno> obtenerTurnosPorEstilistaYEstado(String idEstilista, Turno.EstadoTurno estado) {
        return turnoRepository.findByEstilistaIdEstilistaAndEstado(
            Objects.requireNonNull(idEstilista, "El ID del estilista no puede ser nulo"), 
            estado);
    }

    @Override
    public List<Turno> obtenerTurnosDisponibles() {
        return turnoRepository.findByEstado(Turno.EstadoTurno.DISPONIBLE);
    }
    
    @Override
    public List<Turno> obtenerTurnosNoDisponibles() {
        return turnoRepository.findByEstado(Turno.EstadoTurno.NO_DISPONIBLE);
    }

    @Override
    public Turno guardarTurno(Turno turno) {
        return turnoRepository.save(Objects.requireNonNull(turno));
    }

    @Override
    public void eliminarTurno(String id) {
        turnoRepository.deleteById(Objects.requireNonNull(id));
    }

    @Override
    public Turno obtenerPorId(String id) {
        return turnoRepository.findById(Objects.requireNonNull(id)).orElse(null);
    }
    
    @Override
    @Transactional
    public List<Turno> generarTurnosDisponibles(Estilista estilista, LocalDate fechaInicio, LocalDate fechaFin) {
        List<Turno> turnosGenerados = new ArrayList<>();
        
        if (estilista == null) {
            System.out.println("Error: Estilista es null");
            return turnosGenerados;
        }
        
        System.out.println("Generando turnos para estilista: " + estilista.getNombre() + 
                          " desde " + fechaInicio + " hasta " + fechaFin);
        
        try {
            List<Turno> turnosExistentes = turnoRepository.findByEstilistaIdEstilistaAndEstadoAndFechaHoraBetween(
                estilista.getIdEstilista(), 
                Turno.EstadoTurno.DISPONIBLE,
                fechaInicio.atStartOfDay(),
                fechaFin.atTime(23, 59, 59)
            );
        
            System.out.println("Turnos existentes encontrados: " + turnosExistentes.size());
        
            List<LocalDateTime> fechasHorasExistentes = turnosExistentes.stream()
                .map(Turno::getFechaHora)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
            LocalDate fechaActual = fechaInicio;
            while (!fechaActual.isAfter(fechaFin)) {
                if (estilista.getDiaLibre() == null || fechaActual.getDayOfWeek() != estilista.getDiaLibre()) {
                    LocalTime horaInicio = estilista.getHoraInicio();
                    LocalTime horaFin = estilista.getHoraFin();
                    LocalTime horaInicioAlmuerzo = estilista.getHoraInicioAlmuerzo();
                    LocalTime horaFinAlmuerzo = estilista.getHoraFinAlmuerzo();
                    Integer duracionTurno = estilista.getDuracionTurno();
                
                    if (horaInicio == null || horaFin == null || duracionTurno == null) {
                        System.out.println("Error: Faltan datos de horario para el estilista " + estilista.getNombre());
                        fechaActual = fechaActual.plusDays(1);
                        continue;
                    }
                
                    LocalTime horaActual = horaInicio;
                
                    while (!horaActual.isAfter(horaFin.minusMinutes(duracionTurno))) {
                        if (horaInicioAlmuerzo == null || horaFinAlmuerzo == null ||
                            horaActual.isBefore(horaInicioAlmuerzo) || 
                            !horaActual.isBefore(horaFinAlmuerzo)) {
                        
                            LocalDateTime fechaHoraTurno = LocalDateTime.of(fechaActual, horaActual);
                        
                            if (!fechasHorasExistentes.contains(fechaHoraTurno) &&
                                turnoRepository.findByEstilistaIdEstilistaAndFechaHora(
                                        estilista.getIdEstilista(), fechaHoraTurno).isEmpty()) {
                                try {
                                    Turno nuevoTurno = new Turno();
                                    nuevoTurno.setFechaHora(fechaHoraTurno);
                                    nuevoTurno.setEstilista(estilista);
                                    nuevoTurno.setEstado(Turno.EstadoTurno.DISPONIBLE);
                                
                                    Turno turnoGuardado = turnoRepository.save(nuevoTurno);
                                    turnosGenerados.add(turnoGuardado);
                                } catch (Exception e) {
                                    System.out.println("Error al guardar turno para " + fechaHoraTurno + ": " + e.getMessage());
                                }
                            }
                        }
                    
                        horaActual = horaActual.plusMinutes(duracionTurno);
                    }
                }
            
                fechaActual = fechaActual.plusDays(1);
            }
        
            System.out.println("Turnos generados exitosamente: " + turnosGenerados.size());
        
        } catch (Exception e) {
            System.out.println("Error general al generar turnos: " + e.getMessage());
            e.printStackTrace();
        }
    
        return turnosGenerados;
    }
    
    @Override
    public boolean esTurnoDisponible(String idTurno) {
        Optional<Turno> optTurno = turnoRepository.findById(Objects.requireNonNull(idTurno));
        return optTurno.isPresent() && optTurno.get().getEstado() == Turno.EstadoTurno.DISPONIBLE;
    }
    
    @Override
    @Transactional
    public Turno marcarTurnoNoDisponible(String idTurno) {
        Optional<Turno> optTurno = turnoRepository.findById(Objects.requireNonNull(idTurno));
        if (!optTurno.isPresent()) {
            return null;
        }
        
        Turno turno = optTurno.get();
        turno.setEstado(Turno.EstadoTurno.NO_DISPONIBLE);
        
        return turnoRepository.save(turno);
    }
    
    @Override
    @Transactional
    public Turno marcarTurnoDisponible(String idTurno) {
        Optional<Turno> optTurno = turnoRepository.findById(Objects.requireNonNull(idTurno));
        if (!optTurno.isPresent()) {
            return null;
        }
        
        if (reservaService.existeReservaParaTurno(idTurno)) {
            return null;
        }
        
        Turno turno = optTurno.get();
        turno.setEstado(Turno.EstadoTurno.DISPONIBLE);
        
        return turnoRepository.save(turno);
    }
    
    @Override
    public List<Turno> obtenerTodos() {
        return turnoRepository.findAll();
    }
    
    @Override
    @Transactional
    public Turno completarTurno(String idTurno) {
        Optional<Turno> optTurno = turnoRepository.findById(Objects.requireNonNull(idTurno));
        if (!optTurno.isPresent()) {
            return null;
    }

    Turno turno = optTurno.get();
    turno.setEstado(Turno.EstadoTurno.NO_DISPONIBLE); // o COMPLETADO si luego lo agregas

    return turnoRepository.save(turno);
}
    
    @Override
    @Transactional
    public Turno cancelarReserva(String idTurno) {
        Optional<Turno> optTurno = turnoRepository.findById(Objects.requireNonNull(idTurno));
        if (!optTurno.isPresent()) {
            return null;
        }
        
        Turno turno = optTurno.get();
        turno.setEstado(Turno.EstadoTurno.DISPONIBLE);
        
        return turnoRepository.save(turno);
    }
    
    @Override
    public List<Turno> obtenerTurnosDisponiblesPorEstilistaYFecha(String idEstilista, LocalDate fecha) {
        LocalDateTime inicioDelDia = fecha.atStartOfDay();
        LocalDateTime finDelDia = fecha.atTime(23, 59, 59);
        
        return turnoRepository.findByEstilistaIdEstilistaAndEstadoAndFechaHoraBetween(
            Objects.requireNonNull(idEstilista), 
            Turno.EstadoTurno.DISPONIBLE,
            inicioDelDia,
            finDelDia
        );
    }

    // 🔥 MÉTODO CORREGIDO: ahora convierte correctamente las fechas a UTC para MongoDB
    @Override
    public List<Turno> obtenerTurnosSemana(String idEstilista, LocalDateTime inicio, LocalDateTime fin, Turno.EstadoTurno estado) {
        Objects.requireNonNull(idEstilista, "El ID del estilista no puede ser nulo");
        Objects.requireNonNull(inicio, "La fecha de inicio no puede ser nula");
        Objects.requireNonNull(fin, "La fecha de fin no puede ser nula");
        Objects.requireNonNull(estado, "El estado no puede ser nulo");

        ZoneId bogotaZone = ZoneId.of("America/Bogota");
        ZonedDateTime inicioZoned = inicio.atZone(bogotaZone);
        ZonedDateTime finZoned = fin.atZone(bogotaZone);

        LocalDateTime inicioUtc = inicioZoned.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime finUtc = finZoned.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();

        return turnoRepository.findByEstilistaIdAndFechaHoraBetweenAndEstado(
            idEstilista,
            inicioUtc,
            finUtc,
            estado
        );
    }
}
