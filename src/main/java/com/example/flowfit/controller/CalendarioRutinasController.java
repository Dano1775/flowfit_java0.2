package com.example.flowfit.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.flowfit.model.RutinaSesionProgramada;
import com.example.flowfit.model.Usuario;
import com.example.flowfit.service.RutinaSesionProgramadaService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/usuario/api/calendario")
public class CalendarioRutinasController {

    @Autowired
    private RutinaSesionProgramadaService sesionService;

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
