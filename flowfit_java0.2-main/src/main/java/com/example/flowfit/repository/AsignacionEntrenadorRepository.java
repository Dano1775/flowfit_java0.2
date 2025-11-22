package com.example.flowfit.repository;

import com.example.flowfit.model.AsignacionEntrenador;
import com.example.flowfit.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AsignacionEntrenadorRepository extends JpaRepository<AsignacionEntrenador, Long> {
    
    // Buscar todas las solicitudes de un usuario
    List<AsignacionEntrenador> findByUsuarioId(Integer usuarioId);
    
    // Buscar todas las solicitudes recibidas por un entrenador
    List<AsignacionEntrenador> findByEntrenadorId(Integer entrenadorId);
    
    // Buscar solicitudes por estado
    List<AsignacionEntrenador> findByEstado(AsignacionEntrenador.EstadoAsignacion estado);
    
    // Buscar solicitudes pendientes de un entrenador
    List<AsignacionEntrenador> findByEntrenadorIdAndEstado(Integer entrenadorId, AsignacionEntrenador.EstadoAsignacion estado);
    
    // Verificar si ya existe una solicitud entre usuario y entrenador
    Optional<AsignacionEntrenador> findByUsuarioIdAndEntrenadorId(Integer usuarioId, Integer entrenadorId);
    
    // Buscar entrenador actual de un usuario (asignación aceptada)
    @Query("SELECT a FROM AsignacionEntrenador a WHERE a.usuario.id = :usuarioId AND a.estado = 'ACEPTADA'")
    Optional<AsignacionEntrenador> findEntrenadorActualByUsuarioId(@Param("usuarioId") Integer usuarioId);
    
    // Buscar usuarios asignados a un entrenador
    @Query("SELECT a FROM AsignacionEntrenador a WHERE a.entrenador.id = :entrenadorId AND a.estado = 'ACEPTADA'")
    List<AsignacionEntrenador> findUsuariosAsignadosByEntrenadorId(@Param("entrenadorId") Integer entrenadorId);
}