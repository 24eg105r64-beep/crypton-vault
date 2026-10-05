package com.crypton.service;

import com.crypton.exception.AccessDeniedException;
import com.crypton.exception.InsufficientBalanceException;
import com.crypton.exception.InvalidAmountException;
import com.crypton.model.Asset;
import com.crypton.model.CoinType;
import com.crypton.model.Transaction;
import com.crypton.model.TransactionStatus;
import com.crypton.model.TransactionType;
import com.crypton.model.User;
import com.crypton.model.Vault;
import com.crypton.repository.AssetRepository;
import com.crypton.repository.TransactionRepository;
import com.crypton.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private AssetService assetService;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private User owner;
    private User otherUser;
    private Vault vault;
    private Asset btcAsset;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .userId(1L)
                .name("Alice")
                .email("alice@crypton.local")
                .build();

        otherUser = User.builder()
                .userId(2L)
                .name("Bob")
                .email("bob@crypton.local")
                .build();

        vault = Vault.builder()
                .vaultId(10L)
                .vaultName("Alice Cold Vault")
                .user(owner)
                .build();

        btcAsset = Asset.builder()
                .assetId(100L)
                .vault(vault)
                .coinType(CoinType.BTC)
                .balance(new BigDecimal("2.50000000"))
                .walletAddress("0x1234567890abcdef1234567890abcdef12345678")
                .build();
    }

    @Test
    @DisplayName("Deposit adds amount to asset balance, saves SUCCESS transaction, and logs DEPOSIT")
    void testDepositSuccess() {
        when(assetService.getAssetById(100L, owner)).thenReturn(btcAsset);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BigDecimal depositAmount = new BigDecimal("1.25000000");
        Transaction result = transactionService.deposit(100L, depositAmount, owner);

        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(result.getStatus()).isEqualTo(TransactionStatus.SUCCESS);
        assertThat(result.getAmount()).isEqualByComparingTo(depositAmount);

        // Verify balance was increased
        assertThat(btcAsset.getBalance()).isEqualByComparingTo(new BigDecimal("3.75000000"));
        verify(assetRepository).save(btcAsset);
        verify(auditService).logAction(owner, "DEPOSIT");
    }

    @Test
    @DisplayName("Withdraw subtracts amount from balance, saves SUCCESS transaction, and logs WITHDRAW")
    void testWithdrawSuccess() {
        when(assetService.getAssetById(100L, owner)).thenReturn(btcAsset);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BigDecimal withdrawAmount = new BigDecimal("1.00000000");
        Transaction result = transactionService.withdraw(100L, withdrawAmount, owner);

        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(TransactionType.WITHDRAW);
        assertThat(result.getStatus()).isEqualTo(TransactionStatus.SUCCESS);
        assertThat(result.getAmount()).isEqualByComparingTo(withdrawAmount);

        // Verify balance was decreased
        assertThat(btcAsset.getBalance()).isEqualByComparingTo(new BigDecimal("1.50000000"));
        verify(assetRepository).save(btcAsset);
        verify(auditService).logAction(owner, "WITHDRAW");
    }

    @Test
    @DisplayName("Withdraw with insufficient balance throws InsufficientBalanceException and saves FAILED transaction")
    void testWithdrawInsufficientBalance() {
        when(assetService.getAssetById(100L, owner)).thenReturn(btcAsset);

        BigDecimal excessiveAmount = new BigDecimal("10.00000000");

        assertThatThrownBy(() -> transactionService.withdraw(100L, excessiveAmount, owner))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessageContaining("Insufficient balance");

        // Verify balance was NOT modified
        assertThat(btcAsset.getBalance()).isEqualByComparingTo(new BigDecimal("2.50000000"));
        verify(assetRepository, never()).save(btcAsset);

        // Verify a FAILED transaction was saved
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());
        Transaction savedTxn = captor.getValue();
        assertThat(savedTxn.getType()).isEqualTo(TransactionType.WITHDRAW);
        assertThat(savedTxn.getStatus()).isEqualTo(TransactionStatus.FAILED);
        assertThat(savedTxn.getAmount()).isEqualByComparingTo(excessiveAmount);

        // Audit log should not record a successful WITHDRAW
        verify(auditService, never()).logAction(owner, "WITHDRAW");
    }

    @Test
    @DisplayName("Deposit with amount <= 0 throws InvalidAmountException")
    void testDepositZeroOrNegativeAmount() {
        assertThatThrownBy(() -> transactionService.deposit(100L, BigDecimal.ZERO, owner))
                .isInstanceOf(InvalidAmountException.class)
                .hasMessageContaining("greater than zero");

        assertThatThrownBy(() -> transactionService.deposit(100L, new BigDecimal("-0.5"), owner))
                .isInstanceOf(InvalidAmountException.class)
                .hasMessageContaining("greater than zero");
    }

    @Test
    @DisplayName("Withdraw with more than 8 decimal places throws InvalidAmountException")
    void testWithdrawExcessiveDecimalPlaces() {
        BigDecimal tooManyDecimals = new BigDecimal("0.123456789"); // 9 decimal places

        assertThatThrownBy(() -> transactionService.withdraw(100L, tooManyDecimals, owner))
                .isInstanceOf(InvalidAmountException.class)
                .hasMessageContaining("cannot have more than 8 decimal places");
    }

    @Test
    @DisplayName("Accessing another user's asset for deposit throws AccessDeniedException")
    void testDepositAnotherUserAssetThrowsAccessDenied() {
        when(assetService.getAssetById(100L, otherUser))
                .thenThrow(new AccessDeniedException("Access denied: You do not own asset with id 100"));

        assertThatThrownBy(() -> transactionService.deposit(100L, new BigDecimal("1.0"), otherUser))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Access denied");

        verify(assetRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("List asset transaction history delegates to repository ordered by timestamp desc")
    void testGetAssetTransactionHistory() {
        when(assetService.getAssetById(100L, owner)).thenReturn(btcAsset);
        Transaction txn1 = Transaction.builder().txnId(1L).asset(btcAsset).build();
        when(transactionRepository.findByAssetOrderByTimestampDesc(btcAsset)).thenReturn(List.of(txn1));

        List<Transaction> history = transactionService.getAssetTransactionHistory(100L, owner);

        assertThat(history).hasSize(1);
        assertThat(history.get(0).getTxnId()).isEqualTo(1L);
    }
}
