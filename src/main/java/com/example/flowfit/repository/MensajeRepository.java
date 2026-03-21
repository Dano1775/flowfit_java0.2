package com.example.flowfit.repository;

import com.example.flowfit.model.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

       List<Mensaje> findByConversacionIdOrderByFechaEnvioAsc(Long conversacionId);

       List<Mensaje> findByConversacionIdAndLeidoFalse(Long conversacionId);

       @Modifying
       @Query("UPDATE Mensaje m SET m.leido = true, m.fechaLectura = CURRENT_TIMESTAMP " +
                     "WHERE m.conversacionId = :conversacionId AND m.remitenteId != :usuarioId AND m.leido = false")
       void marcarComoLeidos(@Param("conversacionId") Long conversacionId, @Param("usuarioId") Integer usuarioId);

       @Query("SELECT m FROM Mensaje m WHERE m.conversacionId = :conversacionId AND m.tipoMensaje = 'PROPUESTA_PLAN' " +
                     "ORDER BY m.fechaEnvio DESC")
       List<Mensaje> findPropuestasPorConversacion(@Param("conversacionId") Long conversacionId);

       @Modifying
       @Transactional
       @Query("UPDATE Mensaje m SET m.eliminado = true " +
                     "WHERE m.conversacionId = :conversacionId " +
                     "AND (m.eliminado IS NULL OR m.eliminado = false) " +
                     "AND m.tipoMensaje = 'SISTEMA' " +
                     "AND m.metadata IS NOT NULL " +
                     "AND (m.metadata LIKE CONCAT('%', :pattern1, '%') OR m.metadata LIKE CONCAT('%', :pattern2, '%'))")
       int softDeleteSistemaByMetadataPattern(
                     @Param("conversacionId") Long conversacionId,
                     @Param("pattern1") String pattern1,
                     @Param("pattern2") String pattern2);

       @Query("SELECT COUNT(m) FROM Mensaje m " +
                     "WHERE m.conversacionId = :conversacionId " +
                     "AND (m.eliminado IS NULL OR m.eliminado = false) " +
                     "AND m.tipoMensaje = 'SISTEMA' " +
                     "AND m.metadata IS NOT NULL " +
                     "AND (m.metadata LIKE CONCAT('%', :pattern1, '%') OR m.metadata LIKE CONCAT('%', :pattern2, '%'))")
       long countSistemaByMetadataPattern(
                     @Param("conversacionId") Long conversacionId,
                     @Param("pattern1") String pattern1,
                     @Param("pattern2") String pattern2);
}
