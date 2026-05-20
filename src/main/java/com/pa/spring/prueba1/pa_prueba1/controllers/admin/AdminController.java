package com.pa.spring.prueba1.pa_prueba1.controllers.admin;
import com.pa.spring.prueba1.pa_prueba1.model.Reserva;
import com.pa.spring.prueba1.pa_prueba1.repository.UsuarioRepository;
import com.pa.spring.prueba1.pa_prueba1.repository.AdministradorRepository;
import com.pa.spring.prueba1.pa_prueba1.repository.ClienteRepository;
import com.pa.spring.prueba1.pa_prueba1.repository.EstilistaRepository;
import com.pa.spring.prueba1.pa_prueba1.repository.ServicioRepository;
import com.pa.spring.prueba1.pa_prueba1.repository.ReservaRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

    private final UsuarioRepository usuarioRepository;
    private final EstilistaRepository estilistaRepository;
    private final ServicioRepository servicioRepository;
    private final ReservaRepository reservaRepository;
    private final AdministradorRepository administradorRepository;
    private final ClienteRepository clienteRepository;

    public AdminController(UsuarioRepository usuarioRepository,
                           EstilistaRepository estilistaRepository,
                           ServicioRepository servicioRepository,
                           ReservaRepository reservaRepository,
                           AdministradorRepository administradorRepository,
                           ClienteRepository clienteRepository) {
        this.usuarioRepository = usuarioRepository;
        this.estilistaRepository = estilistaRepository;
        this.servicioRepository = servicioRepository;
        this.reservaRepository = reservaRepository;
        this.administradorRepository = administradorRepository;
        this.clienteRepository = clienteRepository;
    }

    @GetMapping("/admin/panel")
    public String adminHome(Model model) {
        
        // Obtener el authentication del SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String emailOrUsername = authentication.getName();
        
        // Buscar el usuario por email (puede estar en Usuario, Administrador o Cliente según migración)
        if (authentication == null || authentication.getName() == null) {
            model.addAttribute("error", "Usuario no autenticado");
            return "admin/panel";
        }

        String principal = emailOrUsername;
        Object adminObj = null;
        String nombreAdmin = "Administrador";
        try {
            adminObj = administradorRepository.findByCorreo(principal).orElse(null);
            if (adminObj != null) {
                nombreAdmin = ((com.pa.spring.prueba1.pa_prueba1.model.Administrador)adminObj).getNombre();
            }
        } catch (Exception ignored) {}

        if (adminObj == null) {
            try {
                adminObj = usuarioRepository.findByEmail(principal).orElse(null);
                if (adminObj != null && adminObj instanceof com.pa.spring.prueba1.pa_prueba1.model.Cliente) {
                    nombreAdmin = ((com.pa.spring.prueba1.pa_prueba1.model.Cliente)adminObj).getNombre();
                }
            } catch (Exception ignored) {}
        }

        if (adminObj == null) {
            try {
                adminObj = clienteRepository.findByCorreo(principal).orElse(null);
                if (adminObj != null) {
                    nombreAdmin = ((com.pa.spring.prueba1.pa_prueba1.model.Cliente)adminObj).getNombre();
                }
            } catch (Exception ignored) {}
        }

        if (adminObj == null) {
            model.addAttribute("error", "Usuario administrador no encontrado: " + principal);
            model.addAttribute("nombreAdmin", "No encontrado");
            return "admin/panel";
        }

        model.addAttribute("admin", adminObj);
        model.addAttribute("nombreAdmin", nombreAdmin);

        // Estadísticas
        long totalClientes = clienteRepository.count();
        long totalEstilistas = estilistaRepository.count();
        long totalServicios = servicioRepository.count();
        long reservasPendientes = reservaRepository.countByEstado(Reserva.EstadoReserva.PENDIENTE);

        model.addAttribute("totalClientes", totalClientes);
        model.addAttribute("totalEstilistas", totalEstilistas);
        model.addAttribute("totalServicios", totalServicios);
        model.addAttribute("reservasPendientes", reservasPendientes);

        return "admin/panel";
    }
}
