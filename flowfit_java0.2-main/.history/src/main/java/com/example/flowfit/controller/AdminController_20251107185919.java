package com.example.flowfit.controller;

import com.example.flowfit.model.Usuario;
import com.example.flowfit.model.RegistroAprobaciones;
import com.example.flowfit.model.HistorialEdiciones;
import com.example.flowfit.model.EjercicioCatalogo;
import com.example.flowfit.service.UsuarioService;
import com.example.flowfit.service.ExcelExportService;
import com.example.flowfit.service.EjercicioService;
import com.example.flowfit.service.EmailService;

import com.example.flowfit.repository.UsuarioRepository;
import com.example.flowfit.repository.RegistroAprobacionesRepository;
import com.example.flowfit.repository.HistorialEdicionesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.ArrayList;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import jakarta.servlet.http.HttpSession;
import java.util.List;

import java.util.Optional;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UsuarioService usuarioService;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private RegistroAprobacionesRepository registroAprobacionesRepository;
    
    @Autowired
    private HistorialEdicionesRepository historialEdicionesRepository;
    
    @Autowired
    private ExcelExportService excelExportService;
    
    @Autowired
    private EjercicioService ejercicioService;
    
    @Autowired
    private EmailService emailService;

    


    @GetMapping("/dashboard")
    public String adminDashboard(Model model, HttpSession session) {
        // Verificar sesión y permisos de administrador
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || !usuario.getPerfilUsuario().toString().equals("Administrador")) {
            // Crear sesión temporal para testing
            System.out.println("DEBUG: Usuario no autenticado en dashboard, creando sesión temporal");
            
            // Buscar un usuario administrador en la base de datos
            List<Usuario> admins = usuarioRepository.findByPerfilUsuario(Usuario.PerfilUsuario.Administrador);
            
            if (!admins.isEmpty()) {
                usuario = admins.get(0);
                session.setAttribute("usuario", usuario);
                System.out.println("DEBUG: Sesión temporal creada para admin en dashboard: " + usuario.getNombre());
            } else {
                return "redirect:/login";
            }
        }
        
        // Obtener estadísticas para el dashboard
        long usuariosPendientes = usuarioService.contarUsuariosPendientes();
        long totalUsuarios = usuarioService.contarTotalUsuarios();
        long totalEjercicios = ejercicioService.countTotalEjercicios();
        
        // Obtener actividad reciente (temporalmente deshabilitado por problema de BD)
        // List<RegistroAprobaciones> actividadReciente = registroAprobacionesRepository.findRecentApprovals();
        // if (actividadReciente.size() > 10) {
        //     actividadReciente = actividadReciente.subList(0, 10); // Limitar a los últimos 10
        // }
        List<RegistroAprobaciones> actividadReciente = new ArrayList<>(); // Lista vacía temporal
        
        // Agregar datos al modelo
        model.addAttribute("usuario", usuario);
        model.addAttribute("usuariosPendientes", usuariosPendientes);
        model.addAttribute("totalUsuarios", totalUsuarios);
        model.addAttribute("totalEjercicios", totalEjercicios);
        model.addAttribute("actividadReciente", actividadReciente);

        return "admin/dashboard";
    }
    
    @GetMapping("/usuarios-pendientes")
    public String usuariosPendientes(Model model, HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null || !usuario.getPerfilUsuario().toString().equals("Administrador")) {
                return "redirect:/login";
            }
            
            // Obtener usuarios pendientes (estado = 'I')
            List<Usuario> usuariosPendientes = usuarioService.obtenerUsuariosPendientes();
            
            model.addAttribute("usuario", usuario);
            model.addAttribute("usuariosPendientes", usuariosPendientes);
            
            // Log de depuración
            System.out.println("Usuarios pendientes encontrados: " + usuariosPendientes.size());
            
            return "admin/usuarios-pendientes-simple";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error loading pending users: " + e.getMessage());
            return "admin/dashboard";
        }
    }
    
    @GetMapping("/ejercicios")
    public String ejercicios(@RequestParam(defaultValue = "") String search,
                           @RequestParam(defaultValue = "todos") String filtro,
                           Model model, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || !usuario.getPerfilUsuario().toString().equals("Administrador")) {
            return "redirect:/login";
        }
        
        List<EjercicioCatalogo> ejercicios;
        
        if (search != null && !search.trim().isEmpty()) {
            ejercicios = ejercicioService.searchEjercicios(search);
        } else {
            switch (filtro) {
                case "globales":
                    ejercicios = ejercicioService.getGlobalExercicios();
                    break;
                case "entrenadores":
                    ejercicios = ejercicioService.getAllEjercicios().stream()
                        .filter(e -> e.getCreadoPor() != null)
                        .collect(java.util.stream.Collectors.toList());
                    break;
                default:
                    ejercicios = ejercicioService.getAllEjercicios();
                    break;
            }
        }
        
        // Log de depuración
        System.out.println("=== EJERCICIOS DEBUG ===");
        System.out.println("Búsqueda: " + search);
        System.out.println("Filtro: " + filtro);
        System.out.println("Total ejercicios encontrados: " + ejercicios.size());
        for (EjercicioCatalogo ej : ejercicios) {
            System.out.println("- " + ej.getNombre() + " (ID: " + ej.getId() + ")");
        }
        System.out.println("========================");
        
        model.addAttribute("usuario", usuario);
        model.addAttribute("ejercicios", ejercicios);
        model.addAttribute("search", search);
        model.addAttribute("filtro", filtro);
        model.addAttribute("totalEjercicios", ejercicioService.countTotalEjercicios());
        model.addAttribute("totalGlobales", ejercicioService.countGlobalEjercicios());
        model.addAttribute("totalEntrenadores", ejercicioService.countTrainerEjercicios());
        
        return "admin/ejercicios";
    }
    
    @GetMapping("/ejercicios/crear")
    public String mostrarFormularioCrearEjercicio(Model model, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || !usuario.getPerfilUsuario().toString().equals("Administrador")) {
            return "redirect:/login";
        }
        
        model.addAttribute("usuario", usuario);
        return "admin/crear-ejercicio";
    }
    
    @PostMapping("/ejercicios/crear")
    public String crearEjercicio(@RequestParam("nombre") String nombre,
                               @RequestParam("descripcion") String descripcion,
                               @RequestParam("imagen") MultipartFile imagen,
                               RedirectAttributes redirectAttributes,
                               HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null || !usuario.getPerfilUsuario().toString().equals("Administrador")) {
                return "redirect:/login";
            }
            
            if (nombre == null || nombre.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "El nombre del ejercicio es requerido");
                return "redirect:/admin/ejercicios/crear";
            }
            
            if (descripcion == null || descripcion.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "La descripción del ejercicio es requerida");
                return "redirect:/admin/ejercicios/crear";
            }
            
            if (imagen == null || imagen.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "La imagen del ejercicio es requerida");
                return "redirect:/admin/ejercicios/crear";
            }
            
            EjercicioCatalogo ejercicio = ejercicioService.createGlobalExercise(nombre.trim(), descripcion.trim(), imagen);
            
            redirectAttributes.addFlashAttribute("success", "¡Ejercicio creado exitosamente! ID: " + ejercicio.getId());
            return "redirect:/admin/ejercicios";
            
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar la imagen: " + e.getMessage());
            return "redirect:/admin/ejercicios/crear";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al crear el ejercicio: " + e.getMessage());
            return "redirect:/admin/ejercicios/crear";
        }
    }
    
    @PostMapping("/ejercicios/eliminar/{id}")
    public String eliminarEjercicio(@PathVariable("id") Long id,
                                  RedirectAttributes redirectAttributes,
                                  HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null || !usuario.getPerfilUsuario().toString().equals("Administrador")) {
                return "redirect:/login";
            }
            
            ejercicioService.deleteExercise(id);
            redirectAttributes.addFlashAttribute("success", "Ejercicio eliminado exitosamente");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el ejercicio: " + e.getMessage());
        }
        
        return "redirect:/admin/ejercicios";
    }
    
    @GetMapping("/ejercicios/editar/{id}")
    public String editarEjercicioForm(@PathVariable("id") Long id,
                                     Model model,
                                     HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null || !usuario.getPerfilUsuario().toString().equals("Administrador")) {
                return "redirect:/login";
            }
            
            Optional<EjercicioCatalogo> ejercicioOpt = ejercicioService.findById(id);
            if (ejercicioOpt.isEmpty()) {
                model.addAttribute("error", "Ejercicio no encontrado");
                return "redirect:/admin/ejercicios";
            }
            
            EjercicioCatalogo ejercicio = ejercicioOpt.get();
            
            model.addAttribute("ejercicio", ejercicio);
            return "admin/editar-ejercicio";
            
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar el ejercicio: " + e.getMessage());
            return "redirect:/admin/ejercicios";
        }
    }
    
    @PostMapping("/ejercicios/editar/{id}")
    public String actualizarEjercicio(@PathVariable("id") Long id,
                                    @RequestParam("nombre") String nombre,
                                    @RequestParam("descripcion") String descripcion,
                                    @RequestParam(value = "tipo", defaultValue = "CARDIO") String tipo,
                                    @RequestParam(value = "imagen", required = false) MultipartFile imagen,
                                    RedirectAttributes redirectAttributes,
                                    HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null || !usuario.getPerfilUsuario().toString().equals("Administrador")) {
                return "redirect:/login";
            }
            
            Optional<EjercicioCatalogo> ejercicioOpt = ejercicioService.findById(id);
            if (ejercicioOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Ejercicio no encontrado");
                return "redirect:/admin/ejercicios";
            }
            
            // Actualizar el ejercicio
            ejercicioService.updateExercise(id, nombre, descripcion, tipo, imagen);
            redirectAttributes.addFlashAttribute("success", "Ejercicio actualizado exitosamente");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar el ejercicio: " + e.getMessage());
            return "redirect:/admin/ejercicios/editar/" + id;
        }
        
        return "redirect:/admin/ejercicios";
    }
    
    @GetMapping("/usuarios")
    public String usuarios(@RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(defaultValue = "") String search,
                          @RequestParam(defaultValue = "todos") String filtro,
                          Model model, HttpSession session) {
        try {
            Usuario admin = (Usuario) session.getAttribute("usuario");
            if (admin == null || !admin.getPerfilUsuario().toString().equals("Administrador")) {
                return "redirect:/login";
            }
            
            // Obtener todos los usuarios (implementaremos paginación y filtros)
            List<Usuario> todosUsuarios = usuarioService.obtenerTodosLosUsuarios();
            
            // Aplicar filtro de búsqueda
            if (!search.trim().isEmpty()) {
                todosUsuarios = todosUsuarios.stream()
                    .filter(u -> u.getNombre().toLowerCase().contains(search.toLowerCase()) ||
                               u.getCorreo().toLowerCase().contains(search.toLowerCase()) ||
                               u.getNumeroDocumento().contains(search))
                    .collect(java.util.stream.Collectors.toList());
            }
            
            // Aplicar filtro de estado
            if (!"todos".equals(filtro)) {
                todosUsuarios = todosUsuarios.stream()
                    .filter(u -> {
                        switch(filtro) {
                            case "activos": return "A".equals(u.getEstado());
                            case "pendientes": return "I".equals(u.getEstado());
                            case "rechazados": return "R".equals(u.getEstado());
                            case "entrenadores": return "Entrenador".equals(u.getPerfilUsuario().toString());
                            case "nutricionistas": return "Nutricionista".equals(u.getPerfilUsuario().toString());
                            case "usuarios": return "Usuario".equals(u.getPerfilUsuario().toString());
                            default: return true;
                        }
                    })
                    .collect(java.util.stream.Collectors.toList());
            }
            
            // Calcular estadísticas
            long totalUsuarios = todosUsuarios.size();
            long usuariosActivos = todosUsuarios.stream().filter(u -> "A".equals(u.getEstado())).count();
            long usuariosPendientes = todosUsuarios.stream().filter(u -> "I".equals(u.getEstado())).count();
            long usuariosRechazados = todosUsuarios.stream().filter(u -> "R".equals(u.getEstado())).count();
            
            model.addAttribute("usuario", admin);
            model.addAttribute("usuarios", todosUsuarios);
            model.addAttribute("totalUsuarios", totalUsuarios);
            model.addAttribute("usuariosActivos", usuariosActivos);
            model.addAttribute("usuariosPendientes", usuariosPendientes);
            model.addAttribute("usuariosRechazados", usuariosRechazados);
            model.addAttribute("search", search);
            model.addAttribute("filtro", filtro);
            
            return "admin/gestion-usuarios";
            
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error loading users: " + e.getMessage());
            return "admin/dashboard";
        }
    }
    
    /**
     * Approve a user - same logic as PHP aprobar_usuario.php
     */
    @GetMapping("/aprobar-usuario")
    public String aprobarUsuario(@RequestParam("id") Integer usuarioId, 
                                HttpSession session, 
                                RedirectAttributes redirectAttributes) {
        System.out.println("═══════════════════════════════════════");
        System.out.println("🎯 MÉTODO APROBAR USUARIO LLAMADO");
        System.out.println("ID del usuario: " + usuarioId);
        System.out.println("═══════════════════════════════════════");
        
        // Verificar sesión de administrador
        Usuario admin = (Usuario) session.getAttribute("usuario");
        if (admin == null || !admin.getPerfilUsuario().toString().equals("Administrador")) {
            redirectAttributes.addFlashAttribute("error", "Sesión de administrador no iniciada.");
            return "redirect:/login";
        }
        
        try {
            // Obtener el usuario a aprobar
            Usuario usuarioToApprove = usuarioService.buscarPorId(usuarioId);
            if (usuarioToApprove == null) {
                redirectAttributes.addFlashAttribute("error", "Usuario no encontrado.");
                return "redirect:/admin/usuarios-pendientes";
            }
            
            System.out.println("✅ Usuario encontrado: " + usuarioToApprove.getNombre());
            System.out.println("📧 Email: " + usuarioToApprove.getCorreo());
            System.out.println("👤 Perfil: " + usuarioToApprove.getPerfilUsuario());
            
            // Prevenir aprobación de usuarios administradores (no necesitan aprobación)
            if ("Administrador".equals(usuarioToApprove.getPerfilUsuario().toString())) {
                redirectAttributes.addFlashAttribute("error", "No se pueden aprobar usuarios administradores.");
                return "redirect:/admin/usuarios-pendientes";
            }
            
            // Aprobar usuario (cambiar estado a 'A')
            usuarioToApprove.setEstado("A");
            usuarioService.guardarUsuario(usuarioToApprove);
            
            // Registrar aprobación en el historial
            RegistroAprobaciones registro = new RegistroAprobaciones();
            registro.setUsuario(usuarioToApprove);
            registro.setAdmin(admin);
            registro.setAccion(RegistroAprobaciones.Accion.Aprobado);
            registroAprobacionesRepository.save(registro);
            
            // Enviar correo de aprobación
            System.out.println("🚀 [CONTROLLER] Intentando enviar correo de aprobación...");
            System.out.println("🔍 EmailService es null? " + (emailService == null ? "SÍ ❌" : "NO ✅"));
            
            try {
                boolean correoEnviado = emailService.enviarCorreoAprobacion(
                    usuarioToApprove.getCorreo(),
                    usuarioToApprove.getNombre(),
                    usuarioToApprove.getPerfilUsuario().toString()
                );
                
                if (correoEnviado) {
                    System.out.println("✅ Correo de aprobación enviado a: " + usuarioToApprove.getCorreo());
                    redirectAttributes.addFlashAttribute("success", 
                        "Usuario aprobado exitosamente. Se ha enviado un correo de confirmación.");
                } else {
                    System.out.println("⚠️ Usuario aprobado pero el correo no pudo enviarse");
                    redirectAttributes.addFlashAttribute("success", 
                        "Usuario aprobado exitosamente. (El correo de notificación no pudo enviarse)");
                }
            } catch (Exception emailEx) {
                System.err.println("❌ Error al enviar correo de aprobación: " + emailEx.getMessage());
                emailEx.printStackTrace();
                redirectAttributes.addFlashAttribute("success", 
                    "Usuario aprobado exitosamente. (El correo de notificación no pudo enviarse)");
            }
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al aprobar usuario: " + e.getMessage());
        }
        
        return "redirect:/admin/usuarios-pendientes";
    }
    
    /**
     * Reject a user - same logic as PHP rechazar_usuario.php
     */
    @GetMapping("/rechazar-usuario")
    public String rechazarUsuario(@RequestParam("id") Integer usuarioId, 
                                 HttpSession session, 
                                 RedirectAttributes redirectAttributes) {
        // Verificar sesión de administrador
        Usuario admin = (Usuario) session.getAttribute("usuario");
        if (admin == null || !admin.getPerfilUsuario().toString().equals("Administrador")) {
            redirectAttributes.addFlashAttribute("error", "Sesión de administrador no iniciada.");
            return "redirect:/login";
        }
        
        try {
            // Obtener el usuario a rechazar
            Usuario usuarioToReject = usuarioService.buscarPorId(usuarioId);
            if (usuarioToReject == null) {
                redirectAttributes.addFlashAttribute("error", "Usuario no encontrado.");
                return "redirect:/admin/usuarios-pendientes";
            }
            
            // Prevenir rechazo de usuarios administradores
            if ("Administrador".equals(usuarioToReject.getPerfilUsuario().toString())) {
                redirectAttributes.addFlashAttribute("error", "No se pueden rechazar usuarios administradores.");
                return "redirect:/admin/usuarios-pendientes";
            }
            
            // Rechazar usuario (cambiar estado a 'R')
            usuarioToReject.setEstado("R");
            usuarioService.guardarUsuario(usuarioToReject);
            
            // Registrar rechazo en el historial
            RegistroAprobaciones registro = new RegistroAprobaciones();
            registro.setUsuario(usuarioToReject);
            registro.setAdmin(admin);
            registro.setAccion(RegistroAprobaciones.Accion.Rechazado);
            registroAprobacionesRepository.save(registro);
            
            // Enviar correo de rechazo
            System.out.println("🚀 [CONTROLLER] Intentando enviar correo de rechazo...");
            System.out.println("🔍 EmailService es null? " + (emailService == null ? "SÍ ❌" : "NO ✅"));
            
            try {
                boolean correoEnviado = emailService.enviarCorreoRechazo(
                    usuarioToReject.getCorreo(),
                    usuarioToReject.getNombre(),
                    usuarioToReject.getPerfilUsuario().toString(),
                    "Tu solicitud ha sido revisada y no ha sido aprobada en este momento."
                );
                
                if (correoEnviado) {
                    System.out.println("✅ Correo de rechazo enviado a: " + usuarioToReject.getCorreo());
                    redirectAttributes.addFlashAttribute("success", 
                        "Usuario rechazado correctamente. Se ha enviado una notificación.");
                } else {
                    System.out.println("⚠️ Usuario rechazado pero el correo no pudo enviarse");
                    redirectAttributes.addFlashAttribute("success", 
                        "Usuario rechazado correctamente. (El correo de notificación no pudo enviarse)");
                }
            } catch (Exception emailEx) {
                System.err.println("❌ Error al enviar correo de rechazo: " + emailEx.getMessage());
                emailEx.printStackTrace();
                redirectAttributes.addFlashAttribute("success", 
                    "Usuario rechazado correctamente. (El correo de notificación no pudo enviarse)");
            }
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al rechazar usuario: " + e.getMessage());
        }
        
        return "redirect:/admin/usuarios-pendientes";
    }
    
    /**
     * Deactivate an active user - change status from 'A' to 'I'
     */
    @GetMapping("/desactivar-usuario")
    public String desactivarUsuario(@RequestParam("id") Integer usuarioId, 
                                   HttpSession session, 
                                   RedirectAttributes redirectAttributes) {
        // Verify admin session
        Usuario admin = (Usuario) session.getAttribute("usuario");
        if (admin == null || !admin.getPerfilUsuario().toString().equals("Administrador")) {
            redirectAttributes.addFlashAttribute("error", "Sesión de administrador no iniciada.");
            return "redirect:/login";
        }
        
        try {
            // Get the user to deactivate
            Usuario usuarioToDeactivate = usuarioService.buscarPorId(usuarioId);
            if (usuarioToDeactivate == null) {
                redirectAttributes.addFlashAttribute("error", "Usuario no encontrado.");
                return "redirect:/admin/usuarios";
            }
            
            // Prevent deactivating admin users
            if ("Administrador".equals(usuarioToDeactivate.getPerfilUsuario().toString())) {
                redirectAttributes.addFlashAttribute("error", "No se pueden desactivar usuarios administradores por seguridad del sistema.");
                return "redirect:/admin/usuarios";
            }
            
            // Check if user is currently active
            if (!"A".equals(usuarioToDeactivate.getEstado())) {
                redirectAttributes.addFlashAttribute("error", "Solo se pueden desactivar usuarios activos.");
                return "redirect:/admin/usuarios";
            }
            
            // Deactivate user (change estado from 'A' to 'I')
            usuarioToDeactivate.setEstado("I");
            usuarioService.guardarUsuario(usuarioToDeactivate);
            
            // Create detailed edit history entry for the status change
            HistorialEdiciones historial = new HistorialEdiciones();
            historial.setUsuarioEditado(usuarioToDeactivate);
            historial.setAdminEditor(admin);
            historial.setCampoEditado("ESTADO");
            historial.setValorAnterior("Activo");
            historial.setValorNuevo("Inactivo");
            historial.setMotivoCambio("Usuario desactivado por el administrador");
            historial.setFechaEdicion(LocalDateTime.now());
            historialEdicionesRepository.save(historial);
            
            redirectAttributes.addFlashAttribute("success", 
                "Usuario " + usuarioToDeactivate.getNombre() + " desactivado exitosamente.");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al desactivar usuario: " + e.getMessage());
        }
        
        return "redirect:/admin/usuarios";
    }
    
    /**
     * Activate an inactive user - change status from 'I' to 'A'
     */
    @GetMapping("/activar-usuario")
    public String activarUsuario(@RequestParam("id") Integer usuarioId, 
                                HttpSession session, 
                                RedirectAttributes redirectAttributes) {
        // Verify admin session
        Usuario admin = (Usuario) session.getAttribute("usuario");
        if (admin == null || !admin.getPerfilUsuario().toString().equals("Administrador")) {
            redirectAttributes.addFlashAttribute("error", "Sesión de administrador no iniciada.");
            return "redirect:/login";
        }
        
        try {
            // Get the user to activate
            Usuario usuarioToActivate = usuarioService.buscarPorId(usuarioId);
            if (usuarioToActivate == null) {
                redirectAttributes.addFlashAttribute("error", "Usuario no encontrado.");
                return "redirect:/admin/usuarios";
            }
            
            // Prevent activating admin users (they shouldn't be deactivated in the first place)
            if ("Administrador".equals(usuarioToActivate.getPerfilUsuario().toString())) {
                redirectAttributes.addFlashAttribute("error", "No se pueden cambiar estados de usuarios administradores por seguridad del sistema.");
                return "redirect:/admin/usuarios";
            }
            
            // Check if user is currently inactive
            if (!"I".equals(usuarioToActivate.getEstado())) {
                redirectAttributes.addFlashAttribute("error", "Solo se pueden activar usuarios inactivos.");
                return "redirect:/admin/usuarios";
            }
            
            // Activate user (change estado from 'I' to 'A')
            usuarioToActivate.setEstado("A");
            usuarioService.guardarUsuario(usuarioToActivate);
            
            // Create detailed edit history entry for the status change
            HistorialEdiciones historial = new HistorialEdiciones();
            historial.setUsuarioEditado(usuarioToActivate);
            historial.setAdminEditor(admin);
            historial.setCampoEditado("ESTADO");
            historial.setValorAnterior("Inactivo");
            historial.setValorNuevo("Activo");
            historial.setMotivoCambio("Usuario reactivado por el administrador");
            historial.setFechaEdicion(LocalDateTime.now());
            historialEdicionesRepository.save(historial);
            
            redirectAttributes.addFlashAttribute("success", 
                "Usuario " + usuarioToActivate.getNombre() + " activado exitosamente.");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al activar usuario: " + e.getMessage());
        }
        
        return "redirect:/admin/usuarios";
    }
    
    /**
     * Permanently delete a user from the system
     */
    @GetMapping("/eliminar-usuario")
    public String eliminarUsuario(@RequestParam("id") Integer usuarioId, 
                                  HttpSession session, 
                                  RedirectAttributes redirectAttributes) {
        // Verify admin session
        Usuario admin = (Usuario) session.getAttribute("usuario");
        if (admin == null || !admin.getPerfilUsuario().toString().equals("Administrador")) {
            redirectAttributes.addFlashAttribute("error", "Sesión de administrador no iniciada.");
            return "redirect:/login";
        }
        
        try {
            // Get the user to delete
            Usuario usuarioToDelete = usuarioService.buscarPorId(usuarioId);
            if (usuarioToDelete == null) {
                redirectAttributes.addFlashAttribute("error", "Usuario no encontrado.");
                return "redirect:/admin/usuarios";
            }
            
            // Prevent deleting admin users for system security
            if ("Administrador".equals(usuarioToDelete.getPerfilUsuario().toString())) {
                redirectAttributes.addFlashAttribute("error", "No se pueden eliminar usuarios administradores por seguridad del sistema.");
                return "redirect:/admin/usuarios";
            }
            
            String nombreUsuario = usuarioToDelete.getNombre();
            
            // Delete the user permanently
            usuarioService.eliminarUsuario(usuarioId);
            
            redirectAttributes.addFlashAttribute("success", 
                "Usuario '" + nombreUsuario + "' eliminado permanentemente del sistema.");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar usuario: " + e.getMessage());
            e.printStackTrace();
        }
        
        return "redirect:/admin/usuarios";
    }
    
    /**
     * Show history of approved/rejected users
     */
    @GetMapping("/historial-aprobaciones")
    public String historialAprobaciones(Model model, HttpSession session) {
        Usuario admin = (Usuario) session.getAttribute("usuario");
        if (admin == null || !admin.getPerfilUsuario().toString().equals("Administrador")) {
            return "redirect:/login";
        }
        
        try {
            // Get all approval records ordered by date (most recent first)
            List<RegistroAprobaciones> historial = registroAprobacionesRepository.findRecentApprovals();
            
            model.addAttribute("usuario", admin);
            model.addAttribute("historial", historial);
            
            return "admin/historial-aprobaciones";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error loading approval history: " + e.getMessage());
            return "admin/usuarios-pendientes-simple";
        }
    }
    
    /**
     * Export approval history to Excel - Professional formatted report
     */
    @GetMapping("/exportar-historial-excel")
    public ResponseEntity<byte[]> exportarHistorialExcel(HttpSession session) {
        try {
            // Verify admin session
            Usuario admin = (Usuario) session.getAttribute("usuario");
            if (admin == null || !admin.getPerfilUsuario().toString().equals("Administrador")) {
                return ResponseEntity.status(403).build();
            }
            
            // Get all approval records
            List<RegistroAprobaciones> historial = registroAprobacionesRepository.findRecentApprovals();
            
            // Generate Excel file
            ByteArrayOutputStream excelStream = excelExportService.generateHistorialAprobacionesExcel(historial);
            
            // Create filename with current date
            String fileName = "FlowFit_Historial_Aprobaciones_" + 
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm")) + 
                            ".xlsx";
            
            // Set response headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", fileName);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelStream.toByteArray());
                    
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/exportar-usuarios-excel")
    public ResponseEntity<byte[]> exportarUsuariosExcel(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "todos") String filtro,
            HttpSession session) {
        try {
            Usuario admin = (Usuario) session.getAttribute("usuario");
            if (admin == null || !admin.getPerfilUsuario().toString().equals("Administrador")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            // Get users with the same logic as the view
            List<Usuario> usuarios = usuarioService.obtenerTodosLosUsuarios();
            
            // Apply search filter
            if (!search.trim().isEmpty()) {
                usuarios = usuarios.stream()
                    .filter(u -> u.getNombre().toLowerCase().contains(search.toLowerCase()) ||
                               u.getCorreo().toLowerCase().contains(search.toLowerCase()) ||
                               (u.getNumeroDocumento() != null && u.getNumeroDocumento().contains(search)))
                    .toList();
            }
            
            // Apply status/role filter
            usuarios = switch (filtro) {
                case "activos" -> usuarios.stream().filter(u -> "A".equals(u.getEstado())).toList();
                case "pendientes" -> usuarios.stream().filter(u -> "I".equals(u.getEstado())).toList();
                case "rechazados" -> usuarios.stream().filter(u -> "R".equals(u.getEstado())).toList();
                case "entrenadores" -> usuarios.stream().filter(u -> u.getPerfilUsuario().toString().equals("Entrenador")).toList();
                case "nutricionistas" -> usuarios.stream().filter(u -> u.getPerfilUsuario().toString().equals("Nutricionista")).toList();
                case "usuarios" -> usuarios.stream().filter(u -> u.getPerfilUsuario().toString().equals("Usuario")).toList();
                default -> usuarios;
            };
            
            // Generate Excel file
            ByteArrayOutputStream excelStream = excelExportService.generateUsuariosExcel(usuarios, search, filtro);
            
            // Create filename with current date and filters
            String fileName = "FlowFit_Usuarios";
            if (!filtro.equals("todos")) {
                fileName += "_" + filtro.substring(0, 1).toUpperCase() + filtro.substring(1);
            }
            fileName += "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm")) + ".xlsx";
            
            // Set response headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", fileName);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelStream.toByteArray());
                    
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/usuarios/editar/{id}")
    public String editarUsuario(@PathVariable Integer id, Model model, HttpSession session) {
        try {
            Usuario admin = (Usuario) session.getAttribute("usuario");
            if (admin == null || !admin.getPerfilUsuario().toString().equals("Administrador")) {
                return "redirect:/login";
            }
            
            Usuario usuario = usuarioService.obtenerUsuarioPorId(id);
            if (usuario == null) {
                model.addAttribute("error", "Usuario no encontrado");
                return "redirect:/admin/usuarios";
            }
            
            // Prevent editing admin users
            if ("Administrador".equals(usuario.getPerfilUsuario().toString())) {
                model.addAttribute("error", "No se pueden editar usuarios administradores por seguridad del sistema");
                return "redirect:/admin/usuarios";
            }
            
            model.addAttribute("usuario", usuario);
            model.addAttribute("admin", admin);
            return "admin/editar-usuario";
            
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error al cargar usuario: " + e.getMessage());
            return "redirect:/admin/usuarios";
        }
    }

    @PostMapping("/usuarios/editar/{id}")
    public String actualizarUsuario(@PathVariable Integer id,
                                  @RequestParam String nombre,
                                  @RequestParam String correo,
                                  @RequestParam String telefono,
                                  @RequestParam String numeroDocumento,
                                  @RequestParam String perfilUsuario,
                                  @RequestParam String estado,
                                  RedirectAttributes redirectAttributes,
                                  HttpSession session) {
        try {
            Usuario admin = (Usuario) session.getAttribute("usuario");
            if (admin == null || !admin.getPerfilUsuario().toString().equals("Administrador")) {
                return "redirect:/login";
            }
            
            Usuario usuarioOriginal = usuarioService.obtenerUsuarioPorId(id);
            if (usuarioOriginal == null) {
                redirectAttributes.addFlashAttribute("error", "Usuario no encontrado");
                return "redirect:/admin/usuarios";
            }
            
            // Prevent editing admin users
            if ("Administrador".equals(usuarioOriginal.getPerfilUsuario().toString())) {
                redirectAttributes.addFlashAttribute("error", "No se pueden editar usuarios administradores por seguridad del sistema");
                return "redirect:/admin/usuarios";
            }
            
            // Store original values for history tracking
            String originalNombre = usuarioOriginal.getNombre();
            String originalCorreo = usuarioOriginal.getCorreo();
            String originalTelefono = usuarioOriginal.getTelefono();
            String originalDocumento = usuarioOriginal.getNumeroDocumento();
            String originalPerfil = usuarioOriginal.getPerfilUsuario().toString();
            String originalEstado = usuarioOriginal.getEstado();
            
            // Update user data
            usuarioOriginal.setNombre(nombre);
            usuarioOriginal.setCorreo(correo);
            usuarioOriginal.setTelefono(telefono);
            usuarioOriginal.setNumeroDocumento(numeroDocumento);
            usuarioOriginal.setPerfilUsuario(Usuario.PerfilUsuario.valueOf(perfilUsuario));
            usuarioOriginal.setEstado(estado);
            
            // Save changes
            usuarioService.guardarUsuario(usuarioOriginal);
            
            // Create detailed edit history entries for each changed field
            boolean hayCambios = false;
            
            // Check and record each field change separately
            if (!originalNombre.equals(nombre)) {
                HistorialEdiciones historial = new HistorialEdiciones();
                historial.setUsuarioEditado(usuarioOriginal);
                historial.setAdminEditor(admin);
                historial.setCampoEditado("NOMBRE");
                historial.setValorAnterior(originalNombre);
                historial.setValorNuevo(nombre);
                historial.setFechaEdicion(LocalDateTime.now());
                historialEdicionesRepository.save(historial);
                hayCambios = true;
            }
            
            if (!originalCorreo.equals(correo)) {
                HistorialEdiciones historial = new HistorialEdiciones();
                historial.setUsuarioEditado(usuarioOriginal);
                historial.setAdminEditor(admin);
                historial.setCampoEditado("CORREO");
                historial.setValorAnterior(originalCorreo);
                historial.setValorNuevo(correo);
                historial.setFechaEdicion(LocalDateTime.now());
                historialEdicionesRepository.save(historial);
                hayCambios = true;
            }
            
            if (!originalTelefono.equals(telefono)) {
                HistorialEdiciones historial = new HistorialEdiciones();
                historial.setUsuarioEditado(usuarioOriginal);
                historial.setAdminEditor(admin);
                historial.setCampoEditado("TELEFONO");
                historial.setValorAnterior(originalTelefono);
                historial.setValorNuevo(telefono);
                historial.setFechaEdicion(LocalDateTime.now());
                historialEdicionesRepository.save(historial);
                hayCambios = true;
            }
            
            if (!originalDocumento.equals(numeroDocumento)) {
                HistorialEdiciones historial = new HistorialEdiciones();
                historial.setUsuarioEditado(usuarioOriginal);
                historial.setAdminEditor(admin);
                historial.setCampoEditado("NUMERO_DOCUMENTO");
                historial.setValorAnterior(originalDocumento);
                historial.setValorNuevo(numeroDocumento);
                historial.setFechaEdicion(LocalDateTime.now());
                historialEdicionesRepository.save(historial);
                hayCambios = true;
            }
            
            if (!originalPerfil.equals(perfilUsuario)) {
                HistorialEdiciones historial = new HistorialEdiciones();
                historial.setUsuarioEditado(usuarioOriginal);
                historial.setAdminEditor(admin);
                historial.setCampoEditado("PERFIL_USUARIO");
                historial.setValorAnterior(originalPerfil);
                historial.setValorNuevo(perfilUsuario);
                historial.setFechaEdicion(LocalDateTime.now());
                historialEdicionesRepository.save(historial);
                hayCambios = true;
            }
            
            if (!originalEstado.equals(estado)) {
                HistorialEdiciones historial = new HistorialEdiciones();
                historial.setUsuarioEditado(usuarioOriginal);
                historial.setAdminEditor(admin);
                historial.setCampoEditado("ESTADO");
                historial.setValorAnterior(getEstadoTexto(originalEstado));
                historial.setValorNuevo(getEstadoTexto(estado));
                historial.setFechaEdicion(LocalDateTime.now());
                historialEdicionesRepository.save(historial);
                hayCambios = true;
            }
            
            if (hayCambios) {
                redirectAttributes.addFlashAttribute("success", "Usuario actualizado exitosamente. Cambios registrados en el historial de ediciones.");
            } else {
                redirectAttributes.addFlashAttribute("info", "No se detectaron cambios en los datos del usuario.");
            }
            
            return "redirect:/admin/usuarios";
            
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al actualizar usuario: " + e.getMessage());
            return "redirect:/admin/usuarios/editar/" + id;
        }
    }
    
    /**
     * Show edit history for users
     */
    @GetMapping("/historial-ediciones")
    public String historialEdiciones(Model model, HttpSession session) {
        Usuario admin = (Usuario) session.getAttribute("usuario");
        if (admin == null || !admin.getPerfilUsuario().toString().equals("Administrador")) {
            return "redirect:/login";
        }
        
        try {
            // Get all edit records ordered by date (most recent first)
            List<HistorialEdiciones> historialEdiciones = historialEdicionesRepository.findRecentEdits();
            
            model.addAttribute("usuario", admin);
            model.addAttribute("historialEdiciones", historialEdiciones);
            
            return "admin/historial-ediciones";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error loading edit history: " + e.getMessage());
            return "admin/usuarios-pendientes-simple";
        }
    }

    /**
     * Show edit history for a specific user
     */
    @GetMapping("/usuarios/{id}/historial")
    public String historialUsuario(@PathVariable Integer id, Model model, HttpSession session) {
        Usuario admin = (Usuario) session.getAttribute("usuario");
        if (admin == null || !admin.getPerfilUsuario().toString().equals("Administrador")) {
            return "redirect:/login";
        }
        
        try {
            Usuario usuario = usuarioService.obtenerUsuarioPorId(id);
            if (usuario == null) {
                model.addAttribute("error", "Usuario no encontrado");
                return "redirect:/admin/usuarios";
            }
            
            List<HistorialEdiciones> historialEdiciones = historialEdicionesRepository.findByUsuarioEditadoOrderByFechaEdicionDesc(usuario);
            
            model.addAttribute("usuario", admin);
            model.addAttribute("usuarioEditado", usuario);
            model.addAttribute("historialEdiciones", historialEdiciones);
            
            return "admin/historial-usuario-ediciones";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error loading user edit history: " + e.getMessage());
            return "redirect:/admin/usuarios";
        }
    }
    
    private String getEstadoTexto(String estado) {
        return switch (estado) {
            case "A" -> "Activo";
            case "I" -> "Pendiente";
            case "R" -> "Rechazado";
            default -> "Desconocido";
        };
    }
}