package com.example.flowfit.repository;

import com.example.flowfit.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    
    // Find user by email (for login)
    Optional<Usuario> findByCorreo(String correo);
    
    // Find users by profile and status (for admin approval)
    List<Usuario> findByEstadoAndPerfilUsuario(String estado, Usuario.PerfilUsuario perfilUsuario);
    
    // Find pending users (trainers and nutritionists)
    @Query("SELECT u FROM Usuario u WHERE u.estado = 'I' AND u.perfilUsuario IN ('Entrenador', 'Nutricionista')")
    List<Usuario> findPendingUsers();
    
    // Count users by profile
    long countByPerfilUsuario(Usuario.PerfilUsuario perfilUsuario);
    
    // Find users by document number
    Optional<Usuario> findByNumeroDocumento(String numeroDocumento);
    
    // Check if email already exists (for registration)
    boolean existsByCorreo(String correo);
    
    // Admin dashboard methods
    long countByEstado(String estado);
    
    List<Usuario> findByEstadoOrderByIdDesc(String estado);
    
    List<Usuario> findByEstado(String estado);
    

    
    List<Usuario> findByPerfilUsuario(Usuario.PerfilUsuario perfilUsuario);
}