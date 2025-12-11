package com.example.flowfit.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "mensaje")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Mensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "conversacion_id", nullable = false)
    private Long conversacionId;

    @Column(name = "remitente_id", nullable = false)
    private Integer remitenteId;

    @Column(columnDefinition = "TEXT")
    private String contenido;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_mensaje", nullable = false)
    private TipoMensaje tipoMensaje = TipoMensaje.TEXTO;

    @Column(name = "archivo_url")
    private String archivoUrl;

    @Column(name = "archivo_nombre")
    private String archivoNombre;

    @Column(name = "archivo_tipo")
    private String archivoTipo;

    @Column(name = "archivo_tamano")
    private Long archivoTamano;

    @Column(name = "fecha_envio", nullable = false)
    private LocalDateTime fechaEnvio = LocalDateTime.now();

    @Column(nullable = false)
    private Boolean leido = false;

    @Column(name = "fecha_lectura")
    private LocalDateTime fechaLectura;

    @Column(nullable = false)
    private Boolean editado = false;

    @Column(name = "fecha_edicion")
    private LocalDateTime fechaEdicion;

    @Column(nullable = false)
    private Boolean eliminado = false;

    @Column(columnDefinition = "JSON")
    private String metadata;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversacion_id", insertable = false, updatable = false)
    private Conversacion conversacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "remitente_id", insertable = false, updatable = false)
    private Usuario remitente;

    public enum TipoMensaje {
        TEXTO,
        IMAGEN,
        ARCHIVO,
        PROPUESTA_PLAN,
        ACEPTACION_PROPUESTA,
        RECHAZO_PROPUESTA,
        PAGO_GENERADO,
        PAGO_REALIZADO,
        PAGO_CONFIRMADO,
        CONTRATO_ACTIVADO,
        CONTRATO_FINALIZADO,
        CONFIRMACION_SERVICIO,
        DISPUTA_INICIADA,
        SISTEMA
    }
}
