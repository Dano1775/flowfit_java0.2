package com.example.flowfit.service;

import com.example.flowfit.model.Usuario;
import com.example.flowfit.model.RutinaAsignada;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class PdfService {

    @Autowired
    private RutinaService rutinaService;

    @Autowired
    private ProgresoService progresoService;

    /**
     * Genera un PDF con el reporte de progreso del usuario
     */
    public byte[] generarReporteProgreso(Usuario usuario) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, baos);
        document.open();

        // Fuentes y colores
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, new BaseColor(16, 185, 129));
        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.GRAY);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new BaseColor(16, 185, 129));
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
        Font regularFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font cellHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);

        BaseColor primaryColor = new BaseColor(16, 185, 129);
        BaseColor secondaryColor = new BaseColor(59, 130, 246);

        // ===== ENCABEZADO =====
        Paragraph title = new Paragraph("FLOWFIT - REPORTE DE PROGRESO", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(5);
        document.add(title);

        Paragraph subtitle = new Paragraph("Tu evolucion fitness en detalle", subtitleFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(20);
        document.add(subtitle);

        // ===== INFORMACIÓN DEL USUARIO =====
        Paragraph userSection = new Paragraph("INFORMACION PERSONAL", headerFont);
        userSection.setSpacingBefore(10);
        userSection.setSpacingAfter(10);
        document.add(userSection);

        PdfPTable userTable = new PdfPTable(2);
        userTable.setWidthPercentage(100);
        userTable.setWidths(new int[] { 1, 2 });

        addTableRow(userTable, "Nombre:", usuario.getNombre(), regularFont, boldFont);
        addTableRow(userTable, "Correo:", usuario.getCorreo(), regularFont, boldFont);
        addTableRow(userTable, "Fecha del reporte:",
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                regularFont, boldFont);

        document.add(userTable);
        document.add(Chunk.NEWLINE);

        // ===== ESTADÍSTICAS GENERALES =====
        Paragraph statsSection = new Paragraph("ESTADISTICAS GENERALES", headerFont);
        statsSection.setSpacingBefore(10);
        statsSection.setSpacingAfter(10);
        document.add(statsSection);

        // Obtener estadísticas
        List<RutinaAsignada> rutinasAsignadas = rutinaService.obtenerRutinasAsignadas(usuario.getId());
        long rutinasCompletadas = rutinasAsignadas.stream()
                .filter(r -> r.getEstado() == RutinaAsignada.EstadoRutina.COMPLETADA)
                .count();
        long rutinasEnProgreso = rutinasAsignadas.stream()
                .filter(r -> r.getEstado() == RutinaAsignada.EstadoRutina.ACTIVA)
                .count();
        Double progresoGeneral = rutinaService.calcularProgresoGeneralUsuario(usuario.getId());

        Map<String, Object> estadisticas = progresoService.getEstadisticasGenerales(usuario);
        int rachaActual = (Integer) estadisticas.getOrDefault("rachaActual", 0);

        LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
        int diasActivosEsteMes = progresoService.contarDiasEntrenadosEntre(usuario, inicioMes, LocalDate.now());

        PdfPTable statsTable = new PdfPTable(2);
        statsTable.setWidthPercentage(100);
        statsTable.setWidths(new int[] { 2, 1 });

        addTableRow(statsTable, "Rutinas Completadas:", String.valueOf(rutinasCompletadas), regularFont, boldFont);
        addTableRow(statsTable, "Rutinas en Progreso:", String.valueOf(rutinasEnProgreso), regularFont, boldFont);
        addTableRow(statsTable, "Progreso General:",
                String.format("%.1f%%", progresoGeneral != null ? progresoGeneral : 0.0),
                regularFont, boldFont);
        addTableRow(statsTable, "Dias Activos este Mes:", String.valueOf(diasActivosEsteMes), regularFont, boldFont);
        addTableRow(statsTable, "Racha Actual:", rachaActual + " dias", regularFont, boldFont);

        document.add(statsTable);
        document.add(Chunk.NEWLINE);

        // ===== RUTINAS COMPLETADAS =====
        List<RutinaAsignada> completadas = rutinasAsignadas.stream()
                .filter(r -> r.getEstado() == RutinaAsignada.EstadoRutina.COMPLETADA)
                .toList();

        if (!completadas.isEmpty()) {
            Paragraph completadasSection = new Paragraph("RUTINAS COMPLETADAS", headerFont);
            completadasSection.setSpacingBefore(10);
            completadasSection.setSpacingAfter(10);
            document.add(completadasSection);

            PdfPTable rutinasTable = new PdfPTable(3);
            rutinasTable.setWidthPercentage(100);
            rutinasTable.setWidths(new int[] { 3, 2, 1 });

            // Encabezados
            PdfPCell cell1 = new PdfPCell(new Phrase("Rutina", cellHeaderFont));
            cell1.setBackgroundColor(primaryColor);
            cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell1.setPadding(5);
            rutinasTable.addCell(cell1);

            PdfPCell cell2 = new PdfPCell(new Phrase("Fecha Asignacion", cellHeaderFont));
            cell2.setBackgroundColor(primaryColor);
            cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell2.setPadding(5);
            rutinasTable.addCell(cell2);

            PdfPCell cell3 = new PdfPCell(new Phrase("Veces", cellHeaderFont));
            cell3.setBackgroundColor(primaryColor);
            cell3.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell3.setPadding(5);
            rutinasTable.addCell(cell3);

            // Datos
            for (RutinaAsignada ra : completadas) {
                PdfPCell nombreCell = new PdfPCell(new Phrase(ra.getRutina().getNombre(), regularFont));
                nombreCell.setPadding(5);
                rutinasTable.addCell(nombreCell);

                PdfPCell fechaCell = new PdfPCell(new Phrase(
                        ra.getFechaAsignacion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), regularFont));
                fechaCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                fechaCell.setPadding(5);
                rutinasTable.addCell(fechaCell);

                PdfPCell vecesCell = new PdfPCell(new Phrase(String.valueOf(ra.getVecesCompletada()), regularFont));
                vecesCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                vecesCell.setPadding(5);
                rutinasTable.addCell(vecesCell);
            }

            document.add(rutinasTable);
            document.add(Chunk.NEWLINE);
        }

        // ===== RUTINAS ACTIVAS =====
        List<RutinaAsignada> activas = rutinasAsignadas.stream()
                .filter(r -> r.getEstado() == RutinaAsignada.EstadoRutina.ACTIVA)
                .toList();

        if (!activas.isEmpty()) {
            Paragraph activasSection = new Paragraph("RUTINAS EN PROGRESO", headerFont);
            activasSection.setSpacingBefore(10);
            activasSection.setSpacingAfter(10);
            document.add(activasSection);

            PdfPTable activasTable = new PdfPTable(3);
            activasTable.setWidthPercentage(100);
            activasTable.setWidths(new int[] { 3, 2, 1 });

            // Encabezados
            PdfPCell cell1 = new PdfPCell(new Phrase("Rutina", cellHeaderFont));
            cell1.setBackgroundColor(secondaryColor);
            cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell1.setPadding(5);
            activasTable.addCell(cell1);

            PdfPCell cell2 = new PdfPCell(new Phrase("Fecha Asignacion", cellHeaderFont));
            cell2.setBackgroundColor(secondaryColor);
            cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell2.setPadding(5);
            activasTable.addCell(cell2);

            PdfPCell cell3 = new PdfPCell(new Phrase("Progreso", cellHeaderFont));
            cell3.setBackgroundColor(secondaryColor);
            cell3.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell3.setPadding(5);
            activasTable.addCell(cell3);

            // Datos
            for (RutinaAsignada ra : activas) {
                PdfPCell nombreCell = new PdfPCell(new Phrase(ra.getRutina().getNombre(), regularFont));
                nombreCell.setPadding(5);
                activasTable.addCell(nombreCell);

                PdfPCell fechaCell = new PdfPCell(new Phrase(
                        ra.getFechaAsignacion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), regularFont));
                fechaCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                fechaCell.setPadding(5);
                activasTable.addCell(fechaCell);

                PdfPCell progresoCell = new PdfPCell(new Phrase(ra.getProgreso() + "%", regularFont));
                progresoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                progresoCell.setPadding(5);
                activasTable.addCell(progresoCell);
            }

            document.add(activasTable);
        }

        // ===== PIE DE PÁGINA =====
        document.add(Chunk.NEWLINE);
        document.add(Chunk.NEWLINE);

        Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.GRAY);
        Font footerSmallFont = FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.GRAY);

        Paragraph footer = new Paragraph("Generado por FlowFit - Sistema de Gestion de Entrenamiento Personal",
                footerFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);

        Paragraph footerDate = new Paragraph("Fecha de generacion: " +
                java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), footerSmallFont);
        footerDate.setAlignment(Element.ALIGN_CENTER);
        document.add(footerDate);

        // Cerrar documento
        document.close();

        return baos.toByteArray();
    }

    /**
     * Helper para agregar filas a una tabla
     */
    private void addTableRow(PdfPTable table, String label, String value, Font regularFont, Font boldFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, boldFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPaddingBottom(5);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, regularFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPaddingBottom(5);
        table.addCell(valueCell);
    }
}
