package com.example.flowfit.repository;

import com.example.flowfit.model.RutinaAsignada;
import com.example.flowfit.model.RutinaAsignada.EstadoRutina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RutinaAsignadaRepository extends JpaRepository<RutinaAsignada, Integer> {
    
    // Rutinas asignadas a un usuario específico
    List<RutinaAsignada> findByUsuarioIdOrderByFechaAsignacionDesc(Integer usuarioId);
    
    // Rutinas activas de un usuario
    List<RutinaAsignada> findByUsuarioIdAndEstado(Integer usuarioId, EstadoRutina estado);
    
    // Verificar si un usuario ya tiene asignada una rutina específica
    boolean existsByUsuarioIdAndRutinaId(Integer usuarioId, Integer rutinaId);
    
    // Obtener una rutina asignada específica
    Optional<RutinaAsignada> findByUsuarioIdAndRutinaId(Integer usuarioId, Integer rutinaId);
    
    // Contar rutinas completadas por un usuario
    long countByUsuarioIdAndEstado(Integer usuarioId, EstadoRutina estado);
    
    // Rutinas que deben ser completadas hoy
    @Query("SELECT ra FROM RutinaAsignada ra WHERE ra.usuarioId = :usuarioId " +
           "AND ra.estado = :estado " +
           "AND DATE(ra.fechaAsignacion) <= :fechaHoy " +
           "AND (ra.fechaCompletada IS NULL OR DATE(ra.fechaCompletada) != :fechaHoy)")
    List<RutinaAsignada> findRutinasParaHoy(@Param("usuarioId") Integer usuarioId, 
                                           @Param("estado") EstadoRutina estado,
                                           @Param("fechaHoy") LocalDate fechaHoy);
    
    // Progreso general del usuario (porcentaje de rutinas completadas)
    @Query(value = "SELECT " +
           "ROUND((COUNT(CASE WHEN estado = 'COMPLETADA' THEN 1 END) * 100.0 / COUNT(*)), 2) as progreso " +
           "FROM rutina_asignada WHERE usuario_id = :usuarioId", nativeQuery = true)
    Double calcularProgresoGeneral(@Param("usuarioId") Integer usuarioId);
    
    // Rutinas asignadas por un entrenador específico
    @Query("SELECT ra FROM RutinaAsignada ra " +
           "JOIN Rutina r ON ra.rutinaId = r.id " +
           "WHERE r.entrenadorId = :entrenadorId " +
           "ORDER BY ra.fechaAsignacion DESC")
    List<RutinaAsignada> findByEntrenadorId(@Param("entrenadorId") Integer entrenadorId);
    
    // Estadísticas del usuario: total asignadas, completadas, activas
    @Query(value = "SELECT " +
           "COUNT(*) as total, " +
           "SUM(CASE WHEN estado = 'COMPLETADA' THEN 1 ELSE 0 END) as completadas, " +
           "SUM(CASE WHEN estado = 'ACTIVA' THEN 1 ELSE 0 END) as activas " +
           "FROM rutina_asignada WHERE usuario_id = :usuarioId", nativeQuery = true)
    Object[] getEstadisticasUsuario(@Param("usuarioId") Integer usuarioId);
    
    // Rutinas recientes (últimas 5)
    List<RutinaAsignada> findTop5ByUsuarioIdOrderByFechaAsignacionDesc(Integer usuarioId);
    
    // Todas las rutinas de un usuario
    List<RutinaAsignada> findByUsuarioId(Integer usuarioId);
    
    // Progreso de los últimos días
    @Query(value = "SELECT DATE(fecha_asignacion) as fecha, AVG(progreso) as progreso_promedio " +
           "FROM rutina_asignada WHERE usuario_id = :usuarioId " +
           "AND fecha_asignacion >= DATE_SUB(NOW(), INTERVAL :dias DAY) " +
           "GROUP BY DATE(fecha_asignacion) " +
           "ORDER BY fecha", nativeQuery = true)
    List<Object[]> obtenerProgresoUltimosDias(@Param("usuarioId") Integer usuarioId, @Param("dias") int dias);
    
    // Días activos del mes actual
    @Query(value = "SELECT COUNT(DISTINCT DATE(fecha_asignacion)) " +
           "FROM rutina_asignada WHERE usuario_id = :usuarioId " +
           "AND MONTH(fecha_asignacion) = MONTH(NOW()) " +
           "AND YEAR(fecha_asignacion) = YEAR(NOW()) " +
           "AND progreso > 0", nativeQuery = true)
    int contarDiasActivosDelMes(@Param("usuarioId") Integer usuarioId);
    
    // Racha consecutiva de días con actividad
    @Query(value = "SELECT CASE " +
           "WHEN COUNT(*) > 0 THEN " +
           "  (SELECT COUNT(*) FROM (" +
           "    SELECT DATE(fecha_asignacion) as fecha " +
           "    FROM rutina_asignada " +
           "    WHERE usuario_id = :usuarioId AND progreso > 0 " +
           "    GROUP BY DATE(fecha_asignacion) " +
           "    HAVING fecha >= (" +
           "      SELECT MAX(fecha_sin_actividad) FROM (" +
           "        SELECT DATE_SUB(fecha, INTERVAL 1 DAY) as fecha_sin_actividad " +
           "        FROM (" +
           "          SELECT DATE(fecha_asignacion) as fecha " +
           "          FROM rutina_asignada " +
           "          WHERE usuario_id = :usuarioId AND progreso > 0 " +
           "          GROUP BY DATE(fecha_asignacion)" +
           "        ) fechas_activas " +
           "        WHERE NOT EXISTS (" +
           "          SELECT 1 FROM (" +
           "            SELECT DATE(fecha_asignacion) as fecha " +
           "            FROM rutina_asignada " +
           "            WHERE usuario_id = :usuarioId AND progreso > 0 " +
           "            GROUP BY DATE(fecha_asignacion)" +
           "          ) fa WHERE fa.fecha = DATE_SUB(fechas_activas.fecha, INTERVAL 1 DAY)" +
           "        ) " +
           "        ORDER BY fecha_sin_actividad DESC LIMIT 1" +
           "      ) OR '1900-01-01'" +
           "    )" +
           "  ) t)" +
           "ELSE 0 END as racha", nativeQuery = true)
    int calcularRachaConsecutiva(@Param("usuarioId") Integer usuarioId);
}