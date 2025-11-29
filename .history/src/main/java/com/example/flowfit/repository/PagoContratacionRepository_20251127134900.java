package com.example.flowfit.repository;

import com.example.flowfit.model.PagoContratacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PagoContratacionRepository extends JpaRepository<PagoContratacion, Long> {
    
    Optional<PagoContratacion> findByContratacionId(Long contratacionId);
    
    Optional<PagoContratacion> findByMpPreferenceId(String mpPreferenceId);
    
    Optional<PagoContratacion> findByMpPaymentId(String mpPaymentId);
    
    Optional<PagoContratacion> findByMpExternalReference(String mpExternalReference);
    
    List<PagoContratacion> findByUsuarioIdOrderByFechaCreacionDesc(Integer usuarioId);
    
    @Query("SELECT p FROM PagoContratacion p JOIN p.contratacion c WHERE c.entrenadorId = :entrenadorId " +
           "AND p.estadoEscrow = 'LIBERADO' ORDER BY p.fechaLiberacionFondos DESC")
    List<PagoContratacion> findPagosLiberadosByEntrenador(@Param("entrenadorId") Integer entrenadorId);
    
    @Query("SELECT p FROM PagoContratacion p WHERE p.estadoEscrow = 'RETENIDO' " +
           "AND p.fechaLimiteDisputa < CURRENT_TIMESTAMP AND p.disputaActiva = false")
    List<PagoContratacion> findPagosParaLiberacionAutomatica();
    
    @Query("SELECT p FROM PagoContratacion p WHERE p.disputaActiva = true AND p.disputaResuelta = false")
    List<PagoContratacion> findDisputasActivas();
}
