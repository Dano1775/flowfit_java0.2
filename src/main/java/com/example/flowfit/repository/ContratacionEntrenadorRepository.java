package com.example.flowfit.repository;

import com.example.flowfit.model.ContratacionEntrenador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ContratacionEntrenadorRepository extends JpaRepository<ContratacionEntrenador, Long> {

        List<ContratacionEntrenador> findByUsuarioIdOrderByFechaSolicitudDesc(Integer usuarioId);

        List<ContratacionEntrenador> findByEntrenadorIdOrderByFechaSolicitudDesc(Integer entrenadorId);

        @Query("SELECT c FROM ContratacionEntrenador c WHERE c.usuarioId = :usuarioId AND c.estado = :estado")
        List<ContratacionEntrenador> findByUsuarioIdAndEstado(
                        @Param("usuarioId") Integer usuarioId,
                        @Param("estado") ContratacionEntrenador.EstadoContratacion estado);

        @Query("SELECT c FROM ContratacionEntrenador c WHERE c.entrenadorId = :entrenadorId AND c.estado = :estado")
        List<ContratacionEntrenador> findByEntrenadorIdAndEstado(
                        @Param("entrenadorId") Integer entrenadorId,
                        @Param("estado") ContratacionEntrenador.EstadoContratacion estado);

        @Query("SELECT c FROM ContratacionEntrenador c WHERE c.entrenadorId = :entrenadorId " +
                        "AND c.estado IN ('PENDIENTE_APROBACION', 'NEGOCIACION') ORDER BY c.fechaSolicitud DESC")
        List<ContratacionEntrenador> findPendientesByEntrenador(@Param("entrenadorId") Integer entrenadorId);

        @Query("SELECT c FROM ContratacionEntrenador c WHERE c.estado = 'ACTIVA' " +
                        "AND c.fechaFin < CURRENT_TIMESTAMP")
        List<ContratacionEntrenador> findContratacionesVencidas();

        @Query("SELECT c FROM ContratacionEntrenador c WHERE c.planBaseId = :planId AND c.estado != :estado")
        List<ContratacionEntrenador> findByPlanIdAndEstadoNot(
                        @Param("planId") Integer planId,
                        @Param("estado") ContratacionEntrenador.EstadoContratacion estado);

        // Retorna lista ordenada por fecha más reciente para manejar duplicados
        @Query("SELECT c FROM ContratacionEntrenador c WHERE c.usuarioId = :usuarioId AND c.entrenadorId = :entrenadorId ORDER BY c.fechaSolicitud DESC")
        List<ContratacionEntrenador> findAllByUsuarioIdAndEntrenadorId(
                        @Param("usuarioId") Integer usuarioId,
                        @Param("entrenadorId") Integer entrenadorId);

        // Método auxiliar que retorna solo la contratación más reciente
        default java.util.Optional<ContratacionEntrenador> findByUsuarioIdAndEntrenadorId(Integer usuarioId,
                        Integer entrenadorId) {
                List<ContratacionEntrenador> contrataciones = findAllByUsuarioIdAndEntrenadorId(usuarioId,
                                entrenadorId);
                return contrataciones.isEmpty() ? java.util.Optional.empty()
                                : java.util.Optional.of(contrataciones.get(0));
        }
}
