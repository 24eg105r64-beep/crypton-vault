package com.crypton.service;

import com.crypton.model.Transaction;
import com.crypton.model.User;

import java.math.BigDecimal;
import java.util.List;

public interface TransactionService {

    Transaction deposit(Long assetId, BigDecimal amount, User user);

    Transaction withdraw(Long assetId, BigDecimal amount, User user);

    List<Transaction> getAssetTransactionHistory(Long assetId, User user);
}
