package com.pa.spring.prueba1.pa_prueba1.controllers.admin;

import com.pa.spring.prueba1.pa_prueba1.model.Estilista;
import com.pa.spring.prueba1.pa_prueba1.model.Turno;
import com.pa.spring.prueba1.pa_prueba1.service.ReservaService;
import com.pa.spring.prueba1.pa_prueba1.service.TurnoService;
import com.pa.spring.prueba1.pa_prueba1.service.estilista.EstilistaService;

import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/turnos")
public class AdminTurnoController {
    private static final Logger logger = LoggerFactory.getLogger(AdminTurnoController.class);

    @Autowired
    private TurnoService turnoService;
    
    @Autowired
    private EstilistaService estilistaService;
    
    @Autowired
    private ReservaService reservaService;

    /**
     * Ruta principal: redirecciona a /listar para forzar recarga de datos del servidor.
     * Esto asegura que cada acceso a /admin/turnos resulte en una consulta fresca a BD.
     */
    @GetMapping
    public String redirigirATurnos() {
        return "redirect:/admin/turnos/listar";
    }

    /**
     * Ruta de listado: carga todos los turnos desde BD sin usar sesión.
     * Cada carga es independiente y obtiene datos frescos de la base de datos.
     */
    @GetMapping("/listar")
    @Transactional(readOnly = true)
    public String listarTurnos(
            @RequestParam(required = false) String estilistaId,
            @RequestParam(required = false) String estado,
            Model model) {
        try {
            List<Turno> turnos;

            if (estilistaId != null && !estilistaId.isEmpty()) {
                if (estado != null && !estado.isEmpty()) {
                    // Obtener turnos de un estilista con estado específico
                    Turno.EstadoTurno estadoEnum = Turno.EstadoTurno.valueOf(estado);
                    turnos = turnoService.obtenerTurnosPorEstilistaYEstado(estilistaId, estadoEnum);
                    System.out.println("[ADMIN-TURNOS] Turnos por estilista=" + estilistaId + " y estado=" + estado + ": " + turnos.size());
                } else {
                    // Obtener todos los turnos de un estilista
                    turnos = turnoService.obtenerTurnosPorEstilista(estilistaId);
                    System.out.println("[ADMIN-TURNOS] Turnos por estilista=" + estilistaId + ": " + turnos.size());
                }
            } else {
                // Obtener TODOS los turnos sin restricción - Consulta fresca de BD cada vez
                turnos = turnoService.obtenerTodos();
                System.out.println("[ADMIN-TURNOS] obtenerTodos() retornó: " + (turnos == null ? "NULL" : turnos.size() + " turnos"));
            }

            // ===== DIAGNÓSTICO DETALLADO =====
            if (turnos != null && !turnos.isEmpty()) {
                System.out.println("[ADMIN-TURNOS] Primer turno detallado:");
                Turno t0 = turnos.get(0);
                System.out.println("  - idTurno: " + t0.getIdTurno());
                System.out.println("  - fechaHora: " + t0.getFechaHora() + " (tipo: " + (t0.getFechaHora() == null ? "NULL" : t0.getFechaHora().getClass().getSimpleName()) + ")");
                System.out.println("  - estado: " + t0.getEstado());
                System.out.println("  - estilista: " + (t0.getEstilista() == null ? "NULL" : t0.getEstilista().getIdEstilista() + " / " + t0.getEstilista().getNombre()));
            }
            // ===== FIN DIAGNÓSTICO =====

            model.addAttribute("turnos", turnos);
            model.addAttribute("filtroEstilistaId", estilistaId);
            model.addAttribute("filtroEstado", estado);

            if (logger.isDebugEnabled()) {
                logger.debug("Turnos cargados en controlador (count={})", turnos == null ? 0 : turnos.size());
                if (turnos != null && !turnos.isEmpty()) {
                    turnos.stream().limit(5).forEach(t -> logger.debug("  turno id={} fecha={} estilistaId={}", t.getIdTurno(), t.getFechaHora(), t.getEstilista() != null ? t.getEstilista().getIdEstilista() : "null"));
                }
            }

            List<Estilista> estilistas = estilistaService.obtenerTodos();

            model.addAttribute("estilistas", estilistas);
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar los turnos: " + e.getMessage());
            logger.error("Error en listarTurnos", e);
            e.printStackTrace();
        }
        return "admin/turnos/lista";
    }

    @GetMapping("/disponibles")
    @Transactional(readOnly = true)
    public String listarTurnosDisponibles(Model model) {
        try {
            List<Estilista> estilistas = estilistaService.obtenerTodos();
            model.addAttribute("estilistas", estilistas);
            
            List<Turno> turnosDisponibles = turnoService.obtenerTurnosDisponibles();
            model.addAttribute("turnos", turnosDisponibles);
            model.addAttribute("filtroEstado", "DISPONIBLE");
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar los turnos disponibles: " + e.getMessage());
            e.printStackTrace();
        }
        return "admin/turnos/lista";
    }

    @GetMapping("/no-disponibles")
    @Transactional(readOnly = true)
    public String listarTurnosNoDisponibles(Model model) {
        try {
            List<Estilista> estilistas = estilistaService.obtenerTodos();
            model.addAttribute("estilistas", estilistas);
            
            List<Turno> turnosNoDisponibles = turnoService.obtenerTurnosNoDisponibles();
            model.addAttribute("turnos", turnosNoDisponibles);
            model.addAttribute("filtroEstado", "NO_DISPONIBLE");
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar los turnos no disponibles: " + e.getMessage());
            e.printStackTrace();
        }
        return "admin/turnos/lista";
    }

    @GetMapping("/filtrar")
    @Transactional(readOnly = true)
    public String filtrarTurnos(
        @RequestParam(required = false) String estilistaId,
        @RequestParam(required = false) String estado,
        Model model,
        HttpServletRequest request) {
        
        try {
            List<Estilista> estilistas = estilistaService.obtenerTodos();
            model.addAttribute("estilistas", estilistas);
            model.addAttribute("filtroEstilistaId", estilistaId);
            model.addAttribute("filtroEstado", estado);
            
            List<Turno> turnos;
            
            if (estilistaId != null && estado != null && !estado.isEmpty()) {
                Turno.EstadoTurno estadoEnum = Turno.EstadoTurno.valueOf(estado);
                turnos = turnoService.obtenerTurnosPorEstilistaYEstado(estilistaId, estadoEnum);
            } else if (estilistaId != null) {
                turnos = turnoService.obtenerTurnosPorEstilista(estilistaId);
            } else if (estado != null && !estado.isEmpty()) {
                Turno.EstadoTurno estadoEnum = Turno.EstadoTurno.valueOf(estado);
                if (estadoEnum == Turno.EstadoTurno.DISPONIBLE) {
                    turnos = turnoService.obtenerTurnosDisponibles();
                } else {
                    turnos = turnoService.obtenerTurnosNoDisponibles();
                }
            } else {
                turnos = turnoService.obtenerTodos();
            }
            
            // Si vienen turnosGenerados en flash (redirect desde generacion), úsalos primero.
            // Usamos un manejo seguro de tipos para evitar casts sin comprobación.
            java.util.Map<String, ?> flashMap = null;
            if (request != null) {
                flashMap = RequestContextUtils.getInputFlashMap(request);
            }
            if (flashMap != null && flashMap.containsKey("turnosGenerados")) {
                Object fg = flashMap.get("turnosGenerados");
                if (fg instanceof java.util.List<?>) {
                    java.util.List<?> raw = (java.util.List<?>) fg;
                    java.util.List<Turno> fgList = new java.util.ArrayList<>();
                    for (Object o : raw) {
                        if (o instanceof Turno) {
                            fgList.add((Turno) o);
                        }
                    }
                    if (!fgList.isEmpty()) {
                        model.addAttribute("turnos", fgList);
                        model.addAttribute("mensaje", flashMap.get("mensaje"));
                        if (logger.isDebugEnabled()) {
                            logger.debug("Usando turnosGenerados desde flash (count={})", fgList.size());
                        }
                        return "admin/turnos/lista";
                    } else {
                        logger.warn("turnosGenerados en flash presente pero no contiene elementos Turno");
                    }
                }
            }

            model.addAttribute("turnos", turnos);
        } catch (Exception e) {
            model.addAttribute("error", "Error al filtrar los turnos: " + e.getMessage());
            e.printStackTrace();
        }
        
        return "admin/turnos/lista";
    }

    @GetMapping("/marcar-disponible/{id}")
    public String marcarTurnoDisponible(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            if (reservaService.existeReservaParaTurno(id)) {
                redirectAttributes.addFlashAttribute("error", 
                    "No se puede marcar como disponible un turno con reservas pendientes");
                return "redirect:/admin/turnos";
            }
            
            Turno turno = turnoService.marcarTurnoDisponible(id);
            
            if (turno != null) {
                redirectAttributes.addFlashAttribute("mensaje", "Turno marcado como disponible");
            } else {
                redirectAttributes.addFlashAttribute("error", "No se pudo marcar el turno como disponible");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            e.printStackTrace();
        }
        
        return "redirect:/admin/turnos";
    }

    @GetMapping("/marcar-no-disponible/{id}")
    public String marcarTurnoNoDisponible(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            Turno turno = turnoService.marcarTurnoNoDisponible(id);
            
            if (turno != null) {
                redirectAttributes.addFlashAttribute("mensaje", "Turno marcado como no disponible");
            } else {
                redirectAttributes.addFlashAttribute("error", "No se pudo marcar el turno como no disponible");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            e.printStackTrace();
        }
        
        return "redirect:/admin/turnos";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarTurno(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            if (reservaService.existeReservaParaTurno(id)) {
                redirectAttributes.addFlashAttribute("error", 
                    "No se puede eliminar un turno con reservas pendientes");
                return "redirect:/admin/turnos";
            }
            
            turnoService.eliminarTurno(id);
            redirectAttributes.addFlashAttribute("mensaje", "Turno eliminado con éxito");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el turno: " + e.getMessage());
            e.printStackTrace();
        }
        
        return "redirect:/admin/turnos";
    }
}
