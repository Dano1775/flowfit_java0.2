package com.example.flowfit.controller;

import com.example.flowfit.model.Usuario;
import com.example.flowfit.service.UsuarioService;
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

    /**
     * Show main registration page (Usuario)
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
        return "registro"; // returns templates/registro.html
    }

    /**
     * Show trainer registration page
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
        return "registro-entrenador"; // returns templates/registro-entrenador.html
    }

    /**
     * Process user registration - same logic as PHP registro_controller.php
     */
    @PostMapping("/registro")
    public String processRegistro(@RequestParam("numeroDocumento") String numeroDocumento,
                                 @RequestParam("nombre") String nombre,
                                 @RequestParam("telefono") String telefono,
                                 @RequestParam("correo") String correo,
                                 @RequestParam("clave") String clave,
                                 @RequestParam("perfil_usuario") String perfilUsuarioStr,
                                 Model model) {
        
        // Validate required fields (same as PHP validation)
        if (numeroDocumento.isEmpty() || nombre.isEmpty() || telefono.isEmpty() || 
            correo.isEmpty() || clave.isEmpty() || perfilUsuarioStr.isEmpty()) {
            model.addAttribute("error", "Faltan datos en el formulario");
            model.addAttribute("numeroDocumento", numeroDocumento);
            model.addAttribute("nombre", nombre);
            model.addAttribute("telefono", telefono);
            model.addAttribute("correo", correo);
            return "registro";
        }

        // Convert string to enum
        Usuario.PerfilUsuario perfilUsuario;
        try {
            perfilUsuario = Usuario.PerfilUsuario.valueOf(perfilUsuarioStr);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", "Perfil de usuario inválido");
            return "registro";
        }

        // Register user using service (same logic as PHP)
        UsuarioService.RegistrationResult result = usuarioService.register(
            numeroDocumento, nombre, telefono, correo, clave, perfilUsuario
        );

        if (!result.isSuccess()) {
            // Registration failed - return to form with error
            model.addAttribute("error", result.getMessage());
            model.addAttribute("numeroDocumento", numeroDocumento);
            model.addAttribute("nombre", nombre);
            model.addAttribute("telefono", telefono);
            model.addAttribute("correo", correo);
            return "registro";
        }

        // Registration successful - redirect with success message (same as PHP logic)
        if (perfilUsuario == Usuario.PerfilUsuario.Usuario) {
            return "redirect:/login?success=" + 
                   "¡Registro exitoso! Ya puedes iniciar sesión.";
        } else {
            return "redirect:/login?success=" + 
                   "Tu solicitud de registro fue enviada correctamente. Espera a que un administrador apruebe tu cuenta.";
        }
    }

    /**
     * Process trainer registration
     */
    @PostMapping("/registro/entrenador")
    public String processRegistroEntrenador(@RequestParam("numeroDocumento") String numeroDocumento,
                                           @RequestParam("nombre") String nombre,
                                           @RequestParam("telefono") String telefono,
                                           @RequestParam("correo") String correo,
                                           @RequestParam("clave") String clave,
                                           Model model) {
        
        // Validate required fields
        if (numeroDocumento.isEmpty() || nombre.isEmpty() || telefono.isEmpty() || 
            correo.isEmpty() || clave.isEmpty()) {
            model.addAttribute("error", "Faltan datos en el formulario");
            model.addAttribute("numeroDocumento", numeroDocumento);
            model.addAttribute("nombre", nombre);
            model.addAttribute("telefono", telefono);
            model.addAttribute("correo", correo);
            return "registro-entrenador";
        }

        // Register trainer (status will be 'I' for inactive, needs approval)
        UsuarioService.RegistrationResult result = usuarioService.register(
            numeroDocumento, nombre, telefono, correo, clave, Usuario.PerfilUsuario.Entrenador
        );

        if (!result.isSuccess()) {
            // Registration failed
            model.addAttribute("error", result.getMessage());
            model.addAttribute("numeroDocumento", numeroDocumento);
            model.addAttribute("nombre", nombre);
            model.addAttribute("telefono", telefono);
            model.addAttribute("correo", correo);
            return "registro-entrenador";
        }

        // Registration successful - redirect with success message
        return "redirect:/login?success=" + 
               "Tu solicitud de registro fue enviada correctamente. Espera a que un administrador apruebe tu cuenta.";
    }
}