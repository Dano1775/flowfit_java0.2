package com.example.flowfit.controller;

import com.example.flowfit.model.*;
import com.example.flowfit.service.*;
import com.example.flowfit.dto.PropuestaDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import jakarta.servlet.http.HttpSession;
import java.util.*;

/**
 * Controlador para gestionar el sistema de negociación y escrow (protección anti-estafas)
 */
@Controller
@RequestMapping("/negociacion")
public class NegociacionController {

    @Autowired
    private NegociacionService negociacionService;
    
    @Autowired
    private EscrowService escrowService;
    
    /**
     * Entrenador envía propuesta inicial
     */
    @PostMapping("/enviar-propuesta")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> enviarPropuesta(
            @RequestBody PropuestaDTO propuesta,
            @RequestParam Long conversacionId,
            HttpSession session) {
        
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null || !usuario.getPerfilUsuario().name().equals("Entrenador")) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "No tienes permiso para enviar propuestas");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            Map<String, Object> response = negociacionService.enviarPropuestaInicial(
                conversacionId, usuario.getId(), propuesta);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Usuario/Entrenador responde a propuesta
     */
    @PostMapping("/responder")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> responderPropuesta(
            @RequestBody Map<String, Object> requestBody,
            HttpSession session) {
        
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Sesión no válida");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            Long contratacionId = ((Number) requestBody.get("contratacionId")).longValue();
            String accion = (String) requestBody.get("accion");
            
            PropuestaDTO contraoferta = null;
            if (requestBody.containsKey("propuesta")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> propuestaMap = (Map<String, Object>) requestBody.get("propuesta");
                contraoferta = mapToPropuestaDTO(propuestaMap);
            }
            
            Map<String, Object> response;
            if (usuario.getPerfilUsuario().name().equals("Entrenador")) {
                response = negociacionService.entrenadorRespondeContraoferta(
                    contratacionId, usuario.getId(), accion, contraoferta);
            } else {
                response = negociacionService.responderPropuesta(
                    contratacionId, usuario.getId(), accion, contraoferta);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Usuario confirma que recibió el servicio (sistema ESCROW)
     */
    @PostMapping("/confirmar-servicio/usuario")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> usuarioConfirmaServicio(
            @RequestParam Long pagoId,
            @RequestParam(required = false) String comentario,
            HttpSession session) {
        
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Sesión no válida");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            Map<String, Object> response = escrowService.usuarioConfirmaServicio(
                pagoId, usuario.getId(), comentario);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Entrenador confirma que cumplió con el servicio (sistema ESCROW)
     */
    @PostMapping("/confirmar-servicio/entrenador")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> entrenadorConfirmaServicio(
            @RequestParam Long pagoId,
            @RequestParam(required = false) String comentario,
            HttpSession session) {
        
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null || !usuario.getPerfilUsuario().name().equals("Entrenador")) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "No tienes permiso");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            Map<String, Object> response = escrowService.entrenadorConfirmaServicio(
                pagoId, usuario.getId(), comentario);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Iniciar disputa (usuario o entrenador)
     */
    @PostMapping("/disputa/iniciar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> iniciarDisputa(
            @RequestParam Long pagoId,
            @RequestParam String razon,
            HttpSession session) {
        
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Sesión no válida");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            Map<String, Object> response = escrowService.iniciarDisputa(
                pagoId, usuario.getId(), razon);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Obtener estado del escrow
     */
    @GetMapping("/escrow/estado/{pagoId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerEstadoEscrow(
            @PathVariable Long pagoId,
            HttpSession session) {
        
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Sesión no válida");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            Map<String, Object> estado = escrowService.obtenerEstadoEscrow(pagoId);
            estado.put("success", true);
            
            return ResponseEntity.ok(estado);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // Método auxiliar para convertir Map a PropuestaDTO
    private PropuestaDTO mapToPropuestaDTO(Map<String, Object> map) {
        PropuestaDTO dto = new PropuestaDTO();
        
        if (map.get("precio") != null) {
            dto.setPrecio(new java.math.BigDecimal(map.get("precio").toString()));
        }
        if (map.get("duracionDias") != null) {
            dto.setDuracionDias(((Number) map.get("duracionDias")).intValue());
        }
        if (map.get("rutinasMes") != null) {
            dto.setRutinasMes(((Number) map.get("rutinasMes")).intValue());
        }
        if (map.get("videollamadasMes") != null) {
            dto.setVideollamadasMes(((Number) map.get("videollamadasMes")).intValue());
        }
        if (map.get("seguimientoSemanal") != null) {
            dto.setSeguimientoSemanal((Boolean) map.get("seguimientoSemanal"));
        }
        if (map.get("planNutricional") != null) {
            dto.setPlanNutricional((Boolean) map.get("planNutricional"));
        }
        if (map.get("chatDirecto") != null) {
            dto.setChatDirecto((Boolean) map.get("chatDirecto"));
        }
        if (map.get("mensaje") != null) {
            dto.setMensaje((String) map.get("mensaje"));
        }
        
        return dto;
    }
}
