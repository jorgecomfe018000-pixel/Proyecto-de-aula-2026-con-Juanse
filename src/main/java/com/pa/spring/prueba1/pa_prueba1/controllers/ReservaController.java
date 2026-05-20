package com.pa.spring.prueba1.pa_prueba1.controllers;

import com.pa.spring.prueba1.pa_prueba1.model.*;
import com.pa.spring.prueba1.pa_prueba1.service.*;
import com.pa.spring.prueba1.pa_prueba1.service.estilista.EstilistaService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/reserva")
public class ReservaController {

    @Autowired
    private ServicioService servicioService;

    @Autowired
    private EstilistaService estilistaService;

    @Autowired
    private TurnoService turnoService;

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private ClienteService clienteService;

    // Mostrar página de reserva
    @GetMapping
    public String mostrarPaginaReserva(Model model, @AuthenticationPrincipal User user) {
        if (user == null) return "redirect:/login";

        Cliente cliente = clienteService.obtenerPorCorreo(user.getUsername());
        if (cliente == null) return "redirect:/home";

        List<ServicioBelleza> serviciosBellezas = servicioService.obtenerTodos();
        List<Estilista> estilistas = estilistaService.obtenerTodos();

        model.addAttribute("servicios", serviciosBellezas);
        model.addAttribute("estilistas", estilistas);
        model.addAttribute("clienteId", cliente.getIdCliente());

        return "reserva";
    }

    // Obtener turnos disponibles por estilista
    @GetMapping("/turnos/{estilistaId}")
    @ResponseBody
    public List<Turno> obtenerTurnosDisponibles(@PathVariable String estilistaId) {
        return turnoService.obtenerTurnosDisponiblesPorEstilista(estilistaId);
    }

    // Confirmar reserva
    @PostMapping("/confirmar")
    public String confirmarReserva(
            @RequestParam String servicioId,
            @RequestParam String estilistaId,
            @RequestParam String turnoId,
            @RequestParam(required = false) String comentarios,
            @AuthenticationPrincipal User user,
            RedirectAttributes redirectAttributes) {

        if (user == null) return "redirect:/login";

        Cliente cliente = clienteService.obtenerPorCorreo(user.getUsername());
        if (cliente == null) return "redirect:/home";

        // Verificar si el turno sigue disponible
        if (!turnoService.esTurnoDisponible(turnoId)) {
            redirectAttributes.addFlashAttribute("error",
                    "El turno seleccionado ya no está disponible. Intenta con otro horario.");
            return "redirect:/reserva";
        }

        // Crear reserva
        Reserva reserva = reservaService.crearReserva(
                cliente.getIdCliente(),
                estilistaId,
                servicioId,
                turnoId,
                comentarios
        );

        if (reserva == null) {
            redirectAttributes.addFlashAttribute("error",
                    "No se pudo crear la reserva. Intenta nuevamente.");
            return "redirect:/reserva";
        }

        redirectAttributes.addFlashAttribute("mensaje", "¡Reserva confirmada con éxito!");
        redirectAttributes.addFlashAttribute("reserva", reserva);
        redirectAttributes.addFlashAttribute("cliente", cliente);
        redirectAttributes.addFlashAttribute("servicio", servicioService.obtenerPorId(servicioId));
        redirectAttributes.addFlashAttribute("estilista", estilistaService.obtenerPorId(estilistaId));
        redirectAttributes.addFlashAttribute("turno", turnoService.obtenerPorId(turnoId));

        return "redirect:/reserva/confirmacion";
    }

    // Página de confirmación
    @GetMapping("/confirmacion")
    public String mostrarConfirmacion() {
        return "confirmacion";
    }

    // Ver reservas del cliente
    @GetMapping("/mis-reservas")
    public String misReservas(Model model, @AuthenticationPrincipal User user) {
        if (user == null) return "redirect:/login";

        Cliente cliente = clienteService.obtenerPorCorreo(user.getUsername());
        if (cliente == null) return "redirect:/home";

        List<Reserva> reservas = reservaService.obtenerPorCliente(cliente.getIdCliente());
        model.addAttribute("reservas", reservas);
        return "mis-reservas";
    }

    // Cancelar reserva
    @GetMapping("/cancelar/{id}")
    public String cancelarReserva(@PathVariable String id,
                                  RedirectAttributes redirectAttributes,
                                  @AuthenticationPrincipal User user) {
        if (user == null) return "redirect:/login";

        Cliente cliente = clienteService.obtenerPorCorreo(user.getUsername());
        if (cliente == null) return "redirect:/home";

        Reserva reservaExistente = reservaService.obtenerPorId(id);

        if (reservaExistente != null && !reservaExistente.getCliente().getIdCliente().equals(cliente.getIdCliente())) {
            redirectAttributes.addFlashAttribute("error", "No tienes permiso para cancelar esta reserva.");
            return "redirect:/reserva/mis-reservas";
        }

        Reserva reserva = reservaService.cancelarReserva(id);

        if (reserva == null) {
            redirectAttributes.addFlashAttribute("error", "No se pudo cancelar la reserva.");
        } else {
            redirectAttributes.addFlashAttribute("mensaje", "Reserva cancelada con éxito.");
        }

        return "redirect:/reserva/mis-reservas";
    }
}