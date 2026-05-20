package com.pa.spring.prueba1.pa_prueba1.controllers.admin;

import com.pa.spring.prueba1.pa_prueba1.model.ServicioBelleza;
import com.pa.spring.prueba1.pa_prueba1.service.ServicioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controlador que maneja las peticiones relacionadas con los servicios de belleza en el panel administrativo.
 * Permite listar, crear, editar y eliminar servicios de belleza.
 */
@Controller
@RequestMapping("/admin/servicios")
public class AdminServicioController {

    private static final Logger logger = LoggerFactory.getLogger(AdminServicioController.class);

    @Autowired
    private ServicioService servicioService;

    /**
     * Lista todos los servicios de belleza disponibles en el sistema.
     *
     * @param model objeto para enviar datos a la vista
     * @return vista con la lista de servicios
     */
    @GetMapping
    public String listarServicios(Model model) {
        logger.debug("Iniciando listarServicios");
        List<ServicioBelleza> servicioBelleza = servicioService.obtenerTodos();
        logger.debug("Servicios obtenidos: {}", servicioBelleza);
        model.addAttribute("servicios", servicioBelleza);
        return "admin/servicios/lista"; // Retorna la vista que muestra la lista de servicios
    }
    
    /**
     * Muestra el formulario para agregar un nuevo servicio de belleza.
     *
     * @param model objeto para enviar datos a la vista
     * @return vista del formulario para crear un nuevo servicio
     */
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("servicioBelleza", new ServicioBelleza()); // Se crea una nueva instancia de ServicioBelleza
        return "admin/servicios/formulario"; // Retorna la vista del formulario para ingresar los detalles del nuevo servicio
    }
    
    /**
     * Guarda un nuevo servicio de belleza en la base de datos.
     *
     * @param servicioBelleza objeto ServicioBelleza que contiene los detalles del servicio
     * @param redirectAttributes para enviar un mensaje de éxito
     * @return redirige a la lista de servicios
     */
    @PostMapping("/guardar")
    public String guardarServicio(@ModelAttribute ServicioBelleza servicioBelleza, RedirectAttributes redirectAttributes) {
        logger.debug("Guardando servicio: {}", servicioBelleza);
        servicioService.guardar(servicioBelleza); // Llama al servicio para guardar el servicio en la base de datos
        logger.info("Servicio guardado con éxito: {}", servicioBelleza);
        redirectAttributes.addFlashAttribute("mensaje", "Servicio guardado con éxito"); // Añade mensaje de éxito
        return "redirect:/admin/servicios"; // Redirige a la lista de servicios
    }
    
    /**
     * Muestra el formulario para editar un servicio de belleza existente.
     *
     * @param id identificador del servicio a editar
     * @param model objeto para enviar datos a la vista
     * @return vista del formulario con los datos del servicio a editar
     */
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable String id, Model model) {
        logger.debug("Editando servicio con ID: {}", id);
        if (id == null || id.trim().isEmpty()) {
            logger.warn("ID de servicio inválido");
            return "redirect:/admin/servicios";
        }
        ServicioBelleza servicioBelleza = servicioService.obtenerPorId(id);
        if (servicioBelleza == null) {
            logger.warn("Servicio no encontrado para ID: {}", id);
            return "redirect:/admin/servicios";
        }
        model.addAttribute("servicioBelleza", servicioBelleza); // Añadir el servicio al modelo
        return "admin/servicios/formulario"; // Retorna la vista del formulario para editar el servicio
    }

    // Manejar llamadas sin ID para editar
    @GetMapping("/editar")
    public String editarSinId() {
        return "redirect:/admin/servicios";
    }
    
    /**
     * Elimina un servicio de belleza de la base de datos.
     *
     * @param id identificador del servicio a eliminar
     * @param redirectAttributes para enviar un mensaje de éxito
     * @return redirige a la lista de servicios
     */
    @GetMapping("/eliminar/{id}")
    public String eliminarServicio(@PathVariable String id, RedirectAttributes redirectAttributes) {
        logger.debug("Eliminando servicio con ID: {}", id);
        if (id == null || id.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "ID de servicio inválido");
            return "redirect:/admin/servicios";
        }
        try {
            servicioService.eliminar(id); // Llama al servicio para eliminar el servicio por su id
            logger.info("Servicio eliminado con éxito: {}", id);
            redirectAttributes.addFlashAttribute("mensaje", "Servicio eliminado con éxito"); // Mensaje de éxito
        } catch (Exception e) {
            logger.error("Error al eliminar servicio con ID: {}", id, e);
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el servicio");
        }
        return "redirect:/admin/servicios"; // Redirige a la lista de servicios
    }

    // Manejar llamadas a /eliminar sin ID (evitar error por URL incompleta)
    @GetMapping("/eliminar")
    public String eliminarSinId(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "ID de servicio faltante");
        return "redirect:/admin/servicios";
    }
}
