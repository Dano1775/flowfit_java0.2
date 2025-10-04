package com.example.flowfit.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "historial_ediciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistorialEdiciones {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @ManyToOne
    @JoinColumn(name = "usuario_editado_id", nullable = false)
    private Usuario usuarioEditado;
    
    @ManyToOne
    @JoinColumn(name = "admin_editor_id", nullable = false)
    private Usuario adminEditor;
    
    @Column(name = "campo_editado", nullable = false)
    private String campoEditado;
    
    @Column(name = "valor_anterior", columnDefinition = "TEXT")
    private String valorAnterior;
    
    @Column(name = "valor_nuevo", columnDefinition = "TEXT")
    private String valorNuevo;
    
    @Column(name = "fecha_edicion", nullable = false)
    private LocalDateTime fechaEdicion = LocalDateTime.now();
    
    @Column(name = "motivo_cambio", columnDefinition = "TEXT")
    private String motivoCambio;
    
    @Column(name = "ip_origen")
    private String ipOrigen;
    
    // Enumeration for field types
    public enum CampoEditado {
        NOMBRE("Nombre"),
        CORREO("Correo Electrónico"),
        TELEFONO("Teléfono"),
        NUMERO_DOCUMENTO("Número de Documento"),
        PERFIL_USUARIO("Perfil de Usuario"),
        ESTADO("Estado"),
        CONTRASEÑA("Contraseña"),
        FOTO_PERFIL("Foto de Perfil");
        
        private final String descripcion;
        
        CampoEditado(String descripcion) {
            this.descripcion = descripcion;
        }
        
        public String getDescripcion() {
            return descripcion;
        }
    }
}