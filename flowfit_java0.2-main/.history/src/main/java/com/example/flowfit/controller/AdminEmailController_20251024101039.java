package com.example.flowfit.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.example.flowfit.service.EmailMasivoService;
import com.example.flowfit.service.EmailMasivoService.ResultadoEnvio;
import com.example.flowfit.repository.UsuarioRepository;

import jakarta.servlet.http.HttpSession;
import java.util.concurrent.CompletableFuture;

/**
 * Controlador para el envío masivo de correos desde el panel de administrador
 */
@Controller
@RequestMapping("/admin/correos")
public class AdminEmailController {
    
    @Autowired
    private EmailMasivoService emailMasivoService;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    /**
     * Muestra la página de envío masivo de correos
     */
    @GetMapping
    public String mostrarPaginaEnvio(Model model, HttpSession session) {
        // Verificar que el usuario esté logueado y sea administrador
        String perfilUsuario = (String) session.getAttribute("perfil_usuario");
        if (!"Administrador".equals(perfilUsuario)) {
            return "redirect:/admin/login";
        }
        
        // Obtener estadísticas de usuarios para mostrar
        model.addAttribute("totalUsuarios", usuarioRepository.countByEstado("A"));
        model.addAttribute("totalEntrenadores", usuarioRepository.countByPerfilUsuarioStringAndEstado("Entrenador", "A"));
        model.addAttribute("totalClientes", usuarioRepository.countByPerfilUsuarioStringAndEstado("Usuario", "A"));
        model.addAttribute("totalNutricionistas", usuarioRepository.countByPerfilUsuarioStringAndEstado("Nutricionista", "A"));
        model.addAttribute("totalAdministradores", usuarioRepository.countByPerfilUsuarioStringAndEstado("Administrador", "A"));
        
        return "admin/correos/envio-masivo";
    }
    
    /**
     * Procesa el envío masivo de correos
     */
    @PostMapping("/enviar")
    public String enviarCorreoMasivo(
            @RequestParam("asunto") String asunto,
            @RequestParam("mensaje") String mensaje,
            @RequestParam("tipoDestinatario") String tipoDestinatario,
            RedirectAttributes redirectAttributes,
            HttpSession session) {
        
        // Verificar que el usuario esté logueado y sea administrador
        String perfilUsuario = (String) session.getAttribute("perfil_usuario");
        if (!"Administrador".equals(perfilUsuario)) {
            return "redirect:/admin/login";
        }
        
        try {
            // Validar campos obligatorios
            if (asunto == null || asunto.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "El asunto es obligatorio");
                return "redirect:/admin/correos";
            }
            
            if (mensaje == null || mensaje.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "El mensaje es obligatorio");
                return "redirect:/admin/correos";
            }
            
            // Enviar correo masivo de forma asíncrona
            CompletableFuture<ResultadoEnvio> futureResultado = emailMasivoService.enviarCorreoMasivo(asunto, mensaje, tipoDestinatario);
            
            // Esperar el resultado (en una implementación real, podrías manejar esto de forma más sofisticada)
            ResultadoEnvio resultado = futureResultado.get();
            
            if (resultado.getError() != null) {
                redirectAttributes.addFlashAttribute("error", "Error en el envío: " + resultado.getError());
            } else {
                String mensajeExito = String.format(
                    "Envío completado: %d correos enviados exitosamente de %d total (%.1f%% éxito). %d fallidos.",
                    resultado.getEnviadosExitosos(),
                    resultado.getTotalDestinatarios(),
                    resultado.getPorcentajeExito(),
                    resultado.getEnviadosFallidos()
                );
                redirectAttributes.addFlashAttribute("success", mensajeExito);
            }
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error inesperado: " + e.getMessage());
        }
        
        return "redirect:/admin/correos";
    }
    
    /**
     * Endpoint AJAX para previsualizar el mensaje
     */
    @PostMapping("/preview")
    @ResponseBody
    public String previsualizarMensaje(@RequestParam("mensaje") String mensaje) {
        // Reemplazar variables de ejemplo para la previsualización
        String preview = mensaje.replace("{nombre}", "Juan Pérez");
        preview = preview.replace("{email}", "juan.perez@example.com");
        return preview;
    }
    
    /**
     * Endpoint AJAX para obtener conteo de destinatarios
     */
    @GetMapping("/contar/{tipo}")
    @ResponseBody
    public int contarDestinatarios(@PathVariable("tipo") String tipo) {
        switch (tipo.toUpperCase()) {
            case "TODOS":
                return (int) usuarioRepository.countByEstado("A");
            case "ENTRENADORES":
                return (int) usuarioRepository.countByPerfilUsuarioStringAndEstado("Entrenador", "A");
            case "USUARIOS":
            case "CLIENTES":
                return (int) usuarioRepository.countByPerfilUsuarioStringAndEstado("Usuario", "A");
            case "NUTRICIONISTAS":
                return (int) usuarioRepository.countByPerfilUsuarioStringAndEstado("Nutricionista", "A");
            case "ADMINISTRADORES":
                return (int) usuarioRepository.countByPerfilUsuarioStringAndEstado("Administrador", "A");
            default:
                return (int) usuarioRepository.countByEstado("A");
        }
    }
}