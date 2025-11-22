package com.example.flowfit.repository;

import com.example.flowfit.model.Rutina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RutinaRepository extends JpaRepository<Rutina, Integer> {
    
    // Rutinas globales (sin entrenador específico)
    List<Rutina> findByEntrenadorIdIsNull();
    
    // Rutinas creadas por un entrenador específico
    List<Rutina> findByEntrenadorId(Integer entrenadorId);
    
    // Buscar rutinas por nombre
    List<Rutina> findByNombreContainingIgnoreCase(String nombre);
    
    // Rutinas disponibles para un usuario (globales + las de su entrenador si tiene)
    @Query("SELECT r FROM Rutina r WHERE r.entrenadorId IS NULL OR r.entrenadorId = :entrenadorId")
    List<Rutina> findRutinasDisponiblesParaUsuario(@Param("entrenadorId") Integer entrenadorId);
    
    // Rutinas más populares (las que más usuarios tienen asignadas)
    @Query(value = "SELECT r.* FROM rutina r " +
           "LEFT JOIN rutina_asignada ra ON r.id = ra.rutina_id " +
           "WHERE r.entrenador_id IS NULL " +
           "GROUP BY r.id " +
           "ORDER BY COUNT(ra.id) DESC " +
           "LIMIT :limite", nativeQuery = true)
    List<Rutina> findRutinasPopulares(@Param("limite") int limite);
    
    // Contar rutinas por entrenador
    long countByEntrenadorId(Integer entrenadorId);
    
    // Rutinas globales ordenadas por nombre
    @Query("SELECT r FROM Rutina r WHERE r.entrenadorId IS NULL ORDER BY r.nombre")
    List<Rutina> findRutinasGlobalesOrdenadas();
}