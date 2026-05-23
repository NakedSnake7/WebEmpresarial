package com.webempresarial.store.controller;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.webempresarial.store.dto.producto.reportes.ProductoVentaDTO;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.OrderService;
import com.webempresarial.store.theme.StoreResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Controller
public class ProductSalesExportController {

    private final OrderService orderService;
    private final StoreResolver storeResolver;

    public ProductSalesExportController(
            OrderService orderService,
            StoreResolver storeResolver
    ) {
        this.orderService = orderService;
        this.storeResolver = storeResolver;
    }

    @GetMapping("/admin/reports/products/excel")
    public void exportProductSalesExcel(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to,

            HttpServletRequest request,
            HttpServletResponse response

    ) throws IOException {

        Store store = storeResolver.getCurrentStore(request);

        List<ProductoVentaDTO> data =
                orderService.getPaidProductSalesByDate(
                        from,
                        to,
                        store
                );

        response.setContentType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );

        response.setHeader(
                "Content-Disposition",
                "attachment; filename=ventas-productos-" + store.getTheme() + ".xlsx"
        );

        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("Ventas por Producto");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Tienda");
            header.createCell(1).setCellValue("Producto");
            header.createCell(2).setCellValue("Cantidad Vendida");

            int rowNum = 1;

            if (data.isEmpty()) {
                Row row = sheet.createRow(rowNum);
                row.createCell(0).setCellValue(store.getNombre());
                row.createCell(1).setCellValue("Sin ventas en el periodo seleccionado");
            } else {
                for (ProductoVentaDTO dto : data) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(store.getNombre());
                    row.createCell(1).setCellValue(dto.getProductName());
                    row.createCell(2).setCellValue(dto.getTotalQuantity());
                }
            }

            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
            sheet.autoSizeColumn(2);

            workbook.write(response.getOutputStream());
        }
    }

    @GetMapping("/admin/reports/products/pdf")
    public void exportProductSalesPdf(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to,

            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        Store store = storeResolver.getCurrentStore(request);

        List<ProductoVentaDTO> data =
                orderService.getPaidProductSalesByDate(
                        from,
                        to,
                        store
                );

        response.setContentType("application/pdf");

        response.setHeader(
                "Content-Disposition",
                "attachment; filename=ventas-productos-" + store.getTheme() + ".pdf"
        );

        PdfWriter writer =
                new PdfWriter(response.getOutputStream());

        PdfDocument pdf =
                new PdfDocument(writer);

        try (Document document = new Document(pdf)) {

            document.add(
                    new Paragraph("Reporte Ejecutivo de Ventas – " + store.getNombre())
                            .setBold()
                            .setFontSize(18)
            );

            document.add(new Paragraph(
                    "Periodo: " +
                            (from != null ? from : "Inicio") +
                            " → " +
                            (to != null ? to : "Hoy")
            ));

            document.add(new Paragraph("Generado: " + LocalDate.now()));
            document.add(new Paragraph("\n"));

            if (data.isEmpty()) {
                document.add(
                        new Paragraph("No hay ventas registradas en el periodo seleccionado.")
                                .setBold()
                );
                return;
            }

            float[] widths = {30f, 50f, 20f};
            Table table = new Table(widths);

            table.addHeaderCell("Tienda");
            table.addHeaderCell("Producto");
            table.addHeaderCell("Cantidad Vendida");

            int total = 0;

            for (ProductoVentaDTO dto : data) {
                table.addCell(store.getNombre());
                table.addCell(dto.getProductName());
                table.addCell(dto.getTotalQuantity().toString());

                total += dto.getTotalQuantity();
            }

            document.add(table);
            document.add(new Paragraph("\n"));
            document.add(
                    new Paragraph("Total de productos vendidos: " + total)
                            .setBold()
            );
        }
    }
}