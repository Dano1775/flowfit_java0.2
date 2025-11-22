package com.example.flowfit.controller;

import com.example.flowfit.model.Usuario;
import com.example.flowfit.model.Rutina;
import com.example.flowfit.model.RutinaAsignada;
import com.example.flowfit.model.EjercicioCatalogo;
import com.example.flowfit.model.RutinaEjercicio;
import com.example.flowfit.service.UsuarioService;
import com.example.flowfit.service.RutinaService;
import com.example.flowfit.service.EjercicioService;
import com.example.flowfit.service.AsignacionEntrenadorService;
import com.example.flowfit.model.AsignacionEntrenador;
import com.example.flowfit.repository.RutinaAsignadaRepository;
import com.example.flowfit.dto.EjercicioRutinaSimpleDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

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
    
    @Autowired
    private AsignacionEntrenadorService asignacionService;

    /**
     * Dashboard principal del entrenador
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        Usuario entrenador = (Usuario) session.getAttribute("usuario");
        if (entrenador == null || !Usuario.PerfilUsuario.Entrenador.equals(entrenador.getPerfilUsuario())) {
            return "redirect:/login";
        }

        model.addAttribute("currentPage", "dashboard");
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
        if (entrenador == null || !Usuario.PerfilUsuario.Entrenador.equals(entrenador.getPerfilUsuario())) {
            return "redirect:/login";
        }

        model.addAttribute("currentPage", "ver-rutinas");
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
        if (entrenador == null || !Usuario.PerfilUsuario.Entrenador.equals(entrenador.getPerfilUsuario())) {
            return "redirect:/login";
        }

        model.addAttribute("currentPage", "crear-rutinas");
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
            if (entrenador == null || !Usuario.PerfilUsuario.Entrenador.equals(entrenador.getPerfilUsuario())) {
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
        if (entrenador == null || !Usuario.PerfilUsuario.Entrenador.equals(entrenador.getPerfilUsuario())) {
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
        if (entrenador == null || !Usuario.PerfilUsuario.Entrenador.equals(entrenador.getPerfilUsuario())) {
            return "redirect:/login";
        }

        model.addAttribute("currentPage", "asignar-rutinas");
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
            if (entrenador == null || !Usuario.PerfilUsuario.Entrenador.equals(entrenador.getPerfilUsuario())) {
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
            if (entrenador == null || !Usuario.PerfilUsuario.Entrenador.equals(entrenador.getPerfilUsuario())) {
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
        if (entrenador == null || !Usuario.PerfilUsuario.Entrenador.equals(entrenador.getPerfilUsuario())) {
            return "redirect:/login";
        }

        List<EjercicioCatalogo> ejerciciosGlobales = ejercicioService.obtenerEjerciciosGlobales();
        List<EjercicioCatalogo> ejerciciosPersonales = ejercicioService.obtenerEjerciciosPorCreador(entrenador.getId());

        model.addAttribute("currentPage", "ejercicios");
        model.addAttribute("entrenador", entrenador);
        model.addAttribute("ejerciciosGlobales", ejerciciosGlobales);
        model.addAttribute("ejerciciosPersonales", ejerciciosPersonales);

        return "Entrenador/ejercicios";
    }

    /**
     * Crear nuevo ejercicio personalizado
     */
    @PostMapping("/ejercicios/crear")
    public String crearEjercicio(@RequestParam String nombre,
                                @RequestParam String descripcion,
                                @RequestParam(required = false) MultipartFile imagen,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        try {
            Usuario entrenador = (Usuario) session.getAttribute("usuario");
            if (entrenador == null || !Usuario.PerfilUsuario.Entrenador.equals(entrenador.getPerfilUsuario())) {
                return "redirect:/login";
            }

            if (nombre.trim().isEmpty() || descripcion.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "El nombre y la descripción son requeridos");
                return "redirect:/entrenador/ejercicios";
            }

            EjercicioCatalogo ejercicio = new EjercicioCatalogo();
            ejercicio.setNombre(nombre);
            ejercicio.setDescripcion(descripcion);
            ejercicio.setCreadorId(entrenador.getId());

            if (imagen != null && !imagen.isEmpty()) {
                String nombreArchivo = System.currentTimeMillis() + "_" + imagen.getOriginalFilename();
                String rutaArchivo = "ejercicio_image_uploads/" + nombreArchivo;
                Files.copy(imagen.getInputStream(), Paths.get(rutaArchivo), StandardCopyOption.REPLACE_EXISTING);
                ejercicio.setImagen(nombreArchivo);
            }

            ejercicioService.guardarEjercicio(ejercicio);

            redirectAttributes.addFlashAttribute("successMessage", "¡Ejercicio creado exitosamente!");
            return "redirect:/entrenador/ejercicios";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al crear ejercicio: " + e.getMessage());
            return "redirect:/entrenador/ejercicios";
        }
    }

    /**
     * Eliminar ejercicio personalizado
     */
    @PostMapping("/ejercicios/{id}/eliminar")
    public String eliminarEjercicio(@PathVariable Integer id,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        try {
            Usuario entrenador = (Usuario) session.getAttribute("usuario");
            if (entrenador == null || !Usuario.PerfilUsuario.Entrenador.equals(entrenador.getPerfilUsuario())) {
                return "redirect:/login";
            }

            // Verificar que el ejercicio pertenece al entrenador
            EjercicioCatalogo ejercicio = ejercicioService.obtenerEjercicioPorId(id);
            if (!ejercicio.getCreadorId().equals(entrenador.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "No tienes permisos para eliminar este ejercicio");
                return "redirect:/entrenador/ejercicios";
            }

            ejercicioService.eliminarEjercicio(id);
            
            redirectAttributes.addFlashAttribute("successMessage", "Ejercicio eliminado exitosamente");
            return "redirect:/entrenador/ejercicios";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar ejercicio: " + e.getMessage());
            return "redirect:/entrenador/ejercicios";
        }
    }

    /**
     * Historial de asignaciones
     */
    @GetMapping("/historial-asignaciones")
    public String historialAsignaciones(Model model, HttpSession session) {
        Usuario entrenador = (Usuario) session.getAttribute("usuario");
        if (entrenador == null || !Usuario.PerfilUsuario.Entrenador.equals(entrenador.getPerfilUsuario())) {
            return "redirect:/login";
        }

        List<RutinaAsignada> asignaciones = rutinaAsignadaRepository.findByRutinaEntrenadorIdOrderByFechaAsignacionDesc(entrenador.getId());

        model.addAttribute("entrenador", entrenador);
        model.addAttribute("asignaciones", asignaciones);

        return "Entrenador/historial-asignaciones";
    }
    
    // ===== FUNCIONALIDADES PARA GESTIÓN DE USUARIOS =====
    
    /**
     * Página para ver solicitudes de usuarios y gestionar mis usuarios
     */
    @GetMapping("/mis-usuarios")
    public String misUsuarios(HttpSession session, Model model) {
        Usuario entrenador = (Usuario) session.getAttribute("usuario");
        if (entrenador == null || !Usuario.PerfilUsuario.Entrenador.equals(entrenador.getPerfilUsuario())) {
            return "redirect:/login";
        }

        model.addAttribute("currentPage", "mis-usuarios");
        // Obtener solicitudes pendientes
        List<AsignacionEntrenador> solicitudesPendientes = asignacionService.getSolicitudesPendientes(entrenador.getId());
        model.addAttribute("solicitudesPendientes", solicitudesPendientes);
        
        // Obtener usuarios ya asignados
        List<AsignacionEntrenador> usuariosAsignados = asignacionService.getUsuariosAsignados(entrenador.getId());
        model.addAttribute("usuariosAsignados", usuariosAsignados);
        
        // Obtener usuarios rechazados
        List<AsignacionEntrenador> usuariosRechazados = asignacionService.getUsuariosRechazados(entrenador.getId());
        model.addAttribute("usuariosRechazados", usuariosRechazados);
        
        model.addAttribute("entrenador", entrenador);
        
        return "Entrenador/mis-usuarios";
    }
    
    /**
     * Aceptar solicitud de usuario
     */
    @PostMapping("/aceptar-solicitud")
    @ResponseBody
    public Map<String, Object> aceptarSolicitud(
            @RequestParam Long asignacionId,
            @RequestParam(defaultValue = "") String mensaje,
            HttpSession session) {
        
        Map<String, Object> response = new java.util.HashMap<>();
        
        try {
            Usuario entrenador = (Usuario) session.getAttribute("usuario");
            if (entrenador == null || !Usuario.PerfilUsuario.Entrenador.equals(entrenador.getPerfilUsuario())) {
                response.put("success", false);
                response.put("message", "Sesión no válida");
                return response;
            }
            
            boolean exito = asignacionService.aceptarSolicitud(asignacionId, mensaje);
            
            if (exito) {
                response.put("success", true);
                response.put("message", "Usuario aceptado exitosamente");
            } else {
                response.put("success", false);
                response.put("message", "No se pudo aceptar la solicitud");
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error interno: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * Rechazar solicitud de usuario
     */
    @PostMapping("/rechazar-solicitud")
    @ResponseBody
    public Map<String, Object> rechazarSolicitud(
            @RequestParam Long asignacionId,
            @RequestParam(defaultValue = "") String mensaje,
            HttpSession session) {
        
        Map<String, Object> response = new java.util.HashMap<>();
        
        try {
            Usuario entrenador = (Usuario) session.getAttribute("usuario");
            if (entrenador == null || !Usuario.PerfilUsuario.Entrenador.equals(entrenador.getPerfilUsuario())) {
                response.put("success", false);
                response.put("message", "Sesión no válida");
                return response;
            }
            
            boolean exito = asignacionService.rechazarSolicitud(asignacionId, mensaje);
            
            if (exito) {
                response.put("success", true);
                response.put("message", "Solicitud rechazada");
            } else {
                response.put("success", false);
                response.put("message", "No se pudo rechazar la solicitud");
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error interno: " + e.getMessage());
        }
        
        return response;
    }
}
