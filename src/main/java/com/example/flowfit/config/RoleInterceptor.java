package com.example.flowfit.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

import com.example.flowfit.model.Usuario;

/**
 * Interceptor simple para proteger rutas /admin/** verificando el rol en sesión
 */
public class RoleInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        if (path.startsWith(request.getContextPath() + "/admin")) {
            HttpSession session = request.getSession(false);
            if (session == null) {
                response.sendRedirect(request.getContextPath() + "/login");
                return false;
            }
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null || !"Administrador".equals(usuario.getPerfilUsuario().toString())) {
                response.sendRedirect(request.getContextPath() + "/login");
                return false;
            }
        }
        return true;
    }
}
