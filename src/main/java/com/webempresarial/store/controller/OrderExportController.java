package com.webempresarial.store.controller;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.webempresarial.store.model.Order;
import com.webempresarial.store.model.OrderStatus;
import com.webempresarial.store.model.PaymentStatus;
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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class OrderExportController {

    private final OrderService orderService;
    private final StoreResolver storeResolver;

    public OrderExportController(
            OrderService orderService,
            StoreResolver storeResolver
    ) {
        this.orderService = orderService;
        this.storeResolver = storeResolver;
    }

    private final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @GetMapping("/admin/orders/excel")
    public void exportOrdersToExcel(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) PaymentStatus payment,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        Store store = storeResolver.getCurrentStore(request);

        LocalDateTime fromDT =
                from != null ? from.atStartOfDay() : null;

        LocalDateTime toDT =
                to != null ? to.atTime(23, 59, 59) : null;

        List<Order> orders =
                orderService.findOrdersFiltered(
                        status,
                        payment,
                        fromDT,
                        toDT,
                        store
                );

        response.setContentType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );

        response.setHeader(
                "Content-Disposition",
                "attachment; filename=orders-" + store.getTheme() + ".xlsx"
        );

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Órdenes");

        Row header = sheet.createRow(0);

        header.createCell(0).setCellValue("ID");
        header.createCell(1).setCellValue("Tienda");
        header.createCell(2).setCellValue("Cliente");
        header.createCell(3).setCellValue("Total");
        header.createCell(4).setCellValue("Estado");
        header.createCell(5).setCellValue("Pago");
        header.createCell(6).setCellValue("Fecha");

        int rowNum = 1;

        for (Order order : orders) {

            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(order.getId());
            row.createCell(1).setCellValue(store.getNombre());
            row.createCell(2).setCellValue(
                    order.getUser() != null
                            ? order.getUser().getFullName()
                            : order.getCustomerName()
            );
            row.createCell(3).setCellValue(order.getTotal().doubleValue());
            row.createCell(4).setCellValue(order.getOrderStatusLabel());
            row.createCell(5).setCellValue(order.getPaymentStatusLabel());
            row.createCell(6).setCellValue(
                    order.getOrderDate().format(formatter)
            );
        }

        for (int i = 0; i < 7; i++) {
            sheet.autoSizeColumn(i);
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    @GetMapping("/admin/orders/pdf")
    public void exportOrdersToPDF(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) PaymentStatus payment,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws Exception {

        Store store = storeResolver.getCurrentStore(request);

        LocalDateTime fromDate =
                from != null ? from.atStartOfDay() : null;

        LocalDateTime toDate =
                to != null ? to.atTime(23, 59, 59) : null;

        List<Order> orders =
                orderService.findOrdersForExport(
                        status,
                        payment,
                        fromDate,
                        toDate,
                        store
                );

        response.setContentType("application/pdf");

        response.setHeader(
                "Content-Disposition",
                "attachment; filename=orders-" + store.getTheme() + ".pdf"
        );

        Document document =
                new Document(PageSize.A4.rotate());

        PdfWriter.getInstance(
                document,
                response.getOutputStream()
        );

        document.open();

        Font titleFont =
                FontFactory.getFont(
                        FontFactory.HELVETICA_BOLD,
                        14
                );

        Font headerFont =
                FontFactory.getFont(
                        FontFactory.HELVETICA_BOLD,
                        11
                );

        Font cellFont =
                FontFactory.getFont(
                        FontFactory.HELVETICA,
                        10
                );

        document.add(
                new Paragraph(
                        "Listado de Órdenes – " + store.getNombre(),
                        titleFont
                )
        );

        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);

        table.addCell(new PdfPCell(new Phrase("ID", headerFont)));
        table.addCell(new PdfPCell(new Phrase("Tienda", headerFont)));
        table.addCell(new PdfPCell(new Phrase("Cliente", headerFont)));
        table.addCell(new PdfPCell(new Phrase("Total", headerFont)));
        table.addCell(new PdfPCell(new Phrase("Estado", headerFont)));
        table.addCell(new PdfPCell(new Phrase("Pago", headerFont)));
        table.addCell(new PdfPCell(new Phrase("Fecha", headerFont)));

        for (Order order : orders) {

            table.addCell(
                    new PdfPCell(
                            new Phrase(
                                    order.getId().toString(),
                                    cellFont
                            )
                    )
            );

            table.addCell(
                    new PdfPCell(
                            new Phrase(
                                    store.getNombre(),
                                    cellFont
                            )
                    )
            );

            table.addCell(
                    new PdfPCell(
                            new Phrase(
                                    order.getUser() != null
                                            ? order.getUser().getFullName()
                                            : order.getCustomerName(),
                                    cellFont
                            )
                    )
            );

            table.addCell(
                    new PdfPCell(
                            new Phrase(
                            		"$" + order.getTotal().setScale(
                            		        2,
                            		        RoundingMode.HALF_UP
                            		),
                                    cellFont
                            )
                    )
            );

            table.addCell(
                    new PdfPCell(
                            new Phrase(
                                    order.getOrderStatusLabel(),
                                    cellFont
                            )
                    )
            );

            table.addCell(
                    new PdfPCell(
                            new Phrase(
                                    order.getPaymentStatusLabel(),
                                    cellFont
                            )
                    )
            );

            table.addCell(
                    new PdfPCell(
                            new Phrase(
                                    order.getOrderDate().format(formatter),
                                    cellFont
                            )
                    )
            );
        }

        document.add(table);
        document.close();
    }
}