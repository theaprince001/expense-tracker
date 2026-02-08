package com.expensetracker.service;

import com.expensetracker.dto.TransactionRequest;
import com.expensetracker.dto.TransactionResponse;

import java.util.List;

public interface TransactionService {


    TransactionResponse createTransaction(TransactionRequest request);

    List<TransactionResponse> getUserTransactions();

    TransactionResponse getTransactionById(Long id);

    TransactionResponse updateTransaction(Long id, TransactionRequest request);

    void deleteTransaction(Long id);



}
