package com.example.flowfit.controller;

import com.example.flowfit.model.Usuario;
import com.example.flowfit.model.EjercicioCatalogo;
import com.example.flowfit.model.Rutina;
import com.example.flowfit.model.RutinaAsignada;
import com.example.flowfit.service.UsuarioService;
import com.example.flowfit.service.EjercicioService;
import com.example.flowfit.service.RutinaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;
import java.util.List;

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
            Object[] estadisticas = rutinaService.obtenerEstadisticasUsuario(usuario.getId());
            Double progresoGeneral = rutinaService.calcularProgresoGeneralUsuario(usuario.getId());
            
            // Procesar estadísticas (total, completadas, activas)
            int totalRutinas = estadisticas != null && estadisticas.length > 0 ? 
                ((Number) estadisticas[0]).intValue() : 0;
            int rutinasCompletadasCount = estadisticas != null && estadisticas.length > 1 ? 
                ((Number) estadisticas[1]).intValue() : 0;
            int rutinasActivasCount = estadisticas != null && estadisticas.length > 2 ? 
                ((Number) estadisticas[2]).intValue() : 0;
            
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
            
            return "usuario/perfil";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error al cargar perfil");
            return "usuario/dashboard";
        }
    }
}