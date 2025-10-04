package com.example.flowfit.repository;

import com.example.flowfit.model.HistorialEdiciones;
import com.example.flowfit.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HistorialEdicionesRepository extends JpaRepository<HistorialEdiciones, Long> {
    
    // Find edit history by edited user
    List<HistorialEdiciones> findByUsuarioEditado(Usuario usuario);
    
    // Find edit history by admin who made the edits
    List<HistorialEdiciones> findByAdminEditor(Usuario admin);
    
    // Find edit history by specific field
    List<HistorialEdiciones> findByCampoEditado(String campo);
    
    // Find recent edit history ordered by date
    @Query("SELECT he FROM HistorialEdiciones he ORDER BY he.fechaEdicion DESC")
    List<HistorialEdiciones> findRecentEdits();
    
    // Find edits by date range
    List<HistorialEdiciones> findByFechaEdicionBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find edits for a specific user in date range
    @Query("SELECT he FROM HistorialEdiciones he WHERE he.usuarioEditado = :usuario AND he.fechaEdicion BETWEEN :startDate AND :endDate ORDER BY he.fechaEdicion DESC")
    List<HistorialEdiciones> findByUsuarioAndDateRange(@Param("usuario") Usuario usuario, 
                                                       @Param("startDate") LocalDateTime startDate, 
                                                       @Param("endDate") LocalDateTime endDate);
    
    // Count edits by admin
    long countByAdminEditor(Usuario admin);
    
    // Count edits for a specific user
    long countByUsuarioEditado(Usuario usuario);
    
    // Find all edits for a user ordered by date (most recent first)
    @Query("SELECT he FROM HistorialEdiciones he WHERE he.usuarioEditado = :usuario ORDER BY he.fechaEdicion DESC")
    List<HistorialEdiciones> findByUsuarioEditadoOrderByFechaEdicionDesc(@Param("usuario") Usuario usuario);
    
    // Find edits made by admin ordered by date
    @Query("SELECT he FROM HistorialEdiciones he WHERE he.adminEditor = :admin ORDER BY he.fechaEdicion DESC")
    List<HistorialEdiciones> findByAdminEditorOrderByFechaEdicionDesc(@Param("admin") Usuario admin);
}