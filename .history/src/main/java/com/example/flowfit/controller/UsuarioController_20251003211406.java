package com.example.flowfit.controller;

import com.example.flowfit.model.Usuario;
import com.example.flowfit.model.EjercicioCatalogo;
import com.example.flowfit.model.Rutina;
import com.example.flowfit.model.RutinaAsignada;
import com.example.flowfit.model.RutinaEjercicio;
import com.example.flowfit.dto.EjercicioRutinaDto;
import com.example.flowfit.service.UsuarioService;
import com.example.flowfit.service.EjercicioService;
import com.example.flowfit.service.RutinaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

/**
 * Controlador para gestionar las páginas del usuario regular
 * Maneja dashboard, rutinas, ejercicios, progreso y perfil del usuario
 */
@Controller
@RequestMapping("/usuario")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private EjercicioService ejercicioService;
    
    @Autowired
    private RutinaService rutinaService;

    /**
     * Dashboard principal del usuario
     * Muestra resumen de actividad, estadísticas personales y accesos rápidos
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        try {
            // Verificar sesión de usuario
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return "redirect:/login";
            }

            // Verificar que es un usuario regular (no admin)
            if (usuario.getPerfilUsuario().toString().equals("Administrador")) {
                return "redirect:/admin/dashboard";
            }

            // Cargar datos del usuario actualizado desde BD
            usuario = usuarioService.findById(usuario.getId()).orElse(usuario);
            
            // Agregar datos al modelo
            model.addAttribute("usuario", usuario);
            
            // TODO: Agregar estadísticas de rutinas, progreso, etc.
            // Por ahora usamos datos de ejemplo que están en el HTML
            
            return "usuario/dashboard";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error al cargar el dashboard: " + e.getMessage());
            return "redirect:/login";
        }
    }

    /**
     * Página de rutinas del usuario
     * Muestra rutinas asignadas, completadas y disponibles
     */
    @GetMapping("/rutinas")
    public String rutinas(Model model, HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null || usuario.getPerfilUsuario().toString().equals("Administrador")) {
                return "redirect:/login";
            }

            model.addAttribute("usuario", usuario);
            
            // Cargar rutinas del usuario
            List<RutinaAsignada> rutinasActivas = rutinaService.obtenerRutinasActivasDelUsuario(usuario.getId());
            List<RutinaAsignada> rutinasCompletadas = rutinaService.obtenerRutinasCompletadasDelUsuario(usuario.getId());
            List<Rutina> rutinasDisponibles = rutinaService.obtenerRutinasGlobales();
            List<Rutina> rutinasPopulares = rutinaService.obtenerRutinasPopulares(6);
            
            // Estadísticas del usuario
            Double progresoGeneral = rutinaService.calcularProgresoGeneralUsuario(usuario.getId());
            
            // Procesar estadísticas de forma más simple y segura
            int totalRutinas = rutinasActivas.size() + rutinasCompletadas.size();
            int rutinasCompletadasCount = rutinasCompletadas.size();
            int rutinasActivasCount = rutinasActivas.size();
            
            model.addAttribute("rutinasActivas", rutinasActivas);
            model.addAttribute("rutinasCompletadas", rutinasCompletadas);
            model.addAttribute("rutinasDisponibles", rutinasDisponibles);
            model.addAttribute("rutinasPopulares", rutinasPopulares);
            model.addAttribute("totalRutinas", totalRutinas);
            model.addAttribute("rutinasCompletadasCount", rutinasCompletadasCount);
            model.addAttribute("rutinasActivasCount", rutinasActivasCount);
            model.addAttribute("progresoGeneral", progresoGeneral);
            
            return "usuario/rutinas";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error al cargar rutinas");
            return "usuario/dashboard";
        }
    }

    /**
     * Página de ejercicios disponibles para el usuario
     * Muestra catálogo de ejercicios con filtros y favoritos
     */
    @GetMapping("/ejercicios")
    public String ejercicios(Model model, HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null || usuario.getPerfilUsuario().toString().equals("Administrador")) {
                return "redirect:/login";
            }

            // Cargar todos los ejercicios disponibles
            List<EjercicioCatalogo> ejercicios = ejercicioService.getAllEjercicios();
            
            model.addAttribute("usuario", usuario);
            model.addAttribute("ejercicios", ejercicios);
            
            return "usuario/ejercicios";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error al cargar ejercicios");
            return "usuario/dashboard";
        }
    }

    /**
     * Página de progreso del usuario
     * Muestra estadísticas, gráficos y evolución del entrenamiento
     */
    @GetMapping("/progreso")
    public String progreso(Model model, HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null || usuario.getPerfilUsuario().toString().equals("Administrador")) {
                return "redirect:/login";
            }

            model.addAttribute("usuario", usuario);
            // TODO: Cargar datos de progreso y estadísticas
            
            return "usuario/progreso";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error al cargar progreso");
            return "usuario/dashboard";
        }
    }

    /**
     * Página de perfil del usuario
     * Permite ver y editar información personal, objetivos y preferencias
     */
    @GetMapping("/perfil")
    public String perfil(Model model, HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null || usuario.getPerfilUsuario().toString().equals("Administrador")) {
                return "redirect:/login";
            }

            // Cargar usuario actualizado
            usuario = usuarioService.findById(usuario.getId()).orElse(usuario);
            model.addAttribute("usuario", usuario);
            
            // Cargar estadísticas básicas para mostrar en el perfil
            List<RutinaAsignada> rutinasActivas = rutinaService.obtenerRutinasActivasDelUsuario(usuario.getId());
            List<RutinaAsignada> rutinasCompletadas = rutinaService.obtenerRutinasCompletadasDelUsuario(usuario.getId());
            
            model.addAttribute("rutinasActivas", rutinasActivas);
            model.addAttribute("rutinasCompletadas", rutinasCompletadas);
            
            return "usuario/perfil";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error al cargar perfil");
            return "usuario/dashboard";
        }
    }
    
    // ===== ENDPOINTS PARA ACCIONES DE RUTINAS =====
    
    /**
     * Asignar una rutina al usuario
     */
    @PostMapping("/rutinas/asignar")
    public String asignarRutina(@RequestParam Integer rutinaId, 
                               HttpSession session, 
                               RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return "redirect:/login";
            }
            
            rutinaService.asignarRutinaAUsuario(rutinaId, usuario.getId());
            redirectAttributes.addFlashAttribute("successMessage", "¡Rutina asignada correctamente!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al asignar rutina: " + e.getMessage());
        }
        
        return "redirect:/usuario/rutinas";
    }
    
    /**
     * Marcar una rutina como completada
     */
    @PostMapping("/rutinas/completar")
    public String completarRutina(@RequestParam Integer rutinaAsignadaId, 
                                 HttpSession session, 
                                 RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return "redirect:/login";
            }
            
            rutinaService.marcarRutinaComoCompletada(rutinaAsignadaId);
            redirectAttributes.addFlashAttribute("successMessage", "¡Rutina completada! ¡Felicitaciones!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al completar rutina: " + e.getMessage());
        }
        
        return "redirect:/usuario/rutinas";
    }
    
    /**
     * Actualizar progreso de una rutina
     */
    @PostMapping("/rutinas/progreso")
    public String actualizarProgreso(@RequestParam Integer rutinaAsignadaId, 
                                   @RequestParam int progreso,
                                   HttpSession session, 
                                   RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return "redirect:/login";
            }
            
            rutinaService.actualizarProgresoRutina(rutinaAsignadaId, progreso);
            redirectAttributes.addFlashAttribute("successMessage", "Progreso actualizado correctamente");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar progreso: " + e.getMessage());
        }
        
        return "redirect:/usuario/rutinas";
    }
    
    /**
     * Ver detalles de una rutina específica
     * Muestra todos los ejercicios incluidos en la rutina
     */
    @GetMapping("/rutinas/detalles")
    public String verDetallesRutina(@RequestParam Integer rutinaId,
                                   HttpSession session,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return "redirect:/login";
            }
            
            // Obtener la rutina con sus ejercicios
            Optional<Rutina> rutinaOpt = rutinaService.obtenerRutinaPorId(rutinaId);
            if (!rutinaOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Rutina no encontrada");
                return "redirect:/usuario/rutinas";
            }
            
            Rutina rutina = rutinaOpt.get();
            
            // Obtener ejercicios de la rutina usando el método DTO que evita problemas de JPA
            List<EjercicioRutinaDto> ejerciciosRutina = rutinaService.obtenerEjerciciosConDetallesDto(rutinaId);
            
            // Verificar si el usuario tiene esta rutina asignada
            List<RutinaAsignada> rutinasUsuario = rutinaService.obtenerRutinasDelUsuario(usuario.getId());
            RutinaAsignada rutinaAsignada = rutinasUsuario.stream()
                .filter(ra -> ra.getRutinaId().equals(rutinaId))
                .findFirst()
                .orElse(null);
            
            model.addAttribute("usuario", usuario);
            model.addAttribute("rutina", rutina);
            model.addAttribute("ejercicios", ejerciciosRutina);
            model.addAttribute("rutinaAsignada", rutinaAsignada);
            model.addAttribute("totalEjercicios", ejerciciosRutina.size());
            
            return "usuario/rutina-detalles";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al cargar detalles: " + e.getMessage());
            return "redirect:/usuario/rutinas";
        }
    }
    
    /**
     * API endpoint para obtener detalles de rutina en formato JSON
     * Se usa para el modal overlay dinámico
     */
    @GetMapping("/rutinas/detalles-api")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerDetallesRutinaApi(@RequestParam Integer rutinaId,
                                                                        HttpSession session) {
        try {
            System.out.println("=== API DETALLES RUTINA ===");
            System.out.println("RutinaId recibido: " + rutinaId);
            
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                System.out.println("ERROR: Usuario no encontrado en sesión");
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Usuario no autenticado");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }
            
            System.out.println("Usuario encontrado: " + usuario.getNombre() + " (ID: " + usuario.getId() + ")");
            
            // Obtener la rutina con sus ejercicios
            Optional<Rutina> rutinaOpt = rutinaService.obtenerRutinaPorId(rutinaId);
            if (!rutinaOpt.isPresent()) {
                System.out.println("ERROR: Rutina no encontrada con ID: " + rutinaId);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Rutina no encontrada");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            Rutina rutina = rutinaOpt.get();
            System.out.println("Rutina encontrada: " + rutina.getNombre());
            
            // Obtener ejercicios de la rutina
            List<EjercicioRutinaDto> ejerciciosRutina = rutinaService.obtenerEjerciciosConDetallesDto(rutinaId);
            System.out.println("Ejercicios encontrados: " + ejerciciosRutina.size());
            
            // Verificar si el usuario tiene esta rutina asignada
            List<RutinaAsignada> rutinasUsuario = rutinaService.obtenerRutinasDelUsuario(usuario.getId());
            RutinaAsignada rutinaAsignada = rutinasUsuario.stream()
                .filter(ra -> ra.getRutinaId().equals(rutinaId))
                .findFirst()
                .orElse(null);
            
            System.out.println("Rutina asignada: " + (rutinaAsignada != null ? "Sí" : "No"));
            
            // Preparar respuesta JSON
            Map<String, Object> response = new HashMap<>();
            response.put("rutina", rutina);
            response.put("ejercicios", ejerciciosRutina);
            response.put("rutinaAsignada", rutinaAsignada);
            response.put("totalEjercicios", ejerciciosRutina.size());
            response.put("tieneRutinaAsignada", rutinaAsignada != null);
            
            System.out.println("Respuesta preparada exitosamente");
            System.out.println("===========================");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("ERROR en API detalles rutina:");
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error interno del servidor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}