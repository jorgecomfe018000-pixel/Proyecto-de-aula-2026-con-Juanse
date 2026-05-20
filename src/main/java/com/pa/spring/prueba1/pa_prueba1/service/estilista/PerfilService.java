package com.pa.spring.prueba1.pa_prueba1.service.estilista;

import com.pa.spring.prueba1.pa_prueba1.model.Estilista;
import com.pa.spring.prueba1.pa_prueba1.model.Reserva;
import com.pa.spring.prueba1.pa_prueba1.repository.EstilistaRepository;
import com.pa.spring.prueba1.pa_prueba1.repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Objects;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PerfilService {

    @Autowired
    private EstilistaRepository estilistaRepository;
    
    @Autowired
    private ReservaRepository reservaRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    // Directorio para guardar fotos de perfil
    private static final String UPLOAD_DIR = "uploads/perfiles/";

    /**
     * Obtiene las estadísticas del estilista para mostrar en el panel de perfil
     */
    public Map<String, Object> obtenerEstadisticasEstilista(String idEstilista) {
        Map<String, Object> estadisticas = new HashMap<>();
        
        try {
            // Obtener inicio y fin del mes actual
            LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
            LocalDate finMes = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
            
            // Si no se proporcionó idEstilista, devolver estadísticas por defecto
            if (idEstilista == null) {
                estadisticas.put("serviciosEsteMes", 0);
                estadisticas.put("ingresos", "$0");
                estadisticas.put("satisfaccion", 0.0);
                estadisticas.put("valoracion", 0.0);
                estadisticas.put("totalValoraciones", 0);
                estadisticas.put("clientesAtendidos", 0);
                return estadisticas;
            }

            // Obtener el Estilista y reservas del mes usando métodos derivados (evitar queries sobre DBRef.$id)
            Estilista estilista = estilistaRepository.findById(idEstilista).orElse(null);
        List<Reserva> reservasMes = java.util.Collections.emptyList();
        if (estilista != null) {
        reservasMes = reservaRepository
            .findByEstilistaAndFechaHoraTurnoBetweenOrderByFechaHoraTurno(
                estilista,
                inicioMes.atStartOfDay(),
                finMes.atTime(23, 59, 59)
            );
        }
            
            // Contar servicios completados este mes
            long serviciosEsteMes = reservasMes.stream()
                    .filter(r -> r.getEstado() == Reserva.EstadoReserva.COMPLETADA)
                    .count();
            
            // Calcular ingresos del mes
            double ingresos = reservasMes.stream()
                    .filter(r -> r.getEstado() == Reserva.EstadoReserva.COMPLETADA)
                    .filter(r -> r.getServicioBelleza() != null)
                    .mapToDouble(r -> r.getServicioBelleza().getPrecio())
                    .sum();
            
            // Obtener todas las reservas completadas para calcular satisfacción
        List<Reserva> todasReservas = java.util.Collections.emptyList();
        if (estilista != null) {
        todasReservas = reservaRepository
            .findByEstilistaAndEstadoOrderByFechaHoraTurnoDesc(
                estilista,
                Reserva.EstadoReserva.COMPLETADA
            );
        }
            
            // Calcular satisfacción (simulado - puedes implementar un sistema real de valoraciones)
            double satisfaccion = todasReservas.size() > 0 ? 96.0 : 0.0;
            
            // Calcular valoración promedio (simulado - implementar sistema de valoraciones)
            double valoracion = 4.8;
            long totalValoraciones = todasReservas.size();
            
            // Clientes únicos atendidos
        long clientesAtendidos = todasReservas.stream()
            .map(Reserva::getCliente)
            .filter(Objects::nonNull)
            .map(c -> c.getIdCliente())
            .filter(Objects::nonNull)
            .distinct()
            .count();
            
            // Agregar datos al map
            estadisticas.put("serviciosEsteMes", serviciosEsteMes);
            estadisticas.put("ingresos", formatearMoneda(ingresos));
            estadisticas.put("satisfaccion", satisfaccion);
            estadisticas.put("valoracion", valoracion);
            estadisticas.put("totalValoraciones", totalValoraciones);
            estadisticas.put("clientesAtendidos", clientesAtendidos);
            
        } catch (Exception e) {
            // Valores por defecto en caso de error
            estadisticas.put("serviciosEsteMes", 0);
            estadisticas.put("ingresos", "$0");
            estadisticas.put("satisfaccion", 0.0);
            estadisticas.put("valoracion", 0.0);
            estadisticas.put("totalValoraciones", 0);
            estadisticas.put("clientesAtendidos", 0);
        }
        
        return estadisticas;
    }
    

    /**
     * Cambia la contraseña del estilista después de verificar la contraseña actual y validar la nueva contraseña
     */
    @Transactional
    public void cambiarPassword(String email, String passwordActual, String passwordNueva) {
        Estilista estilista = estilistaRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Estilista no encontrado"));
        
        // Verificar que la contraseña actual sea correcta
        if (!passwordEncoder.matches(passwordActual, estilista.getPassword())) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta");
        }
        
        // Validar que la nueva contraseña sea diferente
        if (passwordEncoder.matches(passwordNueva, estilista.getPassword())) {
            throw new IllegalArgumentException("La nueva contraseña debe ser diferente a la actual");
        }
        
        // Actualizar contraseña
        estilista.setPassword(passwordEncoder.encode(passwordNueva));
        estilistaRepository.save(estilista);
    }

    /**
     * Guarda la foto de perfil del estilista
     */
    @Transactional
    public String guardarFotoPerfil(MultipartFile foto, String idEstilista) throws IOException {
        // Crear directorio si no existe
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Generar nombre único para el archivo
        String extension = obtenerExtension(foto.getOriginalFilename());
        String nombreArchivo = "estilista_" + idEstilista + "_" + UUID.randomUUID().toString() + extension;
        
        // Guardar archivo
        Path rutaArchivo = uploadPath.resolve(nombreArchivo);
        Files.copy(foto.getInputStream(), rutaArchivo, StandardCopyOption.REPLACE_EXISTING);
        
        // Retornar ruta relativa
        return "/uploads/perfiles/" + nombreArchivo;
    }

    /**
     * Formatea la última sesión del estilista
     */
    public String formatearUltimaSesion(LocalDateTime ultimaSesion) {
        if (ultimaSesion == null) {
            return "Nunca";
        }
        
        LocalDateTime ahora = LocalDateTime.now();
        long minutosDesde = ChronoUnit.MINUTES.between(ultimaSesion, ahora);
        
        if (minutosDesde < 60) {
            return "Hace " + minutosDesde + " minutos";
        }
        
        long horasDesde = ChronoUnit.HOURS.between(ultimaSesion, ahora);
        if (horasDesde < 24) {
            return "Hace " + horasDesde + " hora" + (horasDesde > 1 ? "s" : "");
        }
        
        long diasDesde = ChronoUnit.DAYS.between(ultimaSesion, ahora);
        if (diasDesde == 0) {
            return "Hoy a las " + ultimaSesion.format(DateTimeFormatter.ofPattern("h:mm a"));
        } else if (diasDesde == 1) {
            return "Ayer a las " + ultimaSesion.format(DateTimeFormatter.ofPattern("h:mm a"));
        } else if (diasDesde < 7) {
            return "Hace " + diasDesde + " días";
        } else {
            return ultimaSesion.format(DateTimeFormatter.ofPattern("d 'de' MMMM 'a las' h:mm a",
                    java.util.Locale.forLanguageTag("es")));
        }
    }

    /**
     * Formatea un valor monetario
     */
    private String formatearMoneda(double valor) {
        return String.format("$%,.0f", valor);
    }

    /**
     * Obtiene la extensión de un archivo
     */
    private String obtenerExtension(String nombreArchivo) {
        if (nombreArchivo == null || !nombreArchivo.contains(".")) {
            return ".jpg";
        }
        return nombreArchivo.substring(nombreArchivo.lastIndexOf("."));
    }

    /**
     * Actualiza las preferencias de notificaciones
     */
    @Transactional
    public void actualizarPreferenciasNotificaciones(String idEstilista, 
                                                     boolean notifReservas,
                                                     boolean notifCancelaciones,
                                                     boolean notifRecordatorios) {
        Estilista estilista = estilistaRepository.findById(java.util.Objects.requireNonNull(idEstilista))
                .orElseThrow(() -> new RuntimeException("Estilista no encontrado"));
        
        estilista.setNotifReservas(notifReservas);
        estilista.setNotifCancelaciones(notifCancelaciones);
        estilista.setNotifRecordatorios(notifRecordatorios);
        
        estilistaRepository.save(java.util.Objects.requireNonNull(estilista));
    }

    /**
     * Activa o desactiva la autenticación en dos pasos
     */
    @Transactional
    public void toggleAutenticacionDosPasos(String idEstilistaString, boolean activar) {
        Estilista estilista = estilistaRepository.findById(java.util.Objects.requireNonNull(idEstilistaString))
                .orElseThrow(() -> new RuntimeException("Estilista no encontrado"));
        
        estilista.setAutenticacionDosPasos(activar);
        estilistaRepository.save(java.util.Objects.requireNonNull(estilista));
    }

    /**
     * Actualiza la información personal del estilista
     */
    @Transactional
    public void actualizarInformacionPersonal(String idEstilista, Map<String, String> datos) {
        Estilista estilista = estilistaRepository.findById(java.util.Objects.requireNonNull(idEstilista))
                .orElseThrow(() -> new RuntimeException("Estilista no encontrado"));
        
        if (datos.containsKey("nombre")) {
            estilista.setNombre(datos.get("nombre"));
        }
        if (datos.containsKey("apellido")) {
            estilista.setApellido(datos.get("apellido"));
        }
        if (datos.containsKey("documento")) {
            estilista.setDocumento(datos.get("documento"));
        }
        if (datos.containsKey("telefono")) {
            estilista.setTelefono(datos.get("telefono"));
        }
        if (datos.containsKey("email")) {
            // Verificar que el email no esté en uso por otro estlilista
            estilistaRepository.findByEmail(datos.get("email"))
                    .ifPresent(b -> {
                        if (!b.getIdEstilista().equals(idEstilista)) {
                            throw new RuntimeException("El email ya está en uso");
                        }
                    });
            estilista.setEmail(datos.get("email"));
        }
        if (datos.containsKey("direccion")) {
            estilista.setDireccion(datos.get("direccion"));
        }
        if (datos.containsKey("fechaNacimiento")) {
            estilista.setFechaNacimiento(LocalDate.parse(datos.get("fechaNacimiento")));
        }
        
        estilistaRepository.save(java.util.Objects.requireNonNull(estilista));
    }

    /**
     * Actualiza la información profesional del estilista
     */
    @Transactional
    public void actualizarInformacionProfesional(String idEstilista, Map<String, String> datos) {
        Estilista estilista = estilistaRepository.findById(java.util.Objects.requireNonNull(idEstilista))
                .orElseThrow(() -> new RuntimeException("Estilista no encontrado"));
        
        if (datos.containsKey("especialidad")) {
            estilista.setEspecialidad(datos.get("especialidad"));
        }
        if (datos.containsKey("experiencia")) {
            estilista.setExperienciaAnios(Integer.parseInt(datos.get("experiencia")));
        }
        if (datos.containsKey("certificaciones")) {
            estilista.setCertificaciones(datos.get("certificaciones"));
        }
        
        estilistaRepository.save(java.util.Objects.requireNonNull(estilista));
    }
}
