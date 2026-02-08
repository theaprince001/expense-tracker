package com.expensetracker.service;

import java.time.LocalDate;
import java.time.YearMonth;

public interface ReportService {
    byte[] generateMonthlyReportPdf(YearMonth month, Long userId);
    void sendMonthlyReportByEmail(YearMonth month, Long userId);
    byte[] generateCsvReport(LocalDate start, LocalDate end, Long userId);
}