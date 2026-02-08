package com.expensetracker.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ReceiptService {
    Long uploadReceipt(MultipartFile file, Long transactionId);
    List<Resource> getReceiptsByTransaction(Long transactionId);
    List<Resource> getReceiptsByUser(Long userId);
    Resource getReceiptById(Long receiptId);
    void deleteReceipt(Long receiptId);
    void deleteReceiptsByTransaction(Long transactionId);
}
