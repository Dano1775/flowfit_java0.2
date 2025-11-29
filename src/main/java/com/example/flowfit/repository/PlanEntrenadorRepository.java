package com.example.flowfit.repository;

import com.example.flowfit.model.PlanEntrenador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PlanEntrenadorRepository extends JpaRepository<PlanEntrenador, Integer> {
    
    List<PlanEntrenador> findByEntrenadorIdAndActivoTrue(Integer entrenadorId);
    
    List<PlanEntrenador> findByEntrenadorId(Integer entrenadorId);
    
    @Query("SELECT p FROM PlanEntrenador p WHERE p.entrenadorId = :entrenadorId " +
           "AND p.esPublico = true AND p.activo = true ORDER BY p.destacado DESC, p.precioMensual ASC")
    List<PlanEntrenador> findPlanesPublicosByEntrenador(@Param("entrenadorId") Integer entrenadorId);
    
    @Query("SELECT COUNT(c) FROM ContratacionEntrenador c WHERE c.planBaseId = :planId AND c.estado = 'ACTIVA'")
    Long contarClientesActivos(@Param("planId") Integer planId);
}
