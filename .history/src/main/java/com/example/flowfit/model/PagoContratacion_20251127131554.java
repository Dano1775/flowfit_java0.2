package com.example.flowfit.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pago_contratacion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagoContratacion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "contratacion_id", nullable = false)
    private Long contratacionId;
    
    @Column(name = "usuario_id", nullable = false)
    private Integer usuarioId;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;
    
    @Column(length = 3)
    private String moneda = "COP";
    
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_pago", nullable = false)
    private EstadoPago estadoPago = EstadoPago.PENDIENTE;
    
    // SISTEMA DE ESCROW (Retención de pagos anti-estafa)
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_escrow", nullable = false)
    private EstadoEscrow estadoEscrow = EstadoEscrow.RETENIDO;
    
    @Column(name = "usuario_confirma_servicio")
    private Boolean usuarioConfirmaServicio = false;
    
    @Column(name = "entrenador_confirma_servicio")
    private Boolean entrenadorConfirmaServicio = false;
    
    @Column(name = "fecha_confirmacion_usuario")
    private LocalDateTime fechaConfirmacionUsuario;
    
    @Column(name = "fecha_confirmacion_entrenador")
    private LocalDateTime fechaConfirmacionEntrenador;
    
    @Column(name = "fecha_liberacion_fondos")
    private LocalDateTime fechaLiberacionFondos;
    
    @Column(name = "fecha_limite_disputa")
    private LocalDateTime fechaLimiteDisputa;
    
    // Sistema de disputas
    @Column(name = "disputa_activa")
    private Boolean disputaActiva = false;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "disputa_iniciada_por")
    private DisputaIniciadaPor disputaIniciadaPor;
    
    @Column(name = "disputa_razon", columnDefinition = "TEXT")
    private String disputaRazon;
    
    @Column(name = "disputa_fecha")
    private LocalDateTime disputaFecha;
    
    @Column(name = "disputa_resuelta")
    private Boolean disputaResuelta = false;
    
    @Column(name = "disputa_resolucion", columnDefinition = "TEXT")
    private String disputaResolucion;
    
    @Column(name = "disputa_fecha_resolucion")
    private LocalDateTime disputaFechaResolucion;
    
    // MercadoPago
    @Column(name = "mp_preference_id")
    private String mpPreferenceId;
    
    @Column(name = "mp_payment_id")
    private String mpPaymentId;
    
    @Column(name = "mp_init_point", columnDefinition = "TEXT")
    private String mpInitPoint;
    
    @Column(name = "mp_external_reference")
    private String mpExternalReference;
    
    @Column(name = "mp_status", length = 50)
    private String mpStatus;
    
    @Column(name = "mp_status_detail", length = 100)
    private String mpStatusDetail;
    
    @Column(name = "mp_payment_method", length = 50)
    private String mpPaymentMethod;
    
    @Column(name = "mp_payment_type", length = 50)
    private String mpPaymentType;
    
    @Column(name = "mp_webhook_data", columnDefinition = "JSON")
    private String mpWebhookData;
    
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();
    
    @Column(name = "fecha_expiracion")
    private LocalDateTime fechaExpiracion;
    
    @Column(name = "fecha_pago")
    private LocalDateTime fechaPago;
    
    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion = LocalDateTime.now();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contratacion_id", insertable = false, updatable = false)
    private ContratacionEntrenador contratacion;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", insertable = false, updatable = false)
    private Usuario usuario;
    
    @PreUpdate
    protected void onUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }
    
    public enum EstadoPago {
        PENDIENTE,
        PROCESANDO,
        APROBADO,
        RECHAZADO,
        CANCELADO,
        REEMBOLSADO,
        EN_MEDIACION,
        EXPIRADO
    }
    
    public enum EstadoEscrow {
        RETENIDO,              // Dinero retenido esperando confirmaciones
        ESPERANDO_USUARIO,     // Esperando confirmación del usuario
        ESPERANDO_ENTRENADOR,  // Esperando confirmación del entrenador
        DISPUTA,               // Hay una disputa activa
        LIBERADO,              // Dinero liberado al entrenador
        REEMBOLSADO            // Dinero devuelto al usuario
    }
    
    public enum DisputaIniciadaPor {
        USUARIO,
        ENTRENADOR,
        ADMIN
    }
}
