package com.expensetracker.controller;

import com.expensetracker.security.CurrentUserService;
import com.expensetracker.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private final ReportService reportService;
    private final CurrentUserService currentUserService;


    @GetMapping("/monthly/pdf-and-email")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> downloadAndEmailMonthlyReport(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {

        Long userId = currentUserService.getCurrentUserId();

        // 1️⃣ Generate PDF ONCE
        byte[] pdfBytes = reportService.generateMonthlyReportPdf(month, userId);

        // 2️⃣ Send email using same month/user
        // (internally regenerates or can reuse later if optimized)
        reportService.sendMonthlyReportByEmail(month, userId);

        // 3️⃣ Return PDF as download
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData(
                "attachment",
                "expense_report_" + month + ".pdf"
        );
        headers.setContentLength(pdfBytes.length);

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @GetMapping("/monthly/pdf")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> getMonthlyPdfReport(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {

        Long userId = currentUserService.getCurrentUserId();
        byte[] pdfBytes = reportService.generateMonthlyReportPdf(month, userId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment",
                "expense_report_" + month + ".pdf");
        headers.setContentLength(pdfBytes.length);

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @PostMapping("/monthly/email")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> sendMonthlyReportEmail(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {

        Long userId = currentUserService.getCurrentUserId();
        reportService.sendMonthlyReportByEmail(month, userId);
        return ResponseEntity.ok("Monthly report has been sent to your email.");
    }

    @GetMapping("/csv")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> getCsvReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        Long userId = currentUserService.getCurrentUserId();
        byte[] csvBytes = reportService.generateCsvReport(start, end, userId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment",
                "transactions_" + start + "_to_" + end + ".csv");
        headers.setContentLength(csvBytes.length);

        return new ResponseEntity<>(csvBytes, headers, HttpStatus.OK);
    }

    @PostMapping("/admin/send-all-monthly")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> sendReportsToAllUsers(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {

        return ResponseEntity.ok("Monthly report sending process initiated for all users.");
    }
}