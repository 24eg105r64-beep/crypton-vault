package com.crypton.dto.api;

import com.crypton.model.Asset;
import com.crypton.model.AuditLog;
import com.crypton.model.Transaction;
import com.crypton.model.User;
import com.crypton.model.Vault;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * JSON shapes sent to and from the React frontend.
 *
 * Entities are never returned directly: User contains the password hash and
 * Vault contains its encryption key reference, which must never leave the server.
 */
public final class ApiDtos {

    private ApiDtos() {
    }

    // ---------- Requests ----------

    public record LoginRequest(String email, String password) {
    }

    public record NameRequest(String name) {
    }

    public record AddAssetRequest(String coinType) {
    }

    public record AmountRequest(String amount) {
    }

    // ---------- Responses ----------

    public record UserView(Long id, String name, String email) {
        public static UserView of(User user) {
            return new UserView(user.getUserId(), user.getName(), user.getEmail());
        }
    }

    public record LoginResponse(String token, UserView user) {
    }

    public record MessageResponse(String message) {
    }

    public record VaultView(Long id, String name, LocalDateTime createdAt) {
        public static VaultView of(Vault vault) {
            return new VaultView(vault.getVaultId(), vault.getVaultName(), vault.getCreatedAt());
        }
    }

    /** Amounts are sent as plain strings (e.g. "1.5") so no precision is lost in JavaScript. */
    public static String plain(BigDecimal value) {
        if (value == null || value.signum() == 0) {
            return "0";
        }
        return value.stripTrailingZeros().toPlainString();
    }

    public record AssetView(Long id, String coinType, String balance, String walletAddress) {
        public static AssetView of(Asset asset) {
            return new AssetView(asset.getAssetId(), asset.getCoinType().name(),
                    plain(asset.getBalance()), asset.getWalletAddress());
        }
    }

    public record VaultDetailView(VaultView vault, List<AssetView> assets, List<String> coinTypes) {
    }

    public record TransactionView(Long id, String type, String amount, String status, LocalDateTime timestamp) {
        public static TransactionView of(Transaction txn) {
            return new TransactionView(txn.getTxnId(), txn.getType().name(), plain(txn.getAmount()),
                    txn.getStatus().name(), txn.getTimestamp());
        }
    }

    public record AssetDetailView(AssetView asset, VaultView vault, List<TransactionView> transactions) {
    }

    public record ActivityView(String action, LocalDateTime timestamp) {
        public static ActivityView of(AuditLog log) {
            return new ActivityView(log.getAction(), log.getTimestamp());
        }
    }

    public record DashboardView(UserView user,
                                Map<String, String> totals,
                                List<VaultView> vaults,
                                List<ActivityView> recentActivity) {
    }
}
