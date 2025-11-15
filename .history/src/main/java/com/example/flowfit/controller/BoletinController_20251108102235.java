package com.example.flowfit.controller;

import com.example.flowfit.model.BoletinInformativo;
import com.example.flowfit.model.BoletinInformativo.TipoDestinatario;
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
        String perfilUsuario = (String) session.getAttribute("perfil_usuario");
        if (!"Administrador".equals(perfilUsuario)) {
            return "redirect:/login";
        }
        
        // Obtener estadísticas de usuarios para mostrar
        model.addAttribute("totalUsuarios", usuarioRepository.count());
        model.addAttribute("totalActivos", usuarioRepository.countByEstado("A"));
        model.addAttribute("totalEntrenadores", usuarioRepository.countByPerfilUsuarioStringAndEstado("Entrenador", "A"));
        model.addAttribute("totalClientes", usuarioRepository.countByPerfilUsuarioStringAndEstado("Usuario", "A"));
        model.addAttribute("totalNutricionistas", usuarioRepository.countByPerfilUsuarioStringAndEstado("Nutricionista", "A"));
        
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
        String perfilUsuario = (String) session.getAttribute("perfil_usuario");
        if (!"Administrador".equals(perfilUsuario)) {
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
        String perfilUsuario = (String) session.getAttribute("perfil_usuario");
        if (!"Administrador".equals(perfilUsuario)) {
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
        
        String perfilUsuario = (String) session.getAttribute("perfil_usuario");
        if (!"Administrador".equals(perfilUsuario)) {
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
            String nombreAdmin = (String) session.getAttribute("nombre_usuario");
            if (nombreAdmin == null) {
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
        
        String perfilUsuario = (String) session.getAttribute("perfil_usuario");
        if (!"Administrador".equals(perfilUsuario)) {
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
            String nombreAdmin = (String) session.getAttribute("nombre_usuario");
            if (nombreAdmin == null) {
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
        String perfilUsuario = (String) session.getAttribute("perfil_usuario");
        if (!"Administrador".equals(perfilUsuario)) {
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
                count = usuarioRepository.countByPerfilUsuarioStringAndEstado("Usuario", "A");
                break;
            case "ENTRENADORES":
                count = usuarioRepository.countByPerfilUsuarioStringAndEstado("Entrenador", "A");
                break;
            case "NUTRICIONISTAS":
                count = usuarioRepository.countByPerfilUsuarioStringAndEstado("Nutricionista", "A");
                break;
            case "ADMINISTRADORES":
                count = usuarioRepository.countByPerfilUsuarioStringAndEstado("Administrador", "A");
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
                "contenido", "<h2>¡Hola {{nombre}}!</h2>" +
                    "<p>¡Bienvenido a <strong>FlowFit</strong>! Estamos emocionados de tenerte con nosotros.</p>" +
                    "<p>Con FlowFit podrás:</p>" +
                    "<ul>" +
                    "<li>✅ Acceder a rutinas personalizadas creadas por entrenadores profesionales</li>" +
                    "<li>✅ Hacer seguimiento de tu progreso</li>" +
                    "<li>✅ Recibir asesoría nutricional</li>" +
                    "<li>✅ Y mucho más...</li>" +
                    "</ul>" +
                    "<p>Tu perfil actual es: <strong>{{perfil}}</strong></p>" +
                    "<p>Si tienes alguna pregunta, no dudes en contactarnos.</p>" +
                    "<p><strong>¡Comienza tu viaje fitness hoy!</strong></p>" +
                    "<hr>" +
                    "<p style='color: #666; font-size: 12px;'>Este correo fue enviado a {{correo}}</p>"
            ),
            Map.of(
                "id", "actualizacion",
                "nombre", "🚀 Nuevas Funcionalidades",
                "descripcion", "Notificar sobre actualizaciones y nuevas features",
                "asunto", "🚀 Nuevas funcionalidades disponibles en FlowFit",
                "contenido", "<h2>¡Hola {{nombre}}!</h2>" +
                    "<p>Tenemos excelentes noticias para ti. <strong>FlowFit</strong> acaba de recibir nuevas actualizaciones:</p>" +
                    "<h3>✨ ¿Qué hay de nuevo?</h3>" +
                    "<ul>" +
                    "<li>🏋️ Sistema mejorado de seguimiento de ejercicios</li>" +
                    "<li>📊 Nuevos reportes de progreso</li>" +
                    "<li>💬 Chat mejorado con tu entrenador</li>" +
                    "<li>📱 Mejor experiencia en dispositivos móviles</li>" +
                    "</ul>" +
                    "<p>Inicia sesión ahora para descubrir todas las mejoras.</p>" +
                    "<p>Gracias por confiar en FlowFit.</p>" +
                    "<hr>" +
                    "<p style='color: #666; font-size: 12px;'>Correo enviado a: {{correo}}</p>"
            ),
            Map.of(
                "id", "motivacional",
                "nombre", "💪 Mensaje Motivacional",
                "descripcion", "Email motivacional para incentivar a los usuarios",
                "asunto", "💪 ¡No te rindas! Tu progreso es increíble",
                "contenido", "<h2>¡Hola {{nombre}}!</h2>" +
                    "<p>Queríamos tomarnos un momento para felicitarte por tu dedicación.</p>" +
                    "<h3>🌟 Recuerda:</h3>" +
                    "<blockquote style='border-left: 4px solid #4CAF50; padding-left: 15px; color: #555;'>" +
                    "\"El éxito no es el resultado de un día, sino de la constancia y el esfuerzo diario.\"" +
                    "</blockquote>" +
                    "<p><strong>Cada entrenamiento cuenta.</strong> Cada comida saludable te acerca más a tu meta. No importa qué tan lento vayas, lo importante es que no te detengas.</p>" +
                    "<p>El equipo de FlowFit está aquí para apoyarte en cada paso del camino.</p>" +
                    "<p><strong>¡Sigue así, {{nombre}}! 💚</strong></p>" +
                    "<hr>" +
                    "<p style='color: #666; font-size: 12px;'>Recibido en: {{correo}}</p>"
            ),
            Map.of(
                "id", "mantenimiento",
                "nombre", "🔧 Mantenimiento Programado",
                "descripcion", "Notificar sobre mantenimiento o downtime planificado",
                "asunto", "⚠️ Mantenimiento programado - FlowFit",
                "contenido", "<h2>Hola {{nombre}},</h2>" +
                    "<p>Te informamos que <strong>FlowFit</strong> estará en mantenimiento programado:</p>" +
                    "<div style='background: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 20px 0;'>" +
                    "<h3 style='margin-top: 0;'>📅 Detalles del mantenimiento:</h3>" +
                    "<p><strong>Inicio:</strong> [FECHA Y HORA]</p>" +
                    "<p><strong>Duración estimada:</strong> [TIEMPO]</p>" +
                    "<p><strong>Motivo:</strong> Mejoras en el sistema y optimización de rendimiento</p>" +
                    "</div>" +
                    "<p>Durante este tiempo, <strong>no podrás acceder a la plataforma</strong>.</p>" +
                    "<p>Disculpa las molestias. Estamos trabajando para brindarte un mejor servicio.</p>" +
                    "<p>Gracias por tu comprensión.</p>" +
                    "<hr>" +
                    "<p style='color: #666; font-size: 12px;'>Contacto: {{correo}}</p>"
            ),
            Map.of(
                "id", "recordatorio",
                "nombre", "⏰ Recordatorio de Actividad",
                "descripcion", "Recordar a usuarios inactivos que regresen",
                "asunto", "⏰ ¡Te extrañamos en FlowFit!",
                "contenido", "<h2>Hola {{nombre}},</h2>" +
                    "<p>Hemos notado que hace tiempo que no inicias sesión en <strong>FlowFit</strong>.</p>" +
                    "<p>¿Todo está bien? Queremos recordarte que tu progreso es importante para nosotros.</p>" +
                    "<h3>🎯 ¿Qué te espera?</h3>" +
                    "<ul>" +
                    "<li>✅ Tus rutinas personalizadas siguen esperándote</li>" +
                    "<li>✅ Tu entrenador está listo para ayudarte</li>" +
                    "<li>✅ Nuevas funcionalidades disponibles</li>" +
                    "</ul>" +
                    "<p><strong>¡Vuelve hoy y continúa tu transformación!</strong></p>" +
                    "<p>Recuerda: <em>\"La constancia es la clave del éxito.\"</em></p>" +
                    "<p>Te esperamos de vuelta.</p>" +
                    "<hr>" +
                    "<p style='color: #666; font-size: 12px;'>Tu cuenta: {{correo}}</p>"
            )
        );
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
