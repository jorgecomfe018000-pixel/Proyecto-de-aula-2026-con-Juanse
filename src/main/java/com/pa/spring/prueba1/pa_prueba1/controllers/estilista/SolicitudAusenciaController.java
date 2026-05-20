package com.pa.spring.prueba1.pa_prueba1.controllers.estilista;

import com.pa.spring.prueba1.pa_prueba1.model.Estilista;
import com.pa.spring.prueba1.pa_prueba1.service.estilista.EstilistaService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/estilista/solicitudes")
public class SolicitudAusenciaController {

    private final EstilistaService estilistaService;

    public SolicitudAusenciaController(EstilistaService estilistaService) {
        this.estilistaService = estilistaService;
    }

    private Estilista obtenerEstilistaActual(Authentication auth) {
        return estilistaService.obtenerEstilistaPorEmail(auth.getName());
    }

    @PostMapping("/cancelar/{id}")
    public String cancelarSolicitud(@PathVariable Long id,
                                    Authentication auth,
                                    RedirectAttributes redirectAttributes) {
        try {
            Estilista estilista = obtenerEstilistaActual(auth);
            estilistaService.cancelarSolicitud(id.toString(), estilista.getIdEstilista());
            redirectAttributes.addFlashAttribute("mensaje", "Solicitud cancelada correctamente");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cancelar: " + e.getMessage());
        }
        return "redirect:/estilista/ausencias";
    }
}
