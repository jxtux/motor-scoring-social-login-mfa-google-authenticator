package com.finanscore.motorscoring.infrastructure.report;

import com.finanscore.motorscoring.application.model.InformeScoringData;
import com.finanscore.motorscoring.application.port.out.PdfGenerator;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class PdfBoxScoringPdfGenerator implements PdfGenerator {
    private static final float MARGIN = 48;
    private static final float FONT_SIZE = 10;
    private final PDType1Font regular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private final PDType1Font bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    @Override
    public byte[] generar(InformeScoringData i) {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PageWriter w = new PageWriter(document);
            w.title("INFORME DE SCORE CREDITICIO");
            w.section("Datos del solicitante");
            w.line("Nombre: " + i.nombreSolicitante());
            w.line("Documento: " + i.tipoDocumento() + " " + i.documentoEnmascarado());
            w.line("Fecha de evaluación: " + i.fechaEvaluacion());

            w.section("Resultado");
            w.line("Score crediticio: " + i.puntajeTotal() + " / 1000");
            w.line("Resultado: " + i.resultado());
            w.line("Estado de evaluación: " + i.estadoEvaluacion());

            w.section("Producto / solicitud");
            w.line("Producto: " + i.nombreProducto() + " (" + i.codigoProducto() + ")");
            w.line("Monto solicitado: " + i.moneda() + " " + i.montoSolicitado());
            w.line("Plazo solicitado: " + i.plazoSolicitado() + " meses");

            w.section("Resumen financiero");
            w.line("Ingresos mensuales: " + i.moneda() + " " + i.ingresosMensuales());
            w.line("Gastos mensuales: " + i.moneda() + " " + i.gastosMensuales());
            w.line("Obligaciones financieras: " + i.moneda() + " " + i.obligacionesFinancieras());
            w.line("Capacidad de pago disponible: " + i.moneda() + " " + i.capacidadPago());
            w.line("Relación deuda/ingreso: " + i.relacionDeudaIngreso() + "%");
            w.line("Relación cuota/ingreso: " + i.relacionCuotaIngreso() + "%");

            w.section("Factores evaluados");
            for (var f : i.factores()) {
                w.line(f.codigo() + " | valor=" + f.valorEvaluado() + " | peso=" + f.pesoAplicado()
                        + "% | aporte=" + f.puntajeObtenido() + " | regla=" + f.reglaAplicada()
                        + (f.excluyente() ? " | EXCLUYENTE" : ""));
                if (f.observacion() != null && !f.observacion().isBlank()) w.small("  " + f.observacion());
            }

            w.section("Modelo utilizado");
            w.line("Versión: " + i.versionModelo());
            w.small("Resultado informativo basado en los datos declarados y las reglas del modelo de scoring vigente.");
            w.close();
            document.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo generar el PDF de scoring", e);
        }
    }

    private final class PageWriter {
        private final PDDocument document;
        private PDPage page;
        private PDPageContentStream content;
        private float y;

        private PageWriter(PDDocument document) throws IOException { this.document = document; newPage(); }

        void title(String text) throws IOException { text(text, bold, 16, 22); }
        void section(String text) throws IOException { ensure(32); y -= 8; text(text, bold, 12, 18); }
        void line(String text) throws IOException { for (String l : wrap(text, 95)) text(l, regular, FONT_SIZE, 14); }
        void small(String text) throws IOException { for (String l : wrap(text, 105)) text(l, regular, 8, 11); }

        private void text(String text, PDType1Font font, float size, float leading) throws IOException {
            ensure(leading + 4);
            content.beginText(); content.setFont(font, size); content.newLineAtOffset(MARGIN, y); content.showText(sanitize(text)); content.endText();
            y -= leading;
        }

        private void ensure(float needed) throws IOException { if (y - needed < MARGIN) newPage(); }

        private void newPage() throws IOException {
            if (content != null) content.close();
            page = new PDPage(PDRectangle.A4); document.addPage(page);
            content = new PDPageContentStream(document, page); y = page.getMediaBox().getHeight() - MARGIN;
        }

        private List<String> wrap(String text, int max) {
            List<String> lines = new ArrayList<>(); StringBuilder line = new StringBuilder();
            for (String word : text.split("\\s+")) {
                if (!line.isEmpty() && line.length() + word.length() + 1 > max) { lines.add(line.toString()); line.setLength(0); }
                if (!line.isEmpty()) line.append(' '); line.append(word);
            }
            if (!line.isEmpty()) lines.add(line.toString());
            return lines;
        }

        private String sanitize(String value) { return value.replace('–', '-').replace('—', '-'); }
        void close() throws IOException { if (content != null) content.close(); }
    }
}
