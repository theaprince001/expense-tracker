package com.expensetracker.controller;

import com.expensetracker.service.ReceiptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/receipts")
@RequiredArgsConstructor
@Slf4j
public class ReceiptController {

    private final ReceiptService receiptService;

    @PostMapping("/upload/{transactionId}")
    public ResponseEntity<?> uploadReceipt(
            @RequestParam("file") MultipartFile file,
            @PathVariable Long transactionId,
            Authentication authentication) {

        Long receiptId = receiptService.uploadReceipt(file, transactionId);
        return ResponseEntity.ok().body("Receipt uploaded successfully. ID: " + receiptId);
    }

    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<List<Resource>> getReceiptsByTransaction(@PathVariable Long transactionId) {
        List<Resource> receipts = receiptService.getReceiptsByTransaction(transactionId);
        return ResponseEntity.ok(receipts);
    }

    @GetMapping("/user")
    public ResponseEntity<List<Resource>> getReceiptsByUser(Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        List<Resource> receipts = receiptService.getReceiptsByUser(userId);
        return ResponseEntity.ok(receipts);
    }

    @GetMapping("/{receiptId}")
    public ResponseEntity<Resource> getReceipt(@PathVariable Long receiptId) {
        Resource receipt = receiptService.getReceiptById(receiptId);

        String contentType = "application/octet-stream";
        if (receipt.getFilename() != null) {
            if (receipt.getFilename().endsWith(".pdf")) {
                contentType = "application/pdf";
            } else if (receipt.getFilename().endsWith(".jpg") || receipt.getFilename().endsWith(".jpeg")) {
                contentType = "image/jpeg";
            } else if (receipt.getFilename().endsWith(".png")) {
                contentType = "image/png";
            }
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + receipt.getFilename() + "\"")
                .body(receipt);
    }

    @DeleteMapping("/{receiptId}")
    public ResponseEntity<?> deleteReceipt(@PathVariable Long receiptId) {
        receiptService.deleteReceipt(receiptId);
        return ResponseEntity.ok().body("Receipt deleted successfully");
    }

    private Long getUserIdFromAuth(Authentication authentication) {
        // TODO: Extract from JWT
        return 1L; // Temporary
    }
}
