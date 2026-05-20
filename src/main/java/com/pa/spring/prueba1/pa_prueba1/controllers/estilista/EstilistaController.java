package com.pa.spring.prueba1.pa_prueba1.controllers.estilista;

import com.pa.spring.prueba1.pa_prueba1.model.Estilista;
import com.pa.spring.prueba1.pa_prueba1.model.Reserva;
import com.pa.spring.prueba1.pa_prueba1.model.SolicitudAusencia;
import com.pa.spring.prueba1.pa_prueba1.service.estilista.EstilistaService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/estilista")
public class EstilistaController {

    private final EstilistaService estilistaService;

    public EstilistaController(EstilistaService estilistaService) {
        this.estilistaService = estilistaService;
    }

    // Helper para obtener el estilista autenticado
    private Estilista obtenerEstilistaActual(Authentication authentication) {
        try {
            if (authentication == null || authentication.getName() == null) {
                return null;
            }
            String email = authentication.getName();
            try {
                return estilistaService.obtenerEstilistaPorEmail(email);
            } catch (RuntimeException e) {
                // Si no existe un estilista con ese email, devolvemos null en lugar de propagar la excepción
                return null;
            }
        } catch (Exception e) {
            // No queremos que una excepción en la obtención del estilista rompa la carga del panel
            return null;
        }
    }

    /**
     * PANEL PRINCIPAL DEL ESTILISTA (HOME/DASHBOARD)
     */
    @GetMapping("/panel")
    public String mostrarPanel(Model model, Authentication authentication) {
        try {
            Estilista estilista = obtenerEstilistaActual(authentication);

            if (estilista == null) {
                model.addAttribute("error", "No se encontró el estilista");
                return "estilista/panel";
            }

            // --- Estadísticas ---
            LocalDateTime inicioHoy = LocalDate.now().atStartOfDay();
            LocalDateTime finHoy = inicioHoy.plusDays(1);

            List<Reserva> reservasSemana = estilistaService.obtenerReservasSemanaActual(estilista.getIdEstilista());

            long reservasHoy = reservasSemana.stream()
                    .filter(r -> r.getFechaHoraTurno().isAfter(inicioHoy)
                            && r.getFechaHoraTurno().isBefore(finHoy)
                            && r.getEstado() != Reserva.EstadoReserva.CANCELADA)
                    .count();

            long reservasSemanaActivas = reservasSemana.stream()
                    .filter(r -> r.getEstado() != Reserva.EstadoReserva.CANCELADA)
                    .count();

            String proximaReserva = reservasSemana.stream()
                    .filter(r -> r.getFechaHoraTurno().isAfter(LocalDateTime.now())
                            && r.getEstado() != Reserva.EstadoReserva.CANCELADA)
                    .sorted((r1, r2) -> r1.getFechaHoraTurno().compareTo(r2.getFechaHoraTurno()))
                    .findFirst()
                    .map(r -> r.getFechaHoraTurno().format(DateTimeFormatter.ofPattern("HH:mm")))
                    .orElse("--:--");

            long solicitudesPendientes = estilistaService.obtenerSolicitudesEstilista(estilista.getIdEstilista())
                    .stream()
                    .filter(s -> s.getEstado() == SolicitudAusencia.EstadoSolicitud.PENDIENTE)
                    .count();

            // --- Datos al modelo ---
            model.addAttribute("nombreEstilista", estilista.getNombre());
            model.addAttribute("estilista", estilista);
            model.addAttribute("reservasHoy", reservasHoy);
            model.addAttribute("reservasSemana", reservasSemanaActivas);
            model.addAttribute("proximaReserva", proximaReserva);
            model.addAttribute("solicitudesPendientes", solicitudesPendientes);

        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar el panel: " + e.getMessage());
        }
        return "estilista/panel";
    }
}
