package com.pa.spring.prueba1.pa_prueba1.controllers.estilista;

import com.pa.spring.prueba1.pa_prueba1.model.Estilista;
import com.pa.spring.prueba1.pa_prueba1.model.Reserva;
import com.pa.spring.prueba1.pa_prueba1.service.estilista.EstilistaService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/estilista/agenda")
public class AgendaController {

    private final EstilistaService estilistaService;

    public AgendaController(EstilistaService estilistaService) {
        this.estilistaService = estilistaService;
    }

    private Estilista obtenerEstilistaActual(Authentication auth) {
        return estilistaService.obtenerEstilistaPorEmail(auth.getName());
    }

    @GetMapping
    public String agenda(Model model, Authentication auth,
                         @RequestParam(required = false) String semana) {
        try {
            Estilista estilista = obtenerEstilistaActual(auth);

            LocalDate inicioSemana = (semana != null && !semana.isEmpty())
                    ? LocalDate.parse(semana)
                    : LocalDate.now().with(java.time.DayOfWeek.MONDAY);

            LocalDate finSemana = inicioSemana.plusDays(6);

            List<Reserva> reservasSemana = estilistaService.obtenerReservasSemanaActual(estilista.getIdEstilista())
                    .stream()
                    .filter(r -> !r.getFechaHoraTurno().isBefore(inicioSemana.atStartOfDay())
                              && !r.getFechaHoraTurno().isAfter(finSemana.atTime(23, 59)))
                    .sorted((r1, r2) -> r1.getFechaHoraTurno().compareTo(r2.getFechaHoraTurno()))
                    .toList();

            Map<LocalDate, List<Reserva>> reservasPorDia = reservasSemana.stream()
                    .collect(Collectors.groupingBy(r -> r.getFechaHoraTurno().toLocalDate()));

            model.addAttribute("nombreEstilista", estilista.getNombre());
            model.addAttribute("estilista", estilista);
            model.addAttribute("inicioSemana", inicioSemana);
            model.addAttribute("finSemana", finSemana);
            model.addAttribute("reservasPorDia", reservasPorDia);
            model.addAttribute("semanaAnterior", inicioSemana.minusWeeks(1));
            model.addAttribute("semanaSiguiente", inicioSemana.plusWeeks(1));

        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar agenda: " + e.getMessage());
        }
        return "barbero/agenda";
    }
}
