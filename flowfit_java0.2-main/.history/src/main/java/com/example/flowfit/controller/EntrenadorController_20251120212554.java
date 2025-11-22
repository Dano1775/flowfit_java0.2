package com.example.flowfit.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.flowfit.dto.EjercicioRutinaSimpleDto;
import com.example.flowfit.model.AsignacionEntrenador;
import com.example.flowfit.model.EjercicioCatalogo;
import com.example.flowfit.model.Rutina;
import com.example.flowfit.model.RutinaAsignada;
import com.example.flowfit.model.RutinaEjercicio;
import com.example.flowfit.model.Usuario;
import com.example.flowfit.repository.RutinaAsignadaRepository;
import com.example.flowfit.service.AsignacionEntrenadorService;
import com.example.flowfit.service.EjercicioService;
import com.example.flowfit.service.RutinaService;
import com.example.flowfit.service.UsuarioService;

import jakarta.servlet.http.HttpSession;

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
     * Endpoint AJAX para obtener datos del gráfico (datos globales del sistema)
     */
    @GetMapping("/dashboard-chart-data")
    @ResponseBody
    public Map<String, Object> getDashboardChartData(HttpSession session) {
        Usuario entrenador = (Usuario) session.getAttribute("usuario");
        if (entrenador == null || !Usuario.PerfilUsuario.Entrenador.equals(entrenador.getPerfilUsuario())) {
            return Map.of("error", "Unauthorized");
        }

        // Obtener datos globales del sistema (como en admin dashboard)
        List<Usuario> todosLosUsuarios = usuarioService.obtenerTodosLosUsuarios();
        
        // Contar usuarios por perfil (estados aprobados: "A")
        long totalUsuarios = todosLosUsuarios.stream()
                .filter(u -> Usuario.PerfilUsuario.Usuario.equals(u.getPerfilUsuario()) && "A".equals(u.getEstado()))
                .count();
        
        long totalEntrenadores = todosLosUsuarios.stream()
                .filter(u -> Usuario.PerfilUsuario.Entrenador.equals(u.getPerfilUsuario()) && "A".equals(u.getEstado()))
                .count();
        
        long totalNutricionistas = todosLosUsuarios.stream()
                .filter(u -> Usuario.PerfilUsuario.Nutricionista.equals(u.getPerfilUsuario()) && "A".equals(u.getEstado()))
                .count();
        
        long totalPendientes = todosLosUsuarios.stream()
                .filter(u -> "I".equals(u.getEstado()))
                .count();

        // Preparar datos para el gráfico (igual a admin dashboard)
        Map<String, Object> chartData = new HashMap<>();
        List<String> labels = Arrays.asList("Usuarios", "Entrenadores", "Nutricionistas", "Pendientes");
        List<Integer> data = Arrays.asList(
                (int) totalUsuarios,
                (int) totalEntrenadores,
                (int) totalNutricionistas,
                (int) totalPendientes
        );
        
        chartData.put("labels", labels);
        chartData.put("data", data);
        
        return chartData;
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
        // SOLO rutinas creadas por el entrenador (NO globales)
        List<Rutina> rutinas = rutinaService.obtenerRutinasPorEntrenador(entrenador.getId());
        // Filtrar para excluir rutinas globales (entrenadorId == null)
        rutinas = rutinas.stream()
                .filter(r -> r.getEntrenadorId() != null && r.getEntrenadorId().equals(entrenador.getId()))
                .collect(Collectors.toList());
        
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
                    String duracion = allParams.get("duracion_" + ejercicioId);
                    String descanso = allParams.get("descanso_" + ejercicioId);
                    String notas = allParams.get("notas_" + ejercicioId);
                    
                    return new EjercicioRutinaSimpleDto(
                        ejercicioId,
                        sets != null ? Integer.parseInt(sets) : 1,
                        reps != null ? Integer.parseInt(reps) : 1,
                        duracion != null && !duracion.isEmpty() ? Integer.parseInt(duracion) : 0,
                        descanso != null && !descanso.isEmpty() ? Integer.parseInt(descanso) : 0,
                        notas
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
     * Ver detalles de una rutina específica (vista tradicional - mantener por compatibilidad)
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
     * API REST: Obtener detalles de una rutina en formato JSON (para modal)
     */
    @GetMapping("/api/rutinas/{id}")
    @ResponseBody
    public ResponseEntity<?> obtenerRutinaJson(@PathVariable Integer id, HttpSession session) {
        Usuario entrenador = (Usuario) session.getAttribute("usuario");
        if (entrenador == null || !Usuario.PerfilUsuario.Entrenador.equals(entrenador.getPerfilUsuario())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "No autorizado"));
        }

        try {
            Rutina rutina = rutinaService.obtenerRutinaPorId(id);
            
            // Verificar que la rutina pertenece al entrenador
            if (rutina == null || !rutina.getEntrenadorId().equals(entrenador.getId())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Rutina no encontrada"));
            }

            List<RutinaEjercicio> ejercicios = rutinaService.obtenerEjerciciosDeRutina(id);
            List<RutinaAsignada> asignaciones = rutinaAsignadaRepository.findByRutinaId(id);

            // Construir respuesta JSON manual para evitar referencias circulares
            Map<String, Object> response = new HashMap<>();
            
            // Rutina (datos básicos)
            Map<String, Object> rutinaData = new HashMap<>();
            rutinaData.put("id", rutina.getId());
            rutinaData.put("nombre", rutina.getNombre());
            rutinaData.put("descripcion", rutina.getDescripcion());
            rutinaData.put("entrenadorId", rutina.getEntrenadorId());
            rutinaData.put("fechaCreacion", rutina.getFechaCreacion());
            response.put("rutina", rutinaData);
            
            // Ejercicios (con datos del catálogo)
            List<Map<String, Object>> ejerciciosData = new ArrayList<>();
            for (RutinaEjercicio ej : ejercicios) {
                Map<String, Object> ejData = new HashMap<>();
                ejData.put("rutinaId", ej.getRutinaId());
                ejData.put("ejercicioId", ej.getEjercicioId());
                ejData.put("orden", ej.getOrden());
                ejData.put("series", ej.getSeries());
                ejData.put("repeticiones", ej.getRepeticiones());
                ejData.put("duracionSegundos", ej.getDuracionSegundos());
                ejData.put("descansoSegundos", ej.getDescansoSegundos());
                ejData.put("pesoKg", ej.getPesoKg());
                ejData.put("notas", ej.getNotas());
                
                // Datos del catálogo de ejercicios
                if (ej.getEjercicioCatalogo() != null) {
                    Map<String, Object> catalogoData = new HashMap<>();
                    catalogoData.put("id", ej.getEjercicioCatalogo().getId());
                    catalogoData.put("nombre", ej.getEjercicioCatalogo().getNombre());
                    catalogoData.put("descripcion", ej.getEjercicioCatalogo().getDescripcion());
                    catalogoData.put("imagen", ej.getEjercicioCatalogo().getImagen());
                    ejData.put("ejercicioCatalogo", catalogoData);
                }
                
                ejerciciosData.add(ejData);
            }
            response.put("ejercicios", ejerciciosData);
            
            // Asignaciones (con datos de usuario)
            List<Map<String, Object>> asignacionesData = new ArrayList<>();
            for (RutinaAsignada asig : asignaciones) {
                Map<String, Object> asigData = new HashMap<>();
                asigData.put("id", asig.getId());
                asigData.put("rutinaId", asig.getRutinaId());
                asigData.put("usuarioId", asig.getUsuarioId());
                asigData.put("fechaAsignacion", asig.getFechaAsignacion());
                asigData.put("estado", asig.getEstado());
                
                // Datos del usuario
                if (asig.getUsuario() != null) {
                    Map<String, Object> usuarioData = new HashMap<>();
                    usuarioData.put("id", asig.getUsuario().getId());
                    usuarioData.put("nombre", asig.getUsuario().getNombre());
                    usuarioData.put("correo", asig.getUsuario().getCorreo());
                    asigData.put("usuario", usuarioData);
                }
                
                asignacionesData.add(asigData);
            }
            response.put("asignaciones", asignacionesData);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace(); // Para debugging
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al cargar la rutina: " + e.getMessage()));
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
        // SOLO rutinas creadas por el entrenador (NO globales)
        List<Rutina> rutinas = rutinaService.obtenerRutinasPorEntrenador(entrenador.getId());
        rutinas = rutinas.stream()
                .filter(r -> r.getEntrenadorId() != null && r.getEntrenadorId().equals(entrenador.getId()))
                .collect(Collectors.toList());
        
        List<Usuario> usuarios = usuarioService.obtenerUsuariosPorEntrenador(entrenador.getId());
        
        // Obtener asignaciones recientes
        List<RutinaAsignada> asignacionesRecientes = rutinaAsignadaRepository
            .findTop10ByOrderByFechaAsignacionDesc();

        model.addAttribute("entrenador", entrenador);
        model.addAttribute("rutinas", rutinas);
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("asignacionesRecientes", asignacionesRecientes);

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
     * Verificar si una rutina tiene asignaciones antes de eliminar
     */
    @GetMapping("/rutinas/{id}/verificar-asignaciones")
    @ResponseBody
    public Map<String, Object> verificarAsignaciones(@PathVariable Integer id, HttpSession session) {
        Usuario entrenador = (Usuario) session.getAttribute("usuario");
        if (entrenador == null || !Usuario.PerfilUsuario.Entrenador.equals(entrenador.getPerfilUsuario())) {
            return Map.of("error", "Unauthorized");
        }

        List<RutinaAsignada> asignaciones = rutinaAsignadaRepository.findByRutinaId(id);
        boolean tieneAsignaciones = !asignaciones.isEmpty();
        
        Map<String, Object> response = new HashMap<>();
        response.put("tieneAsignaciones", tieneAsignaciones);
        response.put("totalAsignaciones", asignaciones.size());
        
        if (tieneAsignaciones) {
            // Obtener rutinas disponibles para reemplazo (solo del entrenador, excluyendo la actual)
            List<Rutina> rutinasDisponibles = rutinaService.obtenerRutinasPorEntrenador(entrenador.getId())
                    .stream()
                    .filter(r -> r.getEntrenadorId() != null && 
                                 r.getEntrenadorId().equals(entrenador.getId()) &&
                                 !r.getId().equals(id))
                    .collect(Collectors.toList());
            
            response.put("rutinasDisponibles", rutinasDisponibles.stream()
                    .map(r -> Map.of(
                        "id", r.getId(),
                        "nombre", r.getNombre(),
                        "descripcion", r.getDescripcion() != null ? r.getDescripcion() : "Sin descripción",
                        "ejercicios", r.getEjercicios() != null ? r.getEjercicios().size() : 0
                    ))
                    .collect(Collectors.toList()));
        }
        
        return response;
    }
    
    /**
     * Eliminar rutina con reemplazo obligatorio si tiene asignaciones
     */
    @PostMapping("/rutinas/{id}/eliminar")
    public String eliminarRutina(@PathVariable Integer id,
                                @RequestParam(required = false) Integer rutinaReemplazoId,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        try {
            Usuario entrenador = (Usuario) session.getAttribute("usuario");
            if (entrenador == null || !Usuario.PerfilUsuario.Entrenador.equals(entrenador.getPerfilUsuario())) {
                return "redirect:/login";
            }

            // Verificar que la rutina pertenece al entrenador
            Rutina rutina = rutinaService.obtenerRutinaPorId(id);
            if (rutina == null || !entrenador.getId().equals(rutina.getEntrenadorId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "No tienes permiso para eliminar esta rutina");
                return "redirect:/entrenador/rutinas";
            }

            // Verificar asignaciones
            List<RutinaAsignada> asignaciones = rutinaAsignadaRepository.findByRutinaId(id);
            
            if (!asignaciones.isEmpty() && rutinaReemplazoId == null) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Esta rutina tiene " + asignaciones.size() + " asignaciones activas. Debes seleccionar una rutina de reemplazo.");
                return "redirect:/entrenador/rutinas";
            }
            
            if (!asignaciones.isEmpty() && rutinaReemplazoId != null) {
                // Reemplazar asignaciones
                Rutina rutinaReemplazo = rutinaService.obtenerRutinaPorId(rutinaReemplazoId);
                if (rutinaReemplazo == null || !entrenador.getId().equals(rutinaReemplazo.getEntrenadorId())) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Rutina de reemplazo no válida");
                    return "redirect:/entrenador/rutinas";
                }
                
                // Transferir asignaciones y guardar inmediatamente
                for (RutinaAsignada asignacion : asignaciones) {
                    asignacion.setRutinaId(rutinaReemplazoId);
                    asignacion.setRutina(null); // Desconectar la referencia a la rutina antigua
                    rutinaAsignadaRepository.save(asignacion);
                }
                
                // Forzar el flush para asegurar que las asignaciones se guarden antes de eliminar
                rutinaAsignadaRepository.flush();
                
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Rutina eliminada exitosamente. Las " + asignaciones.size() + 
                    " asignaciones fueron transferidas a '" + rutinaReemplazo.getNombre() + "'");
            } else {
                redirectAttributes.addFlashAttribute("successMessage", "Rutina eliminada exitosamente");
            }
            
            // Eliminar la rutina
            rutinaService.eliminarRutina(id);
            
            return "redirect:/entrenador/rutinas";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar la rutina: " + e.getMessage());
            return "redirect:/entrenador/rutinas";
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
    
    /**
     * Eliminar asignación rechazada (permite que el usuario vuelva a solicitar)
     */
    @PostMapping("/eliminar-asignacion")
    @ResponseBody
    public Map<String, Object> eliminarAsignacion(
            @RequestParam Long asignacionId,
            HttpSession session) {
        
        Map<String, Object> response = new java.util.HashMap<>();
        
        try {
            Usuario entrenador = (Usuario) session.getAttribute("usuario");
            if (entrenador == null || !Usuario.PerfilUsuario.Entrenador.equals(entrenador.getPerfilUsuario())) {
                response.put("success", false);
                response.put("message", "Sesión no válida");
                return response;
            }
            
            boolean exito = asignacionService.eliminarAsignacion(asignacionId);
            
            if (exito) {
                response.put("success", true);
                response.put("message", "Asignación eliminada. El usuario podrá solicitar de nuevo.");
            } else {
                response.put("success", false);
                response.put("message", "No se pudo eliminar la asignación");
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error interno: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * Aceptar usuario previamente rechazado
     */
    @PostMapping("/aceptar-rechazado")
    @ResponseBody
    public Map<String, Object> aceptarUsuarioRechazado(
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
            
            boolean exito = asignacionService.aceptarUsuarioRechazado(asignacionId, mensaje);
            
            if (exito) {
                response.put("success", true);
                response.put("message", "Usuario aceptado exitosamente");
            } else {
                response.put("success", false);
                response.put("message", "No se pudo aceptar al usuario");
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error interno: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * Descargar plantilla Excel para carga masiva de asignaciones
     */
    @GetMapping("/asignar-rutinas/descargar-plantilla")
    public ResponseEntity<byte[]> descargarPlantillaExcel(HttpSession session) {
        try {
            Usuario entrenador = (Usuario) session.getAttribute("usuario");
            if (entrenador == null || !Usuario.PerfilUsuario.Entrenador.equals(entrenador.getPerfilUsuario())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // Obtener rutinas y usuarios del entrenador
            List<Rutina> rutinas = rutinaService.obtenerRutinasPorEntrenador(entrenador.getId())
                    .stream()
                    .filter(r -> r.getEntrenadorId() != null && r.getEntrenadorId().equals(entrenador.getId()))
                    .collect(Collectors.toList());
            
            List<Usuario> usuarios = usuarioService.obtenerUsuariosPorEntrenador(entrenador.getId());
            
            // Crear workbook Excel
            Workbook workbook = new XSSFWorkbook();
            
            // ===== HOJA 1: Instrucciones =====
            Sheet instruccionesSheet = workbook.createSheet("Instrucciones");
            CreationHelper creationHelper = workbook.getCreationHelper();
            
            // Estilos
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 14);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            CellStyle instructionStyle = workbook.createCellStyle();
            instructionStyle.setWrapText(true);
            
            Row row = instruccionesSheet.createRow(0);
            Cell cell = row.createCell(0);
            cell.setCellValue("INSTRUCCIONES PARA CARGA MASIVA DE ASIGNACIONES");
            cell.setCellStyle(headerStyle);
            
            int rowNum = 2;
            String[] instrucciones = {
                "1. Ve a la pestaña 'Asignaciones' para llenar los datos",
                "2. En la columna 'Usuario ID', ingresa el ID del usuario (consulta la pestaña 'Usuarios Disponibles')",
                "3. En la columna 'Usuario Nombre', ingresa el nombre completo del usuario",
                "4. En la columna 'Rutina ID', ingresa el ID de la rutina (consulta la pestaña 'Rutinas Disponibles')",
                "5. En la columna 'Rutina Nombre', ingresa el nombre de la rutina",
                "6. NO modifiques las cabeceras de las columnas",
                "7. Guarda el archivo y súbelo en la plataforma",
                "",
                "IMPORTANTE:",
                "- Los IDs deben ser exactos (números enteros)",
                "- No dejes filas vacías en medio de los datos",
                "- Si hay errores, se mostrará un reporte detallado"
            };
            
            for (String instruccion : instrucciones) {
                row = instruccionesSheet.createRow(rowNum++);
                cell = row.createCell(0);
                cell.setCellValue(instruccion);
                cell.setCellStyle(instructionStyle);
            }
            
            instruccionesSheet.setColumnWidth(0, 15000);
            
            // ===== HOJA 2: Asignaciones (para llenar) =====
            Sheet asignacionesSheet = workbook.createSheet("Asignaciones");
            
            CellStyle dataHeaderStyle = workbook.createCellStyle();
            Font dataHeaderFont = workbook.createFont();
            dataHeaderFont.setBold(true);
            dataHeaderFont.setColor(IndexedColors.WHITE.getIndex());
            dataHeaderStyle.setFont(dataHeaderFont);
            dataHeaderStyle.setFillForegroundColor(IndexedColors.DARK_GREEN.getIndex());
            dataHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND());
            dataHeaderStyle.setBorderBottom(BorderStyle.THIN);
            dataHeaderStyle.setBorderTop(BorderStyle.THIN);
            dataHeaderStyle.setBorderRight(BorderStyle.THIN);
            dataHeaderStyle.setBorderLeft(BorderStyle.THIN);
            
            Row headerRow = asignacionesSheet.createRow(0);
            String[] columns = {"Usuario ID", "Usuario Nombre", "Rutina ID", "Rutina Nombre"};
            for (int i = 0; i < columns.length; i++) {
                cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(dataHeaderStyle);
                asignacionesSheet.setColumnWidth(i, 6000);
            }
            
            // Agregar filas de ejemplo
            Row ejemploRow = asignacionesSheet.createRow(1);
            ejemploRow.createCell(0).setCellValue("(ID del usuario)");
            ejemploRow.createCell(1).setCellValue("(Nombre completo)");
            ejemploRow.createCell(2).setCellValue("(ID de la rutina)");
            ejemploRow.createCell(3).setCellValue("(Nombre de la rutina)");
            
            // ===== HOJA 3: Usuarios Disponibles =====
            Sheet usuariosSheet = workbook.createSheet("Usuarios Disponibles");
            Row usuarioHeaderRow = usuariosSheet.createRow(0);
            usuarioHeaderRow.createCell(0).setCellValue("ID");
            usuarioHeaderRow.createCell(1).setCellValue("Nombre");
            usuarioHeaderRow.createCell(2).setCellValue("Email");
            usuarioHeaderRow.getCell(0).setCellStyle(dataHeaderStyle);
            usuarioHeaderRow.getCell(1).setCellStyle(dataHeaderStyle);
            usuarioHeaderRow.getCell(2).setCellStyle(dataHeaderStyle);
            
            int userRowNum = 1;
            for (Usuario usuario : usuarios) {
                Row userRow = usuariosSheet.createRow(userRowNum++);
                userRow.createCell(0).setCellValue(usuario.getId());
                userRow.createCell(1).setCellValue(usuario.getNombre());
                userRow.createCell(2).setCellValue(usuario.getCorreo());
            }
            
            usuariosSheet.setColumnWidth(0, 3000);
            usuariosSheet.setColumnWidth(1, 8000);
            usuariosSheet.setColumnWidth(2, 8000);
            
            // ===== HOJA 4: Rutinas Disponibles =====
            Sheet rutinasSheet = workbook.createSheet("Rutinas Disponibles");
            Row rutinaHeaderRow = rutinasSheet.createRow(0);
            rutinaHeaderRow.createCell(0).setCellValue("ID");
            rutinaHeaderRow.createCell(1).setCellValue("Nombre");
            rutinaHeaderRow.createCell(2).setCellValue("Descripción");
            rutinaHeaderRow.createCell(3).setCellValue("Ejercicios");
            rutinaHeaderRow.getCell(0).setCellStyle(dataHeaderStyle);
            rutinaHeaderRow.getCell(1).setCellStyle(dataHeaderStyle);
            rutinaHeaderRow.getCell(2).setCellStyle(dataHeaderStyle);
            rutinaHeaderRow.getCell(3).setCellStyle(dataHeaderStyle);
            
            int rutinaRowNum = 1;
            for (Rutina rutina : rutinas) {
                Row rutinaRow = rutinasSheet.createRow(rutinaRowNum++);
                rutinaRow.createCell(0).setCellValue(rutina.getId());
                rutinaRow.createCell(1).setCellValue(rutina.getNombre());
                rutinaRow.createCell(2).setCellValue(rutina.getDescripcion() != null ? rutina.getDescripcion() : "");
                rutinaRow.createCell(3).setCellValue(rutina.getEjercicios() != null ? rutina.getEjercicios().size() : 0);
            }
            
            rutinasSheet.setColumnWidth(0, 3000);
            rutinasSheet.setColumnWidth(1, 8000);
            rutinasSheet.setColumnWidth(2, 10000);
            rutinasSheet.setColumnWidth(3, 4000);
            
            // Convertir a bytes
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();
            
            byte[] excelBytes = outputStream.toByteArray();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", "plantilla_asignaciones_" + 
                LocalDate.now().toString() + ".xlsx");
            headers.setContentLength(excelBytes.length);
            headers.setCacheControl("no-cache, no-store, must-revalidate");
            headers.setPragma("no-cache");
            headers.setExpires(0);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelBytes);
                    
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Procesar carga masiva desde Excel
     */
    @PostMapping("/asignar-rutinas/cargar-masivo")
    public String cargarAsignacionesMasivas(@RequestParam("archivo") MultipartFile archivo,
                                           HttpSession session,
                                           RedirectAttributes redirectAttributes) {
        try {
            Usuario entrenador = (Usuario) session.getAttribute("usuario");
            if (entrenador == null || !Usuario.PerfilUsuario.Entrenador.equals(entrenador.getPerfilUsuario())) {
                return "redirect:/login";
            }

            if (archivo.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Debe seleccionar un archivo");
                return "redirect:/entrenador/asignar-rutinas";
            }

            // Validar extensión
            String fileName = archivo.getOriginalFilename();
            if (fileName == null || !fileName.endsWith(".xlsx")) {
                redirectAttributes.addFlashAttribute("errorMessage", "El archivo debe ser formato .xlsx");
                return "redirect:/entrenador/asignar-rutinas";
            }

            // Leer Excel
            Workbook workbook = new XSSFWorkbook(archivo.getInputStream());
            Sheet sheet = workbook.getSheet("Asignaciones");
            
            if (sheet == null) {
                workbook.close();
                redirectAttributes.addFlashAttribute("errorMessage", "No se encontró la hoja 'Asignaciones'");
                return "redirect:/entrenador/asignar-rutinas";
            }

            List<String> errores = new ArrayList<>();
            List<RutinaAsignada> asignacionesValidas = new ArrayList<>();
            int filasProcesadas = 0;
            int filasExitosas = 0;

            // Procesar filas (saltar header y ejemplo)
            for (int i = 2; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Cell usuarioIdCell = row.getCell(0);
                Cell rutinaIdCell = row.getCell(2);

                // Validar que no estén vacías
                if (usuarioIdCell == null || rutinaIdCell == null) {
                    continue; // Fila vacía, saltar
                }

                try {
                    int usuarioId = (int) usuarioIdCell.getNumericCellValue();
                    int rutinaId = (int) rutinaIdCell.getNumericCellValue();

                    // Validar que existan
                    Usuario usuario = usuarioService.obtenerUsuarioPorId(usuarioId);
                    Rutina rutina = rutinaService.obtenerRutinaPorId(rutinaId);

                    if (usuario == null) {
                        errores.add("Fila " + (i + 1) + ": Usuario ID " + usuarioId + " no existe");
                        continue;
                    }

                    if (rutina == null) {
                        errores.add("Fila " + (i + 1) + ": Rutina ID " + rutinaId + " no existe");
                        continue;
                    }

                    // Validar que la rutina pertenece al entrenador
                    if (!entrenador.getId().equals(rutina.getEntrenadorId())) {
                        errores.add("Fila " + (i + 1) + ": No tienes permiso para asignar la rutina '" + rutina.getNombre() + "'");
                        continue;
                    }

                    // Validar que el usuario pertenece al entrenador
                    boolean perteneceAlEntrenador = usuarioService.obtenerUsuariosPorEntrenador(entrenador.getId())
                            .stream()
                            .anyMatch(u -> u.getId().equals(usuarioId));

                    if (!perteneceAlEntrenador) {
                        errores.add("Fila " + (i + 1) + ": El usuario '" + usuario.getNombre() + "' no está asignado a ti");
                        continue;
                    }

                    // Verificar si ya existe la asignación
                    boolean yaAsignada = rutinaAsignadaRepository.existsByRutinaIdAndUsuarioId(rutinaId, usuarioId);
                    if (yaAsignada) {
                        errores.add("Fila " + (i + 1) + ": La rutina ya está asignada a " + usuario.getNombre());
                        continue;
                    }

                    // Crear asignación válida
                    RutinaAsignada asignacion = new RutinaAsignada();
                    asignacion.setRutinaId(rutinaId);
                    asignacion.setUsuarioId(usuarioId);
                    asignacion.setFechaAsignacion(LocalDate.now());
                    asignacion.setEstado(RutinaAsignada.EstadoRutina.ACTIVA);

                    asignacionesValidas.add(asignacion);
                    filasExitosas++;

                } catch (Exception e) {
                    errores.add("Fila " + (i + 1) + ": Error al procesar - " + e.getMessage());
                }

                filasProcesadas++;
            }

            workbook.close();

            // Guardar asignaciones válidas
            if (!asignacionesValidas.isEmpty()) {
                rutinaAsignadaRepository.saveAll(asignacionesValidas);
            }

            // Mensaje de resultado
            StringBuilder mensaje = new StringBuilder();
            mensaje.append("Carga masiva completada: ");
            mensaje.append(filasExitosas).append(" asignaciones creadas");
            
            if (!errores.isEmpty()) {
                mensaje.append(". ").append(errores.size()).append(" errores encontrados:");
                for (int i = 0; i < Math.min(5, errores.size()); i++) {
                    mensaje.append("<br>• ").append(errores.get(i));
                }
                if (errores.size() > 5) {
                    mensaje.append("<br>• ... y ").append(errores.size() - 5).append(" errores más");
                }
                redirectAttributes.addFlashAttribute("errorMessage", mensaje.toString());
            } else {
                redirectAttributes.addFlashAttribute("successMessage", mensaje.toString());
            }

            return "redirect:/entrenador/asignar-rutinas";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Error al procesar archivo: " + e.getMessage());
            return "redirect:/entrenador/asignar-rutinas";
        }
    }
}
