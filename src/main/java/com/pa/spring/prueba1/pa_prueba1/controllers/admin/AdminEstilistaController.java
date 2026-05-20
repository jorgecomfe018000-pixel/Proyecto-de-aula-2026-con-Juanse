package com.pa.spring.prueba1.pa_prueba1.controllers.admin;

import com.pa.spring.prueba1.pa_prueba1.model.Estilista;
import com.pa.spring.prueba1.pa_prueba1.model.Turno;
import com.pa.spring.prueba1.pa_prueba1.service.TurnoService;
import com.pa.spring.prueba1.pa_prueba1.service.estilista.EstilistaService;
import com.pa.spring.prueba1.pa_prueba1.repository.EstilistaRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/admin/estilistas")
public class AdminEstilistaController {

    @Autowired
    private EstilistaService estilistaService;

    @Autowired
    private EstilistaRepository estilistaRepository;

    @Autowired
    private TurnoService turnoService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ==================== LISTAR ESTILISTAS (VISTA UNIFICADA) ====================
    @GetMapping
    @Transactional(readOnly = true)
    public String listarEstilistas(Model model) {
        List<Estilista> estilistas = estilistaService.obtenerTodos();
        
        // Estadísticas
        long activos = estilistas.stream().filter(Estilista::isActivo).count();
        long inactivos = estilistas.size() - activos;
        
        model.addAttribute("estilistas", estilistas);
        model.addAttribute("totalEstilistas", estilistas.size());
        model.addAttribute("estilistasActivos", activos);
        model.addAttribute("estilistasInactivos", inactivos);
        
        return "admin/estilistas/lista";
    }

    // ==================== FORMULARIO NUEVO ESTILISTA ====================
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        // Si venimos redirigidos con flashAttributes, el objeto `estilista` ya estará en el modelo
        if (!model.containsAttribute("estilista")) {
            Estilista estilista = new Estilista();
            // Valores por defecto
            estilista.setActivo(true);
            estilista.setNotifReservas(true);
            estilista.setNotifCancelaciones(true);
            estilista.setNotifRecordatorios(true);
            estilista.setAutenticacionDosPasos(false);
            estilista.setRol("ROLE_ESTILISTA"); // ✅ ROL POR DEFECTO
            model.addAttribute("estilista", estilista);
        } else {
            // Asegurar algunos valores por defecto si vienen incompletos
            Estilista e = (Estilista) model.asMap().get("estilista");
            if (e.getNotifReservas() == null) e.setNotifReservas(true);
            if (e.getNotifCancelaciones() == null) e.setNotifCancelaciones(true);
            if (e.getNotifRecordatorios() == null) e.setNotifRecordatorios(true);
            if (e.getAutenticacionDosPasos() == null) e.setAutenticacionDosPasos(false);
            if (e.getRol() == null) e.setRol("ROLE_ESTILISTA");
            if (e.getFechaIngreso() == null) e.setFechaIngreso(LocalDate.now());
        }
        model.addAttribute("diasSemana", Arrays.asList(DayOfWeek.values()));
        return "admin/estilistas/formulario";
    }

    // ==================== GUARDAR ESTILISTA ====================
    @PostMapping("/guardar")
    @Transactional
    public String guardarEstilista(@ModelAttribute Estilista estilista, RedirectAttributes redirectAttributes) {
        boolean esNuevo = false;
        try {
            // Normalizar id recibido desde el formulario: cadena vacía -> null
            if (estilista.getIdEstilista() != null && estilista.getIdEstilista().trim().isEmpty()) {
                estilista.setIdEstilista(null);
            }
            esNuevo = (estilista.getIdEstilista() == null);
            if (esNuevo) {
                // CREAR NUEVO ESTILISTA
                if (estilistaRepository.existsByEmail(estilista.getEmail())) {
                    redirectAttributes.addFlashAttribute("error", "Ya existe un estilista con ese correo electrónico");
                    return "redirect:/admin/estilistas/nuevo";
                }
                if (estilista.getPassword() == null || estilista.getPassword().isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "Debe proporcionar una contraseña para el nuevo estilista");
                    return "redirect:/admin/estilistas/nuevo";
                }
                if (estilista.getPassword().length() < 6) {
                    redirectAttributes.addFlashAttribute("error", "La contraseña debe tener al menos 6 caracteres");
                    return "redirect:/admin/estilistas/nuevo";
                }
                // Asignar valores por defecto
                if (estilista.getRol() == null || estilista.getRol().isEmpty()) {
                    estilista.setRol("ROLE_ESTILISTA"); // ✅ ROL POR DEFECTO
                }
                estilista.setFechaIngreso(LocalDate.now());
                if (estilista.getNotifReservas() == null) estilista.setNotifReservas(true);
                if (estilista.getNotifCancelaciones() == null) estilista.setNotifCancelaciones(true);
                if (estilista.getNotifRecordatorios() == null) estilista.setNotifRecordatorios(true);
                if (estilista.getAutenticacionDosPasos() == null) estilista.setAutenticacionDosPasos(false);
                estilista.setActivo(true);
                // Crear usando el método registrarEstilista
                estilistaService.registrarEstilista(estilista, estilista.getPassword());
                // Mensaje simple de éxito (sin HTML ni credenciales)
                String mensaje = "✅ Estilista creado con éxito";
                redirectAttributes.addFlashAttribute("mensaje", mensaje);
            } else {
                // EDITAR ESTILISTA EXISTENTE
                Estilista estilistaExistente = estilistaService.obtenerPorId(estilista.getIdEstilista());
                if (estilistaExistente == null) {
                    redirectAttributes.addFlashAttribute("error", "Estilista no encontrado");
                    return "redirect:/admin/estilistas";
                }
                // Solo actualizar contraseña si se proporciona una nueva
                if (estilista.getPassword() != null && !estilista.getPassword().isEmpty()) {
                    if (estilista.getPassword().length() < 6) {
                        redirectAttributes.addFlashAttribute("error", "La contraseña debe tener al menos 6 caracteres");
                        return "redirect:/admin/estilistas/editar/" + estilista.getIdEstilista();
                    }
                    estilistaExistente.setPassword(passwordEncoder.encode(estilista.getPassword()));
                }
                // Actualizar campos editables
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
                estilistaExistente.setFotoPerfil(estilista.getFotoPerfil());
                estilistaExistente.setDireccion(estilista.getDireccion());
                estilistaExistente.setFechaNacimiento(estilista.getFechaNacimiento());
                estilistaExistente.setCertificaciones(estilista.getCertificaciones());
                estilistaExistente.setExperienciaAnios(estilista.getExperienciaAnios());
                estilistaExistente.setNotifReservas(estilista.getNotifReservas());
                estilistaExistente.setNotifCancelaciones(estilista.getNotifCancelaciones());
                estilistaExistente.setNotifRecordatorios(estilista.getNotifRecordatorios());
                estilistaService.actualizarEstilista(estilistaExistente);
                String mensaje = String.format("✅ Estilista '%s' actualizado exitosamente", estilistaExistente.getNombre() + " " + estilistaExistente.getApellido());
                redirectAttributes.addFlashAttribute("mensaje", mensaje);
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Error al guardar el estilista: " + e.getMessage());
            // Conservar los datos ingresados en el formulario para mostrarlos al volver
            try {
                redirectAttributes.addFlashAttribute("estilista", estilista);
            } catch (Exception ignored) {
                // No bloquear la redirección por errores al agregar flash
            }
            e.printStackTrace();
            if (esNuevo) {
                return "redirect:/admin/estilistas/nuevo";
            } else {
                // Si por alguna razón el id quedó vacío, redirigir al listado
                if (estilista.getIdEstilista() == null) {
                    return "redirect:/admin/estilistas";
                }
                return "redirect:/admin/estilistas/editar/" + estilista.getIdEstilista();
            }
        }
        return "redirect:/admin/estilistas";
    }
    @GetMapping("/editar/{id}")
    @Transactional(readOnly = true)
    public String mostrarFormularioEditar(@PathVariable String id, Model model) {
        // Si venimos de un redirect con flashAttributes, el modelo ya contendrá el objeto `barbero`
        if (!model.containsAttribute("estilista")) {
            Estilista estilista = estilistaService.obtenerPorId(id);
            if (estilista == null) {
                return "redirect:/admin/estilistas";
            }
            model.addAttribute("estilista", estilista);
        }
        model.addAttribute("diasSemana", Arrays.asList(DayOfWeek.values()));
        model.addAttribute("editando", true);
        return "admin/estilistas/formulario";
    }

    // ==================== CAMBIAR ESTADO (ACTIVAR/DESACTIVAR) ====================
    @PostMapping("/cambiar-estado/{id}")
    @Transactional
    public String cambiarEstado(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            Estilista estilista = estilistaService.obtenerPorId(id);
            if (estilista == null) {
                redirectAttributes.addFlashAttribute("error", "Estilista no encontrado");
                return "redirect:/admin/estilistas";
            }
            
            boolean nuevoEstado = !estilista.isActivo();
            
            // Usar consulta JPQL para actualizar SOLO el campo activo
            int filasActualizadas = estilistaRepository.actualizarEstado(id, nuevoEstado);
            
            if (filasActualizadas > 0) {
                String estado = nuevoEstado ? "activado" : "desactivado";
                String mensaje = "Estilista " + estado + " exitosamente. ";
                
                if (!nuevoEstado) {
                    mensaje += "El estilista no podrá iniciar sesión hasta que se reactive su cuenta.";
                } else {
                    mensaje += "El estilista ya puede iniciar sesión nuevamente.";
                }
                
                redirectAttributes.addFlashAttribute("mensaje", mensaje);
            } else {
                redirectAttributes.addFlashAttribute("error", "No se pudo actualizar el estado del estilista");
            }
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cambiar estado: " + e.getMessage());
            e.printStackTrace();
        }
        return "redirect:/admin/estilistas";
    }

    // ==================== RESETEAR CONTRASEÑA ====================
    @PostMapping("/resetear-password")
    @Transactional
    public String resetearPassword(@RequestParam String id,
                                   @RequestParam String nuevaPassword,
                                   RedirectAttributes redirectAttributes) {
        try {
            if (nuevaPassword == null || nuevaPassword.length() < 6) {
                redirectAttributes.addFlashAttribute("error", "La contraseña debe tener al menos 6 caracteres");
                return "redirect:/admin/estilistas/editar/" + id;
            }
            
            Estilista estilista = estilistaService.obtenerPorId(id);
            if (estilista != null) {
                estilista.setPassword(passwordEncoder.encode(nuevaPassword));
                estilistaService.guardar(estilista);
                redirectAttributes.addFlashAttribute("mensaje", 
                    "Contraseña actualizada exitosamente. El estilista debe usar la nueva contraseña para iniciar sesión.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Estilista no encontrado");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al resetear contraseña: " + e.getMessage());
        }
        return "redirect:/admin/estilistas/editar/" + id;
    }

    // ==================== ELIMINAR BARBERO (SOFT DELETE) ====================
    @GetMapping("/eliminar/{id}")
    @Transactional
    public String eliminarEstilista(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            Estilista estilista = estilistaService.obtenerPorId(id);
            if (estilista == null) {
                redirectAttributes.addFlashAttribute("error", "Estilista no encontrado");
                return "redirect:/admin/estilistas";
            }
            
            // Si ya está inactivo, informar
            if (!estilista.isActivo()) {
                redirectAttributes.addFlashAttribute("mensaje", 
                    "El estilista ya estaba desactivado. Sus datos se mantienen en el sistema.");
                return "redirect:/admin/estilistas";
            }
            
            // SOFT DELETE: Usar consulta JPQL para actualizar SOLO el campo activo
            int filasActualizadas = estilistaRepository.actualizarEstado(id, false);
            
            if (filasActualizadas > 0) {
                redirectAttributes.addFlashAttribute("mensaje", 
                    "El estilista " + estilista.getNombre() + " desactivado exitosamente. No podrá iniciar sesión pero sus datos históricos se mantienen. " +
                    "Puede reactivarlo usando el botón 'Activar'.");
            } else {
                redirectAttributes.addFlashAttribute("error", "No se pudo desactivar el estilista");
            }
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Error al desactivar el estilista: " + e.getMessage());
            e.printStackTrace();
        }
        return "redirect:/admin/estilistas";
    }

    // ==================== GESTIONAR TURNOS DEL ESTILISTA ====================
    @GetMapping("/{id}/turnos")
    @Transactional(readOnly = true)
    public String gestionarTurnos(@PathVariable String id, Model model) {
        Estilista estilista = estilistaService.obtenerPorId(id);
        if (estilista == null) {
            return "redirect:/admin/estilistas";
        }

        model.addAttribute("estilista", estilista);

        // Obtener turnos disponibles y reservados
        List<Turno> turnosDisponibles = turnoService.obtenerTurnosDisponiblesPorEstilista(id);
        List<Turno> turnosReservados = turnoService.obtenerTurnosNoDisponiblesPorEstilista(id);

        model.addAttribute("turnosDisponibles", turnosDisponibles);
        model.addAttribute("turnosReservados", turnosReservados);

        // Fechas por defecto para generar turnos
        model.addAttribute("fechaInicio", LocalDate.now());
        model.addAttribute("fechaFin", LocalDate.now().plusDays(14));

        return "admin/estilistas/turnos";
    }

    // ==================== VISTA DEDICADA PARA GENERAR TURNOS ====================
    @GetMapping("/{id}/generar")
    @Transactional(readOnly = true)
    public String mostrarGenerarTurnos(@PathVariable String id, Model model) {
        Estilista estilista = estilistaService.obtenerPorId(id);
        if (estilista == null) {
            return "redirect:/admin/estilistas";
        }

        model.addAttribute("estilista", estilista);

        // Turnos actuales para mostrar contexto
        List<Turno> turnosDisponibles = turnoService.obtenerTurnosDisponiblesPorEstilista(id);
        List<Turno> turnosReservados = turnoService.obtenerTurnosNoDisponiblesPorEstilista(id);
        model.addAttribute("turnosDisponibles", turnosDisponibles);
        model.addAttribute("turnosReservados", turnosReservados);

        model.addAttribute("fechaInicio", LocalDate.now());
        model.addAttribute("fechaFin", LocalDate.now().plusDays(14));

        return "admin/estilistas/generar-turnos";
    }

    // ==================== GENERAR TURNOS AUTOMÁTICAMENTE ====================
    @PostMapping("/{id}/generar-turnos")
    @Transactional
    public String generarTurnos(
            @PathVariable String id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            RedirectAttributes redirectAttributes) {

        try {
            Estilista estilista = estilistaService.obtenerPorId(id);
            // ← AGREGAR AQUÍ
            System.out.println("=== DEBUG GENERAR TURNOS ===");
            System.out.println("horaInicio: " + estilista.getHoraInicio());
            System.out.println("horaFin: " + estilista.getHoraFin());
            System.out.println("duracionTurno: " + estilista.getDuracionTurno());
            System.out.println("tieneHorarioCompleto: " + estilista.tieneHorarioCompleto());
            // ← FIN DEBUG
            if (estilista != null) {
                // Validar que el estilista tenga configuración de horario
                if (!estilista.tieneHorarioCompleto()) {
                    redirectAttributes.addFlashAttribute("error", 
                        "El estilista debe tener configurado su horario completo (hora inicio, hora fin y duración de turno)");
                    return "redirect:/admin/estilistas/" + id + "/generar";
                }
                
                // Validar fechas
                if (fechaFin.isBefore(fechaInicio)) {
                    redirectAttributes.addFlashAttribute("error", "La fecha de fin debe ser posterior a la fecha de inicio");
                    return "redirect:/admin/estilistas/" + id + "/generar";
                }
                
                // Generar turnos
                List<Turno> turnosGenerados = turnoService.generarTurnosDisponibles(estilista, fechaInicio, fechaFin);

        redirectAttributes.addFlashAttribute("mensaje",
            "Se han generado " + turnosGenerados.size() + " turnos disponibles para " + estilista.getNombreCompleto());
        // Pasar la lista de turnos generados para mostrarlos en la vista tras la redirección
        redirectAttributes.addFlashAttribute("turnosGenerados", turnosGenerados);
            } else {
                redirectAttributes.addFlashAttribute("error", "Estilista no encontrado");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al generar turnos: " + e.getMessage());
            e.printStackTrace();
        }

        // Redirigir al listado global de turnos filtrado por el estilista para ver los turnos generados
        return "redirect:/admin/estilistas/" + id + "/generar";
    }
}
