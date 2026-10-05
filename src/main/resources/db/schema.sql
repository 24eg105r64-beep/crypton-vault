-- ===================================================================
-- Crypton Database Schema for MySQL
-- Database: crypton_db
-- ===================================================================

CREATE DATABASE IF NOT EXISTS crypton_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE crypton_db;

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    phone VARCHAR(25),
    password_hash VARCHAR(255) NOT NULL,
    biometric_key_ref VARCHAR(255),
    failed_attempts INT NOT NULL DEFAULT 0,
    locked_until DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Vaults Table (User 1:N Vault)
CREATE TABLE IF NOT EXISTS vaults (
    vault_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    vault_name VARCHAR(100) NOT NULL,
    encryption_key_ref VARCHAR(255),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_vaults_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Assets Table (Vault 1:N Asset)
CREATE TABLE IF NOT EXISTS assets (
    asset_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vault_id BIGINT NOT NULL,
    coin_type VARCHAR(20) NOT NULL,
    balance DECIMAL(36, 18) NOT NULL,
    wallet_address VARCHAR(255),
    CONSTRAINT fk_assets_vault FOREIGN KEY (vault_id) REFERENCES vaults(vault_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Transactions Table (Asset 1:N Transaction)
-- Note: Named 'transactions' because 'transaction' is a reserved keyword in MySQL
CREATE TABLE IF NOT EXISTS transactions (
    txn_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    asset_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    amount DECIMAL(36, 18) NOT NULL,
    status VARCHAR(20) NOT NULL,
    timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transactions_asset FOREIGN KEY (asset_id) REFERENCES assets(asset_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Audit Logs Table (User 1:N AuditLog)
CREATE TABLE IF NOT EXISTS audit_logs (
    log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    action VARCHAR(100) NOT NULL,
    device_info VARCHAR(255),
    timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_logs_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
