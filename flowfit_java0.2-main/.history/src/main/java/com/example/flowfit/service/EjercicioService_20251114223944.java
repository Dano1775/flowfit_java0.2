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
    
    public long countTotalEjercicios() {
        return ejercicioRepository.count();
    }

    public EjercicioCatalogo obtenerEjercicioPorId(Integer id) {
        return ejercicioRepository.findById(id.longValue())
            .orElseThrow(() -> new RuntimeException("Ejercicio no encontrado"));
    }

    public void guardarEjercicio(EjercicioCatalogo ejercicio) {
        ejercicioRepository.save(ejercicio);
    }

    public void eliminarEjercicio(Integer id) {
        EjercicioCatalogo ejercicio = obtenerEjercicioPorId(id);
        // Delete associated image
        if (ejercicio.getImagen() != null) {
            deleteImage(ejercicio.getImagen(), false);
        }
        ejercicioRepository.deleteById(id.longValue());
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