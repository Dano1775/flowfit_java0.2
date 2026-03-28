package com.example.flowfit.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.flowfit.model.ProgresoEjercicio;
import com.example.flowfit.model.RutinaAsignada;
import com.example.flowfit.model.RutinaEjercicio;
import com.example.flowfit.model.RutinaSesionProgramada;
import com.example.flowfit.model.Usuario;
import com.example.flowfit.repository.ProgresoEjercicioRepository;
import com.example.flowfit.service.ProgresoService;
import com.example.flowfit.service.RutinaEjercicioProgramadoService;
import com.example.flowfit.service.RutinaService;
import com.example.flowfit.repository.RutinaAsignadaRepository;
import com.example.flowfit.service.RutinaSesionProgramadaService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/usuario/api/calendario")
public class CalendarioRutinasController {

    @Autowired
    private RutinaSesionProgramadaService sesionService;

    @Autowired
    private RutinaEjercicioProgramadoService ejercicioProgramadoService;

    @Autowired
    private RutinaService rutinaService;

    @Autowired
    private ProgresoService progresoService;

    @Autowired
    private ProgresoEjercicioRepository progresoRepository;

    @Autowired
    private RutinaAsignadaRepository rutinaAsignadaRepository;

    /**
     * Detalle de un día del calendario: rutina info + ejercicios programados para
     * esa fecha.
     */
    @GetMapping("/dia-detalle")
    @ResponseBody
    public ResponseEntity<?> obtenerDetalleDia(
            @RequestParam Integer rutinaAsignadaId,
            @RequestParam String fecha,
            HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "No autenticado"));
            }

            LocalDate date = LocalDate.parse(fecha);

            // Get the session for this day
            RutinaSesionProgramada sesion = sesionService
                    .obtenerSesionesUsuarioEnRango(usuario.getId(), date, date)
                    .stream()
                    .filter(s -> s.getRutinaAsignada() != null
                            && s.getRutinaAsignada().getId().equals(rutinaAsignadaId))
                    .findFirst()
                    .orElse(null);

            if (sesion == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Sesión no encontrada"));
            }

            RutinaAsignada asignacion = sesion.getRutinaAsignada();

            Map<String, Object> result = new HashMap<>();
            result.put("rutinaNombre", asignacion.getRutina() != null ? asignacion.getRutina().getNombre() : "Rutina");
            result.put("rutinaDescripcion",
                    asignacion.getRutina() != null ? asignacion.getRutina().getDescripcion() : "");
            result.put("fecha", date.toString());
            result.put("estado", sesion.getEstado().toString());

            // Day info
            if (sesion.getRutinaDia() != null) {
                result.put("diaNombre", sesion.getRutinaDia().getNombre());
                result.put("diaTipo", sesion.getRutinaDia().getTipo().toString());
                result.put("esDescanso",
                        sesion.getRutinaDia().getTipo() == com.example.flowfit.model.RutinaDia.TipoDia.DESCANSO);
                result.put("diaOrden", sesion.getRutinaDia().getOrden());
            } else {
                result.put("diaNombre", "Sin programar");
                result.put("diaTipo", "SIN_PROGRAMAR");
                result.put("esDescanso", false);
                result.put("diaOrden", null);
            }

            // Get exercises for this day
            List<RutinaEjercicio> ejercicios = ejercicioProgramadoService
                    .obtenerEjerciciosProgramadosParaSesion(rutinaAsignadaId, date);

            // If no programmed exercises, try template exercises by day
            if ((ejercicios == null || ejercicios.isEmpty()) && sesion.getRutinaDia() != null) {
                ejercicios = rutinaService.obtenerEjerciciosDeRutinaPorDia(
                        asignacion.getRutinaId(), sesion.getRutinaDia().getOrden());
            }

            // Get completed exercises for this day
            List<ProgresoEjercicio> progresos = progresoRepository
                    .findByUsuarioAndAsignacionAndFecha(usuario, rutinaAsignadaId, date);
            Set<Integer> ejerciciosCompletados = progresos.stream()
                    .map(p -> p.getEjercicio().getId())
                    .collect(Collectors.toSet());

            List<Map<String, Object>> ejerciciosList = new ArrayList<>();
            if (ejercicios != null) {
                for (RutinaEjercicio re : ejercicios) {
                    Map<String, Object> ej = new HashMap<>();
                    ej.put("ejercicioId", re.getEjercicioId());
                    ej.put("orden", re.getOrden());
                    ej.put("series", re.getSeries());
                    ej.put("repeticiones", re.getRepeticiones());
                    ej.put("duracionSegundos", re.getDuracionSegundos());
                    ej.put("descansoSegundos", re.getDescansoSegundos());
                    ej.put("pesoKg", re.getPesoKg());
                    ej.put("notas", re.getNotas());
                    ej.put("completado", ejerciciosCompletados.contains(re.getEjercicioId()));
                    if (re.getEjercicioCatalogo() != null) {
                        ej.put("nombre", re.getEjercicioCatalogo().getNombre());
                        ej.put("descripcion", re.getEjercicioCatalogo().getDescripcion());
                        ej.put("imagen", re.getEjercicioCatalogo().getImagen());
                    }
                    ejerciciosList.add(ej);
                }
            }
            result.put("ejercicios", ejerciciosList);
            result.put("totalEjercicios", ejerciciosList.size());
            result.put("ejerciciosCompletados", ejerciciosCompletados.size());
            result.put("rutinaAsignadaId", rutinaAsignadaId);
            result.put("progreso", asignacion.getProgreso());
            result.put("estadoRutina", asignacion.getEstado().toString());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al cargar detalle: " + e.getMessage()));
        }
    }

    /**
     * Completar/descompletar ejercicios desde el popup del calendario.
     * Recibe: { rutinaAsignadaId, fecha, ejercicios: [{ejercicioId, completado,
     * series, repeticiones, peso}] }
     */
    @PostMapping("/completar-ejercicios")
    @ResponseBody
    public ResponseEntity<?> completarEjercicios(@RequestBody Map<String, Object> body,
            HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "No autenticado"));
            }

            Integer rutinaAsignadaId = (Integer) body.get("rutinaAsignadaId");
            String fechaStr = (String) body.get("fecha");
            LocalDate fecha = LocalDate.parse(fechaStr);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> ejercicios = (List<Map<String, Object>>) body.get("ejercicios");

            if (ejercicios == null || ejercicios.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "No se enviaron ejercicios"));
            }

            int registrados = 0;
            for (Map<String, Object> ej : ejercicios) {
                Integer ejercicioId = (Integer) ej.get("ejercicioId");
                Boolean completado = (Boolean) ej.get("completado");

                if (Boolean.TRUE.equals(completado)) {
                    Integer series = ej.get("series") != null ? ((Number) ej.get("series")).intValue() : 0;
                    Integer repeticiones = ej.get("repeticiones") != null
                            ? ((Number) ej.get("repeticiones")).intValue()
                            : 0;
                    Double peso = ej.get("peso") != null ? ((Number) ej.get("peso")).doubleValue() : null;

                    progresoService.registrarProgreso(usuario, rutinaAsignadaId, ejercicioId,
                            series, repeticiones, peso, null, fecha);
                    registrados++;
                }
            }

            if (registrados > 0) {
                sesionService.marcarSesionComoRealizada(rutinaAsignadaId, fecha);
            }

            int progresoActualizado = 0;
            String estadoRutina = "ACTIVA";
            RutinaAsignada asignacionActual = rutinaAsignadaRepository.findById(rutinaAsignadaId).orElse(null);
            if (asignacionActual != null) {
                progresoActualizado = asignacionActual.getProgreso() != null ? asignacionActual.getProgreso() : 0;
                estadoRutina = asignacionActual.getEstado().toString();
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "registrados", registrados,
                    "mensaje", "Se completaron " + registrados + " ejercicios",
                    "progreso", progresoActualizado,
                    "estadoRutina", estadoRutina));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al completar: " + e.getMessage()));
        }
    }

    @GetMapping("/sesiones")
    @ResponseBody
    public ResponseEntity<?> obtenerEventos(
            @RequestParam String start,
            @RequestParam String end,
            HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "No autenticado"));
            }

            LocalDate startDate = LocalDate.parse(start);
            LocalDate endDate = LocalDate.parse(end);

            List<RutinaSesionProgramada> sesiones = sesionService.obtenerSesionesUsuarioEnRango(usuario.getId(),
                    startDate,
                    endDate);

            List<Map<String, Object>> eventos = sesiones.stream().map(s -> {
                Map<String, Object> e = new HashMap<>();
                e.put("id", String.valueOf(s.getId()));
                String nombreRutina = (s.getRutinaAsignada() != null && s.getRutinaAsignada().getRutina() != null)
                        ? s.getRutinaAsignada().getRutina().getNombre()
                        : "Rutina";

                boolean programada = s.getRutinaDia() != null;

                String diaNombre = null;
                String diaTipo = null;
                boolean esDescanso = false;
                if (s.getRutinaDia() != null) {
                    diaNombre = s.getRutinaDia().getNombre();
                    diaTipo = s.getRutinaDia().getTipo() != null ? s.getRutinaDia().getTipo().toString() : null;
                    esDescanso = "DESCANSO".equals(diaTipo);
                }

                if (!programada) {
                    e.put("title", "Sin programar — " + nombreRutina);
                } else if (esDescanso) {
                    e.put("title", "Descanso — " + nombreRutina);
                } else if (diaNombre != null && !diaNombre.isBlank()) {
                    e.put("title", nombreRutina + " — " + diaNombre);
                } else {
                    e.put("title", nombreRutina);
                }
                e.put("start", s.getFecha().toString());
                e.put("allDay", true);

                Map<String, Object> extended = new HashMap<>();
                if (s.getRutinaAsignada() != null) {
                    extended.put("rutinaAsignadaId", s.getRutinaAsignada().getId());
                    extended.put("rutinaId", s.getRutinaAsignada().getRutinaId());
                }
                extended.put("estado", s.getEstado().toString());
                extended.put("diaNombre", diaNombre);
                extended.put("diaTipo", diaTipo);
                extended.put("esDescanso", esDescanso);
                extended.put("programada", programada);
                e.put("extendedProps", extended);

                if (!programada) {
                    e.put("backgroundColor", "#64748b");
                    e.put("borderColor", "#64748b");
                } else if (s.getEstado() == RutinaSesionProgramada.EstadoSesion.REALIZADA) {
                    e.put("backgroundColor", "#10b981");
                    e.put("borderColor", "#10b981");
                } else if (s.getEstado() == RutinaSesionProgramada.EstadoSesion.CANCELADA) {
                    e.put("backgroundColor", "#64748b");
                    e.put("borderColor", "#64748b");
                } else {
                    e.put("backgroundColor", "#3b82f6");
                    e.put("borderColor", "#3b82f6");
                }

                return e;
            }).toList();

            return ResponseEntity.ok(eventos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al cargar eventos: " + e.getMessage()));
        }
    }
}
