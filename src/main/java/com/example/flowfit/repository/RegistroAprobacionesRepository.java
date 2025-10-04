package com.example.flowfit.repository;

import com.example.flowfit.model.RegistroAprobaciones;
import com.example.flowfit.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RegistroAprobacionesRepository extends JpaRepository<RegistroAprobaciones, Long> {
    
    // Find approval records by user
    List<RegistroAprobaciones> findByUsuario(Usuario usuario);
    
    // Find approval records by admin
    List<RegistroAprobaciones> findByAdmin(Usuario admin);
    
    // Find approval records by action type
    List<RegistroAprobaciones> findByAccion(RegistroAprobaciones.Accion accion);
    
    // Find recent approval records
    @Query("SELECT ra FROM RegistroAprobaciones ra ORDER BY ra.fecha DESC")
    List<RegistroAprobaciones> findRecentApprovals();
    
    // Find approvals by date range
    List<RegistroAprobaciones> findByFechaBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Count approvals by admin
    long countByAdmin(Usuario admin);
    
    // Count approvals by action type
    long countByAccion(RegistroAprobaciones.Accion accion);
    

}