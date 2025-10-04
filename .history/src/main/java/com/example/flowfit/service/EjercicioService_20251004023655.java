package com.example.flowfit.service;

import com.example.flowfit.model.EjercicioCatalogo;
import com.example.flowfit.model.Usuario;
import com.example.flowfit.repository.EjercicioCatalogoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class EjercicioService {

    @Autowired
    private EjercicioCatalogoRepository ejercicioRepository;

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/src/main/resources/static/ejercicio_image_uploads/";
    private static final String USER_UPLOAD_DIR = System.getProperty("user.dir") + "/src/main/resources/static/ejercicio_image_uploads/user_uploads/";

    // Get all exercises with pagination
    public Page<EjercicioCatalogo> getAllEjercicios(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : 
                   Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        return ejercicioRepository.findAll(pageable);
    }

    // Get all exercises as list
    public List<EjercicioCatalogo> getAllEjercicios() {
        return ejercicioRepository.findAll(Sort.by(Sort.Direction.ASC, "nombre"));
    }

    // Get global exercises (created by admin)
    public List<EjercicioCatalogo> getGlobalExercicios() {
        return ejercicioRepository.findByCreadoPorIsNull();
    }
    
    // Métodos para entrenadores
    public List<EjercicioCatalogo> obtenerEjerciciosGlobales() {
        return ejercicioRepository.findByCreadoPorIsNull();
    }
    
    public List<EjercicioCatalogo> obtenerEjerciciosPorCreador(Integer creadorId) {
        Usuario creador = new Usuario();
        creador.setId(creadorId);
        return ejercicioRepository.findByCreadoPor(creador);
    }
    
    public long contarEjerciciosPorCreador(Integer creadorId) {
        Usuario creador = new Usuario();
        creador.setId(creadorId);
        return ejercicioRepository.countByCreadoPor(creador);
    }

    // Get exercises created by a specific trainer
    public List<EjercicioCatalogo> getEjerciciosByTrainer(Usuario trainer) {
        return ejercicioRepository.findByCreadoPor(trainer);
    }

    // Search exercises
    public List<EjercicioCatalogo> searchEjercicios(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllEjercicios();
        }
        return ejercicioRepository.searchByNameOrDescription(searchTerm.trim());
    }

    // Find exercise by ID
    public Optional<EjercicioCatalogo> findById(Long id) {
        return ejercicioRepository.findById(id);
    }

    // Create global exercise (admin only)
    public EjercicioCatalogo createGlobalExercise(String nombre, String descripcion, MultipartFile imagen) throws IOException {
        String imageName = saveImage(imagen, false);
        
        EjercicioCatalogo ejercicio = new EjercicioCatalogo();
        ejercicio.setNombre(nombre);
        ejercicio.setDescripcion(descripcion);
        ejercicio.setImagen(imageName);
        ejercicio.setCreadoPor(null); // Global exercise
        
        return ejercicioRepository.save(ejercicio);
    }

    // Create trainer exercise (simplified - same folder as global)
    public EjercicioCatalogo createTrainerExercise(String nombre, String descripcion, MultipartFile imagen, Usuario trainer) throws IOException {
        String imageName = saveImage(imagen, false); // Use same folder for simplicity
        
        EjercicioCatalogo ejercicio = new EjercicioCatalogo();
        ejercicio.setNombre(nombre);
        ejercicio.setDescripcion(descripcion);
        ejercicio.setImagen(imageName);
        ejercicio.setCreadoPor(trainer);
        
        return ejercicioRepository.save(ejercicio);
    }

    // Update exercise
    public EjercicioCatalogo updateExercise(Long id, String nombre, String descripcion, String tipo, MultipartFile imagen) throws IOException {
        Optional<EjercicioCatalogo> ejercicioOpt = ejercicioRepository.findById(id);
        if (ejercicioOpt.isEmpty()) {
            throw new RuntimeException("Ejercicio no encontrado");
        }
        
        EjercicioCatalogo ejercicio = ejercicioOpt.get();
        ejercicio.setNombre(nombre);
        ejercicio.setDescripcion(descripcion);
        
        // Note: tipo field is not part of the current EjercicioCatalogo model
        // Skipping tipo update for now
        
        if (imagen != null && !imagen.isEmpty()) {
            // Delete old image if exists
            deleteImage(ejercicio.getImagen(), false);
            
            // Save new image (all in same folder for simplicity)
            String imageName = saveImage(imagen, false);
            ejercicio.setImagen(imageName);
        }
        
        return ejercicioRepository.save(ejercicio);
    }

    // Delete exercise
    public void deleteExercise(Long id) {
        Optional<EjercicioCatalogo> ejercicioOpt = ejercicioRepository.findById(id);
        if (ejercicioOpt.isPresent()) {
            EjercicioCatalogo ejercicio = ejercicioOpt.get();
            // Delete associated image
            deleteImage(ejercicio.getImagen(), false);
            ejercicioRepository.deleteById(id);
        }
    }

    // Count total exercises
    public long countTotalEjercicios() {
        return ejercicioRepository.count();
    }

    // Count global exercises
    public long countGlobalEjercicios() {
        return ejercicioRepository.findByCreadoPorIsNull().size();
    }

    // Count trainer exercises
    public long countTrainerEjercicios() {
        return ejercicioRepository.count() - countGlobalEjercicios();
    }

    // Private helper methods
    private String saveImage(MultipartFile imagen, boolean isUserUpload) throws IOException {
        if (imagen == null || imagen.isEmpty()) {
            throw new IllegalArgumentException("La imagen es requerida");
        }

        // Generate unique filename
        String originalFilename = imagen.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("Nombre de archivo inválido");
        }
        
        String filename = UUID.randomUUID().toString() + "_" + originalFilename;

        // Determine upload directory
        String uploadDir = isUserUpload ? USER_UPLOAD_DIR : UPLOAD_DIR;
        Path uploadPath = Paths.get(uploadDir);
        
        // Create directory if it doesn't exist
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Save file
        Path filePath = uploadPath.resolve(filename);
        Files.copy(imagen.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filename;
    }

    private void deleteImage(String imageName, boolean isUserUpload) {
        if (imageName != null && !imageName.isEmpty()) {
            try {
                String uploadDir = isUserUpload ? USER_UPLOAD_DIR : UPLOAD_DIR;
                Path imagePath = Paths.get(uploadDir + imageName);
                Files.deleteIfExists(imagePath);
            } catch (IOException e) {
                // Log error but don't throw exception
                System.err.println("Error deleting image: " + imageName);
            }
        }
    }
}