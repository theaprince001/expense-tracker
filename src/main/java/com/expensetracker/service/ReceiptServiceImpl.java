package com.expensetracker.service;

import com.expensetracker.entity.Receipt;
import com.expensetracker.entity.Transaction;
import com.expensetracker.entity.User;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.exception.UnauthorizedAccessException;
import com.expensetracker.repository.ReceiptRepository;
import com.expensetracker.repository.TransactionRepository;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.security.CurrentUserService;
import com.expensetracker.service.FileStorageService;
import com.expensetracker.service.ReceiptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReceiptServiceImpl implements ReceiptService {

    private final ReceiptRepository receiptRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final CurrentUserService currentUserService;

    @Override
    public Long uploadReceipt(MultipartFile file, Long transactionId) {
        Long currentUserId = currentUserService.getCurrentUserId();

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (!transaction.getUser().getId().equals(currentUserId)) {
            throw new UnauthorizedAccessException("Not allowed");
        }

        User user = transaction.getUser();
        String storagePath = fileStorageService.storeFile(file, user.getId());
        Receipt receipt = Receipt.builder()
                .fileName(storagePath.substring(storagePath.lastIndexOf("/") + 1))
                .originalFileName(file.getOriginalFilename())
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .storagePath(storagePath)
                .transaction(transaction)
                .user(user)
                .build();

        Receipt saved = receiptRepository.save(receipt);
        return saved.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Resource> getReceiptsByTransaction(Long transactionId) {
        Long currentUserId = currentUserService.getCurrentUserId();
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (!transaction.getUser().getId().equals(currentUserId)) {
            throw new UnauthorizedAccessException("Unauthorized: You don't have permission to view these receipts");
        }

        List<Receipt> receipts = receiptRepository.findByTransactionId(transactionId);
        return receipts.stream()
                .map(receipt -> fileStorageService.loadFileAsResource(receipt.getStoragePath()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Resource> getReceiptsByUser(Long userId) {
        Long currentUserId = currentUserService.getCurrentUserId();
        if (!currentUserId.equals(userId)) {
            throw new UnauthorizedAccessException("Unauthorized: You can only view your own receipts");
        }

        List<Receipt> receipts = receiptRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return receipts.stream()
                .map(receipt -> fileStorageService.loadFileAsResource(receipt.getStoragePath()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Resource getReceiptById(Long receiptId) {
        Receipt receipt = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt not found"));

        Long currentUserId = currentUserService.getCurrentUserId();
        if (!receipt.getUser().getId().equals(currentUserId)) {
            throw new UnauthorizedAccessException("Unauthorized: You don't have permission to view this receipt");
        }

        return fileStorageService.loadFileAsResource(receipt.getStoragePath());
    }

    @Override
    public void deleteReceipt(Long receiptId) {
        Receipt receipt = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt not found"));

        Long currentUserId = currentUserService.getCurrentUserId();
        if (!receipt.getUser().getId().equals(currentUserId)) {
            throw new UnauthorizedAccessException("Unauthorized: You don't have permission to delete this receipt");
        }

        fileStorageService.deleteFile(receipt.getStoragePath());
        receiptRepository.delete(receipt);
    }

    @Override
    public void deleteReceiptsByTransaction(Long transactionId) {
        Long currentUserId = currentUserService.getCurrentUserId();
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (!transaction.getUser().getId().equals(currentUserId)) {
            throw new UnauthorizedAccessException("Unauthorized: You don't have permission to delete these receipts");
        }

        List<Receipt> receipts = receiptRepository.findByTransactionId(transactionId);
        receipts.forEach(receipt -> fileStorageService.deleteFile(receipt.getStoragePath()));
        receiptRepository.deleteByTransactionId(transactionId);
    }
}