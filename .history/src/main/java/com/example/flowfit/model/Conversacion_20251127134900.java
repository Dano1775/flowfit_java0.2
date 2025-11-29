package com.example.flowfit.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "conversacion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Conversacion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "usuario_id", nullable = false)
    private Integer usuarioId;
    
    @Column(name = "entrenador_id", nullable = false)
    private Integer entrenadorId;
    
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();
    
    @Column(name = "fecha_ultimo_mensaje", nullable = false)
    private LocalDateTime fechaUltimoMensaje = LocalDateTime.now();
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoConversacion estado = EstadoConversacion.ACTIVA;
    
    @Column(name = "usuario_ultimo_leido_id")
    private Long usuarioUltimoLeidoId;
    
    @Column(name = "entrenador_ultimo_leido_id")
    private Long entrenadorUltimoLeidoId;
    
    @Column(name = "mensajes_no_leidos_usuario")
    private Integer mensajesNoLeidosUsuario = 0;
    
    @Column(name = "mensajes_no_leidos_entrenador")
    private Integer mensajesNoLeidosEntrenador = 0;
    
    @OneToMany(mappedBy = "conversacionId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Mensaje> mensajes;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", insertable = false, updatable = false)
    private Usuario usuario;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entrenador_id", insertable = false, updatable = false)
    private Usuario entrenador;
    
    public enum EstadoConversacion {
        ACTIVA,
        ARCHIVADA,
        BLOQUEADA
    }
}
