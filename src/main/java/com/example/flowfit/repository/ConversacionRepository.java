package com.example.flowfit.repository;

import com.example.flowfit.model.Conversacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConversacionRepository extends JpaRepository<Conversacion, Long> {

    // Cambiado a List para manejar duplicados - se ordenan por fecha más reciente
    List<Conversacion> findByUsuarioIdAndEntrenadorIdOrderByFechaUltimoMensajeDesc(Integer usuarioId,
            Integer entrenadorId);

    // Método auxiliar que retorna solo la conversación más reciente (para
    // compatibilidad)
    default Optional<Conversacion> findByUsuarioIdAndEntrenadorId(Integer usuarioId, Integer entrenadorId) {
        List<Conversacion> conversaciones = findByUsuarioIdAndEntrenadorIdOrderByFechaUltimoMensajeDesc(usuarioId,
                entrenadorId);
        return conversaciones.isEmpty() ? Optional.empty() : Optional.of(conversaciones.get(0));
    }

    List<Conversacion> findByUsuarioIdOrderByFechaUltimoMensajeDesc(Integer usuarioId);

    List<Conversacion> findByEntrenadorIdOrderByFechaUltimoMensajeDesc(Integer entrenadorId);

    @Query("SELECT c FROM Conversacion c WHERE (c.usuarioId = :personaId OR c.entrenadorId = :personaId) " +
            "AND c.estado = 'ACTIVA' ORDER BY c.fechaUltimoMensaje DESC")
    List<Conversacion> findConversacionesActivasByPersona(@Param("personaId") Integer personaId);

    @Query("SELECT COUNT(c) FROM Conversacion c WHERE c.usuarioId = :usuarioId AND c.mensajesNoLeidosUsuario > 0")
    Long contarMensajesNoLeidosUsuario(@Param("usuarioId") Integer usuarioId);

    @Query("SELECT COUNT(c) FROM Conversacion c WHERE c.entrenadorId = :entrenadorId AND c.mensajesNoLeidosEntrenador > 0")
    Long contarMensajesNoLeidosEntrenador(@Param("entrenadorId") Integer entrenadorId);
}
