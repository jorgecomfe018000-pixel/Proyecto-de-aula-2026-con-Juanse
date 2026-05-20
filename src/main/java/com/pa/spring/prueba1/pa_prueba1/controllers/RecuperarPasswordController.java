package com.pa.spring.prueba1.pa_prueba1.controllers;

import com.pa.spring.prueba1.pa_prueba1.service.RecuperarPasswordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/recuperar-password")
public class RecuperarPasswordController {

    @Autowired
    private RecuperarPasswordService recuperarPasswordService;

    /**
     * Muestra el formulario de recuperación de contraseña
     */
    @GetMapping
    public String mostrarFormularioRecuperacion() {
        return "recuperar-password";
    }

    /**
     * Procesa la recuperación por correo
     */
    @PostMapping("/por-correo")
    public String recuperarPorCorreo(@RequestParam String correo,
                                     RedirectAttributes redirectAttributes,
                                     Model model) {
        try {
            String resultado = recuperarPasswordService.generarCodigoParaCorreo(correo);
            // Si Firebase generó y envió un link, resultado será un URL (empieza con http)
            if (resultado != null && (resultado.startsWith("http://") || resultado.startsWith("https://"))) {
                redirectAttributes.addFlashAttribute("mensaje", "Se ha enviado un enlace de restablecimiento a tu correo: " + correo);
                return "redirect:/login";
            }

            model.addAttribute("identificador", correo);
            model.addAttribute("tipo", "correo");
            model.addAttribute("mensaje", "Se ha enviado un código a tu correo: " + correo);
            return "verificar-codigo";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/recuperar-password";
        }
    }

    /**
     * Procesa la recuperación por teléfono
     */
    @PostMapping("/por-telefono")
    public String recuperarPorTelefono(@RequestParam String telefono,
                                       RedirectAttributes redirectAttributes,
                                       Model model) {
        try {
            recuperarPasswordService.generarCodigoParaTelefono(telefono);
            
            model.addAttribute("identificador", telefono);
            model.addAttribute("tipo", "telefono");
            model.addAttribute("mensaje", "Se ha enviado un código a tu teléfono: " + telefono);
            return "verificar-codigo";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/recuperar-password";
        }
    }

    /**
     * Verifica el código y resetea la contraseña
     */
    @PostMapping("/verificar-codigo")
    public String verificarCodigoYResetear(@RequestParam String identificador,
                                           @RequestParam String tipo,
                                           @RequestParam String codigo,
                                           @RequestParam String nuevaPassword,
                                           @RequestParam String confirmarPassword,
                                           RedirectAttributes redirectAttributes) {
        try {
            // Validar que las contraseñas coincidan
            if (!nuevaPassword.equals(confirmarPassword)) {
                redirectAttributes.addFlashAttribute("error", "Las contraseñas no coinciden.");
                return "redirect:/recuperar-password";
            }

            // Verificar y resetear
            recuperarPasswordService.verificarYResetearPassword(identificador, codigo, 
                                                               nuevaPassword, tipo);

            redirectAttributes.addFlashAttribute("mensaje", "¡Contraseña resetada exitosamente! Ahora inicia sesión con tu nueva contraseña.");
            return "redirect:/login";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/recuperar-password";
        }
    }
}

