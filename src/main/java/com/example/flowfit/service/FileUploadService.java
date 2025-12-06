package com.example.flowfit.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Service
public class FileUploadService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    // Tipos de archivo permitidos
    private static final Set<String> ALLOWED_IMAGE_TYPES = new HashSet<>(Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"));

    private static final Set<String> ALLOWED_VIDEO_TYPES = new HashSet<>(Arrays.asList(
            "video/mp4", "video/webm", "video/ogg", "video/avi", "video/mov"));

    private static final Set<String> ALLOWED_DOCUMENT_TYPES = new HashSet<>(Arrays.asList(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "text/plain"));

    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB

    /**
     * Sube un archivo y retorna la URL relativa
     */
    public Map<String, Object> uploadFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }

        // Validar tipo de archivo
        String contentType = file.getContentType();
        if (!isAllowedFileType(contentType)) {
            throw new IllegalArgumentException("Tipo de archivo no permitido: " + contentType);
        }

        // Validar tamaño
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("El archivo excede el tamaño máximo permitido (50MB)");
        }

        // Crear directorio si no existe
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generar nombre único para el archivo
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uniqueFilename = UUID.randomUUID().toString() + extension;

        // Guardar archivo
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Retornar información del archivo
        Map<String, Object> fileInfo = new HashMap<>();
        fileInfo.put("url", "/chat_uploads/" + uniqueFilename);
        fileInfo.put("nombre", originalFilename);
        fileInfo.put("tipo", contentType);
        fileInfo.put("tamano", file.getSize());
        fileInfo.put("tipoCategoria", getFileCategory(contentType));

        return fileInfo;
    }

    /**
     * Valida si el tipo de archivo está permitido
     */
    private boolean isAllowedFileType(String contentType) {
        if (contentType == null)
            return false;
        return ALLOWED_IMAGE_TYPES.contains(contentType) ||
                ALLOWED_VIDEO_TYPES.contains(contentType) ||
                ALLOWED_DOCUMENT_TYPES.contains(contentType);
    }

    /**
     * Obtiene la categoría del archivo (IMAGEN, VIDEO, ARCHIVO)
     */
    private String getFileCategory(String contentType) {
        if (ALLOWED_IMAGE_TYPES.contains(contentType)) {
            return "IMAGEN";
        } else if (ALLOWED_VIDEO_TYPES.contains(contentType)) {
            return "VIDEO";
        } else {
            return "ARCHIVO";
        }
    }

    /**
     * Elimina un archivo
     */
    public void deleteFile(String fileUrl) throws IOException {
        if (fileUrl != null && fileUrl.startsWith("/chat_uploads/")) {
            String filename = fileUrl.substring("/chat_uploads/".length());
            Path filePath = Paths.get(uploadDir).resolve(filename);
            Files.deleteIfExists(filePath);
        }
    }

    /**
     * Valida extensión de archivo
     */
    public boolean isValidExtension(String filename) {
        if (filename == null)
            return false;
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        return Arrays.asList("jpg", "jpeg", "png", "gif", "webp", "mp4", "webm", "ogg", "avi", "mov", "pdf", "doc",
                "docx", "xls", "xlsx", "txt").contains(extension);
    }
}
