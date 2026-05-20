package com.pa.spring.prueba1.pa_prueba1.service;

import com.pa.spring.prueba1.pa_prueba1.model.*;
import com.pa.spring.prueba1.pa_prueba1.repository.*;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ReservaServiceImpl implements ReservaService {

    private static final Logger logger = LoggerFactory.getLogger(ReservaServiceImpl.class);

    @Autowired
    private ReservaRepository reservaRepository;
    
    @Autowired
    private ClienteRepository clienteRepository;
    
    @Autowired
    private EstilistaRepository estilistaRepository;
    
    @Autowired
    private ServicioRepository servicioRepository;
    
    @Autowired
    private TurnoRepository turnoRepository;
    
    // Obtener todas las reservas
    @Override
    public List<Reserva> obtenerTodas() {
        List<Reserva> reservas = reservaRepository.findAll();
        logger.info("ReservaService.obtenerTodas(): Encontradas {} reservas", reservas.size());
        return reservas;
    }
    
    // Obtener una reserva por su ID
    @Override
    public Reserva obtenerPorId(String id) {
        Objects.requireNonNull(id, "El ID no puede ser nulo");
        return reservaRepository.findById(id).orElse(null);
    }
    
    // Obtener todas las reservas de un cliente específico
    @Override
    public List<Reserva> obtenerPorCliente(String idCliente) {
        Objects.requireNonNull(idCliente, "El ID del cliente no puede ser nulo");
        logger.debug("ReservaService.obtenerPorCliente(): Buscando cliente con ID: {}", idCliente);
        Cliente cliente = clienteRepository.findById(idCliente).orElse(null);
        if (cliente == null) {
            logger.warn("Cliente no encontrado con ID: {}", idCliente);
            return List.of();
        }
        List<Reserva> reservas = reservaRepository.findByCliente(cliente);
        logger.info("Encontradas {} reservas para cliente: {}", reservas.size(), idCliente);
        reservas.forEach(r -> logger.debug(" - Reserva ID: {}, Estado: {}", r.getIdReserva(), r.getEstado()));
        return reservas;
    }
    
    // Obtener todas las reservas de un estilista específico
    @Override
    public List<Reserva> obtenerPorEstilista(String idEstilista) {
        Objects.requireNonNull(idEstilista, "El ID del estilista no puede ser nulo");
        Estilista estilista = estilistaRepository.findById(idEstilista).orElse(null);
        if (estilista == null) {
            logger.warn("Estilista no encontrado con ID: {}", idEstilista);
            return List.of();
        }
        return reservaRepository.findByEstilista(estilista);
    }
    
    // Obtener reservas por su estado (PENDIENTE, COMPLETADA, CANCELADA)
    @Override
    public List<Reserva> obtenerPorEstado(Reserva.EstadoReserva estado) {
        return reservaRepository.findByEstado(estado);
    }
    
    // Obtener reservas dentro de un rango de fechas
    @Override
    public List<Reserva> obtenerPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin = fechaFin.atTime(LocalTime.MAX);
        return reservaRepository.findByFechaHoraTurnoBetween(inicio, fin);
    }
    
    // Crear una nueva reserva con las relaciones cliente, estilista, servicio, y turno
    @Override
@Transactional
public Reserva crearReserva(String idCliente, String idEstilista, String idServicioBelleza, String idTurno, String comentarios) {
    try {
        Objects.requireNonNull(idCliente, "El ID del cliente no puede ser nulo");
        Objects.requireNonNull(idEstilista, "El ID del estilista no puede ser nulo");
        Objects.requireNonNull(idServicioBelleza, "El ID del servicio no puede ser nulo");
        Objects.requireNonNull(idTurno, "El ID del turno no puede ser nulo");
        
        // Buscar entidades necesarias
        Cliente cliente = clienteRepository.findById(idCliente).orElse(null);
        Estilista estilista = estilistaRepository.findById(idEstilista).orElse(null);
        ServicioBelleza servicioBelleza = servicioRepository.findById(idServicioBelleza).orElse(null);
        Turno turno = turnoRepository.findById(idTurno).orElse(null);

        if (cliente == null || estilista == null || servicioBelleza == null || turno == null) {
            logger.warn("Error: Falta alguna entidad requerida (cliente={}, estilista={}, servicioBelleza={}, turno={})",
                    cliente != null ? cliente.getIdCliente() : null,
                    estilista != null ? estilista.getIdEstilista() : null,
                    servicioBelleza != null ? servicioBelleza.getId() : null,
                    turno != null ? turno.getIdTurno() : null);
            return null;
        }

        // Evitar reservas duplicadas para el mismo turno
        boolean yaExiste = reservaRepository
                .findByTurno(turno)
                .stream()
                .anyMatch(r -> r.getEstado() == Reserva.EstadoReserva.PENDIENTE);
        if (yaExiste) {
            logger.info("Ya existe una reserva pendiente para el turno {}", idTurno);
            return null;
        }

        // Validar disponibilidad
        if (turno.getEstado() != Turno.EstadoTurno.DISPONIBLE) {
            System.out.println("Error: El turno no está disponible");
            return null;
        }

        // Crear y guardar la reserva
        Reserva reserva = new Reserva();
        reserva.setCliente(cliente);
        reserva.setEstilista(estilista);
        reserva.setServicioBelleza(servicioBelleza);
        reserva.setTurno(turno);
        reserva.setFechaHoraReserva(LocalDateTime.now());
        reserva.setFechaHoraTurno(turno.getFechaHora());
        reserva.setEstado(Reserva.EstadoReserva.PENDIENTE);
        reserva.setComentarios(comentarios);

    // Guardar la reserva primero
    Reserva reservaGuardada = reservaRepository.save(reserva);
    logger.info("Reserva guardada con ID: {} (cliente={}, estilista={})",
        reservaGuardada.getIdReserva(),
        reservaGuardada.getCliente() != null ? reservaGuardada.getCliente().getIdCliente() : null,
        reservaGuardada.getEstilista() != null ? reservaGuardada.getEstilista().getIdEstilista() : null);

    // Luego actualizar el turno sin cascada
    turno.setEstado(Turno.EstadoTurno.NO_DISPONIBLE);
    turnoRepository.save(turno);

    logger.info("Reserva creada correctamente con ID: {}", reservaGuardada.getIdReserva());
    return reservaGuardada;
    } catch (Exception e) {
        System.out.println("❌ Error al crear la reserva: " + e.getMessage());
        e.printStackTrace();
        return null;
    }
    }

    // Completar una reserva (cambiar su estado a COMPLETADA)
    @Override
    @Transactional
    public Reserva completarReserva(String idReserva) {
        Objects.requireNonNull(idReserva, "El ID de la reserva no puede ser nulo");
        Optional<Reserva> optReserva = reservaRepository.findById(idReserva);
        if (optReserva.isEmpty()) {
            return null;
        }
        
        Reserva reserva = optReserva.get();
        
        // Verificar que la reserva esté pendiente
        if (reserva.getEstado() != Reserva.EstadoReserva.PENDIENTE) {
            return null;
        }
        
        // Actualizar el estado de la reserva
        reserva.setEstado(Reserva.EstadoReserva.COMPLETADA);
        
        return reservaRepository.save(reserva);
    }
    
    // Cancelar una reserva (cambiar su estado a CANCELADA y liberar el turno)
    @Override
    @Transactional
    public Reserva cancelarReserva(String idReserva) {
        Objects.requireNonNull(idReserva, "El ID de la reserva no puede ser nulo");
        Optional<Reserva> optReserva = reservaRepository.findById(idReserva);
        if (optReserva.isEmpty()) {
            return null;
        }
        
        Reserva reserva = optReserva.get();
        
        // Verificar que la reserva esté pendiente
        if (reserva.getEstado() != Reserva.EstadoReserva.PENDIENTE) {
            return null;
        }
        
        // Actualizar el estado de la reserva
        reserva.setEstado(Reserva.EstadoReserva.CANCELADA);
        
        // Liberar el turno
        Turno turno = reserva.getTurno();
        if (turno != null) {
            turno.setEstado(Turno.EstadoTurno.DISPONIBLE);
            turnoRepository.save(turno);
        }
        
        return reservaRepository.save(reserva);
    }
    
    // Eliminar una reserva (liberar el turno si es necesario)
    @Override
    @Transactional
    public void eliminarReserva(String idReserva) {
        Objects.requireNonNull(idReserva, "El ID de la reserva no puede ser nulo");
        Optional<Reserva> optReserva = reservaRepository.findById(idReserva);
        if (optReserva.isPresent()) {
            Reserva reserva = optReserva.get();
            
            // Liberar el turno si la reserva está pendiente
            if (reserva.getEstado() == Reserva.EstadoReserva.PENDIENTE) {
                Turno turno = reserva.getTurno();
                if (turno != null) {
                    turno.setEstado(Turno.EstadoTurno.DISPONIBLE);
                    turnoRepository.save(turno);
                }
            }
            
            reservaRepository.delete(reserva);
        }
    }
    
    // Verificar si existe una reserva pendiente para un turno específico
    @Override
    public boolean existeReservaParaTurno(String idTurno) {
        Objects.requireNonNull(idTurno, "El ID del turno no puede ser nulo");
        Turno turno = turnoRepository.findById(idTurno).orElse(null);
        if (turno == null) {
            logger.warn("Turno no encontrado con ID: {}", idTurno);
            return false;
        }
        List<Reserva> reservas = reservaRepository.findByTurno(turno);
        return !reservas.isEmpty() && reservas.stream().anyMatch(r -> r.getEstado() == Reserva.EstadoReserva.PENDIENTE);
    }

    // Inicialización del servicio para mostrar el estado de las reservas al iniciar la aplicación
    @PostConstruct
    public void init() {
        try {
            long count = reservaRepository.countAllReservas();
            logger.info("=== INICIALIZACIÓN DE RESERVAS ===");
            logger.info("Número total de reservas en la base de datos: {}", count);

            if (count > 0) {
                List<Reserva> reservas = reservaRepository.findAll();
                logger.info("Listado de reservas:");
                for (Reserva r : reservas) {
                    logger.debug("  - ID: {}, Cliente: {}, Estado: {}, Fecha: {}",
                            r.getIdReserva(),
                            r.getCliente() != null ? r.getCliente().getNombre() : "null",
                            r.getEstado(),
                            r.getFechaHoraTurno());
                }
            }
        } catch (Exception e) {
            System.out.println("Error al inicializar el servicio de reservas: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
