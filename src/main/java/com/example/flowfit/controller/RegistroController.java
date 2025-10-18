package com.example.flowfit.controller;

import com.example.flowfit.model.Usuario;
import com.example.flowfit.service.UsuarioService;
import com.example.flowfit.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RegistroController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private EmailService emailService;

    /**
     * Muestra la página principal de registro (Usuario)
     */
    @GetMapping("/registro")
    public String registroPage(@RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "success", required = false) String success,
            Model model) {
        if (error != null) {
            model.addAttribute("error", error);
        }
        if (success != null) {
            model.addAttribute("success", success);
        }
        return "registro";
    }

    /**
     * Muestra la página de registro de entrenador
     */
    @GetMapping("/registro/entrenador")
    public String registroEntrenadorPage(@RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "success", required = false) String success,
            Model model) {
        if (error != null) {
            model.addAttribute("error", error);
        }
        if (success != null) {
            model.addAttribute("success", success);
        }
        return "registro-entrenador";
    }

    /**
     * Procesa el registro de usuario
     */
    @PostMapping("/registro")
    public String processRegistro(@RequestParam("numeroDocumento") String numeroDocumento,
            @RequestParam("nombre") String nombre,
            @RequestParam("telefono") String telefono,
            @RequestParam("correo") String correo,
            @RequestParam("clave") String clave,
            @RequestParam("perfil_usuario") String perfilUsuarioStr,
            Model model) {

        // Validar campos requeridos
        if (numeroDocumento.isEmpty() || nombre.isEmpty() || telefono.isEmpty() ||
                correo.isEmpty() || clave.isEmpty() || perfilUsuarioStr.isEmpty()) {
            model.addAttribute("error", "Faltan datos en el formulario");
            model.addAttribute("numeroDocumento", numeroDocumento);
            model.addAttribute("nombre", nombre);
            model.addAttribute("telefono", telefono);
            model.addAttribute("correo", correo);
            return "registro";
        }

        // Convertir string a enum
        Usuario.PerfilUsuario perfilUsuario;
        try {
            perfilUsuario = Usuario.PerfilUsuario.valueOf(perfilUsuarioStr);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", "Perfil de usuario inválido");
            return "registro";
        }

        // Registrar usuario usando el servicio
        UsuarioService.RegistrationResult result = usuarioService.register(
                numeroDocumento, nombre, telefono, correo, clave, perfilUsuario);

        if (!result.isSuccess()) {
            // Registro fallido - regresar al formulario con error
            model.addAttribute("error", result.getMessage());
            model.addAttribute("numeroDocumento", numeroDocumento);
            model.addAttribute("nombre", nombre);
            model.addAttribute("telefono", telefono);
            model.addAttribute("correo", correo);
            return "registro";
        }

        // Enviar correo de bienvenida
        try {
            String tipoUsuario = perfilUsuario == Usuario.PerfilUsuario.Usuario ? "Usuario" : perfilUsuario.name();
            emailService.enviarCorreoBienvenida(correo, nombre, tipoUsuario);
        } catch (Exception e) {
            // Si falla el correo, continuar (no afecta el registro)
            System.err.println("Error al enviar correo de bienvenida: " + e.getMessage());
        }

        // Registro exitoso - redirigir con mensaje de éxito
        if (perfilUsuario == Usuario.PerfilUsuario.Usuario) {
            return "redirect:/login?success=" +
                    "¡Registro exitoso! Ya puedes iniciar sesión.";
        } else {
            return "redirect:/login?success=" +
                    "Tu solicitud de registro fue enviada correctamente. Espera a que un administrador apruebe tu cuenta.";
        }
    }

    /**
     * Procesa el registro de entrenador
     */
    @PostMapping("/registro/entrenador")
    public String processRegistroEntrenador(@RequestParam("numeroDocumento") String numeroDocumento,
            @RequestParam("nombre") String nombre,
            @RequestParam("telefono") String telefono,
            @RequestParam("correo") String correo,
            @RequestParam("clave") String clave,
            Model model) {

        // Validar campos requeridos
        if (numeroDocumento.isEmpty() || nombre.isEmpty() || telefono.isEmpty() ||
                correo.isEmpty() || clave.isEmpty()) {
            model.addAttribute("error", "Faltan datos en el formulario");
            model.addAttribute("numeroDocumento", numeroDocumento);
            model.addAttribute("nombre", nombre);
            model.addAttribute("telefono", telefono);
            model.addAttribute("correo", correo);
            return "registro-entrenador";
        }

        // Registrar entrenador (estado será 'I' inactivo, necesita aprobación)
        UsuarioService.RegistrationResult result = usuarioService.register(
                numeroDocumento, nombre, telefono, correo, clave, Usuario.PerfilUsuario.Entrenador);

        if (!result.isSuccess()) {
            // Registro fallido
            model.addAttribute("error", result.getMessage());
            model.addAttribute("numeroDocumento", numeroDocumento);
            model.addAttribute("nombre", nombre);
            model.addAttribute("telefono", telefono);
            model.addAttribute("correo", correo);
            return "registro-entrenador";
        }

        // Enviar correo de bienvenida
        try {
            emailService.enviarCorreoBienvenida(correo, nombre, "Entrenador");
        } catch (Exception e) {
            // Si falla el correo, continuar (no afecta el registro)
            System.err.println("Error al enviar correo de bienvenida: " + e.getMessage());
        }

        // Registro exitoso - redirigir con mensaje de éxito
        return "redirect:/login?success=" +
                "Tu solicitud de registro fue enviada correctamente. Espera a que un administrador apruebe tu cuenta.";
    }
}