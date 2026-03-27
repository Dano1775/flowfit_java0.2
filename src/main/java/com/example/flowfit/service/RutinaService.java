package com.example.flowfit.service;

import com.example.flowfit.model.Rutina;
import com.example.flowfit.model.RutinaAsignada;
import com.example.flowfit.model.RutinaDia;
import com.example.flowfit.model.RutinaEjercicio;
import com.example.flowfit.model.RutinaEjercicioDia;
import com.example.flowfit.model.RutinaAsignada.EstadoRutina;
import com.example.flowfit.dto.EjercicioRutinaMultiDiaDto;
import com.example.flowfit.dto.EjercicioRutinaDto;
import com.example.flowfit.repository.RutinaRepository;
import com.example.flowfit.repository.RutinaAsignadaRepository;
import com.example.flowfit.repository.RutinaDiaRepository;
import com.example.flowfit.repository.RutinaEjercicioRepository;
import com.example.flowfit.repository.RutinaEjercicioDiaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

@Service
@Transactional
public class RutinaService {

    @Autowired
    private RutinaRepository rutinaRepository;

    @Autowired
    private RutinaAsignadaRepository rutinaAsignadaRepository;

    @Autowired
    private RutinaEjercicioRepository rutinaEjercicioRepository;

    @Autowired
    private RutinaDiaRepository rutinaDiaRepository;

    @Autowired
    private RutinaEjercicioDiaRepository rutinaEjercicioDiaRepository;

    // ===== GESTIÓN DE RUTINAS =====

    public List<Rutina> obtenerRutinasGlobales() {
        return rutinaRepository.findRutinasGlobalesOrdenadas();
    }

    public List<Rutina> obtenerRutinasPopulares(int limite) {
        return rutinaRepository.findRutinasPopulares(limite);
    }

    public List<Rutina> obtenerRutinasPorEntrenador(Integer entrenadorId) {
        List<Rutina> rutinas = rutinaRepository.findByEntrenadorId(entrenadorId);
        // Cargar ejercicios para cada rutina
        for (Rutina rutina : rutinas) {
            List<RutinaEjercicio> ejercicios = rutinaEjercicioRepository.findByRutinaIdOrderByOrdenAsc(rutina.getId());
            rutina.setEjercicios(ejercicios);
        }
        return rutinas;
    }

    public Rutina obtenerRutinaPorId(Integer id) {
        Optional<Rutina> rutinaOpt = rutinaRepository.findById(id);
        if (rutinaOpt.isPresent()) {
            Rutina rutina = rutinaOpt.get();
            // Cargar ejercicios
            List<RutinaEjercicio> ejercicios = rutinaEjercicioRepository.findByRutinaIdOrderByOrdenAsc(rutina.getId());
            rutina.setEjercicios(ejercicios);
            return rutina;
        }
        return null;
    }

    public void crearRutina(String nombre, String descripcion, Integer entrenadorId,
            List<com.example.flowfit.dto.EjercicioRutinaSimpleDto> ejercicios) {
        // Crear la rutina
        Rutina rutina = new Rutina();
        rutina.setNombre(nombre);
        rutina.setDescripcion(descripcion);
        rutina.setEntrenadorId(entrenadorId);
        rutina.setFechaCreacion(LocalDate.now());

        Rutina rutinaSalvada = rutinaRepository.save(rutina);

        // Si vienen ejercicios con diaOrden y no existe ciclo aún, crear uno básico
        // mínimo
        // (por compatibilidad: algunas rutinas pueden ser legacy sin ciclo explícito)
        if (rutinaDiaRepository.countByRutinaId(rutinaSalvada.getId()) == 0) {
            // Si no se envió diaOrden, dejamos sin ciclo. Si se envió, creamos ciclo con
            // max(diaOrden).
            int maxDia = ejercicios.stream()
                    .map(e -> e.getDiaOrden() == null ? 0 : e.getDiaOrden())
                    .max(Integer::compareTo)
                    .orElse(0);
            if (maxDia > 0) {
                for (int d = 1; d <= maxDia; d++) {
                    RutinaDia dia = new RutinaDia();
                    dia.setRutinaId(rutinaSalvada.getId());
                    dia.setOrden(d);
                    dia.setNombre("Día " + d);
                    dia.setTipo(RutinaDia.TipoDia.ENTRENAMIENTO);
                    rutinaDiaRepository.save(dia);
                }
            }
        }

        // Agregar ejercicios a la rutina con orden
        int orden = 1;
        for (com.example.flowfit.dto.EjercicioRutinaSimpleDto ejercicioDto : ejercicios) {
            RutinaEjercicio rutinaEjercicio = new RutinaEjercicio();
            rutinaEjercicio.setRutinaId(rutinaSalvada.getId());
            rutinaEjercicio.setEjercicioId(ejercicioDto.getEjercicioId());
            rutinaEjercicio.setOrden(orden++);
            rutinaEjercicio.setDiaOrden(ejercicioDto.getDiaOrden());
            rutinaEjercicio.setSeries(ejercicioDto.getSets());
            rutinaEjercicio.setRepeticiones(ejercicioDto.getRepeticiones());
            rutinaEjercicio.setDuracionSegundos(ejercicioDto.getDuracionSegundos());
            rutinaEjercicio.setDescansoSegundos(ejercicioDto.getDescansoSegundos());
            rutinaEjercicio.setNotas(ejercicioDto.getNotas());

            rutinaEjercicioRepository.save(rutinaEjercicio);
        }
    }

    public void crearRutinaConCiclo(String nombre,
            String descripcion,
            Integer entrenadorId,
            List<RutinaDia> diasCiclo,
            List<EjercicioRutinaMultiDiaDto> ejercicios) {
        if (diasCiclo == null || diasCiclo.isEmpty()) {
            throw new RuntimeException("Debes definir al menos 1 día en el ciclo");
        }

        // Crear rutina
        Rutina rutina = new Rutina();
        rutina.setNombre(nombre);
        rutina.setDescripcion(descripcion);
        rutina.setEntrenadorId(entrenadorId);
        rutina.setFechaCreacion(LocalDate.now());

        Rutina rutinaSalvada = rutinaRepository.save(rutina);

        // Guardar días
        for (RutinaDia dia : diasCiclo) {
            RutinaDia nuevo = new RutinaDia();
            nuevo.setRutinaId(rutinaSalvada.getId());
            nuevo.setOrden(dia.getOrden());
            nuevo.setNombre(dia.getNombre() != null && !dia.getNombre().isBlank() ? dia.getNombre()
                    : ("Día " + dia.getOrden()));
            nuevo.setTipo(dia.getTipo() != null ? dia.getTipo() : RutinaDia.TipoDia.ENTRENAMIENTO);
            rutinaDiaRepository.save(nuevo);
        }

        // Guardar ejercicios (en rutina_ejercicio) + mapeo rutina_ejercicio_dia
        int orden = 1;
        for (EjercicioRutinaMultiDiaDto ejercicioDto : ejercicios) {
            RutinaEjercicio rutinaEjercicio = new RutinaEjercicio();
            rutinaEjercicio.setRutinaId(rutinaSalvada.getId());
            rutinaEjercicio.setEjercicioId(ejercicioDto.getEjercicioId());
            rutinaEjercicio.setOrden(orden++);
            Integer primerDia = (ejercicioDto.getDiasOrdenes() != null && !ejercicioDto.getDiasOrdenes().isEmpty())
                    ? ejercicioDto.getDiasOrdenes().get(0)
                    : null;
            rutinaEjercicio.setDiaOrden(primerDia);
            rutinaEjercicio.setSeries(ejercicioDto.getSets());
            rutinaEjercicio.setRepeticiones(ejercicioDto.getRepeticiones());
            rutinaEjercicio.setDuracionSegundos(ejercicioDto.getDuracionSegundos());
            rutinaEjercicio.setDescansoSegundos(ejercicioDto.getDescansoSegundos());
            rutinaEjercicio.setNotas(ejercicioDto.getNotas());
            rutinaEjercicioRepository.save(rutinaEjercicio);

            if (ejercicioDto.getDiasOrdenes() != null) {
                for (Integer diaOrden : ejercicioDto.getDiasOrdenes()) {
                    if (diaOrden == null) {
                        continue;
                    }
                    rutinaEjercicioDiaRepository.save(
                            new RutinaEjercicioDia(rutinaSalvada.getId(), ejercicioDto.getEjercicioId(), diaOrden));
                }
            }
        }
    }

    public List<Rutina> buscarRutinasPorNombre(String nombre) {
        return rutinaRepository.findByNombreContainingIgnoreCase(nombre);
    }

    // ===== ASIGNACIÓN DE RUTINAS =====

    public RutinaAsignada asignarRutinaAUsuario(Integer rutinaId, Integer usuarioId) {
        // Verificar que la rutina existe
        rutinaRepository.findById(rutinaId)
                .orElseThrow(() -> new RuntimeException("Rutina no encontrada"));

        // Verificar que el usuario no tenga ya esta rutina asignada y activa
        Optional<RutinaAsignada> existente = rutinaAsignadaRepository
                .findByUsuarioIdAndRutinaId(usuarioId, rutinaId);

        if (existente.isPresent() && existente.get().getEstado() == EstadoRutina.ACTIVA) {
            throw new RuntimeException("El usuario ya tiene esta rutina activa");
        }

        // Crear nueva asignación
        RutinaAsignada nuevaAsignacion = new RutinaAsignada();
        nuevaAsignacion.setRutinaId(rutinaId);
        nuevaAsignacion.setUsuarioId(usuarioId);
        nuevaAsignacion.setFechaAsignacion(LocalDate.now());
        nuevaAsignacion.setEstado(EstadoRutina.ACTIVA);
        nuevaAsignacion.setProgreso(0);
        nuevaAsignacion.setUltimaActividad(LocalDate.now());
        nuevaAsignacion.setVecesCompletada(0);

        return rutinaAsignadaRepository.save(nuevaAsignacion);
    }

    public List<RutinaAsignada> obtenerRutinasDelUsuario(Integer usuarioId) {
        return rutinaAsignadaRepository.findByUsuarioIdOrderByFechaAsignacionDesc(usuarioId);
    }

    public List<RutinaAsignada> obtenerRutinasActivasDelUsuario(Integer usuarioId) {
        return rutinaAsignadaRepository.findByUsuarioIdAndEstado(usuarioId, EstadoRutina.ACTIVA);
    }

    public List<RutinaAsignada> obtenerRutinasCompletadasDelUsuario(Integer usuarioId) {
        return rutinaAsignadaRepository.findByUsuarioIdAndEstado(usuarioId, EstadoRutina.COMPLETADA);
    }

    // ===== PROGRESO Y ESTADÍSTICAS =====

    public void actualizarProgresoRutina(Integer rutinaAsignadaId, int progreso) {
        RutinaAsignada rutina = rutinaAsignadaRepository.findById(rutinaAsignadaId)
                .orElseThrow(() -> new RuntimeException("Rutina asignada no encontrada"));

        rutina.actualizarProgreso(progreso);
        rutinaAsignadaRepository.save(rutina);
    }

    public void marcarRutinaComoCompletada(Integer rutinaAsignadaId) {
        RutinaAsignada rutina = rutinaAsignadaRepository.findById(rutinaAsignadaId)
                .orElseThrow(() -> new RuntimeException("Rutina asignada no encontrada"));

        rutina.marcarComoCompletada();
        rutinaAsignadaRepository.save(rutina);
    }

    public Double calcularProgresoGeneralUsuario(Integer usuarioId) {
        Double progreso = rutinaAsignadaRepository.calcularProgresoGeneral(usuarioId);
        return progreso != null ? progreso : 0.0;
    }

    public Object[] obtenerEstadisticasUsuario(Integer usuarioId) {
        return rutinaAsignadaRepository.getEstadisticasUsuario(usuarioId);
    }

    public List<RutinaAsignada> obtenerRutinasAsignadas(Integer usuarioId) {
        // Excluir BORRADOR: no debe ser visible al usuario hasta que el entrenador
        // guarde.
        return rutinaAsignadaRepository.findByUsuarioId(usuarioId)
                .stream()
                .filter(ra -> ra.getEstado() != EstadoRutina.BORRADOR)
                .toList();
    }

    public List<Object[]> obtenerProgresoSemanal(Integer usuarioId) {
        // Retorna datos de progreso de los últimos 7 días
        // Por ahora simulamos datos, pero se puede implementar una consulta real
        return rutinaAsignadaRepository.obtenerProgresoUltimosDias(usuarioId, 7);
    }

    public int contarDiasActivosEsteMes(Integer usuarioId) {
        // Cuenta los días únicos donde el usuario hizo progreso este mes
        return rutinaAsignadaRepository.contarDiasActivosDelMes(usuarioId);
    }

    public int calcularRachaActual(Integer usuarioId) {
        // Calcula la racha de días consecutivos con actividad
        return rutinaAsignadaRepository.calcularRachaConsecutiva(usuarioId);
    }

    /**
     * Obtener rutinas completadas agrupadas por fecha
     * Para el gráfico de progreso semanal
     */
    public List<Object[]> obtenerRutinasCompletadasPorFecha(Integer usuarioId, java.time.LocalDate fechaInicio) {
        return rutinaAsignadaRepository.contarRutinasCompletadasPorFecha(usuarioId, fechaInicio);
    }

    // ===== EJERCICIOS DE RUTINAS =====

    public List<RutinaEjercicio> obtenerEjerciciosDeRutina(Integer rutinaId) {
        return rutinaEjercicioRepository.findByRutinaIdOrderByOrdenAsc(rutinaId);
    }

    public List<RutinaEjercicio> obtenerEjerciciosDeRutinaPorDia(Integer rutinaId, Integer diaOrden) {
        List<Integer> ejercicioIds = rutinaEjercicioDiaRepository
                .findEjercicioIdsByRutinaIdAndDiaOrden(rutinaId, diaOrden);
        if (ejercicioIds != null && !ejercicioIds.isEmpty()) {
            return rutinaEjercicioRepository.findByRutinaIdAndEjercicioIdInOrderByOrdenAsc(rutinaId, ejercicioIds);
        }
        // Fallback legacy (rutinas antiguas que aún usan rutina_ejercicio.dia_orden)
        return rutinaEjercicioRepository.findByRutinaIdAndDiaOrdenOrderByOrdenAsc(rutinaId, diaOrden);
    }

    public List<RutinaDia> obtenerDiasDeRutina(Integer rutinaId) {
        return rutinaDiaRepository.findByRutinaIdOrderByOrdenAsc(rutinaId);
    }

    /**
     * Rutinas legacy/globales: si no tienen ciclo, crea un ciclo básico de 1 día
     * y asigna cualquier ejercicio sin diaOrden a ese día.
     */
    public void asegurarCicloBasicoSiNoExiste(Integer rutinaId) {
        if (rutinaDiaRepository.countByRutinaId(rutinaId) > 0) {
            return;
        }

        RutinaDia dia = new RutinaDia();
        dia.setRutinaId(rutinaId);
        dia.setOrden(1);
        dia.setNombre("Entrenamiento");
        dia.setTipo(RutinaDia.TipoDia.ENTRENAMIENTO);
        rutinaDiaRepository.save(dia);

        List<RutinaEjercicio> ejercicios = rutinaEjercicioRepository.findByRutinaIdOrderByOrdenAsc(rutinaId);
        for (RutinaEjercicio e : ejercicios) {
            if (e.getDiaOrden() == null) {
                e.setDiaOrden(1);
                rutinaEjercicioRepository.save(e);
            }
        }
    }

    // Comentado temporalmente debido a problemas con el campo id en la tabla
    /*
     * public List<RutinaEjercicio> obtenerEjerciciosConDetalles(Integer rutinaId) {
     * return rutinaEjercicioRepository.findEjerciciosConDetalles(rutinaId);
     * }
     */

    public List<EjercicioRutinaDto> obtenerEjerciciosConDetallesDto(Integer rutinaId) {
        List<Object[]> results = rutinaEjercicioRepository.findEjerciciosConDetallesNativo(rutinaId);
        List<EjercicioRutinaDto> ejercicios = new ArrayList<>();

        for (Object[] row : results) {
            EjercicioRutinaDto dto = new EjercicioRutinaDto();
            dto.setRutinaId((Integer) row[0]);
            dto.setEjercicioId((Integer) row[1]);
            dto.setOrden((Integer) row[2]);
            dto.setSeries((Integer) row[3]);
            dto.setRepeticiones((Integer) row[4]);
            dto.setDuracionSegundos((Integer) row[5]);
            dto.setDescansoSegundos((Integer) row[6]);
            dto.setPesoKg((Double) row[7]);
            dto.setNotas((String) row[8]);
            dto.setEjercicioNombre((String) row[9]);
            dto.setEjercicioDescripcion((String) row[10]);
            dto.setEjercicioImagen((String) row[11]);

            ejercicios.add(dto);
        }

        return ejercicios;
    }

    // ===== RUTINAS PARA HOY =====

    public List<RutinaAsignada> obtenerRutinasParaHoy(Integer usuarioId) {
        return rutinaAsignadaRepository.findRutinasParaHoy(
                usuarioId, EstadoRutina.ACTIVA, LocalDate.now());
    }

    // ===== RUTINAS DISPONIBLES PARA USUARIO =====

    public List<Rutina> obtenerRutinasDisponiblesParaUsuario(Integer entrenadorId) {
        if (entrenadorId != null) {
            return rutinaRepository.findRutinasDisponiblesParaUsuario(entrenadorId);
        } else {
            return rutinaRepository.findByEntrenadorIdIsNull(); // Solo rutinas globales
        }
    }

    // ===== CREACIÓN Y EDICIÓN DE RUTINAS =====

    public Rutina crearRutina(Rutina rutina) {
        rutina.setFechaCreacion(LocalDate.now());
        return rutinaRepository.save(rutina);
    }

    public Rutina actualizarRutina(Rutina rutina) {
        if (!rutinaRepository.existsById(rutina.getId())) {
            throw new RuntimeException("Rutina no encontrada");
        }
        return rutinaRepository.save(rutina);
    }

    @Transactional
    public void eliminarRutina(Integer rutinaId) {
        // El controller ya maneja el reemplazo de asignaciones
        // Solo eliminamos ejercicios y rutina

        // Eliminar ejercicios de la rutina
        rutinaEjercicioRepository.deleteByRutinaId(rutinaId);

        // Eliminar la rutina
        rutinaRepository.deleteById(rutinaId);
    }

    // ===== RUTINAS RECIENTES =====

    public List<RutinaAsignada> obtenerRutinasRecientes(Integer usuarioId) {
        return rutinaAsignadaRepository.findTop5ByUsuarioIdOrderByFechaAsignacionDesc(usuarioId);
    }
}