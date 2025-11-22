package com.example.flowfit.service;

import com.example.flowfit.model.Usuario;
import com.example.flowfit.repository.UsuarioRepository;
import com.example.flowfit.repository.EjercicioCatalogoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.List;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EjercicioCatalogoRepository ejercicioCatalogoRepository;
    
    @Autowired
    private EmailService emailService;

    /**
     * Autenticar inicio de sesión del usuario - misma lógica que login_controller de PHP
     */
    public LoginResult login(String correo, String clave) {
        try {
            // Buscar usuario por correo electrónico
            Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreo(correo);
            
            // Usuario no encontrado o contraseña incorrecta
            if (usuarioOpt.isEmpty() || !usuarioOpt.get().getClave().equals(clave)) {
                return new LoginResult(false, "Credenciales incorrectas o inexistentes", null);
            }
            
            Usuario usuario = usuarioOpt.get();
            
            // Verificar si la cuenta está pendiente de aprobación (estado = 'I')
            if ("I".equals(usuario.getEstado())) {
                return new LoginResult(false, "Tu cuenta aún no ha sido aprobada por el administrador.", null);
            }
            
            // Verificar si la cuenta fue rechazada (estado = 'R')
            if ("R".equals(usuario.getEstado())) {
                return new LoginResult(false, "Tu solicitud fue rechazada por el administrador.", null);
            }
            
            // Verificar si la cuenta está activa (estado = 'A')
            if (!"A".equals(usuario.getEstado())) {
                return new LoginResult(false, "Tu cuenta no está activa. Contacta al administrador.", null);
            }
            
            // Inicio de sesión exitoso
            return new LoginResult(true, "Login exitoso", usuario);
            
        } catch (Exception e) {
            // Error de base de datos
            return new LoginResult(false, "Error del servidor, intente más tarde", null);
        }
    }

    /**
     * Registrar nuevo usuario - misma lógica que registro_controller de PHP
     */
    public RegistrationResult register(String numeroDocumento, String nombre, String telefono, 
                                     String correo, String clave, Usuario.PerfilUsuario perfilUsuario) {
        try {
            // Verificar si el correo ya existe
            if (usuarioRepository.existsByCorreo(correo)) {
                return new RegistrationResult(false, "Ya existe un usuario con ese correo", null);
            }

            // Crear nuevo usuario
            Usuario usuario = new Usuario();
            usuario.setNumeroDocumento(numeroDocumento);
            usuario.setNombre(nombre);
            usuario.setTelefono(telefono);
            usuario.setCorreo(correo);
            usuario.setClave(clave); // Texto plano como en la versión PHP
            usuario.setPerfilUsuario(perfilUsuario);
            
            // Establecer estado basado en el perfil
            if (perfilUsuario == Usuario.PerfilUsuario.Usuario) {
                usuario.setEstado("A"); // Activo inmediatamente
            } else {
                usuario.setEstado("I"); // Inactivo, necesita aprobación
            }

            // Guardar usuario
            Usuario savedUser = usuarioRepository.save(usuario);
            
            // Enviar correo de bienvenida basado en el perfil
            try {
                String tipoUsuario = perfilUsuario == Usuario.PerfilUsuario.Entrenador ? "Entrenador" : "Usuario";
                emailService.enviarCorreoBienvenida(
                    correo, 
                    nombre, 
                    tipoUsuario
                );
            } catch (Exception e) {
                // Si falla el correo, continuar igual (no interrumpir el registro)
                System.err.println("Error al enviar correo de bienvenida: " + e.getMessage());
            }
            
            return new RegistrationResult(true, "Registro exitoso", savedUser);
            
        } catch (Exception e) {
            return new RegistrationResult(false, "Error del servidor: " + e.getMessage(), null);
        }
    }

    /**
     * Obtener todos los usuarios
     */
    public List<Usuario> obtenerUsuarios() {
        return usuarioRepository.findAll();
    }

    /**
     * Obtener URL de redirección basada en el perfil del usuario - misma lógica que el switch de PHP
     */
    public String getRedirectUrl(Usuario.PerfilUsuario perfil) {
        switch (perfil) {
            case Administrador:
                return "/admin/dashboard";
            case Entrenador:
                return "/entrenador/dashboard";
            case Nutricionista:
                // Funcionalidad temporalmente deshabilitada
                return "/login?error=Funcionalidad de nutricionista en desarrollo";
            case Usuario:
                return "/usuario/dashboard";
            default:
                return "/login?error=Perfil no reconocido";
        }
    }

    /**
     * Clase de resultado de inicio de sesión para encapsular la respuesta del login
     */
    public static class LoginResult {
        private final boolean success;
        private final String message;
        private final Usuario usuario;

        public LoginResult(boolean success, String message, Usuario usuario) {
            this.success = success;
            this.message = message;
            this.usuario = usuario;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Usuario getUsuario() { return usuario; }
    }

    /**
     * Clase de resultado de registro para encapsular la respuesta del registro
     */
    public static class RegistrationResult {
        private final boolean success;
        private final String message;
        private final Usuario usuario;

        public RegistrationResult(boolean success, String message, Usuario usuario) {
            this.success = success;
            this.message = message;
            this.usuario = usuario;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Usuario getUsuario() { return usuario; }
    }

    /**
     * Métodos de estadísticas del panel de administración
     */
    public long contarUsuariosPendientes() {
        return usuarioRepository.countByEstado("I");
    }

    public long contarTotalUsuarios() {
        return usuarioRepository.count();
    }

    public long contarTotalEjercicios() {
        return ejercicioCatalogoRepository.count();
    }

    public List<Usuario> obtenerUsuariosPendientes() {
        return usuarioRepository.findByEstadoOrderByIdDesc("I");
    }

    /**
     * Métodos de gestión de perfiles
     */
    public Usuario buscarPorId(Integer id) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        return usuarioOpt.orElse(null);
    }
    
    public Optional<Usuario> buscarPorCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo);
    }

    public Usuario guardarUsuario(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }
    
    /**
     * Obtener todos los usuarios para gestión
     */
    public List<Usuario> obtenerTodosLosUsuarios() {
        return usuarioRepository.findAll();
    }

    public Usuario obtenerUsuarioPorId(Integer id) {
        return usuarioRepository.findById(id).orElse(null);
    }
    
    /**
     * Buscar usuario por ID - retorna Optional para consistencia
     */
    public Optional<Usuario> findById(Integer id) {
        return usuarioRepository.findById(id);
    }
    
    /**
     * Eliminar usuario permanentemente del sistema
     */
    public void eliminarUsuario(Integer id) {
        usuarioRepository.deleteById(id);
    }
}