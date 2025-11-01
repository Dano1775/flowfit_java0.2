package com.example.flowfit.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.flowfit.model.Usuario;
import com.example.flowfit.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioGroupService {

    private final UsuarioRepository usuarioRepository;

    public int procesarArchivo(MultipartFile file) throws Exception {
        int contador = 0;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String linea;
            boolean primeraLinea = true;

            while ((linea = br.readLine()) != null) {
                // Saltar encabezado si existe
                if (primeraLinea && linea.toLowerCase().contains("numero_documento")) {
                    primeraLinea = false;
                    continue;
                }
                primeraLinea = false;

                String[] datos = linea.split(",");
                if (datos.length < 6) continue;

                Usuario usuario = new Usuario();
                usuario.setNumeroDocumento(datos[0].trim());
                usuario.setNombre(datos[1].trim());
                usuario.setTelefono(datos[2].trim());
                usuario.setCorreo(datos[3].trim());
                usuario.setClave(datos[4].trim());
                usuario.setPerfilUsuario(Usuario.PerfilUsuario.valueOf(datos[5].trim()));
                usuario.setEstado("A");

                usuarioRepository.save(usuario);
                contador++;
            }
        }

        return contador;
    }
}
