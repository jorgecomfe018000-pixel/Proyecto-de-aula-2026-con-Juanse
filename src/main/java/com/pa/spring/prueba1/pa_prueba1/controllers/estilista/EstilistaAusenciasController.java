package com.pa.spring.prueba1.pa_prueba1.controllers.estilista;

import com.pa.spring.prueba1.pa_prueba1.model.Estilista;
import com.pa.spring.prueba1.pa_prueba1.model.SolicitudAusencia;
import com.pa.spring.prueba1.pa_prueba1.service.estilista.EstilistaService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/estilista/ausencias")
public class EstilistaAusenciasController {

    private final EstilistaService estilistaService;

    public EstilistaAusenciasController(EstilistaService estilistaService) {
        this.estilistaService = estilistaService;
    }

    private Estilista obtenerEstilistaActual(Authentication authentication) {
        String email = authentication.getName();
        return estilistaService.obtenerEstilistaPorEmail(email);
    }

    @GetMapping
    public String ausencias(Model model, Authentication authentication) {
        try {
            Estilista estilista = obtenerEstilistaActual(authentication);
            
            List<SolicitudAusencia> solicitudes = estilistaService.obtenerSolicitudesEstilista(estilista.getIdEstilista());
            
            long solicitudesPendientes = solicitudes.stream()
                    .filter(s -> s.getEstado() == SolicitudAusencia.EstadoSolicitud.PENDIENTE).count();
            long solicitudesAprobadas = solicitudes.stream()
                    .filter(s -> s.getEstado() == SolicitudAusencia.EstadoSolicitud.APROBADA).count();
            long solicitudesRechazadas = solicitudes.stream()
                    .filter(s -> s.getEstado() == SolicitudAusencia.EstadoSolicitud.RECHAZADA).count();

            model.addAttribute("nombreEstilista", estilista.getNombre());
            model.addAttribute("estilista", estilista);
            model.addAttribute("solicitudes", solicitudes);
            model.addAttribute("solicitudesPendientes", solicitudesPendientes);
            model.addAttribute("solicitudesAprobadas", solicitudesAprobadas);
            model.addAttribute("solicitudesRechazadas", solicitudesRechazadas);
            model.addAttribute("diasLibresRestantes", 10);
            model.addAttribute("nuevaSolicitud", new SolicitudAusencia());

        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar ausencias: " + e.getMessage());
        }
        return "estilista/ausencias";
    }
}
