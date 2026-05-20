package com.pa.spring.prueba1.pa_prueba1.service.estilista;

import com.pa.spring.prueba1.pa_prueba1.model.Estilista;
import com.pa.spring.prueba1.pa_prueba1.model.Notificacion;
import com.pa.spring.prueba1.pa_prueba1.model.Reserva;
import com.pa.spring.prueba1.pa_prueba1.model.SolicitudAusencia;
import com.pa.spring.prueba1.pa_prueba1.repository.EstilistaRepository;
import com.pa.spring.prueba1.pa_prueba1.repository.NotificacionRepository;
import com.pa.spring.prueba1.pa_prueba1.repository.ReservaRepository;
import com.pa.spring.prueba1.pa_prueba1.repository.SolicitudAusenciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class EstilistaService {

    @Autowired
    private EstilistaRepository estilistaRepository;
    
    @Autowired
    private ReservaRepository reservaRepository;
    
    @Autowired
    private SolicitudAusenciaRepository solicitudAusenciaRepository;
    
    @Autowired
    private NotificacionRepository notificacionRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    // ==================== MÉTODOS BÁSICOS ====================

    public Estilista obtenerEstilistaPorEmail(String email) {
        return estilistaRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Estilista no encontrado con email: " + email));
    }

    public Estilista obtenerEstilistaPorId(String id) {
        Objects.requireNonNull(id, "El ID del estilista no puede ser nulo");
        return estilistaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Estilista no encontrado con ID: " + id));
    }

    public List<Estilista> obtenerEstilistasActivos() {
        return estilistaRepository.findByActivoTrue();
    }

    public List<Reserva> obtenerReservasSemanaActual(String idEstilista) {
        LocalDateTime inicioSemana = LocalDateTime.now()
                .with(DayOfWeek.MONDAY)
                .toLocalDate()
                .atStartOfDay();
        LocalDateTime finSemana = inicioSemana.plusDays(6).toLocalDate().atTime(23, 59, 59);
        
        Estilista estilista = obtenerEstilistaPorId(idEstilista);
        return reservaRepository.findByEstilistaAndFechaHoraTurnoBetweenOrderByFechaHoraTurno(
                estilista, inicioSemana, finSemana);
    }

    public List<SolicitudAusencia> obtenerSolicitudesEstilista(String idEstilista) {
        return solicitudAusenciaRepository.findByEstilistaIdEstilista(idEstilista);
    }

    @Transactional
    public Estilista registrarEstilista(Estilista estilista, String passwordPlain) {
        if (estilistaRepository.existsByEmail(estilista.getEmail())) {
            throw new RuntimeException("Ya existe un estilista con el email: " + estilista.getEmail());
        }

        estilista.setPassword(passwordEncoder.encode(passwordPlain));
        estilista.setActivo(true);

        return estilistaRepository.save(estilista);
    }

    @Transactional
    public Estilista actualizarEstilista(Estilista estilista) {
        Estilista estilistaExistente = obtenerEstilistaPorId(estilista.getIdEstilista());
        
        estilistaExistente.setNombre(estilista.getNombre());
        estilistaExistente.setApellido(estilista.getApellido());
        estilistaExistente.setTelefono(estilista.getTelefono());
        estilistaExistente.setEspecialidad(estilista.getEspecialidad());
        estilistaExistente.setDiaLibre(estilista.getDiaLibre());
        estilistaExistente.setHoraInicio(estilista.getHoraInicio());
        estilistaExistente.setHoraFin(estilista.getHoraFin());
        estilistaExistente.setHoraInicioAlmuerzo(estilista.getHoraInicioAlmuerzo());
        estilistaExistente.setHoraFinAlmuerzo(estilista.getHoraFinAlmuerzo());
        estilistaExistente.setDuracionTurno(estilista.getDuracionTurno());

        return estilistaRepository.save(estilistaExistente);
    }

    @Transactional
    public void cambiarPassword(String email, String passwordActual, String passwordNueva) {
        Estilista estilista = obtenerEstilistaPorEmail(email);
        
        if (!passwordEncoder.matches(passwordActual, estilista.getPassword())) {
            throw new RuntimeException("La contraseña actual es incorrecta");
        }

        estilista.setPassword(passwordEncoder.encode(passwordNueva));
        estilistaRepository.save(estilista);
    }

    @Transactional
    public void cambiarEstadoEstilista(String idEstilista, boolean activo) {
        Estilista estilista = obtenerEstilistaPorId(idEstilista);
        estilista.setActivo(activo);
        estilistaRepository.save(estilista);
    }

    public boolean estaDisponible(String idEstilista, java.time.LocalDate fecha) {
        List<SolicitudAusencia> ausencias = solicitudAusenciaRepository
                .findByEstilistaIdEstilistaAndEstado(idEstilista, SolicitudAusencia.EstadoSolicitud.APROBADA);
        
        return ausencias.stream()
                .noneMatch(ausencia -> 
                    !fecha.isBefore(ausencia.getFechaInicio()) && 
                    !fecha.isAfter(ausencia.getFechaFin()));
    }

    // ==================== GESTIÓN DE AUSENCIAS ====================

    @Transactional
    public SolicitudAusencia crearSolicitudAusencia(SolicitudAusencia solicitud) {
        Estilista estilista = obtenerEstilistaPorId(solicitud.getEstilista().getIdEstilista());
        
        // Validaciones según tipo de ausencia
        if (solicitud.getTipoAusencia() == SolicitudAusencia.TipoAusencia.DIA_COMPLETO) {
            if (solicitud.getFechaInicio() == null || solicitud.getFechaFin() == null) {
                throw new RuntimeException("Debe especificar fecha de inicio y fin para día completo");
            }
            if (solicitud.getFechaInicio().isAfter(solicitud.getFechaFin())) {
                throw new RuntimeException("La fecha de inicio no puede ser posterior a la fecha fin");
            }
        } else if (solicitud.getTipoAusencia() == SolicitudAusencia.TipoAusencia.HORAS_ESPECIFICAS) {
            if (solicitud.getFecha() == null) {
                throw new RuntimeException("Debe especificar la fecha para horas específicas");
            }
            if (solicitud.getHoraInicio() == null || solicitud.getHoraFin() == null) {
                throw new RuntimeException("Debe especificar hora de inicio y fin");
            }
            if (solicitud.getHoraInicio().isAfter(solicitud.getHoraFin())) {
                throw new RuntimeException("La hora de inicio no puede ser posterior a la hora fin");
            }
        }
        
        List<SolicitudAusencia> solicitudesExistentes = solicitudAusenciaRepository
                .findByEstilistaIdEstilistaAndEstado(estilista.getIdEstilista(), SolicitudAusencia.EstadoSolicitud.PENDIENTE);
        
        List<SolicitudAusencia> solicitudesAprobadas = solicitudAusenciaRepository
                .findByEstilistaIdEstilistaAndEstado(estilista.getIdEstilista(), SolicitudAusencia.EstadoSolicitud.APROBADA);
        
        for (SolicitudAusencia existente : solicitudesExistentes) {
            if (hayConflictoFechas(solicitud, existente)) {
                throw new RuntimeException("Ya existe una solicitud pendiente para estas fechas");
            }
        }
        
        for (SolicitudAusencia aprobada : solicitudesAprobadas) {
            if (hayConflictoFechas(solicitud, aprobada)) {
                throw new RuntimeException("Ya tiene una ausencia aprobada para estas fechas");
            }
        }
        
        solicitud.setEstado(SolicitudAusencia.EstadoSolicitud.PENDIENTE);
        solicitud.setFechaRespuesta(null);
        solicitud.setMotivoRechazo(null);
        
        return solicitudAusenciaRepository.save(solicitud);
    }

    @Transactional
    public void cancelarSolicitud(String idSolicitud, String idEstilista) {
        Objects.requireNonNull(idSolicitud, "El ID de la solicitud no puede ser nulo");
        Objects.requireNonNull(idEstilista, "El ID del estilista no puede ser nulo");
        SolicitudAusencia solicitud = solicitudAusenciaRepository.findById(idSolicitud)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
        
        if (!solicitud.getEstilista().getIdEstilista().equals(idEstilista)) {
            throw new RuntimeException("No tiene permisos para cancelar esta solicitud");
        }
        
        if (solicitud.getEstado() != SolicitudAusencia.EstadoSolicitud.PENDIENTE) {
            throw new RuntimeException("Solo se pueden cancelar solicitudes pendientes");
        }
        
        solicitudAusenciaRepository.delete(solicitud);
    }

    private boolean hayConflictoFechas(SolicitudAusencia solicitud1, SolicitudAusencia solicitud2) {
        if (solicitud1.getTipoAusencia() == SolicitudAusencia.TipoAusencia.DIA_COMPLETO &&
            solicitud2.getTipoAusencia() == SolicitudAusencia.TipoAusencia.DIA_COMPLETO) {
            
            if (solicitud1.getFechaInicio() == null || solicitud1.getFechaFin() == null ||
                solicitud2.getFechaInicio() == null || solicitud2.getFechaFin() == null) {
                return false;
            }
            
            return !(solicitud1.getFechaFin().isBefore(solicitud2.getFechaInicio()) || 
                     solicitud1.getFechaInicio().isAfter(solicitud2.getFechaFin()));
        }
        
        if (solicitud1.getTipoAusencia() == SolicitudAusencia.TipoAusencia.HORAS_ESPECIFICAS &&
            solicitud2.getTipoAusencia() == SolicitudAusencia.TipoAusencia.HORAS_ESPECIFICAS) {
            
            if (solicitud1.getFecha() == null || solicitud2.getFecha() == null) {
                return false;
            }
            
            if (!solicitud1.getFecha().equals(solicitud2.getFecha())) {
                return false;
            }
            
            if (solicitud1.getHoraInicio() == null || solicitud1.getHoraFin() == null ||
                solicitud2.getHoraInicio() == null || solicitud2.getHoraFin() == null) {
                return false;
            }
            
            return !(solicitud1.getHoraFin().isBefore(solicitud2.getHoraInicio()) ||
                     solicitud1.getHoraInicio().isAfter(solicitud2.getHoraFin()) ||
                     solicitud1.getHoraFin().equals(solicitud2.getHoraInicio()) ||
                     solicitud1.getHoraInicio().equals(solicitud2.getHoraFin()));
        }
        
        if (solicitud1.getTipoAusencia() == SolicitudAusencia.TipoAusencia.DIA_COMPLETO) {
            if (solicitud1.getFechaInicio() == null || solicitud1.getFechaFin() == null ||
                solicitud2.getFecha() == null) {
                return false;
            }
            return !solicitud2.getFecha().isBefore(solicitud1.getFechaInicio()) &&
                   !solicitud2.getFecha().isAfter(solicitud1.getFechaFin());
        } else {
            if (solicitud1.getFecha() == null || solicitud2.getFechaInicio() == null ||
                solicitud2.getFechaFin() == null) {
                return false;
            }
            return !solicitud1.getFecha().isBefore(solicitud2.getFechaInicio()) &&
                   !solicitud1.getFecha().isAfter(solicitud2.getFechaFin());
        }
    }

    public List<SolicitudAusencia> obtenerSolicitudesPendientes() {
        return solicitudAusenciaRepository.findByEstado(SolicitudAusencia.EstadoSolicitud.PENDIENTE);
    }

    public long contarSolicitudesPendientes() {
        return solicitudAusenciaRepository.findByEstado(SolicitudAusencia.EstadoSolicitud.PENDIENTE).size();
    }

    public List<SolicitudAusencia> obtenerTodasLasSolicitudes() {
        return solicitudAusenciaRepository.findAll();
    }

    @Transactional
    public void aprobarSolicitud(String idSolicitud, String emailAdmin, String comentario) {
        Objects.requireNonNull(idSolicitud, "El ID de la solicitud no puede ser nulo");
        Objects.requireNonNull(emailAdmin, "El email del administrador no puede ser nulo");
        SolicitudAusencia solicitud = solicitudAusenciaRepository.findById(idSolicitud)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
        
        if (solicitud.getEstado() != SolicitudAusencia.EstadoSolicitud.PENDIENTE) {
            throw new RuntimeException("Solo se pueden aprobar solicitudes pendientes");
        }
        
        solicitud.setEstado(SolicitudAusencia.EstadoSolicitud.APROBADA);
        solicitud.setFechaRespuesta(LocalDateTime.now());
        
        if (comentario != null && !comentario.trim().isEmpty()) {
            solicitud.setMotivoRechazo(comentario);
        }
        
        solicitudAusenciaRepository.save(solicitud);
        
        // Crear notificación
        String mensaje = "Tu solicitud de ausencia para el " + 
            solicitud.getFechaInicio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + 
            " ha sido aprobada";
        if (comentario != null && !comentario.trim().isEmpty()) {
            mensaje += ". Comentario: " + comentario;
        }
        
        crearNotificacion(
            solicitud.getEstilista(),
            Notificacion.TipoNotificacion.AUSENCIA_APROBADA,
            "Ausencia Aprobada",
            mensaje,
            null
        );
    }

    @Transactional
    public void rechazarSolicitud(String idSolicitud, String emailAdmin, String motivoRechazo) {
        Objects.requireNonNull(idSolicitud, "El ID de la solicitud no puede ser nulo");
        Objects.requireNonNull(emailAdmin, "El email del administrador no puede ser nulo");
        if (motivoRechazo == null || motivoRechazo.trim().isEmpty()) {
            throw new RuntimeException("Debe proporcionar un motivo de rechazo");
        }
        
        SolicitudAusencia solicitud = solicitudAusenciaRepository.findById(idSolicitud)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
        
        if (solicitud.getEstado() != SolicitudAusencia.EstadoSolicitud.PENDIENTE) {
            throw new RuntimeException("Solo se pueden rechazar solicitudes pendientes");
        }
        
        solicitud.setEstado(SolicitudAusencia.EstadoSolicitud.RECHAZADA);
        solicitud.setFechaRespuesta(LocalDateTime.now());
        solicitud.setMotivoRechazo(motivoRechazo);
        
        solicitudAusenciaRepository.save(solicitud);
        
        // Crear notificación
        crearNotificacion(
            solicitud.getEstilista(),
            Notificacion.TipoNotificacion.AUSENCIA_RECHAZADA,
            "Ausencia Rechazada",
            "Tu solicitud de ausencia ha sido rechazada. Motivo: " + motivoRechazo,
            null
        );
    }

    public long obtenerReservasAfectadas(String idSolicitud) {
        SolicitudAusencia solicitud = solicitudAusenciaRepository.findById(java.util.Objects.requireNonNull(idSolicitud, "El ID de la solicitud no puede ser nulo"))
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
        
        String idEstilista = solicitud.getEstilista().getIdEstilista();
        
        if (solicitud.getTipoAusencia() == SolicitudAusencia.TipoAusencia.DIA_COMPLETO) {
            LocalDateTime inicioAusencia = solicitud.getFechaInicio().atStartOfDay();
            LocalDateTime finAusencia = solicitud.getFechaFin().atTime(23, 59, 59);
            
            Estilista estilista = obtenerEstilistaPorId(idEstilista);
            List<Reserva> reservas = 
                reservaRepository.findByEstilistaAndFechaHoraTurnoBetweenOrderByFechaHoraTurno(
                    estilista, inicioAusencia, finAusencia);
            
            return reservas.stream()
                    .filter(r -> r.getEstado() != Reserva.EstadoReserva.CANCELADA)
                    .count();
        } else {
            LocalDateTime inicioAusencia = LocalDateTime.of(
                solicitud.getFecha(), solicitud.getHoraInicio());
            LocalDateTime finAusencia = LocalDateTime.of(
                solicitud.getFecha(), solicitud.getHoraFin());
            
            Estilista estilista = obtenerEstilistaPorId(idEstilista);
            List<Reserva> reservas = 
                reservaRepository.findByEstilistaAndFechaHoraTurnoBetweenOrderByFechaHoraTurno(
                    estilista, inicioAusencia, finAusencia);
            
            return reservas.stream()
                    .filter(r -> r.getEstado() != Reserva.EstadoReserva.CANCELADA)
                    .count();
        }
    }

    // ==================== GESTIÓN DE RESERVAS ====================

    @Transactional
    public void completarReserva(String idReserva, String idEstilista) {
        Objects.requireNonNull(idReserva, "El ID de la reserva no puede ser nulo");
        Objects.requireNonNull(idEstilista, "El ID del estilista no puede ser nulo");
        Reserva reserva = reservaRepository.findById(idReserva)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));
        
        if (!reserva.getEstilista().getIdEstilista().equals(idEstilista)) {
            throw new RuntimeException("No tiene permisos para completar esta reserva");
        }
        
        if (reserva.getEstado() == Reserva.EstadoReserva.CANCELADA ||
            reserva.getEstado() == Reserva.EstadoReserva.COMPLETADA) {
            throw new RuntimeException("No se puede completar una reserva cancelada o ya completada");
        }
        
        reserva.setEstado(Reserva.EstadoReserva.COMPLETADA);
        reservaRepository.save(reserva);
    }

    @Transactional
    public void cancelarReserva(String idReserva, String idEstilista, String motivo) {
        Reserva reserva = reservaRepository.findById(java.util.Objects.requireNonNull(idReserva, "El ID de la reserva no puede ser nulo"))
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));
        
        if (!reserva.getEstilista().getIdEstilista().equals(idEstilista)) {
            throw new RuntimeException("No tiene permisos para cancelar esta reserva");
        }
        
        if (reserva.getEstado() == Reserva.EstadoReserva.COMPLETADA) {
            throw new RuntimeException("No se puede cancelar una reserva completada");
        }
        
        reserva.setEstado(Reserva.EstadoReserva.CANCELADA);
        reservaRepository.save(reserva);
        
        crearNotificacion(
            reserva.getEstilista(),
            Notificacion.TipoNotificacion.RESERVA_CANCELADA,
            "Reserva Cancelada",
            "Has cancelado la reserva del " + 
                reserva.getFechaHoraTurno().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) +
                ". Motivo: " + (motivo != null ? motivo : "No especificado"),
            reserva
        );
    }

    // ==================== OTROS MÉTODOS ====================

    @Transactional
    public Estilista guardar(Estilista estilista) {
        if (estilista.getIdEstilista() == null) {
            if (estilistaRepository.existsByEmail(estilista.getEmail())) {
                throw new RuntimeException("Ya existe un estilista con el email: " + estilista.getEmail());
            }
            estilista.setActivo(true);
        }
        return estilistaRepository.save(estilista);
    }

    @Transactional
    public void eliminar(String id) {
        Objects.requireNonNull(id, "El ID del estilista no puede ser nulo");
        Estilista estilista = obtenerEstilistaPorId(id);
        Objects.requireNonNull(estilista, "El estilista no puede ser nulo");
        
        LocalDateTime ahora = LocalDateTime.now();
        List<Reserva> reservasFuturas = 
            reservaRepository.findByEstilistaAndFechaHoraTurnoBetweenOrderByFechaHoraTurno(
                estilista, ahora, ahora.plusYears(1));
        
        long reservasActivas = reservasFuturas.stream()
                .filter(r -> r.getEstado() != Reserva.EstadoReserva.CANCELADA)
                .count();
        
        if (reservasActivas > 0) {
            throw new RuntimeException("No se puede eliminar el estilista porque tiene " + 
                                       reservasActivas + " reservas activas");
        }
        
        estilistaRepository.delete(estilista);
    }

    public List<Estilista> obtenerTodos() {
        return estilistaRepository.findAll();
    }

    public Estilista obtenerPorId(String id) {
        Objects.requireNonNull(id, "El ID del estilista no puede ser nulo");
        Optional<Estilista> estilista = estilistaRepository.findById(id);
        return estilista.orElse(null);
    }

    // ==================== MÉTODOS PARA NOTIFICACIONES ====================
    
    @Transactional
    public Notificacion crearNotificacion(Estilista estilista, Notificacion.TipoNotificacion tipo, 
                                         String titulo, String mensaje, Reserva reserva) {
        Notificacion notificacion = new Notificacion();
        notificacion.setEstilista(estilista);
        notificacion.setTipo(tipo);
        notificacion.setTitulo(titulo);
        notificacion.setMensaje(mensaje);
        notificacion.setReserva(reserva);
        notificacion.setLeida(false);
        notificacion.setFechaCreacion(LocalDateTime.now());
        
        return notificacionRepository.save(notificacion);
    }
    
    public List<Notificacion> obtenerNotificacionesEstilista(String idEstilista) {
        return notificacionRepository.findByEstilistaIdEstilistaOrderByFechaCreacionDesc(idEstilista);
    }
    
    public List<Notificacion> obtenerNotificacionesNoLeidas(String idEstilista) {
        return notificacionRepository.findByEstilistaIdEstilistaAndLeidaOrderByFechaCreacionDesc(
            idEstilista, false);
    }
    
    public List<Notificacion> obtenerNotificacionesPorTipo(String idEstilista, 
                                                           Notificacion.TipoNotificacion tipo) {
        return notificacionRepository.findByEstilistaIdEstilistaAndTipoOrderByFechaCreacionDesc(
            idEstilista, tipo);
    }
    
    public long contarNotificacionesNoLeidas(String idEstilista) {
        return notificacionRepository.countByEstilistaIdEstilistaAndLeida(idEstilista, false);
    }
    
    @Transactional
    public void marcarNotificacionComoLeida(String idNotificacion) {
        Objects.requireNonNull(idNotificacion, "El ID de la notificación no puede ser nulo");
        Notificacion notificacion = notificacionRepository.findById(idNotificacion)
                .orElseThrow(() -> new RuntimeException("Notificación no encontrada"));
        notificacion.setLeida(true);
        notificacionRepository.save(notificacion);
    }
    
    @Transactional
    public void marcarTodasComoLeidas(String idEstilista) {
        List<Notificacion> notificaciones = obtenerNotificacionesNoLeidas(idEstilista);
        notificaciones.forEach(n -> n.setLeida(true));
        notificacionRepository.saveAll(notificaciones);
    }
    
    @Transactional
    public void eliminarNotificacion(String idNotificacion, String idEstilista) {
        Objects.requireNonNull(idNotificacion, "El ID de la notificación no puede ser nulo");
        Objects.requireNonNull(idEstilista, "El ID del estilista no puede ser nulo");
        Notificacion notificacion = notificacionRepository.findById(idNotificacion)
                .orElseThrow(() -> new RuntimeException("Notificación no encontrada"));
        
        if (!notificacion.getEstilista().getIdEstilista().equals(idEstilista)) {
            throw new RuntimeException("No tiene permisos para eliminar esta notificación");
        }
        
        notificacionRepository.delete(notificacion);
    }
}
