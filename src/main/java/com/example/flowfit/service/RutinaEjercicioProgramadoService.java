package com.example.flowfit.service;

import com.example.flowfit.model.Rutina;
import com.example.flowfit.model.RutinaAsignada;
import com.example.flowfit.model.RutinaDia;
import com.example.flowfit.model.RutinaEjercicio;
import com.example.flowfit.model.RutinaEjercicioProgramado;
import com.example.flowfit.repository.RutinaAsignadaRepository;
import com.example.flowfit.repository.RutinaEjercicioDiaRepository;
import com.example.flowfit.repository.RutinaEjercicioProgramadoRepository;
import com.example.flowfit.repository.RutinaEjercicioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class RutinaEjercicioProgramadoService {

    @Autowired
    private RutinaEjercicioProgramadoRepository programadoRepo;

    @Autowired
    private RutinaAsignadaRepository rutinaAsignadaRepository;

    @Autowired
    private RutinaEjercicioRepository rutinaEjercicioRepository;

    @Autowired
    private RutinaEjercicioDiaRepository rutinaEjercicioDiaRepository;

    @Autowired
    private RutinaService rutinaService;

    @Autowired
    private RutinaSesionProgramadaService rutinaSesionProgramadaService;

    public RutinaEjercicioProgramado obtenerPorId(Integer programadoId) {
        return programadoRepo.findById(programadoId)
                .orElseThrow(() -> new RuntimeException("Programación no encontrada"));
    }

    public List<RutinaEjercicioProgramado> listarEnRango(Integer rutinaAsignadaId, LocalDate start, LocalDate end) {
        return programadoRepo.findByRutinaAsignadaIdAndFechaBetweenOrderByFechaAscOrdenAsc(rutinaAsignadaId, start,
                end);
    }

    public List<RutinaEjercicioProgramado> listarDia(Integer rutinaAsignadaId, LocalDate fecha) {
        return programadoRepo.findByRutinaAsignadaIdAndFechaOrderByOrdenAsc(rutinaAsignadaId, fecha);
    }

    public List<RutinaEjercicioProgramado> reordenarDia(Integer rutinaAsignadaId, LocalDate fecha,
            List<Integer> programadoIdsEnOrden) {
        if (programadoIdsEnOrden == null || programadoIdsEnOrden.isEmpty()) {
            return listarDia(rutinaAsignadaId, fecha);
        }

        List<RutinaEjercicioProgramado> actuales = listarDia(rutinaAsignadaId, fecha);
        Map<Integer, RutinaEjercicioProgramado> byId = new HashMap<>();
        for (RutinaEjercicioProgramado p : actuales) {
            byId.put(p.getId(), p);
        }

        // Validar que todos los IDs pertenecen a esta asignación/fecha
        Set<Integer> idsActuales = new HashSet<>(byId.keySet());
        Set<Integer> idsEnviados = new HashSet<>(programadoIdsEnOrden);
        if (!idsActuales.containsAll(idsEnviados)) {
            throw new RuntimeException("Orden inválido: hay elementos que no pertenecen a ese día");
        }

        int orden = 1;
        for (Integer id : programadoIdsEnOrden) {
            RutinaEjercicioProgramado p = byId.get(id);
            if (p != null) {
                p.setOrden(orden++);
                programadoRepo.save(p);
            }
        }

        // Los que no vinieron, se dejan al final manteniendo su orden relativo
        for (RutinaEjercicioProgramado p : actuales) {
            if (!idsEnviados.contains(p.getId())) {
                p.setOrden(orden++);
                programadoRepo.save(p);
            }
        }

        return listarDia(rutinaAsignadaId, fecha);
    }

    public RutinaEjercicioProgramado programarEjercicio(Integer rutinaAsignadaId, LocalDate fecha,
            Integer ejercicioId) {
        RutinaAsignada asignacion = rutinaAsignadaRepository.findById(rutinaAsignadaId)
                .orElseThrow(() -> new RuntimeException("Asignación no encontrada"));

        // No permitir programar sobre una sesión ya realizada/cancelada
        try {
            var sesionActual = rutinaSesionProgramadaService.obtenerSesion(rutinaAsignadaId, fecha);
            if (sesionActual.isPresent()) {
                var estado = sesionActual.get().getEstado();
                if (estado == com.example.flowfit.model.RutinaSesionProgramada.EstadoSesion.REALIZADA
                        || estado == com.example.flowfit.model.RutinaSesionProgramada.EstadoSesion.CANCELADA) {
                    throw new RuntimeException("No puedes programar ejercicios en una sesión " + estado);
                }
            }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ignored) {
        }

        // Validar que el ejercicio pertenece a la rutina asignada
        Integer rutinaId = asignacion.getRutinaId();
        RutinaEjercicio base = rutinaEjercicioRepository.findByRutinaIdAndEjercicioId(rutinaId, ejercicioId);
        if (base == null) {
            throw new RuntimeException("El ejercicio no pertenece a la rutina asignada");
        }

        Integer maxOrden = programadoRepo.findMaxOrdenForDate(rutinaAsignadaId, fecha);
        int nextOrden = (maxOrden != null ? maxOrden : 0) + 1;

        RutinaEjercicioProgramado p = new RutinaEjercicioProgramado();
        p.setRutinaAsignadaId(rutinaAsignadaId);
        p.setFecha(fecha);
        p.setEjercicioId(ejercicioId);
        p.setOrden(nextOrden);

        RutinaEjercicioProgramado saved = programadoRepo.save(p);

        return saved;
    }

    /**
     * Programa (inserta) todos los ejercicios pertenecientes a un día del ciclo en
     * una fecha.
     * Usa rutina_ejercicio_dia si existe mapeo; si no, cae a
     * rutina_ejercicio.dia_orden.
     * Evita duplicar ejercicios ya existentes en esa fecha.
     *
     * @return cantidad de ejercicios creados
     */
    public int programarDiaCiclo(Integer rutinaAsignadaId, LocalDate fecha, Integer diaOrden) {
        if (diaOrden == null || diaOrden <= 0) {
            throw new RuntimeException("Día inválido");
        }

        RutinaAsignada asignacion = rutinaAsignadaRepository.findById(rutinaAsignadaId)
                .orElseThrow(() -> new RuntimeException("Asignación no encontrada"));

        // No permitir programar sobre una sesión ya realizada/cancelada
        try {
            var sesionActual = rutinaSesionProgramadaService.obtenerSesion(rutinaAsignadaId, fecha);
            if (sesionActual.isPresent()) {
                var estado = sesionActual.get().getEstado();
                if (estado == com.example.flowfit.model.RutinaSesionProgramada.EstadoSesion.REALIZADA
                        || estado == com.example.flowfit.model.RutinaSesionProgramada.EstadoSesion.CANCELADA) {
                    throw new RuntimeException("No puedes programar ejercicios en una sesión " + estado);
                }
            }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ignored) {
        }

        Integer rutinaId = asignacion.getRutinaId();

        // Validar que el día exista y no sea descanso
        List<RutinaDia> dias = rutinaService.obtenerDiasDeRutina(rutinaId);
        RutinaDia dia = null;
        for (RutinaDia d : dias) {
            if (d != null && d.getOrden() != null && d.getOrden().intValue() == diaOrden) {
                dia = d;
                break;
            }
        }
        if (dia == null) {
            throw new RuntimeException("El día no existe en el ciclo");
        }
        if (dia.getTipo() == RutinaDia.TipoDia.DESCANSO) {
            throw new RuntimeException("No puedes programar ejercicios en un día de descanso");
        }

        // 1) Buscar ejercicios del día (nuevo modelo)
        List<Integer> ejercicioIds = rutinaEjercicioDiaRepository.findEjercicioIdsByRutinaIdAndDiaOrden(rutinaId,
                diaOrden);

        // 2) Fallback legacy: rutina_ejercicio.dia_orden
        if (ejercicioIds == null || ejercicioIds.isEmpty()) {
            List<RutinaEjercicio> legacy = rutinaEjercicioRepository.findByRutinaIdAndDiaOrdenOrderByOrdenAsc(rutinaId,
                    diaOrden);
            if (legacy != null && !legacy.isEmpty()) {
                ejercicioIds = legacy.stream().map(RutinaEjercicio::getEjercicioId).toList();
            }
        }

        if (ejercicioIds == null || ejercicioIds.isEmpty()) {
            throw new RuntimeException("No hay ejercicios definidos para este día");
        }

        // Ordenar según el orden de la rutina base
        List<RutinaEjercicio> baseOrdered = rutinaEjercicioRepository
                .findByRutinaIdAndEjercicioIdInOrderByOrdenAsc(rutinaId, ejercicioIds);

        if (baseOrdered == null || baseOrdered.isEmpty()) {
            throw new RuntimeException("No se pudieron resolver ejercicios del día");
        }

        // Evitar duplicados por fecha (mismo ejercicio ya programado)
        List<RutinaEjercicioProgramado> actuales = listarDia(rutinaAsignadaId, fecha);
        Set<Integer> yaProgramados = new HashSet<>();
        if (actuales != null) {
            for (RutinaEjercicioProgramado p : actuales) {
                if (p != null && p.getEjercicioId() != null) {
                    yaProgramados.add(p.getEjercicioId());
                }
            }
        }

        Integer maxOrden = programadoRepo.findMaxOrdenForDate(rutinaAsignadaId, fecha);
        int nextOrden = (maxOrden != null ? maxOrden : 0) + 1;

        int created = 0;
        for (RutinaEjercicio re : baseOrdered) {
            Integer ejercicioId = re != null ? re.getEjercicioId() : null;
            if (ejercicioId == null) {
                continue;
            }
            if (yaProgramados.contains(ejercicioId)) {
                continue;
            }

            RutinaEjercicioProgramado p = new RutinaEjercicioProgramado();
            p.setRutinaAsignadaId(rutinaAsignadaId);
            p.setFecha(fecha);
            p.setEjercicioId(ejercicioId);
            p.setOrden(nextOrden++);
            programadoRepo.save(p);
            created++;
        }

        if (created <= 0) {
            throw new RuntimeException("Ese día ya está programado en esta fecha");
        }

        return created;
    }

    public RutinaEjercicioProgramado moverProgramado(Integer programadoId, LocalDate nuevaFecha) {
        RutinaEjercicioProgramado p = obtenerPorId(programadoId);

        // No permitir mover a una sesión ya realizada/cancelada
        try {
            var sesionTarget = rutinaSesionProgramadaService.obtenerSesion(p.getRutinaAsignadaId(), nuevaFecha);
            if (sesionTarget.isPresent()) {
                var estado = sesionTarget.get().getEstado();
                if (estado == com.example.flowfit.model.RutinaSesionProgramada.EstadoSesion.REALIZADA
                        || estado == com.example.flowfit.model.RutinaSesionProgramada.EstadoSesion.CANCELADA) {
                    throw new RuntimeException("No puedes mover ejercicios a una sesión " + estado);
                }
            }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ignored) {
        }

        Integer maxOrden = programadoRepo.findMaxOrdenForDate(p.getRutinaAsignadaId(), nuevaFecha);
        int nextOrden = (maxOrden != null ? maxOrden : 0) + 1;

        p.setFecha(nuevaFecha);
        p.setOrden(nextOrden);
        RutinaEjercicioProgramado saved = programadoRepo.save(p);

        return saved;
    }

    public void eliminarProgramado(Integer programadoId) {
        if (programadoRepo.existsById(programadoId)) {
            programadoRepo.deleteById(programadoId);
        }
    }

    public List<RutinaEjercicio> obtenerEjerciciosProgramadosParaSesion(Integer rutinaAsignadaId, LocalDate fecha) {
        RutinaAsignada asignacion = rutinaAsignadaRepository.findById(rutinaAsignadaId)
                .orElseThrow(() -> new RuntimeException("Asignación no encontrada"));

        List<RutinaEjercicioProgramado> programados = listarDia(rutinaAsignadaId, fecha);
        if (programados == null || programados.isEmpty()) {
            return List.of();
        }

        Integer rutinaId = asignacion.getRutinaId();
        List<Integer> ids = programados.stream().map(RutinaEjercicioProgramado::getEjercicioId).toList();

        List<RutinaEjercicio> base = rutinaEjercicioRepository.findByRutinaIdAndEjercicioIdInOrderByOrdenAsc(rutinaId,
                ids);

        Map<Integer, RutinaEjercicio> byEj = new HashMap<>();
        for (RutinaEjercicio re : base) {
            byEj.put(re.getEjercicioId(), re);
        }

        List<RutinaEjercicio> ordered = new ArrayList<>();
        for (RutinaEjercicioProgramado p : programados) {
            RutinaEjercicio re = byEj.get(p.getEjercicioId());
            if (re != null) {
                ordered.add(re);
            }
        }

        return ordered;
    }
}
