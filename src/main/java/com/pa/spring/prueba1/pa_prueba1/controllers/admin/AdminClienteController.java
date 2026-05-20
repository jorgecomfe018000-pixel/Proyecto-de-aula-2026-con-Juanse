package com.pa.spring.prueba1.pa_prueba1.controllers.admin;

import com.pa.spring.prueba1.pa_prueba1.model.Cliente;
import com.pa.spring.prueba1.pa_prueba1.service.ClienteService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/clientes")
public class AdminClienteController {

    @Autowired
    private ClienteService clienteService;

    @GetMapping
    public String listarClientes(Model model, HttpSession session) {
        model.addAttribute("clientes", clienteService.obtenerTodos());
        model.addAttribute("admin", session.getAttribute("adminLogueado"));
        return "admin/clientes/lista";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model, HttpSession session) {
        model.addAttribute("cliente", new Cliente());
        model.addAttribute("admin", session.getAttribute("adminLogueado"));
        model.addAttribute("esNuevo", true);
        return "admin/clientes/formulario";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable String id, Model model, HttpSession session) {
        Cliente cliente = clienteService.obtenerPorId(id);
        if (cliente == null) {
            return "redirect:/admin/clientes";
        }
        model.addAttribute("cliente", cliente);
        model.addAttribute("admin", session.getAttribute("adminLogueado"));
        model.addAttribute("esNuevo", false);
        return "admin/clientes/formulario";
    }

    @PostMapping("/guardar")
    public String guardarCliente(@ModelAttribute Cliente cliente, RedirectAttributes redirectAttributes) {
        boolean esNuevo = (cliente.getIdCliente() == null || cliente.getIdCliente().trim().isEmpty());

        if (esNuevo) {
            cliente.setIdCliente(null);
            if (cliente.getCorreo() != null && clienteService.existeCliente(cliente.getCorreo())) {
                redirectAttributes.addFlashAttribute("error", "Ya existe un cliente con ese correo electrónico");
                return "redirect:/admin/clientes/nuevo";
            }
        }

        clienteService.guardar(cliente);

        String mensaje = esNuevo ? "Cliente creado correctamente" : "Cliente actualizado correctamente";
        redirectAttributes.addFlashAttribute("mensaje", mensaje);

        return "redirect:/admin/clientes";
    }

    @GetMapping("/inhabilitar/{id}")
    public String inhabilitarCliente(@PathVariable String id, RedirectAttributes redirectAttributes) {
        clienteService.inhabilitarCliente(id);
        redirectAttributes.addFlashAttribute("mensaje", "Cliente inhabilitado correctamente");
        return "redirect:/admin/clientes";
    }

    @GetMapping("/detalle/{id}")
    public String verDetalleCliente(@PathVariable String id, Model model, HttpSession session) {
        Cliente cliente = clienteService.obtenerPorId(id);
        if (cliente == null) {
            return "redirect:/admin/clientes";
        }
        model.addAttribute("cliente", cliente);
        model.addAttribute("admin", session.getAttribute("adminLogueado"));
        return "admin/clientes/detalle";
    }
}