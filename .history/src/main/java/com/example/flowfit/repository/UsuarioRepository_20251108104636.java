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
    
    // Buscar usuario por correo electrónico (para login)
    Optional<Usuario> findByCorreo(String correo);
    
    // Buscar usuarios por perfil y estado (para aprobación de admin)
    List<Usuario> findByEstadoAndPerfilUsuario(String estado, Usuario.PerfilUsuario perfilUsuario);
    
    // Buscar usuarios pendientes (entrenadores y nutricionistas)
    @Query("SELECT u FROM Usuario u WHERE u.estado = 'I' AND u.perfilUsuario IN ('Entrenador', 'Nutricionista')")
    List<Usuario> findPendingUsers();
    
    // Contar usuarios por perfil
    long countByPerfilUsuario(Usuario.PerfilUsuario perfilUsuario);
    
    // Buscar usuarios por número de documento
    Optional<Usuario> findByNumeroDocumento(String numeroDocumento);
    
    // Verificar si el correo ya existe (para registro)
    boolean existsByCorreo(String correo);
    
    // Métodos del panel de administración
    long countByEstado(String estado);
    
    List<Usuario> findByEstadoOrderByIdDesc(String estado);
    
    List<Usuario> findByEstado(String estado);
    
    List<Usuario> findByPerfilUsuario(Usuario.PerfilUsuario perfilUsuario);
    
    // Buscar usuarios por perfil y estado
    List<Usuario> findByPerfilUsuarioAndEstado(Usuario.PerfilUsuario perfilUsuario, String estado);
    
    // Contar usuarios por perfil y estado
    long countByPerfilUsuarioAndEstado(Usuario.PerfilUsuario perfilUsuario, String estado);
    
    // Métodos para boletines informativos - segmentación
    
    // Buscar usuarios activos (con estado 'A')
    @Query("SELECT u FROM Usuario u WHERE u.estado = 'A'")
    List<Usuario> findUsuariosActivos();
    
    // Buscar solo entrenadores activos
    @Query("SELECT u FROM Usuario u WHERE u.perfilUsuario = 'Entrenador' AND u.estado = 'A'")
    List<Usuario> findEntrenadoresActivos();
    
    // Buscar solo usuarios regulares activos
    @Query("SELECT u FROM Usuario u WHERE u.perfilUsuario = 'Usuario' AND u.estado = 'A'")
    List<Usuario> findUsuariosRegularesActivos();
    
    // Buscar usuarios inactivos (estados diferentes a 'A')
    @Query("SELECT u FROM Usuario u WHERE u.estado != 'A'")
    List<Usuario> findUsuariosInactivos();
    
    // Métodos adicionales para estadísticas (usando String en lugar de enum para mayor flexibilidad)
    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.perfilUsuario = :perfil AND u.estado = :estado")
    long countByPerfilUsuarioStringAndEstado(@Param("perfil") String perfil, @Param("estado") String estado);
    
    @Query("SELECT u FROM Usuario u WHERE u.perfilUsuario = :perfil AND u.estado = :estado")
    List<Usuario> findByPerfilUsuarioStringAndEstado(@Param("perfil") String perfil, @Param("estado") String estado);
}