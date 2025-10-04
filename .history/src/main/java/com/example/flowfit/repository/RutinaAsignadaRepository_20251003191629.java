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
}