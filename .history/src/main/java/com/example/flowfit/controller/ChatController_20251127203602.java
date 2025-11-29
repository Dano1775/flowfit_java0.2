package com.example.flowfit.controller;

import com.example.flowfit.model.*;
import com.example.flowfit.service.*;
import com.example.flowfit.dto.MensajeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import jakarta.servlet.http.HttpSession;
import java.util.*;

/**
 * Controlador para gestionar el sistema de chat entre usuarios y entrenadores
 */
@Controller
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ChatService chatService;
    
    @Autowired
    private UsuarioService usuarioService;
    
    /**
     * Vista principal de chat
     */
    @GetMapping("")
    public String verChats(Model model, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/login";
        }
        
        boolean esUsuario = !usuario.getPerfilUsuario().name().equals("Entrenador");
        
        List<Conversacion> conversaciones;
        if (usuario.getPerfilUsuario().name().equals("Entrenador")) {
            conversaciones = chatService.obtenerConversacionesEntrenador(usuario.getId());
        } else {
            conversaciones = chatService.obtenerConversacionesUsuario(usuario.getId());
        }
        
        model.addAttribute("conversaciones", conversaciones);
        model.addAttribute("usuario", usuario);
        model.addAttribute("esUsuario", esUsuario);
        
        return "chat/lista-conversaciones";
    }
    
    /**
     * Vista de conversación específica
     */
    @GetMapping("/conversacion/{conversacionId}")
    public String verConversacion(@PathVariable Long conversacionId, Model model, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/login";
        }
        
        boolean esUsuario = !usuario.getPerfilUsuario().name().equals("Entrenador");
        
        // Obtener conversación primero
        Conversacion conversacion = chatService.obtenerConversacion(conversacionId);
        if (conversacion == null) {
            return "redirect:/chat";
        }
        
        // Obtener mensajes
        List<Mensaje> mensajes = chatService.obtenerMensajes(conversacionId);
        
        // Marcar mensajes como leídos
        chatService.marcarComoLeidos(conversacionId, usuario.getId());
        
        // Obtener información de la otra persona
        Usuario otraPersona = null;
        if (conversacion.getUsuarioId().equals(usuario.getId())) {
            otraPersona = usuarioService.obtenerUsuarioPorId(conversacion.getEntrenadorId());
        } else {
            otraPersona = usuarioService.obtenerUsuarioPorId(conversacion.getUsuarioId());
        }
        
        model.addAttribute("conversacion", conversacion);
        model.addAttribute("mensajes", mensajes);
        model.addAttribute("otraPersona", otraPersona);
        model.addAttribute("usuario", usuario);
        model.addAttribute("esUsuario", esUsuario);
        
        return "chat/conversacion";
    }
    
    /**
     * Iniciar chat con un entrenador
     */
    @PostMapping("/iniciar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> iniciarChat(
            @RequestParam Integer entrenadorId,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                response.put("success", false);
                response.put("message", "Sesión no válida");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            Conversacion conversacion = chatService.obtenerOCrearConversacion(usuario.getId(), entrenadorId);
            
            response.put("success", true);
            response.put("conversacionId", conversacion.getId());
            response.put("message", "Chat iniciado");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Enviar mensaje
     */
    @PostMapping("/enviar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> enviarMensaje(
            @RequestParam Long conversacionId,
            @RequestParam String contenido,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                response.put("success", false);
                response.put("message", "Sesión no válida");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // Crear DTO con los datos
            MensajeDTO mensajeDTO = new MensajeDTO();
            mensajeDTO.setConversacionId(conversacionId);
            mensajeDTO.setContenido(contenido);
            
            Mensaje mensaje = chatService.enviarMensaje(mensajeDTO, usuario.getId());
            
            // Enviar notificación en tiempo real vía WebSocket
            Map<String, Object> mensajeWs = new HashMap<>();
            mensajeWs.put("id", mensaje.getId());
            mensajeWs.put("conversacionId", mensaje.getConversacionId());
            mensajeWs.put("remitenteId", mensaje.getRemitenteId());
            mensajeWs.put("contenido", mensaje.getContenido());
            mensajeWs.put("fechaEnvio", mensaje.getFechaEnvio().toString());
            mensajeWs.put("remitenteNombre", usuario.getNombre());
            
            // Enviar a todos los suscritos a esta conversación
            messagingTemplate.convertAndSend("/topic/conversacion/" + conversacionId, mensajeWs);
            
            response.put("success", true);
            response.put("mensajeId", mensaje.getId());
            response.put("message", "Mensaje enviado");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Obtener mensajes no leídos
     */
    @GetMapping("/no-leidos")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerMensajesNoLeidos(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                response.put("success", false);
                response.put("message", "Sesión no válida");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            boolean esEntrenador = usuario.getPerfilUsuario().name().equals("Entrenador");
            Long noLeidos = chatService.contarMensajesNoLeidos(usuario.getId(), esEntrenador);
            
            response.put("success", true);
            response.put("noLeidos", noLeidos);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
