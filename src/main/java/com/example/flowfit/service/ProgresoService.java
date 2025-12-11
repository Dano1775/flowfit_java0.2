package com.example.flowfit.service;

import com.example.flowfit.model.*;
import com.example.flowfit.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProgresoService {

    @Autowired
    private ProgresoEjercicioRepository progresoRepository;

    @Autowired
    private RutinaAsignadaRepository rutinaAsignadaRepository;

    @Autowired
    private EjercicioCatalogoRepository ejercicioRepository;

    /**
     * Registrar progreso de un ejercicio
     */
    @Transactional
    public ProgresoEjercicio registrarProgreso(Usuario usuario, Integer rutinaAsignadaId,
            Integer ejercicioId, Integer series,
            Integer repeticiones, Double peso, String comentarios) {
        RutinaAsignada rutina = rutinaAsignadaRepository.findById(rutinaAsignadaId)
                .orElseThrow(() -> new RuntimeException("Rutina no encontrada"));

        EjercicioCatalogo ejercicio = ejercicioRepository.findById(ejercicioId)
                .orElseThrow(() -> new RuntimeException("Ejercicio no encontrado"));

        ProgresoEjercicio progreso = new ProgresoEjercicio(usuario, rutina, ejercicio);
        progreso.setSeriesCompletadas(series);
        progreso.setRepeticionesRealizadas(repeticiones);
        progreso.setPesoUtilizado(peso);
        progreso.setComentarios(comentarios);

        return progresoRepository.save(progreso);
    }

    /**
     * Obtener estadísticas generales del usuario
     */
    public Map<String, Object> getEstadisticasGenerales(Usuario usuario) {
        Map<String, Object> stats = new HashMap<>();

        // Total de ejercicios completados
        Long totalEjercicios = progresoRepository.countByUsuario(usuario);
        stats.put("totalEjercicios", totalEjercicios);

        // Días entrenados última semana
        LocalDate hace7Dias = LocalDate.now().minusDays(7);
        Long diasEntrenados = progresoRepository.countDiasEntrenadosUltimaSemana(usuario, hace7Dias);
        stats.put("diasEntrenadosUltimaSemana", diasEntrenados);

        // Total series última semana
        Integer totalSeries = progresoRepository.sumSeriesCompletadasDesde(usuario, hace7Dias);
        stats.put("totalSeriesUltimaSemana", totalSeries);

        // Racha actual (días consecutivos)
        int racha = calcularRachaActual(usuario);
        stats.put("rachaActual", racha);

        // Progreso del mes
        LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
        List<ProgresoEjercicio> progresoMes = progresoRepository.findByUsuarioAndFechaBetween(
                usuario, inicioMes, LocalDate.now());
        stats.put("ejerciciosMes", progresoMes.size());

        return stats;
    }

    /**
     * Contar días únicos entrenados entre dos fechas
     */
    public int contarDiasEntrenadosEntre(Usuario usuario, LocalDate fechaInicio, LocalDate fechaFin) {
        List<ProgresoEjercicio> progresos = progresoRepository.findByUsuarioAndFechaBetween(usuario, fechaInicio,
                fechaFin);
        // Contar días únicos (agrupar por fecha)
        return (int) progresos.stream()
                .map(ProgresoEjercicio::getFecha)
                .distinct()
                .count();
    }

    /**
     * Obtener datos para gráficas de progreso
     */
    public Map<String, Object> getDatosGraficas(Usuario usuario, int dias) {
        Map<String, Object> datos = new HashMap<>();

        LocalDate fechaInicio = LocalDate.now().minusDays(dias);
        List<Object[]> estadisticas = progresoRepository.getEstadisticasPorFecha(usuario, fechaInicio);

        // Preparar datos para gráfica de ejercicios por día
        List<String> fechas = new ArrayList<>();
        List<Long> ejerciciosPorDia = new ArrayList<>();
        List<Long> seriesPorDia = new ArrayList<>();
        List<Long> repeticionesPorDia = new ArrayList<>();

        for (Object[] row : estadisticas) {
            LocalDate fecha = (LocalDate) row[0];
            Long total = ((Number) row[1]).longValue();
            Long totalSeries = row[2] != null ? ((Number) row[2]).longValue() : 0L;
            Long totalReps = row[3] != null ? ((Number) row[3]).longValue() : 0L;

            fechas.add(fecha.toString());
            ejerciciosPorDia.add(total);
            seriesPorDia.add(totalSeries);
            repeticionesPorDia.add(totalReps);
        }

        datos.put("fechas", fechas);
        datos.put("ejerciciosPorDia", ejerciciosPorDia);
        datos.put("seriesPorDia", seriesPorDia);
        datos.put("repeticionesPorDia", repeticionesPorDia);

        return datos;
    }

    /**
     * Obtener evolución de peso en un ejercicio específico
     */
    public Map<String, Object> getEvolucionPeso(Usuario usuario, Integer ejercicioId) {
        List<ProgresoEjercicio> historial = progresoRepository.findByUsuarioAndEjercicioId(usuario, ejercicioId);

        List<String> fechas = historial.stream()
                .map(p -> p.getFecha().toString())
                .collect(Collectors.toList());

        List<Double> pesos = historial.stream()
                .map(p -> p.getPesoUtilizado() != null ? p.getPesoUtilizado() : 0.0)
                .collect(Collectors.toList());

        Map<String, Object> datos = new HashMap<>();
        datos.put("fechas", fechas);
        datos.put("pesos", pesos);
        datos.put("ejercicio", historial.isEmpty() ? "" : historial.get(0).getEjercicio().getNombre());

        return datos;
    }

    /**
     * Calcular racha actual de días consecutivos
     */
    private int calcularRachaActual(Usuario usuario) {
        List<ProgresoEjercicio> progreso = progresoRepository.findByUsuarioOrderByFechaDesc(usuario);

        if (progreso.isEmpty())
            return 0;

        // Obtener fechas únicas ordenadas descendente
        List<LocalDate> fechasUnicas = progreso.stream()
                .map(ProgresoEjercicio::getFecha)
                .distinct()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());

        // Verificar si entrenó hoy o ayer
        LocalDate hoy = LocalDate.now();
        LocalDate ayer = hoy.minusDays(1);

        if (!fechasUnicas.contains(hoy) && !fechasUnicas.contains(ayer)) {
            return 0;
        }

        // Contar días consecutivos
        int racha = 1;
        LocalDate fechaEsperada = fechasUnicas.contains(hoy) ? hoy.minusDays(1) : ayer.minusDays(1);

        for (int i = 1; i < fechasUnicas.size(); i++) {
            if (fechasUnicas.get(i).equals(fechaEsperada)) {
                racha++;
                fechaEsperada = fechaEsperada.minusDays(1);
            } else {
                break;
            }
        }

        return racha;
    }

    /**
     * Obtener último progreso del usuario
     */
    public List<ProgresoEjercicio> getUltimoProgreso(Usuario usuario, int limite) {
        List<ProgresoEjercicio> todo = progresoRepository.findByUsuarioOrderByFechaDesc(usuario);
        return todo.stream().limit(limite).collect(Collectors.toList());
    }

    /**
     * Obtener ejercicios más realizados
     */
    public Map<String, Long> getEjerciciosMasRealizados(Usuario usuario, int limite) {
        List<ProgresoEjercicio> progreso = progresoRepository.findByUsuarioOrderByFechaDesc(usuario);

        return progreso.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getEjercicio().getNombre(),
                        Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limite)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new));
    }
}
