package com.pa.spring.prueba1.pa_prueba1.controllers.estilista;

import com.pa.spring.prueba1.pa_prueba1.model.Estilista;
import com.pa.spring.prueba1.pa_prueba1.model.Notificacion;
import com.pa.spring.prueba1.pa_prueba1.service.estilista.EstilistaService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/estilista/notificaciones")
public class NotificacionesController {

    private final EstilistaService estilistaService;

    public NotificacionesController(EstilistaService estilistaService) {
        this.estilistaService = estilistaService;
    }

    private Estilista obtenerEstilistaActual(Authentication auth) {
        return estilistaService.obtenerEstilistaPorEmail(auth.getName());
    }

    @GetMapping
    public String notificaciones(Model model, Authentication auth,
                                 @RequestParam(required = false) String tipo) {
        try {
            Estilista estilista = obtenerEstilistaActual(auth);
            List<Notificacion> notificaciones;

            if (tipo != null && !tipo.isEmpty()) {
                if (tipo.equals("noLeidas")) {
                    notificaciones = estilistaService.obtenerNotificacionesNoLeidas(estilista.getIdEstilista());
                } else {
                    try {
                        Notificacion.TipoNotificacion tipoEnum = Notificacion.TipoNotificacion.valueOf(tipo.toUpperCase());
                        notificaciones = estilistaService.obtenerNotificacionesPorTipo(estilista.getIdEstilista(), tipoEnum);
                    } catch (IllegalArgumentException e) {
                        notificaciones = estilistaService.obtenerNotificacionesEstilista(estilista.getIdEstilista());
                    }
                }
            } else {
                notificaciones = estilistaService.obtenerNotificacionesEstilista(estilista.getIdEstilista());
            }

            long total = estilistaService.obtenerNotificacionesEstilista(estilista.getIdEstilista()).size();
            long noLeidas = estilistaService.contarNotificacionesNoLeidas(estilista.getIdEstilista());
            long notifReservas = notificaciones.stream().filter(n -> n.getTipo() == Notificacion.TipoNotificacion.NUEVA_RESERVA || n.getTipo() == Notificacion.TipoNotificacion.RESERVA_CANCELADA).count();
            long notifAusencias = notificaciones.stream().filter(n -> n.getTipo() == Notificacion.TipoNotificacion.AUSENCIA_APROBADA || n.getTipo() == Notificacion.TipoNotificacion.AUSENCIA_RECHAZADA).count();
            long notifSistema = notificaciones.stream().filter(n -> n.getTipo() == Notificacion.TipoNotificacion.SISTEMA).count();

            model.addAttribute("nombreEstilista", estilista.getNombre());
            model.addAttribute("notificaciones", notificaciones);
            model.addAttribute("totalNotificaciones", total);
            model.addAttribute("noLeidas", noLeidas);
            model.addAttribute("notifReservas", notifReservas);
            model.addAttribute("notifAusencias", notifAusencias);
            model.addAttribute("notifSistema", notifSistema);
            model.addAttribute("tipoFiltro", tipo);

        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar notificaciones: " + e.getMessage());
        }
        return "estilista/notificaciones";
    }

    @PostMapping("/{id}/leer")
    @ResponseBody
    public Map<String, Object> marcarNotificacionLeida(@PathVariable Long id, Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        try {
            estilistaService.marcarNotificacionComoLeida(id.toString());
            Estilista estilista = obtenerEstilistaActual(auth);
            long noLeidas = estilistaService.contarNotificacionesNoLeidas(estilista.getIdEstilista());

            response.put("success", true);
            response.put("noLeidas", noLeidas);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @PostMapping("/leer-todas")
    @ResponseBody
    public Map<String, Object> marcarTodasLeidas(Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        try {
            Estilista estilista = obtenerEstilistaActual(auth);
            estilistaService.marcarTodasComoLeidas(estilista.getIdEstilista());

            response.put("success", true);
            response.put("message", "Todas las notificaciones marcadas como leídas");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }
}
