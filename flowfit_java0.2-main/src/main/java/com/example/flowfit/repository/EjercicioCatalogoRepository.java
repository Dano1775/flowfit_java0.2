package com.example.flowfit.repository;

import com.example.flowfit.model.EjercicioCatalogo;
import com.example.flowfit.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EjercicioCatalogoRepository extends JpaRepository<EjercicioCatalogo, Long> {
    
    // Find exercises by name (partial match)
    List<EjercicioCatalogo> findByNombreContainingIgnoreCase(String nombre);
    
    // Find exercises created by a specific user (trainer/admin)
    List<EjercicioCatalogo> findByCreadoPor(Usuario creadoPor);
    
    // Find global exercises (created by system/admin)
    List<EjercicioCatalogo> findByCreadoPorIsNull();
    
    // Search exercises by name or description
    @Query("SELECT e FROM EjercicioCatalogo e WHERE " +
           "LOWER(e.nombre) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(e.descripcion) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<EjercicioCatalogo> searchByNameOrDescription(@Param("searchTerm") String searchTerm);
    
    // Conteos para estadísticas
    long countByCreadoPor(Usuario creadoPor);
    
    long countByCreadoPorIsNull();
}