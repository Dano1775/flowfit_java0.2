package com.example.flowfit.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flowfit.model.RutinaAsignada;
import com.example.flowfit.model.RutinaDia;
import com.example.flowfit.model.RutinaSesionProgramada;
import com.example.flowfit.model.RutinaSesionProgramada.EstadoSesion;
import com.example.flowfit.repository.RutinaAsignadaRepository;
import com.example.flowfit.repository.RutinaDiaRepository;
import com.example.flowfit.repository.RutinaSesionProgramadaRepository;

@Service
public class RutinaSesionProgramadaService {

    private static final int DIAS_POR_DEFECTO = 30;

    @Autowired
    private RutinaSesionProgramadaRepository sesionRepo;

    @Autowired
    private RutinaAsignadaRepository rutinaAsignadaRepo;

    @Autowired
    private RutinaDiaRepository rutinaDiaRepo;

    @Autowired
    private RutinaService rutinaService;

    @Transactional
    public void crearSesionesMesPorDefecto(Integer rutinaAsignadaId) {
        RutinaAsignada asignada = rutinaAsignadaRepo.findById(rutinaAsignadaId)
                .orElseThrow(() -> new RuntimeException("Rutina asignada no encontrada"));

        LocalDate inicio = asignada.getFechaAsignacion() != null ? asignada.getFechaAsignacion() : LocalDate.now();

        List<RutinaDia> diasPlantilla = obtenerDiasCiclo(asignada);
        RutinaDia diaPorDefecto = resolverDiaPorDefecto(diasPlantilla);

        List<RutinaSesionProgramada> existentes = sesionRepo.findByRutinaAsignada_IdOrderByFechaAsc(rutinaAsignadaId);
        Map<LocalDate, RutinaSesionProgramada> existentesPorFecha = existentes.stream()
                .filter(s -> s.getFecha() != null)
                .collect(Collectors.toMap(RutinaSesionProgramada::getFecha, s -> s, (a, b) -> a));

        for (int i = 0; i < DIAS_POR_DEFECTO; i++) {
            LocalDate fecha = inicio.plusDays(i);

            RutinaSesionProgramada sesion = existentesPorFecha.get(fecha);
            if (sesion == null) {
                sesion = new RutinaSesionProgramada();
                sesion.setRutinaAsignada(asignada);
                sesion.setFecha(fecha);
                sesion.setEstado(EstadoSesion.PROGRAMADA);
            }

            // No repetimos automáticamente un ciclo: se deja un día por defecto (editable
            // por el entrenador)
            if (sesion.getRutinaDia() == null) {
                sesion.setRutinaDia(diaPorDefecto);
            }

            sesionRepo.save(sesion);
        }
    }

    @Transactional(readOnly = true)
    public java.util.Optional<RutinaSesionProgramada> obtenerSesion(Integer rutinaAsignadaId, LocalDate fecha) {
        return sesionRepo.findByRutinaAsignada_IdAndFecha(rutinaAsignadaId, fecha);
    }

    @Transactional
    public void marcarSesionComoRealizada(Integer rutinaAsignadaId, LocalDate fecha) {
        RutinaSesionProgramada sesion = sesionRepo.findByRutinaAsignada_IdAndFecha(rutinaAsignadaId, fecha)
                .orElseGet(() -> {
                    RutinaAsignada asignada = rutinaAsignadaRepo.findById(rutinaAsignadaId)
                            .orElseThrow(() -> new RuntimeException("Rutina asignada no encontrada"));
                    RutinaDia dia = resolverDiaParaFecha(asignada, fecha);
                    RutinaSesionProgramada nueva = new RutinaSesionProgramada();
                    nueva.setRutinaAsignada(asignada);
                    nueva.setFecha(fecha);
                    nueva.setEstado(EstadoSesion.PROGRAMADA);
                    nueva.setRutinaDia(dia);
                    return nueva;
                });

        sesion.setEstado(EstadoSesion.REALIZADA);
        sesionRepo.save(sesion);

        recalcularProgresoDesdeSesiones(rutinaAsignadaId);
    }

    private List<RutinaDia> obtenerDiasCiclo(RutinaAsignada asignada) {
        Integer rutinaId = asignada.getRutinaId();
        if (rutinaId == null) {
            throw new RuntimeException("Rutina asignada inválida (rutinaId null)");
        }

        if (rutinaDiaRepo.countByRutinaId(rutinaId) == 0) {
            rutinaService.asegurarCicloBasicoSiNoExiste(rutinaId);
        }

        List<RutinaDia> dias = rutinaDiaRepo.findByRutinaIdOrderByOrdenAsc(rutinaId);
        if (dias == null || dias.isEmpty()) {
            throw new RuntimeException("La rutina no tiene días de ciclo configurados");
        }
        return dias;
    }

    private RutinaDia resolverDiaParaFecha(RutinaAsignada asignada, LocalDate fecha) {
        // Si la sesión ya existe y tiene día asignado, respetarlo.
        if (asignada.getId() != null) {
            var sesionOpt = sesionRepo.findByRutinaAsignada_IdAndFecha(asignada.getId(), fecha);
            if (sesionOpt.isPresent() && sesionOpt.get().getRutinaDia() != null) {
                return sesionOpt.get().getRutinaDia();
            }
        }

        // Fallback: usar el primer día de entrenamiento (o el primero disponible)
        List<RutinaDia> dias = obtenerDiasCiclo(asignada);
        return resolverDiaPorDefecto(dias);
    }

    private RutinaDia resolverDiaPorDefecto(List<RutinaDia> diasPlantilla) {
        if (diasPlantilla == null || diasPlantilla.isEmpty()) {
            throw new RuntimeException("La rutina no tiene días configurados");
        }
        for (RutinaDia d : diasPlantilla) {
            if (d != null && d.getTipo() == RutinaDia.TipoDia.ENTRENAMIENTO) {
                return d;
            }
        }
        return diasPlantilla.get(0);
    }

    @Transactional
    public void asignarDiaSesion(Integer rutinaAsignadaId, LocalDate fecha, Integer rutinaDiaId) {
        RutinaAsignada asignada = rutinaAsignadaRepo.findById(rutinaAsignadaId)
                .orElseThrow(() -> new RuntimeException("Rutina asignada no encontrada"));

        RutinaSesionProgramada sesion = sesionRepo.findByRutinaAsignada_IdAndFecha(rutinaAsignadaId, fecha)
                .orElseGet(() -> {
                    RutinaSesionProgramada nueva = new RutinaSesionProgramada();
                    nueva.setRutinaAsignada(asignada);
                    nueva.setFecha(fecha);
                    nueva.setEstado(EstadoSesion.PROGRAMADA);
                    return nueva;
                });

        if (sesion.getEstado() == EstadoSesion.REALIZADA) {
            throw new RuntimeException("No puedes reprogramar una sesión ya realizada");
        }
        if (sesion.getEstado() == EstadoSesion.CANCELADA) {
            throw new RuntimeException("No puedes reprogramar una sesión cancelada");
        }

        if (rutinaDiaId == null) {
            sesion.setRutinaDia(null);
            sesionRepo.save(sesion);
            recalcularProgresoDesdeSesiones(rutinaAsignadaId);
            return;
        }

        RutinaDia dia = rutinaDiaRepo.findById(rutinaDiaId)
                .orElseThrow(() -> new RuntimeException("Día de rutina no encontrado"));
        if (asignada.getRutinaId() == null || dia.getRutinaId() == null
                || !dia.getRutinaId().equals(asignada.getRutinaId())) {
            throw new RuntimeException("El día seleccionado no pertenece a esta rutina");
        }

        sesion.setRutinaDia(dia);
        sesionRepo.save(sesion);
        recalcularProgresoDesdeSesiones(rutinaAsignadaId);
    }

    @Transactional
    public void cancelarSesionesFuturasPendientes(Integer rutinaAsignadaId, LocalDate desdeExclusivo) {
        List<RutinaSesionProgramada> sesiones = sesionRepo.findByRutinaAsignada_IdOrderByFechaAsc(rutinaAsignadaId);
        for (RutinaSesionProgramada s : sesiones) {
            if (s.getFecha() != null && s.getFecha().isAfter(desdeExclusivo)
                    && s.getEstado() == EstadoSesion.PROGRAMADA) {
                s.setEstado(EstadoSesion.CANCELADA);
                sesionRepo.save(s);
            }
        }
    }

    @Transactional
    public int recalcularProgresoDesdeSesiones(Integer rutinaAsignadaId) {
        List<RutinaSesionProgramada> sesiones = sesionRepo.findByRutinaAsignada_IdOrderByFechaAsc(rutinaAsignadaId);
        long total = sesiones.stream()
                .filter(s -> s.getEstado() != EstadoSesion.CANCELADA)
                .filter(s -> s.getRutinaDia() == null || s.getRutinaDia().getTipo() != RutinaDia.TipoDia.DESCANSO)
                .count();
        long realizadas = sesiones.stream()
                .filter(s -> s.getEstado() == EstadoSesion.REALIZADA)
                .filter(s -> s.getRutinaDia() == null || s.getRutinaDia().getTipo() != RutinaDia.TipoDia.DESCANSO)
                .count();

        int progreso = 0;
        if (total > 0) {
            progreso = (int) Math.round((realizadas * 100.0) / total);
        }

        RutinaAsignada asignada = rutinaAsignadaRepo.findById(rutinaAsignadaId)
                .orElseThrow(() -> new RuntimeException("Rutina asignada no encontrada"));
        asignada.setProgreso(progreso);
        // Auto-completar la rutina cuando se alcanza 100% de sesiones realizadas
        if (progreso >= 100 && asignada.getEstado() == RutinaAsignada.EstadoRutina.ACTIVA) {
            asignada.setEstado(RutinaAsignada.EstadoRutina.COMPLETADA);
            asignada.setFechaCompletada(LocalDate.now());
        }
        rutinaAsignadaRepo.save(asignada);

        return progreso;
    }

    @Transactional(readOnly = true)
    public List<RutinaSesionProgramada> obtenerSesionesUsuarioEnRango(Integer usuarioId, LocalDate start,
            LocalDate end) {
        return sesionRepo.findEventosUsuarioEnRango(usuarioId, start, end);
    }

    @Transactional(readOnly = true)
    public List<RutinaSesionProgramada> obtenerSesionesAsignacionEnRango(Integer rutinaAsignadaId, LocalDate start,
            LocalDate end) {
        return sesionRepo.findEventosAsignacionEnRango(rutinaAsignadaId, start, end);
    }
}
