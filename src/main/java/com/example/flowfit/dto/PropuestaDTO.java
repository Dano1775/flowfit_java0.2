package com.example.flowfit.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropuestaDTO {
    private Long contratacionId;
    private Integer planBaseId;
    private BigDecimal precio;
    private Integer duracionDias;
    private Integer rutinasMes;
    private boolean seguimientoSemanal;
    private Integer videollamadasMes;
    private boolean planNutricional;
    private boolean chatDirecto;
    private String serviciosAdicionales;
    private String mensaje;
}
