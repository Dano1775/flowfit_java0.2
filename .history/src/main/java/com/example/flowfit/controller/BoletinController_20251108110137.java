package com.example.flowfit.controller;

import com.example.flowfit.model.BoletinInformativo;
import com.example.flowfit.model.BoletinInformativo.TipoDestinatario;
import com.example.flowfit.model.Usuario;
import com.example.flowfit.service.BoletinService;
import com.example.flowfit.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador para gestión de boletines informativos y envíos masivos
 * Soporta dos modos: mensaje personalizado y templates predefinidos
 */
@Controller
@RequestMapping("/admin/boletines")
public class BoletinController {
    
    @Autowired
    private BoletinService boletinService;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    /**
     * Muestra la página principal de boletines con opciones:
     * 1. Enviar mensaje personalizado
     * 2. Usar template predefinido
     */
    @GetMapping
    public String mostrarPaginaBoletines(Model model, HttpSession session) {
        // Verificar que el usuario esté logueado y sea administrador
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || !usuario.getPerfilUsuario().toString().equals("Administrador")) {
            return "redirect:/login";
        }
        
        // Obtener estadísticas de usuarios para mostrar
        model.addAttribute("totalUsuarios", usuarioRepository.count());
        model.addAttribute("totalActivos", usuarioRepository.countByEstado("A"));
        model.addAttribute("totalEntrenadores", usuarioRepository.countByPerfilUsuarioAndEstado(Usuario.PerfilUsuario.Entrenador, "A"));
        model.addAttribute("totalClientes", usuarioRepository.countByPerfilUsuarioAndEstado(Usuario.PerfilUsuario.Usuario, "A"));
        model.addAttribute("totalNutricionistas", usuarioRepository.countByPerfilUsuarioAndEstado(Usuario.PerfilUsuario.Nutricionista, "A"));
        
        // Obtener historial de boletines
        List<BoletinInformativo> historial = boletinService.obtenerTodosBoletines();
        model.addAttribute("historial", historial);
        
        // Templates disponibles
        model.addAttribute("templates", obtenerTemplatesDisponibles());
        
        return "admin/boletines/index";
    }
    
    /**
     * Muestra el formulario para crear mensaje personalizado
     */
    @GetMapping("/personalizado")
    public String mostrarFormularioPersonalizado(Model model, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || !usuario.getPerfilUsuario().toString().equals("Administrador")) {
            return "redirect:/login";
        }
        
        model.addAttribute("totalActivos", usuarioRepository.countByEstado("A"));
        model.addAttribute("destinatarios", TipoDestinatario.values());
        
        return "admin/boletines/personalizado";
    }
    
    /**
     * Muestra el formulario para seleccionar y enviar template
     */
    @GetMapping("/template")
    public String mostrarFormularioTemplate(Model model, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || !usuario.getPerfilUsuario().toString().equals("Administrador")) {
            return "redirect:/login";
        }
        
        model.addAttribute("templates", obtenerTemplatesDisponibles());
        model.addAttribute("destinatarios", TipoDestinatario.values());
        
        return "admin/boletines/template";
    }
    
    /**
     * Procesa el envío de mensaje personalizado
     */
    @PostMapping("/enviar-personalizado")
    public String enviarMensajePersonalizado(
            @RequestParam("asunto") String asunto,
            @RequestParam("contenido") String contenido,
            @RequestParam("tipoDestinatario") String tipoDestinatarioStr,
            RedirectAttributes redirectAttributes,
            HttpSession session) {
        
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || !usuario.getPerfilUsuario().toString().equals("Administrador")) {
            return "redirect:/login";
        }
        
        try {
            // Validar campos
            if (asunto == null || asunto.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "El asunto es obligatorio");
                return "redirect:/admin/boletines/personalizado";
            }
            
            if (contenido == null || contenido.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "El contenido es obligatorio");
                return "redirect:/admin/boletines/personalizado";
            }
            
            // Convertir tipo de destinatario
            TipoDestinatario tipoDestinatario = TipoDestinatario.valueOf(tipoDestinatarioStr.toUpperCase());
            
            // Obtener nombre del administrador
            String nombreAdmin = usuario.getNombre();
            if (nombreAdmin == null || nombreAdmin.isEmpty()) {
                nombreAdmin = "Administrador";
            }
            
            // Crear boletín
            BoletinInformativo boletin = boletinService.crearBoletin(
                asunto, 
                contenido, 
                tipoDestinatario, 
                nombreAdmin
            );
            
            // Enviar de forma asíncrona
            boletinService.enviarBoletin(boletin.getId());
            
            redirectAttributes.addFlashAttribute("success", 
                "✅ Boletín #" + boletin.getId() + " creado y enviando en segundo plano. Revisa el historial para ver el progreso.");
            
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Tipo de destinatario inválido");
            return "redirect:/admin/boletines/personalizado";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error inesperado: " + e.getMessage());
            return "redirect:/admin/boletines/personalizado";
        }
        
        return "redirect:/admin/boletines";
    }
    
    /**
     * Procesa el envío usando un template predefinido
     */
    @PostMapping("/enviar-template")
    public String enviarTemplate(
            @RequestParam("templateId") String templateId,
            @RequestParam("tipoDestinatario") String tipoDestinatarioStr,
            RedirectAttributes redirectAttributes,
            HttpSession session) {
        
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || !usuario.getPerfilUsuario().toString().equals("Administrador")) {
            return "redirect:/login";
        }
        
        try {
            // Obtener template
            Map<String, Object> template = obtenerTemplatePorId(templateId);
            if (template == null) {
                redirectAttributes.addFlashAttribute("error", "Template no encontrado");
                return "redirect:/admin/boletines/template";
            }
            
            // Convertir tipo de destinatario
            TipoDestinatario tipoDestinatario = TipoDestinatario.valueOf(tipoDestinatarioStr.toUpperCase());
            
            // Obtener nombre del administrador
            String nombreAdmin = usuario.getNombre();
            if (nombreAdmin == null || nombreAdmin.isEmpty()) {
                nombreAdmin = "Administrador";
            }
            
            // Crear boletín con el template
            BoletinInformativo boletin = boletinService.crearBoletin(
                (String) template.get("asunto"),
                (String) template.get("contenido"),
                tipoDestinatario,
                nombreAdmin
            );
            
            // Enviar de forma asíncrona
            boletinService.enviarBoletin(boletin.getId());
            
            redirectAttributes.addFlashAttribute("success", 
                "✅ Template '" + template.get("nombre") + "' enviado correctamente. Boletín #" + boletin.getId() + " en proceso.");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al enviar template: " + e.getMessage());
            return "redirect:/admin/boletines/template";
        }
        
        return "redirect:/admin/boletines";
    }
    
    /**
     * Ver detalle de un boletín enviado
     */
    @GetMapping("/{id}")
    public String verDetalleBoletin(@PathVariable Long id, Model model, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || !usuario.getPerfilUsuario().toString().equals("Administrador")) {
            return "redirect:/login";
        }
        
        BoletinInformativo boletin = boletinService.obtenerBoletinPorId(id);
        if (boletin == null) {
            return "redirect:/admin/boletines";
        }
        
        model.addAttribute("boletin", boletin);
        return "admin/boletines/detalle";
    }
    
    /**
     * Endpoint AJAX para obtener conteo de destinatarios
     */
    @GetMapping("/contar/{tipo}")
    @ResponseBody
    public Map<String, Object> contarDestinatarios(@PathVariable("tipo") String tipo) {
        Map<String, Object> response = new HashMap<>();
        
        long count;
        switch (tipo.toUpperCase()) {
            case "TODOS":
                count = usuarioRepository.count();
                break;
            case "USUARIOS_ACTIVOS":
                count = usuarioRepository.countByEstado("A");
                break;
            case "USUARIOS":
                count = usuarioRepository.countByPerfilUsuarioAndEstado(Usuario.PerfilUsuario.Usuario, "A");
                break;
            case "ENTRENADORES":
                count = usuarioRepository.countByPerfilUsuarioAndEstado(Usuario.PerfilUsuario.Entrenador, "A");
                break;
            case "NUTRICIONISTAS":
                count = usuarioRepository.countByPerfilUsuarioAndEstado(Usuario.PerfilUsuario.Nutricionista, "A");
                break;
            case "ADMINISTRADORES":
                count = usuarioRepository.countByPerfilUsuarioAndEstado(Usuario.PerfilUsuario.Administrador, "A");
                break;
            case "USUARIOS_INACTIVOS":
                count = usuarioRepository.countByEstado("I");
                break;
            default:
                count = 0;
        }
        
        response.put("count", count);
        response.put("tipo", tipo);
        return response;
    }
    
    /**
     * Templates predefinidos del sistema
     */
    private List<Map<String, Object>> obtenerTemplatesDisponibles() {
        return List.of(
            Map.of(
                "id", "bienvenida",
                "nombre", "🎉 Bienvenida a Nuevos Usuarios",
                "descripcion", "Template de bienvenida para usuarios recién registrados",
                "asunto", "¡Bienvenido a FlowFit! 🎉",
                "contenido", generarEmailBienvenida()
            ),
            Map.of(
                "id", "actualizacion",
                "nombre", "🚀 Nuevas Funcionalidades",
                "descripcion", "Notificar sobre actualizaciones y nuevas features",
                "asunto", "🚀 Nuevas funcionalidades disponibles en FlowFit",
                "contenido", generarEmailActualizacion()
            ),
            Map.of(
                "id", "motivacional",
                "nombre", "💪 Mensaje Motivacional",
                "descripcion", "Email motivacional para incentivar a los usuarios",
                "asunto", "💪 ¡No te rindas! Tu progreso es increíble",
                "contenido", generarEmailMotivacional()
            ),
            Map.of(
                "id", "mantenimiento",
                "nombre", "🔧 Mantenimiento Programado",
                "descripcion", "Notificar sobre mantenimiento o downtime planificado",
                "asunto", "⚠️ Mantenimiento programado - FlowFit",
                "contenido", generarEmailMantenimiento()
            ),
            Map.of(
                "id", "recordatorio",
                "nombre", "⏰ Recordatorio de Actividad",
                "descripcion", "Recordar a usuarios inactivos que regresen",
                "asunto", "⏰ ¡Te extrañamos en FlowFit!",
                "contenido", generarEmailRecordatorio()
            )
        );
    }
    
    /**
     * Genera HTML profesional para email de bienvenida (sin variables)
     */
    private String generarEmailBienvenida() {
        return "<!DOCTYPE html>" +
            "<html><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width,initial-scale=1.0'></head>" +
            "<body style='margin:0;padding:0;font-family:Arial,sans-serif;background-color:#f4f4f4;'>" +
            "<table width='100%' cellpadding='0' cellspacing='0' style='background-color:#f4f4f4;padding:20px 0;'>" +
            "<tr><td align='center'>" +
            "<table width='600' cellpadding='0' cellspacing='0' style='background-color:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.1);'>" +
            "<tr><td style='background:linear-gradient(135deg,#dc2626 0%,#991b1b 100%);padding:40px 30px;text-align:center;'>" +
            "<h1 style='color:#ffffff;margin:0;font-size:28px;font-weight:bold;'>¡Bienvenido a FlowFit! 🎉</h1>" +
            "</td></tr>" +
            "<tr><td style='padding:40px 30px;color:#333333;'>" +
            "<h2 style='color:#dc2626;margin:0 0 20px 0;font-size:22px;'>¡Hola!</h2>" +
            "<p style='line-height:1.6;margin:0 0 20px 0;font-size:16px;'>¡Bienvenido a <strong>FlowFit</strong>! Estamos emocionados de tenerte con nosotros.</p>" +
            "<p style='line-height:1.6;margin:0 0 15px 0;font-size:16px;font-weight:bold;color:#dc2626;'>Con FlowFit podrás:</p>" +
            "<table width='100%' style='margin-bottom:25px;'>" +
            "<tr><td style='padding:8px 0;'><span style='color:#dc2626;font-size:18px;'>✅</span> <span style='margin-left:10px;'>Acceder a rutinas personalizadas creadas por entrenadores profesionales</span></td></tr>" +
            "<tr><td style='padding:8px 0;'><span style='color:#dc2626;font-size:18px;'>✅</span> <span style='margin-left:10px;'>Hacer seguimiento de tu progreso</span></td></tr>" +
            "<tr><td style='padding:8px 0;'><span style='color:#dc2626;font-size:18px;'>✅</span> <span style='margin-left:10px;'>Recibir asesoría nutricional</span></td></tr>" +
            "<tr><td style='padding:8px 0;'><span style='color:#dc2626;font-size:18px;'>✅</span> <span style='margin-left:10px;'>Y mucho más...</span></td></tr>" +
            "</table>" +
            "<p style='line-height:1.6;margin:0 0 20px 0;font-size:16px;'>Si tienes alguna pregunta, no dudes en contactarnos.</p>" +
            "<div style='text-align:center;margin:30px 0;'>" +
            "<a href='#' style='display:inline-block;background:#dc2626;color:#ffffff;padding:15px 40px;text-decoration:none;border-radius:6px;font-weight:bold;font-size:16px;'>¡Comienza tu viaje fitness hoy!</a>" +
            "</div>" +
            "</td></tr>" +
            "<tr><td style='background-color:#f8f9fa;padding:20px 30px;text-align:center;border-top:1px solid #e9ecef;'>" +
            "<p style='margin:0;color:#6c757d;font-size:12px;'>© 2024 FlowFit. Todos los derechos reservados.</p>" +
            "</td></tr>" +
            "</table>" +
            "</td></tr></table>" +
            "</body></html>";
    }
    
    /**
     * Genera HTML profesional para email de actualización (sin variables)
     */
    private String generarEmailActualizacion() {
        return "<!DOCTYPE html>" +
            "<html><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width,initial-scale=1.0'></head>" +
            "<body style='margin:0;padding:0;font-family:Arial,sans-serif;background-color:#f4f4f4;'>" +
            "<table width='100%' cellpadding='0' cellspacing='0' style='background-color:#f4f4f4;padding:20px 0;'>" +
            "<tr><td align='center'>" +
            "<table width='600' cellpadding='0' cellspacing='0' style='background-color:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.1);'>" +
            "<tr><td style='background:#dc2626;padding:40px 30px;text-align:center;'>" +
            "<h1 style='color:#ffffff;margin:0;font-size:28px;font-weight:bold;'>🚀 Nuevas Funcionalidades</h1>" +
            "</td></tr>" +
            "<tr><td style='padding:40px 30px;color:#333333;'>" +
            "<h2 style='color:#dc2626;margin:0 0 20px 0;font-size:22px;'>¡Hola!</h2>" +
            "<p style='line-height:1.6;margin:0 0 25px 0;font-size:16px;'>Tenemos excelentes noticias para ti. <strong>FlowFit</strong> acaba de recibir nuevas actualizaciones:</p>" +
            "<div style='background:#f8f9fa;border-left:4px solid #dc2626;padding:20px;margin:20px 0;border-radius:4px;'>" +
            "<h3 style='color:#dc2626;margin:0 0 15px 0;font-size:18px;'>✨ ¿Qué hay de nuevo?</h3>" +
            "<table width='100%'>" +
            "<tr><td style='padding:8px 0;'><span style='font-size:18px;'>🏋️</span> <span style='margin-left:10px;'>Sistema mejorado de seguimiento de ejercicios</span></td></tr>" +
            "<tr><td style='padding:8px 0;'><span style='font-size:18px;'>📊</span> <span style='margin-left:10px;'>Nuevos reportes de progreso</span></td></tr>" +
            "<tr><td style='padding:8px 0;'><span style='font-size:18px;'>💬</span> <span style='margin-left:10px;'>Chat mejorado con tu entrenador</span></td></tr>" +
            "<tr><td style='padding:8px 0;'><span style='font-size:18px;'>📱</span> <span style='margin-left:10px;'>Mejor experiencia en dispositivos móviles</span></td></tr>" +
            "</table>" +
            "</div>" +
            "<p style='line-height:1.6;margin:20px 0;font-size:16px;'>Inicia sesión ahora para descubrir todas las mejoras.</p>" +
            "<div style='text-align:center;margin:30px 0;'>" +
            "<a href='#' style='display:inline-block;background:#dc2626;color:#ffffff;padding:15px 40px;text-decoration:none;border-radius:6px;font-weight:bold;font-size:16px;'>Explorar Ahora</a>" +
            "</div>" +
            "<p style='line-height:1.6;margin:0;font-size:16px;color:#6c757d;'>Gracias por confiar en FlowFit.</p>" +
            "</td></tr>" +
            "<tr><td style='background-color:#f8f9fa;padding:20px 30px;text-align:center;border-top:1px solid #e9ecef;'>" +
            "<p style='margin:0;color:#6c757d;font-size:12px;'>© 2024 FlowFit. Todos los derechos reservados.</p>" +
            "</td></tr>" +
            "</table>" +
            "</td></tr></table>" +
            "</body></html>";
    }
    
    /**
     * Genera HTML profesional para email motivacional (sin variables)
     */
    private String generarEmailMotivacional() {
        return "<!DOCTYPE html>" +
            "<html><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width,initial-scale=1.0'></head>" +
            "<body style='margin:0;padding:0;font-family:Arial,sans-serif;background-color:#f4f4f4;'>" +
            "<table width='100%' cellpadding='0' cellspacing='0' style='background-color:#f4f4f4;padding:20px 0;'>" +
            "<tr><td align='center'>" +
            "<table width='600' cellpadding='0' cellspacing='0' style='background-color:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.1);'>" +
            "<tr><td style='background:linear-gradient(135deg,#10b981 0%,#059669 100%);padding:40px 30px;text-align:center;'>" +
            "<h1 style='color:#ffffff;margin:0;font-size:28px;font-weight:bold;'>💪 ¡Sigue Así!</h1>" +
            "</td></tr>" +
            "<tr><td style='padding:40px 30px;color:#333333;'>" +
            "<h2 style='color:#10b981;margin:0 0 20px 0;font-size:22px;'>¡Hola!</h2>" +
            "<p style='line-height:1.6;margin:0 0 25px 0;font-size:16px;'>Queríamos tomarnos un momento para felicitarte por tu dedicación.</p>" +
            "<div style='background:#f0fdf4;border-left:5px solid #10b981;padding:20px;margin:25px 0;border-radius:4px;'>" +
            "<h3 style='color:#059669;margin:0 0 15px 0;font-size:18px;'>🌟 Recuerda:</h3>" +
            "<p style='font-style:italic;color:#555555;font-size:16px;line-height:1.7;margin:0;'>" +
            "\"El éxito no es el resultado de un día, sino de la constancia y el esfuerzo diario.\"" +
            "</p>" +
            "</div>" +
            "<p style='line-height:1.6;margin:0 0 20px 0;font-size:16px;'><strong>Cada entrenamiento cuenta.</strong> Cada comida saludable te acerca más a tu meta. No importa qué tan lento vayas, lo importante es que no te detengas.</p>" +
            "<p style='line-height:1.6;margin:0 0 20px 0;font-size:16px;'>El equipo de FlowFit está aquí para apoyarte en cada paso del camino.</p>" +
            "<div style='text-align:center;margin:30px 0;'>" +
            "<p style='font-size:20px;font-weight:bold;color:#10b981;margin:0;'>¡Sigue así! 💚</p>" +
            "</div>" +
            "</td></tr>" +
            "<tr><td style='background-color:#f8f9fa;padding:20px 30px;text-align:center;border-top:1px solid #e9ecef;'>" +
            "<p style='margin:0;color:#6c757d;font-size:12px;'>© 2024 FlowFit. Todos los derechos reservados.</p>" +
            "</td></tr>" +
            "</table>" +
            "</td></tr></table>" +
            "</body></html>";
    }
    
    /**
     * Genera HTML profesional para email de mantenimiento (sin variables)
     */
    private String generarEmailMantenimiento() {
        return "<!DOCTYPE html>" +
            "<html><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width,initial-scale=1.0'></head>" +
            "<body style='margin:0;padding:0;font-family:Arial,sans-serif;background-color:#f4f4f4;'>" +
            "<table width='100%' cellpadding='0' cellspacing='0' style='background-color:#f4f4f4;padding:20px 0;'>" +
            "<tr><td align='center'>" +
            "<table width='600' cellpadding='0' cellspacing='0' style='background-color:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.1);'>" +
            "<tr><td style='background:#f59e0b;padding:40px 30px;text-align:center;'>" +
            "<h1 style='color:#ffffff;margin:0;font-size:28px;font-weight:bold;'>🔧 Mantenimiento Programado</h1>" +
            "</td></tr>" +
            "<tr><td style='padding:40px 30px;color:#333333;'>" +
            "<h2 style='color:#f59e0b;margin:0 0 20px 0;font-size:22px;'>Estimado usuario,</h2>" +
            "<p style='line-height:1.6;margin:0 0 25px 0;font-size:16px;'>Te informamos que <strong>FlowFit</strong> estará en mantenimiento programado:</p>" +
            "<div style='background:#fffbeb;border:2px solid #fbbf24;padding:25px;margin:20px 0;border-radius:8px;'>" +
            "<h3 style='color:#d97706;margin:0 0 15px 0;font-size:18px;'>📅 Detalles del mantenimiento:</h3>" +
            "<table width='100%' style='color:#78350f;'>" +
            "<tr><td style='padding:8px 0;'><strong>Inicio:</strong> Próximamente</td></tr>" +
            "<tr><td style='padding:8px 0;'><strong>Duración estimada:</strong> 2-3 horas</td></tr>" +
            "<tr><td style='padding:8px 0;'><strong>Motivo:</strong> Mejoras en el sistema y optimización de rendimiento</td></tr>" +
            "</table>" +
            "</div>" +
            "<div style='background:#fef3c7;padding:15px;border-radius:4px;margin:20px 0;'>" +
            "<p style='margin:0;font-size:15px;'><strong>⚠️ Importante:</strong> Durante este tiempo, <strong>no podrás acceder a la plataforma</strong>.</p>" +
            "</div>" +
            "<p style='line-height:1.6;margin:20px 0;font-size:16px;'>Disculpa las molestias. Estamos trabajando para brindarte un mejor servicio.</p>" +
            "<p style='line-height:1.6;margin:0;font-size:16px;color:#6c757d;'>Gracias por tu comprensión.</p>" +
            "</td></tr>" +
            "<tr><td style='background-color:#f8f9fa;padding:20px 30px;text-align:center;border-top:1px solid #e9ecef;'>" +
            "<p style='margin:0;color:#6c757d;font-size:12px;'>© 2024 FlowFit. Todos los derechos reservados.</p>" +
            "</td></tr>" +
            "</table>" +
            "</td></tr></table>" +
            "</body></html>";
    }
    
    /**
     * Genera HTML profesional para email de recordatorio (sin variables)
     */
    private String generarEmailRecordatorio() {
        return "<!DOCTYPE html>" +
            "<html><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width,initial-scale=1.0'></head>" +
            "<body style='margin:0;padding:0;font-family:Arial,sans-serif;background-color:#f4f4f4;'>" +
            "<table width='100%' cellpadding='0' cellspacing='0' style='background-color:#f4f4f4;padding:20px 0;'>" +
            "<tr><td align='center'>" +
            "<table width='600' cellpadding='0' cellspacing='0' style='background-color:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.1);'>" +
            "<tr><td style='background:linear-gradient(135deg,#3b82f6 0%,#1d4ed8 100%);padding:40px 30px;text-align:center;'>" +
            "<h1 style='color:#ffffff;margin:0;font-size:28px;font-weight:bold;'>⏰ ¡Te Extrañamos!</h1>" +
            "</td></tr>" +
            "<tr><td style='padding:40px 30px;color:#333333;'>" +
            "<h2 style='color:#3b82f6;margin:0 0 20px 0;font-size:22px;'>Hola,</h2>" +
            "<p style='line-height:1.6;margin:0 0 20px 0;font-size:16px;'>Hemos notado que hace tiempo que no inicias sesión en <strong>FlowFit</strong>.</p>" +
            "<p style='line-height:1.6;margin:0 0 25px 0;font-size:16px;'>¿Todo está bien? Queremos recordarte que tu progreso es importante para nosotros.</p>" +
            "<div style='background:#eff6ff;border-left:5px solid #3b82f6;padding:20px;margin:25px 0;border-radius:4px;'>" +
            "<h3 style='color:#1e40af;margin:0 0 15px 0;font-size:18px;'>🎯 ¿Qué te espera?</h3>" +
            "<table width='100%'>" +
            "<tr><td style='padding:8px 0;'><span style='color:#3b82f6;font-size:18px;'>✅</span> <span style='margin-left:10px;'>Tus rutinas personalizadas siguen esperándote</span></td></tr>" +
            "<tr><td style='padding:8px 0;'><span style='color:#3b82f6;font-size:18px;'>✅</span> <span style='margin-left:10px;'>Tu entrenador está listo para ayudarte</span></td></tr>" +
            "<tr><td style='padding:8px 0;'><span style='color:#3b82f6;font-size:18px;'>✅</span> <span style='margin-left:10px;'>Nuevas funcionalidades disponibles</span></td></tr>" +
            "</table>" +
            "</div>" +
            "<div style='text-align:center;margin:30px 0;'>" +
            "<a href='#' style='display:inline-block;background:#3b82f6;color:#ffffff;padding:15px 40px;text-decoration:none;border-radius:6px;font-weight:bold;font-size:16px;'>¡Vuelve Hoy!</a>" +
            "</div>" +
            "<p style='line-height:1.6;margin:20px 0;font-size:16px;font-style:italic;color:#6c757d;text-align:center;'>\"La constancia es la clave del éxito.\"</p>" +
            "<p style='line-height:1.6;margin:0;font-size:16px;text-align:center;'>Te esperamos de vuelta.</p>" +
            "</td></tr>" +
            "<tr><td style='background-color:#f8f9fa;padding:20px 30px;text-align:center;border-top:1px solid #e9ecef;'>" +
            "<p style='margin:0;color:#6c757d;font-size:12px;'>© 2024 FlowFit. Todos los derechos reservados.</p>" +
            "</td></tr>" +
            "</table>" +
            "</td></tr></table>" +
            "</body></html>";
    }
    
    /**
     * Obtiene un template específico por su ID
     */
    private Map<String, Object> obtenerTemplatePorId(String templateId) {
        return obtenerTemplatesDisponibles().stream()
            .filter(t -> templateId.equals(t.get("id")))
            .findFirst()
            .orElse(null);
    }
}
