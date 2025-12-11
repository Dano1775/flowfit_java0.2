package com.example.flowfit.repository;

import com.example.flowfit.model.RutinaAsignada;
import com.example.flowfit.model.RutinaAsignada.EstadoRutina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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

       // Métodos para entrenadores
       List<RutinaAsignada> findByRutinaEntrenadorIdAndEstado(Integer entrenadorId, EstadoRutina estado);

       List<RutinaAsignada> findByRutinaId(Integer rutinaId);

       List<RutinaAsignada> findByRutinaEntrenadorIdOrderByFechaAsignacionDesc(Integer entrenadorId);

       boolean existsByRutinaIdAndUsuarioId(Integer rutinaId, Integer usuarioId);

       @Transactional
       @Modifying
       void deleteByRutinaIdAndUsuarioId(Integer rutinaId, Integer usuarioId);

       // Rutinas que deben ser completadas hoy
       @Query("SELECT ra FROM RutinaAsignada ra WHERE ra.usuarioId = :usuarioId " +
                     "AND ra.estado = :estado " +
                     "AND DATE(ra.fechaAsignacion) <= :fechaHoy " +
                     "AND (ra.fechaCompletada IS NULL OR DATE(ra.fechaCompletada) != :fechaHoy)")
       List<RutinaAsignada> findRutinasParaHoy(@Param("usuarioId") Integer usuarioId,
                     @Param("estado") EstadoRutina estado,
                     @Param("fechaHoy") LocalDate fechaHoy);

       // Progreso general del usuario (promedio de progreso de todas las rutinas)
       @Query(value = "SELECT " +
                     "ROUND(AVG(COALESCE(progreso, 0)), 2) as progreso " +
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

       // Rutinas recientes (últimas 10) - Para entrenadores
       List<RutinaAsignada> findTop10ByOrderByFechaAsignacionDesc();

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

       // Racha consecutiva de días con actividad (simplificada)
       @Query(value = "SELECT COUNT(DISTINCT DATE(fecha_asignacion)) " +
                     "FROM rutina_asignada " +
                     "WHERE usuario_id = :usuarioId " +
                     "AND progreso > 0 " +
                     "AND fecha_asignacion >= DATE_SUB(NOW(), INTERVAL 30 DAY)", nativeQuery = true)
       int calcularRachaConsecutiva(@Param("usuarioId") Integer usuarioId);

       // Contar rutinas completadas por fecha (para gráfica de progreso)
       @Query(value = "SELECT DATE(fecha_completada) as fecha, COUNT(*) as total " +
                     "FROM rutina_asignada " +
                     "WHERE usuario_id = :usuarioId " +
                     "AND estado = 'COMPLETADA' " +
                     "AND fecha_completada >= :fechaInicio " +
                     "GROUP BY DATE(fecha_completada) " +
                     "ORDER BY fecha", nativeQuery = true)
       List<Object[]> contarRutinasCompletadasPorFecha(@Param("usuarioId") Integer usuarioId,
                     @Param("fechaInicio") LocalDate fechaInicio);
}