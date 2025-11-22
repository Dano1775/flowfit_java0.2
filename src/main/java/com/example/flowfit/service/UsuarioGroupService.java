package com.example.flowfit.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.flowfit.model.Usuario;
import com.example.flowfit.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UsuarioGroupService {

    private final UsuarioRepository usuarioRepository;

    /**
     * Procesa archivos CSV o Excel para registrar usuarios masivamente
     * @param file archivo a procesar (.csv, .xlsx, .xls)
     * @return cantidad de usuarios registrados
     * @throws Exception si hay error en el procesamiento
     */
    public int procesarArchivo(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }

        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null) {
            throw new IllegalArgumentException("Nombre de archivo no válido");
        }

        String fileName = originalFileName.toLowerCase();
        log.info("Procesando archivo: {}", originalFileName);

        if (fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) {
            return procesarExcel(file);
        } else if (fileName.endsWith(".csv")) {
            return procesarCSV(file);
        } else {
            throw new IllegalArgumentException("Formato de archivo no soportado. Use .xlsx, .xls o .csv");
        }
    }

    /**
     * Procesa archivos Excel (.xlsx, .xls)
     */
    private int procesarExcel(MultipartFile file) throws Exception {
        int contador = 0;
        Workbook workbook = null;

        try {
            String fileName = file.getOriginalFilename();
            if (fileName != null && fileName.endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(file.getInputStream());
            } else {
                workbook = new HSSFWorkbook(file.getInputStream());
            }

            Sheet sheet = workbook.getSheetAt(0);
            log.info("Procesando hoja Excel con {} filas", sheet.getLastRowNum());

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) continue;

                try {
                    String numeroDocumento = getCellValueAsString(row, 0).trim();
                    String nombre = getCellValueAsString(row, 1).trim();
                    String telefono = getCellValueAsString(row, 2).trim();
                    String correo = getCellValueAsString(row, 3).trim();
                    String clave = getCellValueAsString(row, 4).trim();
                    String perfil = getCellValueAsString(row, 5).trim();

                    // Validar campos obligatorios
                    if (numeroDocumento.isEmpty() || nombre.isEmpty()) {
                        log.debug("Fila {} ignorada por campos vacíos", rowIndex + 1);
                        continue;
                    }

                    // Crear usuario
                    Usuario usuario = crearUsuario(numeroDocumento, nombre, telefono, correo, clave, perfil);
                    usuarioRepository.save(usuario);
                    contador++;

                    log.debug("Usuario registrado: {} - {}", numeroDocumento, nombre);

                } catch (Exception e) {
                    log.error("Error procesando fila {}: {}", rowIndex + 1, e.getMessage(), e);
                }
            }

            log.info("Excel procesado: {} usuarios registrados", contador);

        } finally {
            if (workbook != null) {
                workbook.close();
            }
        }

        return contador;
    }

    /**
     * Procesa archivos CSV
     */
    private int procesarCSV(MultipartFile file) throws Exception {
        int contador = 0;
        int lineaNumero = 0;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String linea;

            while ((linea = br.readLine()) != null) {
                lineaNumero++;

                // Saltar encabezado si existe
                if (lineaNumero == 1 && linea.toLowerCase().contains("numero_documento")) {
                    log.debug("Encabezado detectado en línea 1");
                    continue;
                }

                String[] datos = linea.split(",");
                if (datos.length < 6) {
                    log.debug("Línea {} ignorada: número de campos insuficiente", lineaNumero);
                    continue;
                }

                try {
                    String numeroDocumento = datos[0].trim();
                    String nombre = datos[1].trim();
                    String telefono = datos[2].trim();
                    String correo = datos[3].trim();
                    String clave = datos[4].trim();
                    String perfil = datos[5].trim();

                    // Validar campos obligatorios
                    if (numeroDocumento.isEmpty() || nombre.isEmpty()) {
                        log.debug("Línea {} ignorada por campos vacíos", lineaNumero);
                        continue;
                    }

                    // Crear usuario
                    Usuario usuario = crearUsuario(numeroDocumento, nombre, telefono, correo, clave, perfil);
                    usuarioRepository.save(usuario);
                    contador++;

                    log.info("Usuario registrado: {} - {}", numeroDocumento, nombre);

                } catch (DataIntegrityViolationException e) {
                    log.warn("Violación de integridad línea {}: {}", lineaNumero, e.getMessage());
                } catch (Exception e) {
                    log.error("Error procesando línea {}: {} ", lineaNumero, e.getMessage());
                }
            }

            log.info("CSV procesado: {} usuarios registrados", contador);

        }

        return contador;
    }

    /**
     * Crea una instancia de Usuario con los datos proporcionados
     */
    private Usuario crearUsuario(String numeroDocumento, String nombre, String telefono, 
                                  String correo, String clave, String perfil) {
        Usuario usuario = new Usuario();
        usuario.setNumeroDocumento(numeroDocumento);
        usuario.setNombre(nombre);
        usuario.setTelefono(telefono);
        usuario.setCorreo(correo);
        usuario.setClave(clave);

        // Asignar perfil con fallback a Usuario si es inválido
        try {
            usuario.setPerfilUsuario(Usuario.PerfilUsuario.valueOf(perfil));
        } catch (IllegalArgumentException e) {
            log.warn("Perfil '{}' inválido, asignando perfil Usuario por defecto", perfil);
            usuario.setPerfilUsuario(Usuario.PerfilUsuario.Usuario);
        }

        // Estado: Activo
        usuario.setEstado("A");

        return usuario;
    }

    /**
     * Obtiene el valor de una celda Excel como String
     */
    private String getCellValueAsString(Row row, int columnIndex) {
        Cell cell = row.getCell(columnIndex);
        if (cell == null) return "";

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getDateCellValue().toString();
                } else {
                    yield String.valueOf((long) cell.getNumericCellValue());
                }
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }
}