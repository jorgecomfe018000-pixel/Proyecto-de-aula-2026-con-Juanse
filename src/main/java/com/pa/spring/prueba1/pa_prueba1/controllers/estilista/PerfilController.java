package com.pa.spring.prueba1.pa_prueba1.controllers.estilista;

import com.pa.spring.prueba1.pa_prueba1.model.Estilista;
import com.pa.spring.prueba1.pa_prueba1.service.estilista.EstilistaService;
import com.pa.spring.prueba1.pa_prueba1.service.estilista.PerfilService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

import java.time.format.DateTimeFormatter;
import java.util.Map;

@Controller
@RequestMapping("/estilista/perfil")
public class PerfilController {

    private final EstilistaService estilistaService;
    private final PerfilService perfilService;

    public PerfilController(EstilistaService estilistaService, PerfilService perfilService) {
        this.estilistaService = estilistaService;
        this.perfilService = perfilService;
    }

    /**
     * Obtiene el estilista autenticado
     */
    private Estilista obtenerEstilistaActual(Authentication auth) {
        return estilistaService.obtenerEstilistaPorEmail(auth.getName());
    }

    /**
     * VER PERFIL - Vista principal
     */
    @GetMapping
    public String verPerfil(Model model, Authentication auth) {
        try {
            Estilista estilista = obtenerEstilistaActual(auth);

            // Información básica
            model.addAttribute("nombreEstilista", estilista.getNombre());
            model.addAttribute("estilista", estilista);
            
            // Información personal
            model.addAttribute("nombreCompleto", estilista.getNombre() + 
                    (estilista.getApellido() != null ? " " + estilista.getApellido() : ""));
            model.addAttribute("documento", estilista.getDocumento() != null ? 
                    estilista.getDocumento() : "No registrado");
            model.addAttribute("fechaNacimiento", estilista.getFechaNacimiento() != null ? 
                    estilista.getFechaNacimiento().format(DateTimeFormatter.ofPattern("d 'de' MMMM, yyyy", 
                    java.util.Locale.forLanguageTag("es"))) : "No registrada");
            model.addAttribute("email", estilista.getEmail());
            model.addAttribute("telefono", estilista.getTelefono() != null ? 
                    estilista.getTelefono() : "No registrado");
            model.addAttribute("direccion", estilista.getDireccion() != null ? 
                    estilista.getDireccion() : "No registrada");
            
            // Información profesional
            model.addAttribute("anosExperiencia", estilista.getExperienciaAnios() != null ? 
                    estilista.getExperienciaAnios() + " años" : "No especificado");
            model.addAttribute("fechaIngreso", estilista.getFechaIngreso() != null ? 
                    estilista.getFechaIngreso().format(DateTimeFormatter.ofPattern("d 'de' MMMM, yyyy", 
                    java.util.Locale.forLanguageTag("es"))) : "No registrada");
            model.addAttribute("especialidad", estilista.getEspecialidad() != null ? 
                    estilista.getEspecialidad() : "Estilista General");
            model.addAttribute("certificaciones", estilista.getCertificaciones());
            
            // Estadísticas
            Map<String, Object> estadisticas = perfilService.obtenerEstadisticasEstilista(estilista.getIdEstilista());
            model.addAttribute("serviciosEsteMes", estadisticas.get("serviciosEsteMes"));
            model.addAttribute("ingresos", estadisticas.get("ingresos"));
            model.addAttribute("satisfaccion", estadisticas.get("satisfaccion"));
            model.addAttribute("valoracion", estadisticas.get("valoracion"));
            model.addAttribute("totalValoraciones", estadisticas.get("totalValoraciones"));
            model.addAttribute("clientesAtendidos", estadisticas.get("clientesAtendidos"));
            
            // Seguridad
            model.addAttribute("ultimaSesion", estilista.getUltimaSesion() != null ? 
                    perfilService.formatearUltimaSesion(estilista.getUltimaSesion()) : "Nunca");
            
            // Preferencias
            model.addAttribute("notifReservas", estilista.getNotifReservas() != null ? 
                    estilista.getNotifReservas() : true);
            model.addAttribute("notifCancelaciones", estilista.getNotifCancelaciones() != null ? 
                    estilista.getNotifCancelaciones() : true);
            model.addAttribute("notifRecordatorios", estilista.getNotifRecordatorios() != null ? 
                    estilista.getNotifRecordatorios() : true);
            model.addAttribute("autenticacionDosPasos", estilista.getAutenticacionDosPasos() != null ? 
                    estilista.getAutenticacionDosPasos() : false);
            
            // Foto de perfil
            model.addAttribute("fotoPerfil", estilista.getFotoPerfil() != null ? 
                    estilista.getFotoPerfil() : generarAvatarURL(estilista.getNombre()));

        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar perfil: " + e.getMessage());
        }
        return "estilista/perfil";
    }

    /**
     * ACTUALIZAR INFORMACIÓN PERSONAL
     */
    @PostMapping("/actualizar")
    public String actualizarPerfil(@RequestParam Map<String, String> params,
                                   Authentication auth,
                                   RedirectAttributes redirectAttributes) {
        try {
            Estilista estilista = obtenerEstilistaActual(auth);

            // Actualizar campos
            if (params.containsKey("nombre")) {
                estilista.setNombre(params.get("nombre"));
            }
            if (params.containsKey("documento")) {
                estilista.setDocumento(params.get("documento"));
            }
            if (params.containsKey("fechaNacimiento")) {
                LocalDate fecha = LocalDate.parse(params.get("fechaNacimiento"));
                estilista.setFechaNacimiento(fecha);
            }
            if (params.containsKey("telefono")) {
                estilista.setTelefono(params.get("telefono"));
            }
            if (params.containsKey("email")) {
                estilista.setEmail(params.get("email"));
            }
            if (params.containsKey("direccion")) {
                estilista.setDireccion(params.get("direccion"));
            }

            estilistaService.actualizarEstilista(estilista);
            redirectAttributes.addFlashAttribute("mensaje", "Información personal actualizada correctamente");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar: " + e.getMessage());
        }
        return "redirect:/estilista/perfil";
    }

    /**
     * ACTUALIZAR INFORMACIÓN PROFESIONAL
     */
    @PostMapping("/actualizar-profesional")
    public String actualizarProfesional(@RequestParam Map<String, String> params,
                                       Authentication auth,
                                       RedirectAttributes redirectAttributes) {
        try {
            Estilista estilista = obtenerEstilistaActual(auth);

            if (params.containsKey("experiencia")) {
                estilista.setExperienciaAnios(Integer.parseInt(params.get("experiencia")));
            }
            if (params.containsKey("certificaciones")) {
                estilista.setCertificaciones(params.get("certificaciones"));
            }
            
            // Construir especialidades desde checkboxes
            StringBuilder especialidades = new StringBuilder();
            if (params.containsKey("esp1")) especialidades.append("Cortes Clásicos,");
            if (params.containsKey("esp2")) especialidades.append("Fade,");
            if (params.containsKey("esp3")) especialidades.append("Barba,");
            if (params.containsKey("esp4")) especialidades.append("Degradados,");
            if (params.containsKey("esp5")) especialidades.append("Rapado,");
            if (params.containsKey("esp6")) especialidades.append("Diseño,");
            
            if (especialidades.length() > 0) {
                especialidades.deleteCharAt(especialidades.length() - 1); // Quitar última coma
                estilista.setEspecialidad(especialidades.toString());
            }

            estilistaService.actualizarEstilista(estilista);
            redirectAttributes.addFlashAttribute("mensaje", "Información profesional actualizada correctamente");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar: " + e.getMessage());
        }
        return "redirect:/estilista/perfil";
    }

    /**
     * CAMBIAR CONTRASEÑA
     */
    @PostMapping("/cambiar-password")
    public String cambiarPassword(@RequestParam("passwordActual") String passwordActual,
                                  @RequestParam("passwordNueva") String passwordNueva,
                                  @RequestParam("passwordConfirmar") String passwordConfirmar,
                                  Authentication auth,
                                  RedirectAttributes redirectAttributes) {
        try {
            // Validar que las contraseñas coincidan
            if (!passwordNueva.equals(passwordConfirmar)) {
                redirectAttributes.addFlashAttribute("error", "Las contraseñas nuevas no coinciden");
                return "redirect:/estilista/perfil";
            }
            
            // Validar longitud mínima
            if (passwordNueva.length() < 8) {
                redirectAttributes.addFlashAttribute("error", "La contraseña debe tener al menos 8 caracteres");
                return "redirect:/estilista/perfil";
            }

            String email = auth.getName();
            perfilService.cambiarPassword(email, passwordActual, passwordNueva);
            
            redirectAttributes.addFlashAttribute("mensaje", "Contraseña cambiada correctamente");

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cambiar contraseña: " + e.getMessage());
        }
        return "redirect:/estilista/perfil";
    }

    /**
     * CAMBIAR FOTO DE PERFIL
     */
    @PostMapping("/cambiar-foto")
    public String cambiarFoto(@RequestParam("foto") MultipartFile foto,
                             Authentication auth,
                             RedirectAttributes redirectAttributes) {
        try {
            // Validar archivo
            if (foto.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Debe seleccionar una foto");
                return "redirect:/estilista/perfil";
            }
            
            // Validar tamaño (máximo 2MB)
            if (foto.getSize() > 2 * 1024 * 1024) {
                redirectAttributes.addFlashAttribute("error", "La foto no debe superar los 2MB");
                return "redirect:/estilista/perfil";
            }
            
            // Validar tipo de archivo
            String contentType = foto.getContentType();
            if (contentType == null || (!contentType.equals("image/jpeg") && 
                !contentType.equals("image/png") && !contentType.equals("image/jpg"))) {
                redirectAttributes.addFlashAttribute("error", "Solo se permiten imágenes JPG o PNG");
                return "redirect:/estilista/perfil";
            }

            Estilista estilista = obtenerEstilistaActual(auth);
            String rutaFoto = perfilService.guardarFotoPerfil(foto, estilista.getIdEstilista());
            
            estilista.setFotoPerfil(rutaFoto);
            estilistaService.actualizarEstilista(estilista);
            
            redirectAttributes.addFlashAttribute("mensaje", "Foto de perfil actualizada correctamente");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al subir foto: " + e.getMessage());
        }
        return "redirect:/estilista/perfil";
    }

    /**
     * GUARDAR PREFERENCIAS
     */
    @PostMapping("/guardar-preferencias")
    @ResponseBody
    public Map<String, Object> guardarPreferencias(@RequestBody Map<String, Object> preferencias,
                                                   Authentication auth) {
        Map<String, Object> response = new java.util.HashMap<>();
        try {
            Estilista estilista = obtenerEstilistaActual(auth);
            
            // Actualizar preferencias de notificaciones
            if (preferencias.containsKey("notifReservas")) {
                estilista.setNotifReservas((Boolean) preferencias.get("notifReservas"));
            }
            if (preferencias.containsKey("notifCancelaciones")) {
                estilista.setNotifCancelaciones((Boolean) preferencias.get("notifCancelaciones"));
            }
            if (preferencias.containsKey("notifRecordatorios")) {
                estilista.setNotifRecordatorios((Boolean) preferencias.get("notifRecordatorios"));
            }
            
            estilistaService.actualizarEstilista(estilista);
            
            response.put("success", true);
            response.put("message", "Preferencias guardadas correctamente");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al guardar preferencias: " + e.getMessage());
        }
        return response;
    }

    /**
     * ACTIVAR/DESACTIVAR AUTENTICACIÓN EN DOS PASOS
     */
    @PostMapping("/toggle-2fa")
    @ResponseBody
    public Map<String, Object> toggleAutenticacionDosPasos(@RequestParam("activar") boolean activar,
                                                           Authentication auth) {
        Map<String, Object> response = new java.util.HashMap<>();
        try {
            Estilista estilista = obtenerEstilistaActual(auth);
            estilista.setAutenticacionDosPasos(activar);
            estilistaService.actualizarEstilista(estilista);
            
            response.put("success", true);
            response.put("message", activar ? 
                    "Autenticación en dos pasos activada" : 
                    "Autenticación en dos pasos desactivada");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al cambiar configuración: " + e.getMessage());
        }
        return response;
    }

    /**
     * Genera URL de avatar por defecto
     */
    private String generarAvatarURL(String nombre) {
        String nombreEncoded = nombre.replace(" ", "+");
        return "https://ui-avatars.com/api/?name=" + nombreEncoded + 
               "&size=200&background=0d6efd&color=fff";
    }
}
