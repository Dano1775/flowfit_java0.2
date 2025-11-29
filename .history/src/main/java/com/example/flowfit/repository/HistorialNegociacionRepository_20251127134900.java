package com.example.flowfit.repository;

import com.example.flowfit.model.HistorialNegociacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HistorialNegociacionRepository extends JpaRepository<HistorialNegociacion, Long> {
    
    List<HistorialNegociacion> findByContratacionIdOrderByVersionAsc(Long contratacionId);
    
    HistorialNegociacion findTopByContratacionIdOrderByVersionDesc(Long contratacionId);
    
    Long countByContratacionId(Long contratacionId);
}
