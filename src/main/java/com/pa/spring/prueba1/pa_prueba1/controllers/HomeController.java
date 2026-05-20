package com.pa.spring.prueba1.pa_prueba1.controllers;

import com.pa.spring.prueba1.pa_prueba1.model.Cliente;
import com.pa.spring.prueba1.pa_prueba1.service.ClienteService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final ClienteService clienteService;

    public HomeController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping({"/", "/home"})
    public String home(Model model, Authentication auth) {
        if (auth != null && auth.isAuthenticated()) {
            String correo = auth.getName();

            boolean esCliente = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_USER"));

            if (esCliente) {
                Cliente cliente = clienteService.obtenerPorCorreo(correo);
                model.addAttribute("clienteLogueado", cliente);
            }
        }
        return "index";
    }
}