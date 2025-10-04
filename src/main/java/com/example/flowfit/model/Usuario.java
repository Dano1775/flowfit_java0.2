package com.example.flowfit.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


@Entity
@Table(name = "usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "numero_documento", nullable = false, length = 20)
    private String numeroDocumento;
    
    @Column(nullable = false, length = 100)
    private String nombre;
    
    @Column(nullable = false, length = 20)
    private String telefono;
    
    @Column(nullable = false, unique = true, length = 100)
    private String correo;
    
    @Column(nullable = false, length = 100)
    private String clave;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "perfil_usuario", nullable = false)
    private PerfilUsuario perfilUsuario;
    
    @Column(nullable = false, length = 1)
    private String estado = "A";
    
    public enum PerfilUsuario {
        Usuario, Administrador, Entrenador, Nutricionista
    }
}