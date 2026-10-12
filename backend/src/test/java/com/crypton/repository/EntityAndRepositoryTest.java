package com.crypton.repository;

import com.crypton.model.Asset;
import com.crypton.model.AuditLog;
import com.crypton.model.CoinType;
import com.crypton.model.Transaction;
import com.crypton.model.TransactionStatus;
import com.crypton.model.TransactionType;
import com.crypton.model.User;
import com.crypton.model.Vault;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class EntityAndRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VaultRepository vaultRepository;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Test
    @DisplayName("Verify User, Vault, Asset, Transaction, and AuditLog persistence and repository finder methods")
    void testEntitiesAndRepositories() {
        // 1. Create and save User
        User user = User.builder()
                .name("Alice Nakamoto")
                .email("alice@crypton.local")
                .phone("+15551234567")
                .passwordHash("$2a$10$hashedPasswordValue")
                .biometricKeyRef("bio-key-ref-98765")
                .build();
        User savedUser = userRepository.save(user);
        assertThat(savedUser.getUserId()).isNotNull();

        // Test findByEmail
        Optional<User> foundUser = userRepository.findByEmail("alice@crypton.local");
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("Alice Nakamoto");

        // 2. Create and save Vault for User
        Vault vault = Vault.builder()
                .user(savedUser)
                .vaultName("Primary Cold Storage")
                .encryptionKeyRef("kms://vault/key-001")
                .build();
        Vault savedVault = vaultRepository.save(vault);
        assertThat(savedVault.getVaultId()).isNotNull();

        // Test findByUser
        List<Vault> userVaults = vaultRepository.findByUser(savedUser);
        assertThat(userVaults).hasSize(1);
        assertThat(userVaults.get(0).getVaultName()).isEqualTo("Primary Cold Storage");

        // 3. Create and save Asset for Vault
        Asset asset = Asset.builder()
                .vault(savedVault)
                .coinType(CoinType.BTC)
                .balance(new BigDecimal("2.500000000000000000"))
                .walletAddress("bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq")
                .build();
        Asset savedAsset = assetRepository.save(asset);
        assertThat(savedAsset.getAssetId()).isNotNull();

        // Test findByVault
        List<Asset> vaultAssets = assetRepository.findByVault(savedVault);
        assertThat(vaultAssets).hasSize(1);
        assertThat(vaultAssets.get(0).getCoinType()).isEqualTo(CoinType.BTC);
        assertThat(vaultAssets.get(0).getBalance()).isEqualByComparingTo("2.50");

        // 4. Create and save Transactions for Asset
        Transaction txn1 = Transaction.builder()
                .asset(savedAsset)
                .type(TransactionType.DEPOSIT)
                .amount(new BigDecimal("1.500000000000000000"))
                .status(TransactionStatus.SUCCESS)
                .timestamp(LocalDateTime.now().minusHours(2))
                .build();

        Transaction txn2 = Transaction.builder()
                .asset(savedAsset)
                .type(TransactionType.DEPOSIT)
                .amount(new BigDecimal("1.000000000000000000"))
                .status(TransactionStatus.SUCCESS)
                .timestamp(LocalDateTime.now().minusHours(1))
                .build();

        transactionRepository.save(txn1);
        transactionRepository.save(txn2);

        // Test findByAssetOrderByTimestampDesc
        List<Transaction> assetTxns = transactionRepository.findByAssetOrderByTimestampDesc(savedAsset);
        assertThat(assetTxns).hasSize(2);
        assertThat(assetTxns.get(0).getAmount()).isEqualByComparingTo("1.000000000000000000"); // most recent first
        assertThat(assetTxns.get(1).getAmount()).isEqualByComparingTo("1.500000000000000000");

        // 5. Create and save AuditLog for User
        AuditLog audit1 = AuditLog.builder()
                .user(savedUser)
                .action("USER_LOGIN")
                .deviceInfo("Chrome 128 / macOS 15")
                .timestamp(LocalDateTime.now().minusMinutes(30))
                .build();

        AuditLog audit2 = AuditLog.builder()
                .user(savedUser)
                .action("DEPOSIT_INITIATED")
                .deviceInfo("Chrome 128 / macOS 15")
                .timestamp(LocalDateTime.now().minusMinutes(5))
                .build();

        auditLogRepository.save(audit1);
        auditLogRepository.save(audit2);

        // Test findByUserOrderByTimestampDesc
        List<AuditLog> userLogs = auditLogRepository.findByUserOrderByTimestampDesc(savedUser);
        assertThat(userLogs).hasSize(2);
        assertThat(userLogs.get(0).getAction()).isEqualTo("DEPOSIT_INITIATED"); // most recent first
        assertThat(userLogs.get(1).getAction()).isEqualTo("USER_LOGIN");
    }
}
