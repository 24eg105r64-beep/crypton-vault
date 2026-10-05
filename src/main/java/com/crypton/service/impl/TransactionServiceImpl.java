package com.crypton.service.impl;

import com.crypton.exception.InsufficientBalanceException;
import com.crypton.exception.InvalidAmountException;
import com.crypton.model.Asset;
import com.crypton.model.Transaction;
import com.crypton.model.TransactionStatus;
import com.crypton.model.TransactionType;
import com.crypton.model.User;
import com.crypton.repository.AssetRepository;
import com.crypton.repository.TransactionRepository;
import com.crypton.service.AssetService;
import com.crypton.service.AuditService;
import com.crypton.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    public static final int MAX_DECIMAL_PLACES = 8;

    private final TransactionRepository transactionRepository;
    private final AssetRepository assetRepository;
    private final AssetService assetService;
    private final AuditService auditService;

    @Override
    @Transactional(noRollbackFor = {InsufficientBalanceException.class, InvalidAmountException.class})
    public Transaction deposit(Long assetId, BigDecimal amount, User user) {
        validateAmount(amount);

        // Validates asset exists and belongs to the user
        Asset asset = assetService.getAssetById(assetId, user);

        asset.setBalance(asset.getBalance().add(amount));
        assetRepository.save(asset);

        Transaction txn = Transaction.builder()
                .asset(asset)
                .type(TransactionType.DEPOSIT)
                .amount(amount)
                .status(TransactionStatus.SUCCESS)
                .timestamp(LocalDateTime.now())
                .build();

        Transaction savedTxn = transactionRepository.save(txn);
        auditService.logAction(user, "DEPOSIT");
        return savedTxn;
    }

    @Override
    @Transactional(noRollbackFor = {InsufficientBalanceException.class, InvalidAmountException.class})
    public Transaction withdraw(Long assetId, BigDecimal amount, User user) {
        validateAmount(amount);

        // Validates asset exists and belongs to the user
        Asset asset = assetService.getAssetById(assetId, user);

        // Check if balance is sufficient
        if (asset.getBalance().compareTo(amount) < 0) {
            Transaction failedTxn = Transaction.builder()
                    .asset(asset)
                    .type(TransactionType.WITHDRAW)
                    .amount(amount)
                    .status(TransactionStatus.FAILED)
                    .timestamp(LocalDateTime.now())
                    .build();

            transactionRepository.save(failedTxn);

            throw new InsufficientBalanceException(String.format(
                    "Insufficient balance: current balance is %s %s, requested withdrawal is %s",
                    asset.getBalance().toPlainString(), asset.getCoinType(), amount.toPlainString()
            ));
        }

        asset.setBalance(asset.getBalance().subtract(amount));
        assetRepository.save(asset);

        Transaction txn = Transaction.builder()
                .asset(asset)
                .type(TransactionType.WITHDRAW)
                .amount(amount)
                .status(TransactionStatus.SUCCESS)
                .timestamp(LocalDateTime.now())
                .build();

        Transaction savedTxn = transactionRepository.save(txn);
        auditService.logAction(user, "WITHDRAW");
        return savedTxn;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> getAssetTransactionHistory(Long assetId, User user) {
        Asset asset = assetService.getAssetById(assetId, user);
        return transactionRepository.findByAssetOrderByTimestampDesc(asset);
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new InvalidAmountException("Amount cannot be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Amount must be greater than zero");
        }
        if (amount.stripTrailingZeros().scale() > MAX_DECIMAL_PLACES) {
            throw new InvalidAmountException("Amount cannot have more than " + MAX_DECIMAL_PLACES + " decimal places");
        }
    }
}
