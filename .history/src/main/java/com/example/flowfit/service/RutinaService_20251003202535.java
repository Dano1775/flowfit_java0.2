package com.example.flowfit.service;

import com.example.flowfit.model.Rutina;
import com.example.flowfit.model.RutinaAsignada;
import com.example.flowfit.model.RutinaEjercicio;
import com.example.flowfit.model.RutinaAsignada.EstadoRutina;
import com.example.flowfit.dto.EjercicioRutinaDto;
import com.example.flowfit.repository.RutinaRepository;
import com.example.flowfit.repository.RutinaAsignadaRepository;
import com.example.flowfit.repository.RutinaEjercicioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

@Service
@Transactional
public class RutinaService {

    @Autowired
    private RutinaRepository rutinaRepository;
    
    @Autowired
    private RutinaAsignadaRepository rutinaAsignadaRepository;
    
    @Autowired
    private RutinaEjercicioRepository rutinaEjercicioRepository;

    // ===== GESTIÓN DE RUTINAS =====
    
    public List<Rutina> obtenerRutinasGlobales() {
        return rutinaRepository.findRutinasGlobalesOrdenadas();
    }
    
    public List<Rutina> obtenerRutinasPopulares(int limite) {
        return rutinaRepository.findRutinasPopulares(limite);
    }
    
    public List<Rutina> obtenerRutinasPorEntrenador(Integer entrenadorId) {
        return rutinaRepository.findByEntrenadorId(entrenadorId);
    }
    
    public Optional<Rutina> obtenerRutinaPorId(Integer id) {
        return rutinaRepository.findById(id);
    }
    
    public List<Rutina> buscarRutinasPorNombre(String nombre) {
        return rutinaRepository.findByNombreContainingIgnoreCase(nombre);
    }

    // ===== ASIGNACIÓN DE RUTINAS =====
    
    public RutinaAsignada asignarRutinaAUsuario(Integer rutinaId, Integer usuarioId) {
        // Verificar que la rutina existe
        rutinaRepository.findById(rutinaId)
            .orElseThrow(() -> new RuntimeException("Rutina no encontrada"));
        
        // Verificar que el usuario no tenga ya esta rutina asignada y activa
        Optional<RutinaAsignada> existente = rutinaAsignadaRepository
            .findByUsuarioIdAndRutinaId(usuarioId, rutinaId);
            
        if (existente.isPresent() && existente.get().getEstado() == EstadoRutina.ACTIVA) {
            throw new RuntimeException("El usuario ya tiene esta rutina activa");
        }
        
        // Crear nueva asignación
        RutinaAsignada nuevaAsignacion = new RutinaAsignada();
        nuevaAsignacion.setRutinaId(rutinaId);
        nuevaAsignacion.setUsuarioId(usuarioId);
        nuevaAsignacion.setFechaAsignacion(LocalDate.now());
        nuevaAsignacion.setEstado(EstadoRutina.ACTIVA);
        nuevaAsignacion.setProgreso(0);
        nuevaAsignacion.setUltimaActividad(LocalDate.now());
        nuevaAsignacion.setVecesCompletada(0);
        
        return rutinaAsignadaRepository.save(nuevaAsignacion);
    }
    
    public List<RutinaAsignada> obtenerRutinasDelUsuario(Integer usuarioId) {
        return rutinaAsignadaRepository.findByUsuarioIdOrderByFechaAsignacionDesc(usuarioId);
    }
    
    public List<RutinaAsignada> obtenerRutinasActivasDelUsuario(Integer usuarioId) {
        return rutinaAsignadaRepository.findByUsuarioIdAndEstado(usuarioId, EstadoRutina.ACTIVA);
    }
    
    public List<RutinaAsignada> obtenerRutinasCompletadasDelUsuario(Integer usuarioId) {
        return rutinaAsignadaRepository.findByUsuarioIdAndEstado(usuarioId, EstadoRutina.COMPLETADA);
    }

    // ===== PROGRESO Y ESTADÍSTICAS =====
    
    public void actualizarProgresoRutina(Integer rutinaAsignadaId, int progreso) {
        RutinaAsignada rutina = rutinaAsignadaRepository.findById(rutinaAsignadaId)
            .orElseThrow(() -> new RuntimeException("Rutina asignada no encontrada"));
        
        rutina.actualizarProgreso(progreso);
        rutinaAsignadaRepository.save(rutina);
    }
    
    public void marcarRutinaComoCompletada(Integer rutinaAsignadaId) {
        RutinaAsignada rutina = rutinaAsignadaRepository.findById(rutinaAsignadaId)
            .orElseThrow(() -> new RuntimeException("Rutina asignada no encontrada"));
        
        rutina.marcarComoCompletada();
        rutinaAsignadaRepository.save(rutina);
    }
    
    public Double calcularProgresoGeneralUsuario(Integer usuarioId) {
        Double progreso = rutinaAsignadaRepository.calcularProgresoGeneral(usuarioId);
        return progreso != null ? progreso : 0.0;
    }
    
    public Object[] obtenerEstadisticasUsuario(Integer usuarioId) {
        return rutinaAsignadaRepository.getEstadisticasUsuario(usuarioId);
    }

    // ===== EJERCICIOS DE RUTINAS =====
    
    public List<RutinaEjercicio> obtenerEjerciciosDeRutina(Integer rutinaId) {
        return rutinaEjercicioRepository.findByRutinaIdOrderByOrdenAsc(rutinaId);
    }
    
    // Comentado temporalmente debido a problemas con el campo id en la tabla
    /*
    public List<RutinaEjercicio> obtenerEjerciciosConDetalles(Integer rutinaId) {
        return rutinaEjercicioRepository.findEjerciciosConDetalles(rutinaId);
    }
    */
    
    public List<EjercicioRutinaDto> obtenerEjerciciosConDetallesDto(Integer rutinaId) {
        List<Object[]> results = rutinaEjercicioRepository.findEjerciciosConDetallesNativo(rutinaId);
        List<EjercicioRutinaDto> ejercicios = new ArrayList<>();
        
        for (Object[] row : results) {
            EjercicioRutinaDto dto = new EjercicioRutinaDto();
            dto.setRutinaId((Integer) row[0]);
            dto.setEjercicioId((Integer) row[1]);
            dto.setOrden((Integer) row[2]);
            dto.setSeries((Integer) row[3]);
            dto.setRepeticiones((Integer) row[4]);
            dto.setDuracionSegundos((Integer) row[5]);
            dto.setDescansoSegundos((Integer) row[6]);
            dto.setPesoKg((Double) row[7]);
            dto.setNotas((String) row[8]);
            dto.setEjercicioNombre((String) row[9]);
            dto.setEjercicioDescripcion((String) row[10]);
            dto.setEjercicioImagen((String) row[11]);
            
            ejercicios.add(dto);
        }
        
        return ejercicios;
    }

    // ===== RUTINAS PARA HOY =====
    
    public List<RutinaAsignada> obtenerRutinasParaHoy(Integer usuarioId) {
        return rutinaAsignadaRepository.findRutinasParaHoy(
            usuarioId, EstadoRutina.ACTIVA, LocalDate.now());
    }

    // ===== RUTINAS DISPONIBLES PARA USUARIO =====
    
    public List<Rutina> obtenerRutinasDisponiblesParaUsuario(Integer entrenadorId) {
        if (entrenadorId != null) {
            return rutinaRepository.findRutinasDisponiblesParaUsuario(entrenadorId);
        } else {
            return rutinaRepository.findByEntrenadorIdIsNull(); // Solo rutinas globales
        }
    }

    // ===== CREACIÓN Y EDICIÓN DE RUTINAS =====
    
    public Rutina crearRutina(Rutina rutina) {
        rutina.setFechaCreacion(LocalDate.now());
        return rutinaRepository.save(rutina);
    }
    
    public Rutina actualizarRutina(Rutina rutina) {
        if (!rutinaRepository.existsById(rutina.getId())) {
            throw new RuntimeException("Rutina no encontrada");
        }
        return rutinaRepository.save(rutina);
    }
    
    @Transactional
    public void eliminarRutina(Integer rutinaId) {
        // Verificar que no tenga asignaciones activas
        long asignacionesActivas = rutinaAsignadaRepository
            .countByUsuarioIdAndEstado(null, EstadoRutina.ACTIVA);
        
        if (asignacionesActivas > 0) {
            throw new RuntimeException("No se puede eliminar una rutina con asignaciones activas");
        }
        
        // Eliminar ejercicios de la rutina
        rutinaEjercicioRepository.deleteByRutinaId(rutinaId);
        
        // Eliminar la rutina
        rutinaRepository.deleteById(rutinaId);
    }

    // ===== RUTINAS RECIENTES =====
    
    public List<RutinaAsignada> obtenerRutinasRecientes(Integer usuarioId) {
        return rutinaAsignadaRepository.findTop5ByUsuarioIdOrderByFechaAsignacionDesc(usuarioId);
    }
}