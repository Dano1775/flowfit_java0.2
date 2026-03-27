package com.example.flowfit.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.flowfit.model.RutinaDia;

@Repository
public interface RutinaDiaRepository extends JpaRepository<RutinaDia, Integer> {

    List<RutinaDia> findByRutinaIdOrderByOrdenAsc(Integer rutinaId);

    long countByRutinaId(Integer rutinaId);

    Optional<RutinaDia> findByRutinaIdAndOrden(Integer rutinaId, Integer orden);
}
