package com.example.flowfit.repository;

import com.example.flowfit.model.RutinaEjercicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RutinaEjercicioRepository extends JpaRepository<RutinaEjercicio, Integer> {
    
    // Ejercicios de una rutina específica ordenados por orden
    List<RutinaEjercicio> findByRutinaIdOrderByOrdenAsc(Integer rutinaId);
    
    // Contar ejercicios en una rutina
    long countByRutinaId(Integer rutinaId);
    
    // Eliminar todos los ejercicios de una rutina
    void deleteByRutinaId(Integer rutinaId);
    
    // Ejercicios de una rutina con detalles del catálogo
    @Query("SELECT re FROM RutinaEjercicio re " +
           "JOIN EjercicioCatalogo ec ON re.ejercicioId = ec.id " +
           "WHERE re.rutinaId = :rutinaId " +
           "ORDER BY re.orden")
    List<RutinaEjercicio> findEjerciciosConDetalles(@Param("rutinaId") Integer rutinaId);
    
    // Duración total de una rutina (suma de todos los ejercicios)
    @Query("SELECT SUM(re.duracionSegundos) FROM RutinaEjercicio re WHERE re.rutinaId = :rutinaId")
    Integer calcularDuracionTotalRutina(@Param("rutinaId") Integer rutinaId);
    
    // Ejercicios más utilizados en rutinas
    @Query(value = "SELECT ejercicio_id, COUNT(*) as total " +
           "FROM rutina_ejercicio " +
           "GROUP BY ejercicio_id " +
           "ORDER BY total DESC " +
           "LIMIT :limite", nativeQuery = true)
    List<Object[]> findEjerciciosMasUtilizados(@Param("limite") int limite);
    
    // Verificar si un ejercicio está siendo usado en alguna rutina
    boolean existsByEjercicioId(Integer ejercicioId);
    
    // Obtener el último orden de ejercicios en una rutina (para agregar nuevos)
    @Query("SELECT COALESCE(MAX(re.orden), 0) FROM RutinaEjercicio re WHERE re.rutinaId = :rutinaId")
    Integer obtenerUltimoOrden(@Param("rutinaId") Integer rutinaId);
}