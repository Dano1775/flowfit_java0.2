package com.example.flowfit.controller;

import com.example.flowfit.model.*;
import com.example.flowfit.repository.ContratacionEntrenadorRepository;
import com.example.flowfit.repository.MensajeRepository;
import com.example.flowfit.repository.PagoContratacionRepository;
import com.example.flowfit.service.*;
import com.example.flowfit.dto.MensajeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Optional;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
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

    @Autowired
    private PlanEntrenadorService planEntrenadorService;

    @Autowired
    private ContratacionEntrenadorRepository contratacionRepo;

    @Autowired
    private PagoContratacionRepository pagoContratacionRepository;

    @Autowired
    private MensajeRepository mensajeRepository;

    @Autowired
    private FileUploadService fileUploadService;

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
        try {
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

            // Obtener información de la otra persona
            Usuario otraPersona = null;
            if (conversacion.getUsuarioId().equals(usuario.getId())) {
                otraPersona = usuarioService.obtenerUsuarioPorId(conversacion.getEntrenadorId());
            } else {
                otraPersona = usuarioService.obtenerUsuarioPorId(conversacion.getUsuarioId());
            }

            // Si es entrenador, cargar sus planes activos
            if (!esUsuario) {
                List<PlanEntrenador> planesEntrenador = planEntrenadorService.obtenerPlanesActivos(usuario.getId());
                model.addAttribute("planesEntrenador", planesEntrenador);
            }

            // Buscar contratación activa para esta conversación
            Optional<ContratacionEntrenador> contratacionOpt = contratacionRepo.findByUsuarioIdAndEntrenadorId(
                    conversacion.getUsuarioId(),
                    conversacion.getEntrenadorId());

            ContratacionEntrenador contratacion = null;
            if (contratacionOpt.isPresent()) {
                contratacion = contratacionOpt.get();
            }

            // Reconciliación en carga: si ya hay pago aprobado, no permitir que reaparezcan
            // CTAs/propuestas y
            // asegurar confirmación persistente en el historial.
            if (contratacion != null && contratacion.getId() != null) {
                Optional<PagoContratacion> pagoOpt = pagoContratacionRepository
                        .findByContratacionId(contratacion.getId());
                pagoOpt.ifPresent(pago -> model.addAttribute("pago", pago));

                if (pagoOpt.isPresent() && pagoOpt.get().getEstadoPago() == PagoContratacion.EstadoPago.APROBADO) {
                    // 1) Eliminar CTA obsoleto (pago pendiente)
                    mensajeRepository.softDeleteSistemaByMetadataPattern(
                            conversacionId,
                            "\"tipo\":\"PAGO_PENDIENTE\"",
                            "\"tipo\" : \"PAGO_PENDIENTE\"");

                    // 2) Invalidar propuestas activas para evitar que se rendericen luego del pago
                    List<Mensaje> propuestas = mensajeRepository.findPropuestasPorConversacion(conversacionId);
                    if (!propuestas.isEmpty()) {
                        for (Mensaje propuesta : propuestas) {
                            propuesta.setEliminado(true);
                        }
                        mensajeRepository.saveAll(propuestas);
                    }

                    // 3) Garantizar presencia de mensaje PAGO_APROBADO en historial (una sola vez)
                    long pagoAprobadoCount = mensajeRepository.countSistemaByMetadataPattern(
                            conversacionId,
                            "\"tipo\":\"PAGO_APROBADO\"",
                            "\"tipo\" : \"PAGO_APROBADO\"");

                    if (pagoAprobadoCount == 0) {
                        Map<String, Object> metadata = new HashMap<>();
                        metadata.put("tipo", "PAGO_APROBADO");
                        metadata.put("pagoId", pagoOpt.get().getId());
                        metadata.put("contratacionId", contratacion.getId());
                        metadata.put("monto", pagoOpt.get().getMonto());
                        metadata.put("duracionDias", contratacion.getDuracionDiasAcordada());
                        metadata.put("status", "approved");

                        chatService.crearMensajeDeSistema(
                                conversacionId,
                                "✅ Pago aprobado. Importante: no vuelvas a pagar por este servicio; tu pago ya fue registrado. Ya pueden comenzar con el entrenamiento.\n\nSiguiente paso: Confirmen la finalización del servicio cuando corresponda para liberar los fondos al entrenador.",
                                metadata);
                    }
                }
            }

            // Obtener mensajes y convertir a DTOs (después de reconciliación)
            List<Mensaje> mensajes = chatService.obtenerMensajes(conversacionId);
            List<MensajeDTO> mensajesDTO = mensajes.stream()
                    .map(MensajeDTO::new)
                    .toList();

            // Marcar mensajes como leídos
            chatService.marcarComoLeidos(conversacionId, usuario.getId());

            model.addAttribute("conversacion", conversacion);
            model.addAttribute("mensajes", mensajesDTO);
            model.addAttribute("otraPersona", otraPersona);
            model.addAttribute("usuario", usuario);
            model.addAttribute("esUsuario", esUsuario);
            model.addAttribute("contratacion", contratacion);

            return "chat/conversacion";
        } catch (Exception e) {
            System.err.println("Error fatal al cargar la conversación: " + e.getMessage());
            e.printStackTrace();
            // Considera redirigir a una página de error o devolver un estado de error
            return "redirect:/chat?error=Error al cargar la conversación";
        }
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
     * Iniciar chat con solicitud de personalización de plan
     * Crea una conversación Y una contratación pendiente de aprobación
     */
    @GetMapping("/iniciar-personalizacion")
    public String iniciarPersonalizacion(
            @RequestParam Integer entrenadorId,
            @RequestParam Integer planId,
            HttpSession session,
            Model model) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/login";
        }

        try {
            // Obtener el plan solicitado
            PlanEntrenador plan = planEntrenadorService.obtenerPlan(planId);
            if (plan == null || !plan.getActivo() || !plan.getEsPublico()) {
                model.addAttribute("error", "Plan no disponible");
                return "redirect:/usuario/explorar-planes";
            }

            // Crear o obtener conversación existente
            Conversacion conversacion = chatService.obtenerOCrearConversacion(usuario.getId(), entrenadorId);

            // Verificar si ya existe una solicitud pendiente para este plan
            List<ContratacionEntrenador> solicitudesExistentes = contratacionRepo.findByUsuarioIdAndEstado(
                    usuario.getId(),
                    ContratacionEntrenador.EstadoContratacion.PENDIENTE_APROBACION);

            boolean yaExisteSolicitud = solicitudesExistentes.stream()
                    .anyMatch(s -> s.getEntrenadorId().equals(entrenadorId) &&
                            s.getPlanBaseId() != null &&
                            s.getPlanBaseId().equals(planId));

            if (!yaExisteSolicitud) {
                // Crear nueva contratación en estado PENDIENTE_APROBACION
                ContratacionEntrenador contratacion = new ContratacionEntrenador();
                contratacion.setUsuarioId(usuario.getId());
                contratacion.setEntrenadorId(entrenadorId);
                contratacion.setPlanBaseId(planId);
                contratacion.setTipoContratacion(ContratacionEntrenador.TipoContratacion.PERSONALIZADO);
                contratacion.setEstado(ContratacionEntrenador.EstadoContratacion.PENDIENTE_APROBACION);

                // Copiar valores iniciales del plan base
                contratacion.setPrecioAcordado(plan.getPrecioMensual());
                contratacion.setDuracionDiasAcordada(plan.getDuracionDias());
                contratacion.setRutinasMesAcordadas(plan.getRutinasMes());
                contratacion.setSeguimientoSemanalAcordado(plan.getSeguimientoSemanal());
                contratacion.setChatDirectoAcordado(plan.getChatDirecto());
                contratacion.setVideollamadasMesAcordadas(plan.getVideollamadasMes());
                contratacion.setPlanNutricionalAcordado(plan.getPlanNutricional());

                // Configurar sistema de negociación inteligente
                contratacion.setRondasNegociacion(0);
                contratacion.setPorcentajeVariacionPermitido(new java.math.BigDecimal("30.00")); // Primera ronda: ±30%
                contratacion.setUltimaPropuestaDe(ContratacionEntrenador.PropuestaDe.USUARIO);

                contratacionRepo.save(contratacion);

                // Enviar mensaje automático al entrenador con la solicitud
                MensajeDTO mensajeDTO = new MensajeDTO();
                mensajeDTO.setConversacionId(conversacion.getId());
                mensajeDTO.setContenido(String.format(
                        "¡Hola! Me interesa tu plan \"%s\" ($%,.0f/mes). Me gustaría discutir los detalles y ver si podemos personalizar algo para mis objetivos.",
                        plan.getNombre(),
                        plan.getPrecioMensual()));

                chatService.enviarMensaje(mensajeDTO, usuario.getId());
            }

            // Redirigir a la conversación
            return "redirect:/chat/conversacion/" + conversacion.getId();

        } catch (Exception e) {
            model.addAttribute("error", "Error al procesar la solicitud: " + e.getMessage());
            return "redirect:/usuario/explorar-planes";
        }
    }

    /**
     * Enviar mensaje
     */
    @PostMapping("/enviar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> enviarMensaje(
            @RequestParam Long conversacionId,
            @RequestParam(required = false) String contenido,
            @RequestParam(required = false) MultipartFile archivo,
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
            mensajeDTO.setContenido(contenido != null ? contenido : "");

            // Si hay archivo adjunto
            if (archivo != null && !archivo.isEmpty()) {
                try {
                    Map<String, Object> fileInfo = fileUploadService.uploadFile(archivo);
                    mensajeDTO.setArchivoUrl((String) fileInfo.get("url"));
                    mensajeDTO.setArchivoNombre((String) fileInfo.get("nombre"));
                    mensajeDTO.setArchivoTipo((String) fileInfo.get("tipo"));
                    mensajeDTO.setArchivoTamano((Long) fileInfo.get("tamano"));

                    // Establecer tipo de mensaje según categoría
                    String categoria = (String) fileInfo.get("tipoCategoria");
                    if ("IMAGEN".equals(categoria)) {
                        mensajeDTO.setTipoMensaje(Mensaje.TipoMensaje.IMAGEN);
                    } else {
                        mensajeDTO.setTipoMensaje(Mensaje.TipoMensaje.ARCHIVO);
                    }

                    // Si no hay contenido, usar nombre del archivo
                    if (mensajeDTO.getContenido().isEmpty()) {
                        mensajeDTO.setContenido("📎 " + fileInfo.get("nombre"));
                    }
                } catch (Exception e) {
                    response.put("success", false);
                    response.put("message", "Error al subir archivo: " + e.getMessage());
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }
            }

            Mensaje mensaje = chatService.enviarMensaje(mensajeDTO, usuario.getId());

            // Enviar notificación en tiempo real vía WebSocket
            Map<String, Object> mensajeWs = new HashMap<>();
            mensajeWs.put("id", mensaje.getId());
            mensajeWs.put("conversacionId", mensaje.getConversacionId());
            mensajeWs.put("remitenteId", mensaje.getRemitenteId());
            mensajeWs.put("contenido", mensaje.getContenido());
            mensajeWs.put("tipoMensaje", mensaje.getTipoMensaje().toString());
            mensajeWs.put("archivoUrl", mensaje.getArchivoUrl());
            mensajeWs.put("archivoNombre", mensaje.getArchivoNombre());
            mensajeWs.put("archivoTipo", mensaje.getArchivoTipo());
            mensajeWs.put("archivoTamano", mensaje.getArchivoTamano());
            mensajeWs.put("fechaEnvio", mensaje.getFechaEnvio().toString());
            mensajeWs.put("remitenteNombre", usuario.getNombre());
            mensajeWs.put("metadata", mensaje.getMetadata());

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
