package com.example.flowfit.controller;

import com.example.flowfit.model.Usuario;
import com.example.flowfit.model.Rutina;
import com.example.flowfit.model.RutinaAsignada;
import com.example.flowfit.model.EjercicioCatalogo;
import com.example.flowfit.model.RutinaEjercicio;
import com.example.flowfit.service.UsuarioService;
import com.example.flowfit.service.RutinaService;
import com.example.flowfit.service.EjercicioService;
import com.example.flowfit.repository.RutinaAsignadaRepository;
import com.example.flowfit.dto.EjercicioRutinaSimpleDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/entrenador")
public class EntrenadorController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private RutinaService rutinaService;

    @Autowired
    private EjercicioService ejercicioService;

    @Autowired
    private RutinaAsignadaRepository rutinaAsignadaRepository;

    /**
     * Dashboard principal del entrenador
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        Usuario entrenador = (Usuario) session.getAttribute("usuario");
        if (entrenador == null || !Usuario.PerfilUsuario.Entrenador.equals(entrenador.getPerfilUsuario())) {
            return "redirect:/login";
        }

        // Estadísticas del entrenador
        List<Rutina> rutinasCreadas = rutinaService.obtenerRutinasPorEntrenador(entrenador.getId());
        List<RutinaAsignada> asignacionesActivas = rutinaAsignadaRepository.findByRutinaEntrenadorIdAndEstado(
                entrenador.getId(), RutinaAsignada.EstadoRutina.ACTIVA);
        
        long totalEjerciciosPersonalizados = ejercicioService.contarEjerciciosPorCreador(entrenador.getId());
        
        // Obtener usuarios con rutinas asignadas
        List<Usuario> usuariosRecientes = asignacionesActivas.stream()
                .map(RutinaAsignada::getUsuario)
                .distinct()
                .limit(6)
                .collect(Collectors.toList());
        
        model.addAttribute("entrenador", entrenador);
        model.addAttribute("totalRutinasCreadas", rutinasCreadas.size());
        model.addAttribute("totalAsignacionesActivas", asignacionesActivas.size());
        model.addAttribute("totalEjerciciosPersonalizados", totalEjerciciosPersonalizados);
        model.addAttribute("rutinasRecientes", rutinasCreadas.size() > 5 ? 
                           rutinasCreadas.subList(0, 5) : rutinasCreadas);
        model.addAttribute("usuariosRecientes", usuariosRecientes);
        model.addAttribute("totalUsuarios", usuariosRecientes.size());
        model.addAttribute("rutinasAsignadas", asignacionesActivas.size());
        model.addAttribute("progresoPromedio", asignacionesActivas.size() > 0 ? "85%" : "0%");

        return "Entrenador/dashboard";
    }

    /**
     * Gestión de rutinas del entrenador
     */
    @GetMapping("/rutinas")
    public String rutinas(Model model, HttpSession session) {
        Usuario entrenador = (Usuario) session.getAttribute("usuario");
        if (entrenador == null || !"Entrenador".equals(entrenador.getPerfilUsuario())) {
            return "redirect:/login";
        }

        List<Rutina> rutinas = rutinaService.obtenerRutinasPorEntrenador(entrenador.getId());
        model.addAttribute("entrenador", entrenador);
        model.addAttribute("rutinas", rutinas);

        return "Entrenador/rutinas";
    }

    /**
     * Formulario para crear nueva rutina
     */
    @GetMapping("/rutinas/crear")
    public String crearRutinaForm(Model model, HttpSession session) {
        Usuario entrenador = (Usuario) session.getAttribute("usuario");
        if (entrenador == null || !"Entrenador".equals(entrenador.getPerfilUsuario())) {
            return "redirect:/login";
        }

        List<EjercicioCatalogo> ejerciciosGlobales = ejercicioService.obtenerEjerciciosGlobales();
        List<EjercicioCatalogo> ejerciciosPersonales = ejercicioService.obtenerEjerciciosPorCreador(entrenador.getId());

        model.addAttribute("entrenador", entrenador);
        model.addAttribute("ejerciciosGlobales", ejerciciosGlobales);
        model.addAttribute("ejerciciosPersonales", ejerciciosPersonales);

        return "Entrenador/crear-rutina";
    }

    /**
     * Procesar creación de rutina
     */
    @PostMapping("/rutinas/crear")
    public String crearRutina(@RequestParam String nombre,
                              @RequestParam String descripcion,
                              @RequestParam(required = false) List<Integer> ejercicios,
                              @RequestParam Map<String, String> allParams,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        try {
            Usuario entrenador = (Usuario) session.getAttribute("usuario");
            if (entrenador == null || !"Entrenador".equals(entrenador.getPerfilUsuario())) {
                return "redirect:/login";
            }

            if (nombre.trim().isEmpty() || ejercicios == null || ejercicios.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Debes ingresar un nombre y al menos un ejercicio");
                return "redirect:/entrenador/rutinas/crear";
            }

            // Preparar lista de ejercicios con sets y repeticiones
            List<EjercicioRutinaSimpleDto> ejerciciosRutina = ejercicios.stream()
                .map(ejercicioId -> {
                    String sets = allParams.get("sets_" + ejercicioId);
                    String reps = allParams.get("reps_" + ejercicioId);
                    return new EjercicioRutinaSimpleDto(
                        ejercicioId,
                        sets != null ? Integer.parseInt(sets) : 1,
                        reps != null ? Integer.parseInt(reps) : 1
                    );
                })
                .toList();

            rutinaService.crearRutina(nombre, descripcion, entrenador.getId(), ejerciciosRutina);
            
            redirectAttributes.addFlashAttribute("successMessage", "¡Rutina creada exitosamente!");
            return "redirect:/entrenador/rutinas";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al crear rutina: " + e.getMessage());
            return "redirect:/entrenador/rutinas/crear";
        }
    }

    /**
     * Ver detalles de una rutina específica
     */
    @GetMapping("/rutinas/{id}")
    public String verRutina(@PathVariable Integer id, Model model, HttpSession session) {
        Usuario entrenador = (Usuario) session.getAttribute("usuario");
        if (entrenador == null || !"Entrenador".equals(entrenador.getPerfilUsuario())) {
            return "redirect:/login";
        }

        try {
            Rutina rutina = rutinaService.obtenerRutinaPorId(id);
            
            // Verificar que la rutina pertenece al entrenador
            if (!rutina.getEntrenadorId().equals(entrenador.getId())) {
                return "redirect:/entrenador/rutinas";
            }

            List<RutinaEjercicio> ejercicios = rutinaService.obtenerEjerciciosDeRutina(id);
            List<RutinaAsignada> asignaciones = rutinaAsignadaRepository.findByRutinaId(id);

            model.addAttribute("entrenador", entrenador);
            model.addAttribute("rutina", rutina);
            model.addAttribute("ejercicios", ejercicios);
            model.addAttribute("asignaciones", asignaciones);

            return "Entrenador/rutina-detalles";

        } catch (Exception e) {
            return "redirect:/entrenador/rutinas";
        }
    }

    /**
     * Asignar rutinas a usuarios
     */
    @GetMapping("/asignar-rutinas")
    public String asignarRutinasForm(Model model, HttpSession session) {
        Usuario entrenador = (Usuario) session.getAttribute("usuario");
        if (entrenador == null || !"Entrenador".equals(entrenador.getPerfilUsuario())) {
            return "redirect:/login";
        }

        List<Rutina> rutinas = rutinaService.obtenerRutinasPorEntrenador(entrenador.getId());
        List<Usuario> usuarios = usuarioService.obtenerUsuarios();

        model.addAttribute("entrenador", entrenador);
        model.addAttribute("rutinas", rutinas);
        model.addAttribute("usuarios", usuarios);

        return "Entrenador/asignar-rutinas";
    }

    /**
     * Procesar asignación de rutina
     */
    @PostMapping("/asignar-rutina")
    public String asignarRutina(@RequestParam Integer rutinaId,
                               @RequestParam Integer usuarioId,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        try {
            Usuario entrenador = (Usuario) session.getAttribute("usuario");
            if (entrenador == null || !"Entrenador".equals(entrenador.getPerfilUsuario())) {
                return "redirect:/login";
            }

            // Verificar que la rutina pertenece al entrenador
            Rutina rutina = rutinaService.obtenerRutinaPorId(rutinaId);
            if (!rutina.getEntrenadorId().equals(entrenador.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "No tienes permisos para asignar esta rutina");
                return "redirect:/entrenador/asignar-rutinas";
            }

            // Verificar si ya existe la asignación
            boolean yaAsignada = rutinaAsignadaRepository.existsByRutinaIdAndUsuarioId(rutinaId, usuarioId);
            if (yaAsignada) {
                redirectAttributes.addFlashAttribute("errorMessage", "Esta rutina ya está asignada al usuario");
                return "redirect:/entrenador/asignar-rutinas";
            }

            // Crear nueva asignación
            RutinaAsignada asignacion = new RutinaAsignada();
            asignacion.setRutinaId(rutinaId);
            asignacion.setUsuarioId(usuarioId);
            asignacion.setFechaAsignacion(LocalDate.now());
            asignacion.setEstado(RutinaAsignada.EstadoRutina.ACTIVA);

            rutinaAsignadaRepository.save(asignacion);

            redirectAttributes.addFlashAttribute("successMessage", "¡Rutina asignada exitosamente!");
            return "redirect:/entrenador/asignar-rutinas";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al asignar rutina: " + e.getMessage());
            return "redirect:/entrenador/asignar-rutinas";
        }
    }

    /**
     * Desasignar rutina
     */
    @PostMapping("/desasignar-rutina")
    public String desasignarRutina(@RequestParam Integer rutinaId,
                                  @RequestParam Integer usuarioId,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        try {
            Usuario entrenador = (Usuario) session.getAttribute("usuario");
            if (entrenador == null || !"Entrenador".equals(entrenador.getPerfilUsuario())) {
                return "redirect:/login";
            }

            rutinaAsignadaRepository.deleteByRutinaIdAndUsuarioId(rutinaId, usuarioId);

            redirectAttributes.addFlashAttribute("successMessage", "Rutina desasignada exitosamente");
            return "redirect:/entrenador/asignar-rutinas";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al desasignar rutina");
            return "redirect:/entrenador/asignar-rutinas";
        }
    }

    /**
     * Gestión de ejercicios personalizados
     */
    @GetMapping("/ejercicios")
    public String ejercicios(Model model, HttpSession session) {
        Usuario entrenador = (Usuario) session.getAttribute("usuario");
        if (entrenador == null || !"Entrenador".equals(entrenador.getPerfilUsuario())) {
            return "redirect:/login";
        }

        List<EjercicioCatalogo> ejerciciosGlobales = ejercicioService.obtenerEjerciciosGlobales();
        List<EjercicioCatalogo> ejerciciosPersonales = ejercicioService.obtenerEjerciciosPorCreador(entrenador.getId());

        model.addAttribute("entrenador", entrenador);
        model.addAttribute("ejerciciosGlobales", ejerciciosGlobales);
        model.addAttribute("ejerciciosPersonales", ejerciciosPersonales);

        return "Entrenador/ejercicios";
    }

    /**
     * Historial de asignaciones
     */
    @GetMapping("/historial-asignaciones")
    public String historialAsignaciones(Model model, HttpSession session) {
        Usuario entrenador = (Usuario) session.getAttribute("usuario");
        if (entrenador == null || !"Entrenador".equals(entrenador.getPerfilUsuario())) {
            return "redirect:/login";
        }

        List<RutinaAsignada> asignaciones = rutinaAsignadaRepository.findByRutinaEntrenadorIdOrderByFechaAsignacionDesc(entrenador.getId());

        model.addAttribute("entrenador", entrenador);
        model.addAttribute("asignaciones", asignaciones);

        return "Entrenador/historial-asignaciones";
    }
}
