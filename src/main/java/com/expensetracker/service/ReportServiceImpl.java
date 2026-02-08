package com.expensetracker.service;

import com.expensetracker.dto.DashboardSummary;
import com.expensetracker.entity.Transaction;
import com.expensetracker.entity.User;
import com.expensetracker.repository.TransactionRepository;
import com.expensetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {

    private final DashboardService dashboardService;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;

    /* =========================================================
       =============== PDF REPORT GENERATION ===================
       ========================================================= */

    @Override
    @Transactional(readOnly = true)
    public byte[] generateMonthlyReportPdf(YearMonth month, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        DashboardSummary summary = dashboardService.getFinancialSummary(month);

        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        List<Transaction> transactions =
                transactionRepository.findTop10ByUserIdAndDateBetweenOrderByDateDesc(
                        userId, start, end
                );

        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream cs = new PDPageContentStream(document, page);

            float y = 780;

            /* ---------- HEADER ---------- */
            cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 18);
            write(cs, 50, y, "Expense Tracker – Monthly Report");
            y -= 25;

            cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
            write(cs, 50, y, "User: " + user.getFirstName() + " " + user.getLastName());
            y -= 15;
            write(cs, 50, y, "Period: " + month.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
            y -= 20;

            drawLine(cs, y);
            y -= 20;

            /* ---------- SUMMARY CARD ---------- */
            cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 13);
            write(cs, 50, y, "Financial Summary");
            y -= 15;

            cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
            write(cs, 60, y, "Total Income  : $" + format(summary.getTotalIncome())); y -= 15;
            write(cs, 60, y, "Total Expense : $" + format(summary.getTotalExpense())); y -= 15;
            write(cs, 60, y, "Net Balance   : $" + format(summary.getNetBalance())); y -= 15;
            write(cs, 60, y, "Savings Rate  : " + format(summary.getSavingsRate()) + "%"); y -= 25;

            drawLine(cs, y);
            y -= 20;

            /* ---------- TRANSACTIONS TABLE ---------- */
            cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 13);
            write(cs, 50, y, "Recent Transactions");
            y -= 15;

            cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
            write(cs, 50, y, "Date");
            write(cs, 120, y, "Description");
            write(cs, 320, y, "Amount");
            write(cs, 420, y, "Type");
            y -= 10;

            drawLine(cs, y);
            y -= 10;

            cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);

            for (Transaction tx : transactions) {
                write(cs, 50, y, tx.getDate().toString());
                write(cs, 120, y, truncate(tx.getDescription(), 30));
                write(cs, 320, y, "$" + format(tx.getAmount()));
                write(cs, 420, y, tx.getType().name());
                y -= 14;

                if (y < 80) break;
            }

            /* ---------- FOOTER ---------- */
            y = 40;
            cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE), 8);
            write(cs, 50, y, "Generated by Expense Tracker • Confidential");

            cs.close();
            document.save(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("PDF generation failed", e);
        }
    }

    /* =========================================================
       ================= EMAIL REPORT ==========================
       ========================================================= */

    @Override
    public void sendMonthlyReportByEmail(YearMonth month, Long userId) {

        byte[] pdf = generateMonthlyReportPdf(month, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom("Expense Tracker <noreply@expensetracker.com>");
            helper.setTo(user.getEmail());
            helper.setSubject("Monthly Expense Report – " + month);
            helper.setText(
                    "Hi " + user.getFirstName() + ",\n\n" +
                            "Your monthly expense report is attached.\n\n" +
                            "Regards,\nExpense Tracker"
            );

            helper.addAttachment(
                    "Monthly_Report_" + month + ".pdf",
                    new ByteArrayResource(pdf)
            );

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Email sending failed", e);
        }
    }

    /* =========================================================
       ================= CSV REPORT =============================
       ========================================================= */

    @Override
    @Transactional(readOnly = true)
    public byte[] generateCsvReport(LocalDate start, LocalDate end, Long userId) {

        List<Transaction> txs =
                transactionRepository.findByUserIdAndDateBetween(userId, start, end);

        StringBuilder csv = new StringBuilder();
        csv.append("Date,Description,Amount,Type\n");

        for (Transaction tx : txs) {
            csv.append(tx.getDate()).append(",")
                    .append(escape(tx.getDescription())).append(",")
                    .append(tx.getAmount()).append(",")
                    .append(tx.getType()).append("\n");
        }

        return csv.toString().getBytes();
    }

    /* =========================================================
       ================= HELPERS ================================
       ========================================================= */

    private void write(PDPageContentStream cs, float x, float y, String text) throws IOException {
        cs.beginText();
        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
    }

    private void drawLine(PDPageContentStream cs, float y) throws IOException {
        cs.moveTo(50, y);
        cs.lineTo(550, y);
        cs.stroke();
    }

    private String format(BigDecimal v) {
        return v == null ? "0.00" : v.setScale(2, RoundingMode.HALF_UP).toString();
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max) + "…" : s;
    }

    private String escape(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}

