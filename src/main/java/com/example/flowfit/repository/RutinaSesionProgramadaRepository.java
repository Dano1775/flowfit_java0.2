package com.example.flowfit.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.flowfit.model.RutinaSesionProgramada;

@Repository
public interface RutinaSesionProgramadaRepository extends JpaRepository<RutinaSesionProgramada, Integer> {

        List<RutinaSesionProgramada> findByRutinaAsignada_IdOrderByFechaAsc(Integer rutinaAsignadaId);

        Optional<RutinaSesionProgramada> findByRutinaAsignada_IdAndFecha(Integer rutinaAsignadaId, LocalDate fecha);

        @Query("select s from RutinaSesionProgramada s join fetch s.rutinaAsignada ra join fetch ra.rutina r left join fetch s.rutinaDia d where ra.usuarioId = :usuarioId and ra.estado <> com.example.flowfit.model.RutinaAsignada.EstadoRutina.BORRADOR and s.fecha >= :start and s.fecha <= :end")
        List<RutinaSesionProgramada> findEventosUsuarioEnRango(
                        @Param("usuarioId") Integer usuarioId,
                        @Param("start") LocalDate start,
                        @Param("end") LocalDate end);

        @Query("select s from RutinaSesionProgramada s join fetch s.rutinaAsignada ra join fetch ra.rutina r left join fetch s.rutinaDia d where ra.id = :rutinaAsignadaId and s.fecha >= :start and s.fecha <= :end")
        List<RutinaSesionProgramada> findEventosAsignacionEnRango(
                        @Param("rutinaAsignadaId") Integer rutinaAsignadaId,
                        @Param("start") LocalDate start,
                        @Param("end") LocalDate end);
}
