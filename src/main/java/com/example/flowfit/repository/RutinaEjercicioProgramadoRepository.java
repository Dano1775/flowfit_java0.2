package com.example.flowfit.repository;

import com.example.flowfit.model.RutinaEjercicioProgramado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RutinaEjercicioProgramadoRepository extends JpaRepository<RutinaEjercicioProgramado, Integer> {

        List<RutinaEjercicioProgramado> findByRutinaAsignadaIdAndFechaBetweenOrderByFechaAscOrdenAsc(
                        Integer rutinaAsignadaId,
                        LocalDate start,
                        LocalDate end);

        List<RutinaEjercicioProgramado> findByRutinaAsignadaIdAndFechaOrderByOrdenAsc(Integer rutinaAsignadaId,
                        LocalDate fecha);

        boolean existsByRutinaAsignadaIdAndFecha(Integer rutinaAsignadaId, LocalDate fecha);

        @Query("SELECT COALESCE(MAX(p.orden), 0) FROM RutinaEjercicioProgramado p WHERE p.rutinaAsignadaId = :rutinaAsignadaId AND p.fecha = :fecha")
        Integer findMaxOrdenForDate(@Param("rutinaAsignadaId") Integer rutinaAsignadaId,
                        @Param("fecha") LocalDate fecha);

        long countByRutinaAsignadaId(Integer rutinaAsignadaId);

        void deleteByRutinaAsignadaId(Integer rutinaAsignadaId);
}
