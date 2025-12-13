package com.example.flowfit.service;

import com.example.flowfit.model.RegistroAprobaciones;
import com.example.flowfit.model.Usuario;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExcelExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * Genera un archivo Excel del historial de aprobaciones
     */
    public ByteArrayOutputStream generateHistorialAprobacionesExcel(List<RegistroAprobaciones> historial)
            throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Historial de Aprobaciones FlowFit");

            // Create styles
            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle approvedStyle = createApprovedStyle(workbook);
            CellStyle rejectedStyle = createRejectedStyle(workbook);

            int rowNum = 0;

            // Sección de título
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("FLOWFIT - HISTORIAL DE APROBACIONES");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 8));

            // Subtítulo
            rowNum++;
            Row subtitleRow = sheet.createRow(rowNum++);
            Cell subtitleCell = subtitleRow.createCell(0);
            subtitleCell.setCellValue("Registro completo de usuarios aprobados y rechazados");
            subtitleCell.setCellStyle(dataStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 8));

            // Fecha de generación
            Row dateRow = sheet.createRow(rowNum++);
            Cell dateCell = dateRow.createCell(0);
            dateCell.setCellValue("Fecha de generación: "
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            dateCell.setCellStyle(dateStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 8));

            // Información del sistema
            Row systemRow = sheet.createRow(rowNum++);
            Cell systemCell = systemRow.createCell(0);
            systemCell.setCellValue("Sistema de gestión FlowFit - Administración de usuarios");
            systemCell.setCellStyle(dateStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 8));

            // Separador
            rowNum += 2;

            // Statistics section
            Row statsHeaderRow = sheet.createRow(rowNum++);
            Cell statsHeaderCell = statsHeaderRow.createCell(0);
            statsHeaderCell.setCellValue("ESTADÍSTICAS DEL HISTORIAL");
            statsHeaderCell.setCellStyle(headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 8));

            // Calculate statistics
            long totalAprobados = historial.stream().filter(h -> "Aprobado".equals(h.getAccion().toString())).count();
            long totalRechazados = historial.stream().filter(h -> "Rechazado".equals(h.getAccion().toString())).count();
            long totalEditados = historial.stream().filter(h -> "Editado".equals(h.getAccion().toString())).count();

            // Stats rows
            addStatisticRow(sheet, rowNum++, "Total de usuarios aprobados:", String.valueOf(totalAprobados), dataStyle);
            addStatisticRow(sheet, rowNum++, "Total de usuarios rechazados:", String.valueOf(totalRechazados),
                    dataStyle);
            addStatisticRow(sheet, rowNum++, "Total de usuarios editados:", String.valueOf(totalEditados), dataStyle);
            addStatisticRow(sheet, rowNum++, "Total de registros:", String.valueOf(historial.size()), headerStyle);

            // Otro separador
            rowNum += 2;

            // Encabezado de sección de datos
            Row dataHeaderRow = sheet.createRow(rowNum++);
            Cell dataHeaderCell = dataHeaderRow.createCell(0);
            dataHeaderCell.setCellValue("HISTORIAL DETALLADO");
            dataHeaderCell.setCellStyle(headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 8));

            rowNum++;

            // Fila de encabezados
            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = { "Fecha", "Hora", "Usuario", "Documento", "Correo Electrónico", "Perfil", "Acción",
                    "Detalles", "Administrador" };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Filas de datos
            for (RegistroAprobaciones registro : historial) {
                Row dataRow = sheet.createRow(rowNum++);

                // Date
                Cell dateDataCell = dataRow.createCell(0);
                dateDataCell.setCellValue(registro.getFecha().format(DATE_FORMATTER));
                dateDataCell.setCellStyle(dataStyle);

                // Time
                Cell timeCell = dataRow.createCell(1);
                timeCell.setCellValue(registro.getFecha().format(TIME_FORMATTER));
                timeCell.setCellStyle(dataStyle);

                // User name
                Cell userCell = dataRow.createCell(2);
                userCell.setCellValue(registro.getUsuario().getNombre());
                userCell.setCellStyle(dataStyle);

                // Document
                Cell docCell = dataRow.createCell(3);
                docCell.setCellValue(registro.getUsuario().getNumeroDocumento());
                docCell.setCellStyle(dataStyle);

                // Email
                Cell emailCell = dataRow.createCell(4);
                emailCell.setCellValue(registro.getUsuario().getCorreo());
                emailCell.setCellStyle(dataStyle);

                // Profile
                Cell profileCell = dataRow.createCell(5);
                profileCell.setCellValue(registro.getUsuario().getPerfilUsuario().toString());
                profileCell.setCellStyle(dataStyle);

                // Acción (con estilo especial)
                Cell actionCell = dataRow.createCell(6);
                actionCell.setCellValue(registro.getAccion().toString());
                if ("Aprobado".equals(registro.getAccion().toString())) {
                    actionCell.setCellStyle(approvedStyle);
                } else if ("Editado".equals(registro.getAccion().toString())) {
                    actionCell.setCellStyle(dataStyle); // Usar estilo normal para editado
                } else {
                    actionCell.setCellStyle(rejectedStyle);
                }

                // Detalles (motivo)
                Cell detailsCell = dataRow.createCell(7);
                detailsCell.setCellValue(registro.getMotivo() != null ? registro.getMotivo() : "-");
                detailsCell.setCellStyle(dataStyle);

                // Administrador
                Cell adminCell = dataRow.createCell(8);
                adminCell.setCellValue(registro.getAdmin().getNombre());
                adminCell.setCellStyle(dataStyle);
            }

            // Agregar sección de pie de página
            rowNum += 2;

            Row footerRow1 = sheet.createRow(rowNum++);
            Cell footerCell1 = footerRow1.createCell(0);
            footerCell1.setCellValue("═══════════════════════════════════════════════════════════════════════════════");
            footerCell1.setCellStyle(dateStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 8));

            Row footerRow2 = sheet.createRow(rowNum++);
            Cell footerCell2 = footerRow2.createCell(0);
            footerCell2.setCellValue("FlowFit © 2025 - Sistema de Gestión Fitness Profesional");
            footerCell2.setCellStyle(dateStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 8));

            Row footerRow3 = sheet.createRow(rowNum++);
            Cell footerCell3 = footerRow3.createCell(0);
            footerCell3.setCellValue("Documento generado automáticamente - Confidencial");
            footerCell3.setCellStyle(dateStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 8));

            // Autoajustar ancho de columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                // Establecer ancho mínimo para asegurar legibilidad
                int currentWidth = sheet.getColumnWidth(i);
                sheet.setColumnWidth(i, Math.max(currentWidth, 3000));
            }

            // Escribir a ByteArrayOutputStream
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            return baos;

        } catch (IOException e) {
            throw new RuntimeException("Error generating Excel file", e);
        }
    }

    public ByteArrayOutputStream generateUsuariosExcel(List<Usuario> usuarios, String search, String filtro)
            throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            // Crear hoja principal de datos
            Sheet dataSheet = workbook.createSheet("Usuarios FlowFit");

            // Crear estilos
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle statusActiveStyle = createStatusStyle(workbook, IndexedColors.GREEN);
            CellStyle statusPendingStyle = createStatusStyle(workbook, IndexedColors.YELLOW);
            CellStyle statusRejectedStyle = createStatusStyle(workbook, IndexedColors.RED);

            // Crear fila de encabezados
            Row headerRow = dataSheet.createRow(0);
            String[] headers = { "ID", "Nombre Completo", "Email", "Teléfono", "Documento", "Perfil", "Estado",
                    "Fecha Registro", "Fecha Último Acceso" };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Poblar filas de datos
            int rowNum = 1;
            for (Usuario usuario : usuarios) {
                Row row = dataSheet.createRow(rowNum++);

                // ID
                Cell idCell = row.createCell(0);
                idCell.setCellValue(usuario.getId());
                idCell.setCellStyle(dataStyle);

                // Nombre
                Cell nameCell = row.createCell(1);
                nameCell.setCellValue(usuario.getNombre() != null ? usuario.getNombre() : "");
                nameCell.setCellStyle(dataStyle);

                // Email
                Cell emailCell = row.createCell(2);
                emailCell.setCellValue(usuario.getCorreo() != null ? usuario.getCorreo() : "");
                emailCell.setCellStyle(dataStyle);

                // Teléfono
                Cell phoneCell = row.createCell(3);
                phoneCell.setCellValue(usuario.getTelefono() != null ? usuario.getTelefono() : "");
                phoneCell.setCellStyle(dataStyle);

                // Documento
                Cell docCell = row.createCell(4);
                docCell.setCellValue(usuario.getNumeroDocumento() != null ? usuario.getNumeroDocumento() : "");
                docCell.setCellStyle(dataStyle);

                // Perfil
                Cell profileCell = row.createCell(5);
                profileCell.setCellValue(
                        usuario.getPerfilUsuario() != null ? usuario.getPerfilUsuario().toString() : "Usuario");
                profileCell.setCellStyle(dataStyle);

                // Estado
                Cell statusCell = row.createCell(6);
                String estadoTexto = switch (usuario.getEstado()) {
                    case "A" -> "ACTIVO";
                    case "I" -> "PENDIENTE";
                    case "R" -> "RECHAZADO";
                    default -> "DESCONOCIDO";
                };
                statusCell.setCellValue(estadoTexto);

                // Aplicar estilo específico según estado
                CellStyle statusStyle = switch (usuario.getEstado()) {
                    case "A" -> statusActiveStyle;
                    case "I" -> statusPendingStyle;
                    case "R" -> statusRejectedStyle;
                    default -> dataStyle;
                };
                statusCell.setCellStyle(statusStyle);

                // Fecha Registro
                Cell regDateCell = row.createCell(7);
                regDateCell.setCellValue("No disponible");
                regDateCell.setCellStyle(dataStyle);

                // Fecha Último Acceso (marcador de posición)
                Cell lastAccessCell = row.createCell(8);
                lastAccessCell.setCellValue("No disponible");
                lastAccessCell.setCellStyle(dataStyle);
            }

            // Autoajustar ancho de columnas
            for (int i = 0; i < headers.length; i++) {
                dataSheet.autoSizeColumn(i);
                // Establecer ancho mínimo
                int currentWidth = dataSheet.getColumnWidth(i);
                dataSheet.setColumnWidth(i, Math.max(currentWidth, 3000));
            }

            // Crear hoja de resumen
            Sheet summarySheet = workbook.createSheet("Resumen Estadísticas");
            createUsersSummarySheet(summarySheet, usuarios, workbook, search, filtro);

            // Escribir a ByteArrayOutputStream
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            return baos;

        } catch (IOException e) {
            throw new RuntimeException("Error generating users Excel file", e);
        }
    }

    private void createUsersSummarySheet(Sheet sheet, List<Usuario> usuarios, Workbook workbook, String search,
            String filtro) {
        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);

        int currentRow = 0;

        // Título
        Row titleRow = sheet.createRow(currentRow++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("FLOWFIT - REPORTE DE USUARIOS");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(currentRow - 1, currentRow - 1, 0, 3));

        // Información de generación
        currentRow++;
        Row infoRow1 = sheet.createRow(currentRow++);
        Cell infoCell1 = infoRow1.createCell(0);
        infoCell1.setCellValue("Fecha de generación: "
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        infoCell1.setCellStyle(dataStyle);

        // Información de filtros
        Row filterRow = sheet.createRow(currentRow++);
        Cell filterCell = filterRow.createCell(0);
        String filterText = "Filtros aplicados: ";
        if (!search.isEmpty()) {
            filterText += "Búsqueda: '" + search + "' ";
        }
        if (!filtro.equals("todos")) {
            filterText += "Filtro: " + filtro;
        }
        if (search.isEmpty() && filtro.equals("todos")) {
            filterText += "Ninguno (todos los usuarios)";
        }
        filterCell.setCellValue(filterText);
        filterCell.setCellStyle(dataStyle);

        currentRow++;

        // Statistics by status
        Row statusHeaderRow = sheet.createRow(currentRow++);
        Cell statusHeaderCell = statusHeaderRow.createCell(0);
        statusHeaderCell.setCellValue("ESTADÍSTICAS POR ESTADO");
        statusHeaderCell.setCellStyle(headerStyle);

        long activos = usuarios.stream().filter(u -> "A".equals(u.getEstado())).count();
        long pendientes = usuarios.stream().filter(u -> "I".equals(u.getEstado())).count();
        long rechazados = usuarios.stream().filter(u -> "R".equals(u.getEstado())).count();

        addStatisticRow(sheet, currentRow++, "Usuarios Activos:", String.valueOf(activos), dataStyle);
        addStatisticRow(sheet, currentRow++, "Usuarios Pendientes:", String.valueOf(pendientes), dataStyle);
        addStatisticRow(sheet, currentRow++, "Usuarios Rechazados:", String.valueOf(rechazados), dataStyle);
        addStatisticRow(sheet, currentRow++, "Total de Usuarios:", String.valueOf(usuarios.size()), headerStyle);

        currentRow++;

        // Statistics by profile
        Row profileHeaderRow = sheet.createRow(currentRow++);
        Cell profileHeaderCell = profileHeaderRow.createCell(0);
        profileHeaderCell.setCellValue("ESTADÍSTICAS POR PERFIL");
        profileHeaderCell.setCellStyle(headerStyle);

        long entrenadores = usuarios.stream().filter(u -> u.getPerfilUsuario().toString().equals("Entrenador")).count();
        long nutricionistas = usuarios.stream().filter(u -> u.getPerfilUsuario().toString().equals("Nutricionista"))
                .count();
        long usuariosNormales = usuarios.stream().filter(u -> u.getPerfilUsuario().toString().equals("Usuario"))
                .count();
        long administradores = usuarios.stream().filter(u -> u.getPerfilUsuario().toString().equals("Administrador"))
                .count();

        addStatisticRow(sheet, currentRow++, "Entrenadores:", String.valueOf(entrenadores), dataStyle);
        addStatisticRow(sheet, currentRow++, "Nutricionistas:", String.valueOf(nutricionistas), dataStyle);
        addStatisticRow(sheet, currentRow++, "Usuarios:", String.valueOf(usuariosNormales), dataStyle);
        addStatisticRow(sheet, currentRow++, "Administradores:", String.valueOf(administradores), dataStyle);

        // Auto-size columns
        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }

        // Set column widths
        sheet.setColumnWidth(0, 8000);
        sheet.setColumnWidth(1, 4000);
    }

    private void addStatisticRow(Sheet sheet, int rowNum, String label, String value, CellStyle style) {
        Row row = sheet.createRow(rowNum);

        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(style);

        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(value);
        valueCell.setCellStyle(style);
    }

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 18);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_80_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THICK);
        style.setBorderTop(BorderStyle.THICK);
        style.setBorderRight(BorderStyle.THICK);
        style.setBorderLeft(BorderStyle.THICK);
        return style;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setBorderTop(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.MEDIUM);
        style.setBorderLeft(BorderStyle.MEDIUM);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        font.setColor(IndexedColors.GREY_80_PERCENT.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    private CellStyle createApprovedStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        font.setColor(IndexedColors.GREEN.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setBorderTop(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.MEDIUM);
        style.setBorderLeft(BorderStyle.MEDIUM);
        return style;
    }

    private CellStyle createRejectedStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        font.setColor(IndexedColors.RED.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setBorderTop(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.MEDIUM);
        style.setBorderLeft(BorderStyle.MEDIUM);
        return style;
    }

    private CellStyle createStatusStyle(Workbook workbook, IndexedColors color) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        font.setColor(color.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }
}