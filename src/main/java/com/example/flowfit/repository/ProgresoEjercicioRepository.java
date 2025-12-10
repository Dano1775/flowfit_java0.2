package com.example.flowfit.repository;

import com.example.flowfit.model.ProgresoEjercicio;
import com.example.flowfit.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProgresoEjercicioRepository extends JpaRepository<ProgresoEjercicio, Integer> {
    
    // Obtener progreso por usuario
    List<ProgresoEjercicio> findByUsuarioOrderByFechaDesc(Usuario usuario);
    
    // Obtener progreso por usuario y rango de fechas
    @Query("SELECT p FROM ProgresoEjercicio p WHERE p.usuario = :usuario " +
           "AND p.fecha BETWEEN :fechaInicio AND :fechaFin ORDER BY p.fecha DESC")
    List<ProgresoEjercicio> findByUsuarioAndFechaBetween(
        @Param("usuario") Usuario usuario,
        @Param("fechaInicio") LocalDate fechaInicio,
        @Param("fechaFin") LocalDate fechaFin
    );
    
    // Contar ejercicios completados por usuario
    @Query("SELECT COUNT(p) FROM ProgresoEjercicio p WHERE p.usuario = :usuario")
    Long countByUsuario(@Param("usuario") Usuario usuario);
    
    // Obtener progreso de un ejercicio específico
    @Query("SELECT p FROM ProgresoEjercicio p WHERE p.usuario = :usuario " +
           "AND p.ejercicio.id = :ejercicioId ORDER BY p.fecha DESC")
    List<ProgresoEjercicio> findByUsuarioAndEjercicioId(
        @Param("usuario") Usuario usuario,
        @Param("ejercicioId") Integer ejercicioId
    );
    
    // Estadísticas de la última semana
    @Query("SELECT COUNT(DISTINCT p.fecha) FROM ProgresoEjercicio p " +
           "WHERE p.usuario = :usuario AND p.fecha >= :fechaInicio")
    Long countDiasEntrenadosUltimaSemana(
        @Param("usuario") Usuario usuario,
        @Param("fechaInicio") LocalDate fechaInicio
    );
    
    // Total de series completadas
    @Query("SELECT COALESCE(SUM(p.seriesCompletadas), 0) FROM ProgresoEjercicio p " +
           "WHERE p.usuario = :usuario AND p.fecha >= :fechaInicio")
    Integer sumSeriesCompletadasDesde(
        @Param("usuario") Usuario usuario,
        @Param("fechaInicio") LocalDate fechaInicio
    );
    
    // Promedio de peso utilizado por ejercicio
    @Query("SELECT AVG(p.pesoUtilizado) FROM ProgresoEjercicio p " +
           "WHERE p.usuario = :usuario AND p.ejercicio.id = :ejercicioId " +
           "AND p.pesoUtilizado IS NOT NULL")
    Double avgPesoUtilizadoByEjercicio(
        @Param("usuario") Usuario usuario,
        @Param("ejercicioId") Integer ejercicioId
    );
    
    // Obtener progreso agrupado por fecha (para gráficas)
    @Query("SELECT p.fecha as fecha, COUNT(p) as total, " +
           "SUM(p.seriesCompletadas) as totalSeries, " +
           "SUM(p.repeticionesRealizadas) as totalReps " +
           "FROM ProgresoEjercicio p " +
           "WHERE p.usuario = :usuario AND p.fecha >= :fechaInicio " +
           "GROUP BY p.fecha ORDER BY p.fecha")
    List<Object[]> getEstadisticasPorFecha(
        @Param("usuario") Usuario usuario,
        @Param("fechaInicio") LocalDate fechaInicio
    );
}
