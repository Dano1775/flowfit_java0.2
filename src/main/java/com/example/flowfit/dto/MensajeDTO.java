package com.example.flowfit.dto;

import com.example.flowfit.model.Mensaje;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public class MensajeDTO {
    private Long id;
    private Long conversacionId;
    private Integer remitenteId;
    private String contenido;
    private Mensaje.TipoMensaje tipoMensaje;
    private LocalDateTime fechaEnvio;
    private Boolean leido;
    private LocalDateTime fechaLectura;
    private Boolean editado;
    private LocalDateTime fechaEdicion;
    private Boolean eliminado;
    private Map<String, Object> metadata;
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    public MensajeDTO(Mensaje mensaje) {
        this.id = mensaje.getId();
        this.conversacionId = mensaje.getConversacionId();
        this.remitenteId = mensaje.getRemitenteId();
        this.contenido = mensaje.getContenido();
        this.tipoMensaje = mensaje.getTipoMensaje();
        this.fechaEnvio = mensaje.getFechaEnvio();
        this.leido = mensaje.getLeido();
        this.fechaLectura = mensaje.getFechaLectura();
        this.editado = mensaje.getEditado();
        this.fechaEdicion = mensaje.getFechaEdicion();
        this.eliminado = mensaje.getEliminado();
        
        // Parsear metadata JSON a Map
        if (mensaje.getMetadata() != null && !mensaje.getMetadata().isEmpty()) {
            try {
                this.metadata = objectMapper.readValue(mensaje.getMetadata(), new TypeReference<Map<String, Object>>() {});
            } catch (Exception e) {
                this.metadata = new HashMap<>();
            }
        } else {
            this.metadata = new HashMap<>();
        }
    }
}
