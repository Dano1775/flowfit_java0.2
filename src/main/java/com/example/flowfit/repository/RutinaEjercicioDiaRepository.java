package com.example.flowfit.repository;

import com.example.flowfit.model.RutinaEjercicioDia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RutinaEjercicioDiaRepository
        extends JpaRepository<RutinaEjercicioDia, RutinaEjercicioDia.RutinaEjercicioDiaId> {

    @Query("SELECT red.ejercicioId FROM RutinaEjercicioDia red WHERE red.rutinaId = :rutinaId AND red.diaOrden = :diaOrden")
    List<Integer> findEjercicioIdsByRutinaIdAndDiaOrden(@Param("rutinaId") Integer rutinaId,
            @Param("diaOrden") Integer diaOrden);

    void deleteByRutinaId(Integer rutinaId);
}
