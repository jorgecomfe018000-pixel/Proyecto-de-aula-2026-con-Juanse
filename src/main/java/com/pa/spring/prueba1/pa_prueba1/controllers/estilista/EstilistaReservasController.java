package com.pa.spring.prueba1.pa_prueba1.controllers.estilista;

import com.pa.spring.prueba1.pa_prueba1.model.Estilista;
import com.pa.spring.prueba1.pa_prueba1.model.Reserva;
import com.pa.spring.prueba1.pa_prueba1.service.estilista.EstilistaService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/estilista/reservas")
public class EstilistaReservasController {

    private final EstilistaService estilistaService;

    public EstilistaReservasController(EstilistaService estilistaService) {
        this.estilistaService = estilistaService;
    }

    private Estilista obtenerEstilistaActual(Authentication authentication) {
        String email = authentication.getName();
        return estilistaService.obtenerEstilistaPorEmail(email);
    }

    @GetMapping
    public String misReservas(Model model, Authentication authentication,
                              @RequestParam(required = false, defaultValue = "hoy") String filtro) {
        try {
            Estilista estilista = obtenerEstilistaActual(authentication);
            
            LocalDateTime inicioFiltro;
            LocalDateTime finFiltro;
            
            switch (filtro.toLowerCase()) {
                case "semana":
                    inicioFiltro = LocalDate.now()
                            .with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                            .atStartOfDay();
                    finFiltro = inicioFiltro.plusWeeks(1);
                    break;
                case "mes":
                    inicioFiltro = LocalDate.now().withDayOfMonth(1).atStartOfDay();
                    finFiltro = inicioFiltro.plusMonths(1);
                    break;
                case "todas":
                    inicioFiltro = LocalDateTime.now().minusYears(1);
                    finFiltro = LocalDateTime.now().plusYears(1);
                    break;
                default: // hoy
                    inicioFiltro = LocalDate.now().atStartOfDay();
                    finFiltro = inicioFiltro.plusDays(1);
            }

            List<Reserva> todasReservas = estilistaService.obtenerReservasSemanaActual(estilista.getIdEstilista());
            
            List<Reserva> reservasFiltradas = todasReservas.stream()
                    .filter(r -> r.getFechaHoraTurno().isAfter(inicioFiltro) 
                            && r.getFechaHoraTurno().isBefore(finFiltro))
                    .sorted(Comparator.comparing(Reserva::getFechaHoraTurno))
                    .collect(Collectors.toList());

            long reservasCompletadas = todasReservas.stream()
                    .filter(r -> r.getEstado() == Reserva.EstadoReserva.COMPLETADA).count();
            long reservasPendientes = todasReservas.stream()
                    .filter(r -> r.getEstado() == Reserva.EstadoReserva.PENDIENTE).count();
            long reservasCanceladas = todasReservas.stream()
                    .filter(r -> r.getEstado() == Reserva.EstadoReserva.CANCELADA).count();

            model.addAttribute("nombreEstilista", estilista.getNombre());
            model.addAttribute("reservasHoyList", reservasFiltradas);
            model.addAttribute("reservasHoy", reservasFiltradas.size());
            model.addAttribute("reservasCompletadas", reservasCompletadas);
            model.addAttribute("reservasPendientes", reservasPendientes);
            model.addAttribute("reservasCanceladas", reservasCanceladas);
            model.addAttribute("fechaHoy", LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM yyyy")));
            model.addAttribute("filtroActual", filtro);

        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar reservas: " + e.getMessage());
        }
        return "estilista/reservas";
    }

    @PostMapping("/completar/{id}")
    @ResponseBody
    public Map<String, Object> completarReserva(@PathVariable String id,
                                               Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        try {
             Estilista estilista = obtenerEstilistaActual(authentication);
            estilistaService.completarReserva(id.toString(), estilista.getIdEstilista());
            
            response.put("success", true);
            response.put("message", "Reserva completada correctamente");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @PostMapping("/cancelar/{id}")
    @ResponseBody
    public Map<String, Object> cancelarReservaAjax(@PathVariable String id,
                                                  @RequestBody Map<String, String> payload,
                                                  Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        try {
            Estilista estilista = obtenerEstilistaActual(authentication);
            String motivo = payload.get("motivo");
            
            estilistaService.cancelarReserva(id.toString(), estilista.getIdEstilista(), motivo);
            
            response.put("success", true);
            response.put("message", "Reserva cancelada correctamente");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }
}
