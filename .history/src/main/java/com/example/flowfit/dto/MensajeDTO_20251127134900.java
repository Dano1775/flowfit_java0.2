package com.example.flowfit.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MensajeDTO {
    private Long conversacionId;
    private String contenido;
    private String tipoMensaje;
    private String metadata;
}
