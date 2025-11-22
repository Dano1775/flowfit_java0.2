package com.example.flowfit.repository;

import com.example.flowfit.model.BoletinInformativo;
import com.example.flowfit.model.BoletinInformativo.EstadoEnvio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BoletinInformativoRepository extends JpaRepository<BoletinInformativo, Long> {
    
    /**
     * Busca boletines por estado de envío
     */
    List<BoletinInformativo> findByEstadoEnvio(EstadoEnvio estado);
    
    /**
     * Busca boletines creados por un usuario
     */
    List<BoletinInformativo> findByCreadoPor(String creadoPor);
    
    /**
     * Busca boletines creados en un rango de fechas
     */
    List<BoletinInformativo> findByFechaCreacionBetween(LocalDateTime inicio, LocalDateTime fin);
    
    /**
     * Busca todos los boletines ordenados por fecha de creación descendente
     */
    List<BoletinInformativo> findAllByOrderByFechaCreacionDesc();
}
