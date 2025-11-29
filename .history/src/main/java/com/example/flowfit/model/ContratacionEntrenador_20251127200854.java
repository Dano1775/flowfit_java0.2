package com.example.flowfit.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "contratacion_entrenador")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContratacionEntrenador {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "usuario_id", nullable = false)
    private Integer usuarioId;
    
    @Column(name = "entrenador_id", nullable = false)
    private Integer entrenadorId;
    
    @Column(name = "plan_base_id")
    private Integer planBaseId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoContratacion estado = EstadoContratacion.PENDIENTE_APROBACION;
    
    @Column(name = "precio_acordado", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioAcordado;
    
    @Column(name = "descuento_aplicado", precision = 10, scale = 2)
    private BigDecimal descuentoAplicado = BigDecimal.ZERO;
    
    @Column(name = "comision_plataforma_porcentaje", precision = 5, scale = 2)
    private BigDecimal comisionPlataformaPorcentaje = new BigDecimal("10.00");
    
    @Column(name = "monto_entrenador", precision = 10, scale = 2)
    private BigDecimal montoEntrenador;
    
    @Column(name = "monto_comision", precision = 10, scale = 2)
    private BigDecimal montoComision;
    
    @Column(name = "duracion_dias_acordada", nullable = false)
    private Integer duracionDiasAcordada;
    
    @Column(name = "rutinas_mes_acordadas")
    private Integer rutinasMesAcordadas;
    
    @Column(name = "seguimiento_semanal_acordado")
    private Boolean seguimientoSemanalAcordado = false;
    
    @Column(name = "chat_directo_acordado")
    private Boolean chatDirectoAcordado = true;
    
    @Column(name = "videollamadas_mes_acordadas")
    private Integer videollamadasMesAcordadas = 0;
    
    @Column(name = "plan_nutricional_acordado")
    private Boolean planNutricionalAcordado = false;
    
    @Column(name = "servicios_adicionales", columnDefinition = "TEXT")
    private String serviciosAdicionales;
    
    @Column(name = "version_negociacion")
    private Integer versionNegociacion = 1;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "ultima_propuesta_de")
    private PropuestaDe ultimaPropuestaDe;
    
    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud = LocalDateTime.now();
    
    @Column(name = "fecha_aprobacion")
    private LocalDateTime fechaAprobacion;
    
    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio;
    
    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;
    
    @Column(name = "fecha_cancelacion")
    private LocalDateTime fechaCancelacion;
    
    @Column(name = "nota_usuario", columnDefinition = "TEXT")
    private String notaUsuario;
    
    @Column(name = "nota_entrenador", columnDefinition = "TEXT")
    private String notaEntrenador;
    
    @Column(name = "razon_cancelacion", columnDefinition = "TEXT")
    private String razonCancelacion;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", insertable = false, updatable = false)
    private Usuario usuario;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entrenador_id", insertable = false, updatable = false)
    private Usuario entrenador;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_base_id", insertable = false, updatable = false)
    private PlanEntrenador planBase;
    
    @OneToMany(mappedBy = "contratacionId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<HistorialNegociacion> historialNegociaciones;
    
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "id", referencedColumnName = "contratacion_id", insertable = false, updatable = false)
    private PagoContratacion pago;
    
    public enum EstadoContratacion {
        PENDIENTE_APROBACION,
        NEGOCIACION,
        PENDIENTE_PAGO,
        PAGO_PROCESANDO,
        ACTIVA,
        PAUSADA,
        VENCIDA,
        CANCELADA,
        RECHAZADA
    }
    
    public enum PropuestaDe {
        USUARIO,
        ENTRENADOR
    }
}
