# Crypton - Spring Boot Application

Crypton is a secure crypto vault and wallet tracking application built with Spring Boot 3, Java 17, Maven, Spring Security, Spring Data JPA, Thymeleaf, and MySQL.

> [!NOTE]
> Balances and transactions are stored and tracked exclusively in the local database (`crypton_db`). No real blockchain network calls are made.

---

## 🛠 Tech Stack & Dependencies

- **Java**: 17
- **Framework**: Spring Boot 3.3.4
- **Build Tool**: Maven
- **Core Dependencies**:
  - `spring-boot-starter-web`: RESTful APIs and Spring MVC
  - `spring-boot-starter-security`: Authentication and authorization
  - `spring-boot-starter-data-jpa`: Object-relational mapping with Hibernate
  - `spring-boot-starter-thymeleaf`: Server-side HTML template rendering
  - `thymeleaf-extras-springsecurity6`: Security dialect tags for Thymeleaf
  - `mysql-connector-j`: Official MySQL JDBC driver
  - `lombok`: Annotation-driven boilerplate reduction (`@Getter`, `@Setter`, `@Builder`, etc.)
  - `spring-boot-starter-validation`: Jakarta Bean Validation
  - `spring-boot-devtools`: Fast application restart and live reload

---

## 🏛 Domain Model & Database Schema

The JPA entities in `com.crypton.model` map to the following relational design in MySQL:

| Entity | Table Name | Primary Key | Key Attributes | Relationships |
| :--- | :--- | :--- | :--- | :--- |
| **`User`** | `users` | `userId` | `name`, `email` (unique), `phone`, `passwordHash`, `biometricKeyRef`, `createdAt` | 1:N `Vault`, 1:N `AuditLog` |
| **`Vault`** | `vaults` | `vaultId` | `vaultName`, `encryptionKeyRef`, `createdAt` | N:1 `User`, 1:N `Asset` |
| **`Asset`** | `assets` | `assetId` | `coinType` (`BTC`, `ETH`, `SOL`, `USDT`), `balance` (`BigDecimal`), `walletAddress` | N:1 `Vault`, 1:N `Transaction` |
| **`Transaction`** | `transactions` | `txnId` | `type` (`DEPOSIT`, `WITHDRAW`), `amount` (`BigDecimal`), `status` (`PENDING`, `SUCCESS`, `FAILED`), `timestamp` | N:1 `Asset` |
| **`AuditLog`** | `audit_logs` | `logId` | `action`, `deviceInfo`, `timestamp` | N:1 `User` |

> [!IMPORTANT]
> The table for `Transaction` is named **`transactions`** because `TRANSACTION` is a reserved SQL keyword in MySQL.

---

## 📁 Package Architecture

Root package: `com.crypton`

```text
src/main/java/com/crypton/
├── CryptonApplication.java          # Spring Boot main entry point
├── config/                         # Configuration classes & Spring Beans
│   └── AppConfig.java
├── controller/                     # MVC & REST Controllers
│   └── HomeController.java         # Serves Thymeleaf UI (/)
├── dto/                            # Data Transfer Objects
├── exception/                      # Custom exceptions & global handlers
│   ├── ResourceNotFoundException.java
│   └── GlobalExceptionHandler.java
├── model/                          # JPA Entities & Enums
│   ├── User.java
│   ├── Vault.java
│   ├── Asset.java
│   ├── Transaction.java            # Table: transactions
│   ├── AuditLog.java               # Table: audit_logs
│   ├── CoinType.java               # Enum: BTC, ETH, SOL, USDT
│   ├── TransactionType.java        # Enum: DEPOSIT, WITHDRAW
│   └── TransactionStatus.java      # Enum: PENDING, SUCCESS, FAILED
├── repository/                     # Spring Data JPA Repositories
│   ├── UserRepository.java         # findByEmail, existsByEmail
│   ├── VaultRepository.java        # findByUser, findByUserUserId
│   ├── AssetRepository.java        # findByVault, findByVaultAndCoinType
│   ├── TransactionRepository.java  # findByAssetOrderByTimestampDesc
│   └── AuditLogRepository.java     # findByUserOrderByTimestampDesc
├── security/                       # Security filter chain configuration
│   └── SecurityConfig.java
├── service/                        # Service layer interfaces & impls
└── util/                           # Constants and utility helpers
    └── AppConstants.java
```

---

## ⚙️ Configuration (`application.properties`)

Located in [`src/main/resources/application.properties`](src/main/resources/application.properties):

```properties
# Server configuration
server.port=8080

# Local MySQL Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/crypton_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true
spring.datasource.username=${DB_USERNAME:your_username}
spring.datasource.password=${DB_PASSWORD:your_password}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA / Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false
spring.jpa.open-in-view=false

# Thymeleaf Configuration
spring.thymeleaf.cache=false
spring.thymeleaf.mode=HTML
```

---

## 🚀 How to Run the Application & Generate Tables

### 1. Ensure MySQL is running

```bash
brew services start mysql
```

### 2. Configure Credentials

Update your credentials in [`src/main/resources/application.properties`](src/main/resources/application.properties):
```properties
spring.datasource.username=root
spring.datasource.password=your_mysql_password
```
*Or pass environment variables:*
```bash
export DB_USERNAME=root
export DB_PASSWORD=your_mysql_password
```

### 3. Start the Application

```bash
cd /Users/tanvireddy/crypton
./mvnw spring-boot:run
```
*(Or `mvn spring-boot:run`)*

Because `spring.jpa.hibernate.ddl-auto=update` is enabled and `createDatabaseIfNotExist=true` is set on the JDBC connection, Hibernate will automatically connect to MySQL, create `crypton_db` if not present, and create all 5 tables:
- `users`
- `vaults`
- `assets`
- `transactions`
- `audit_logs`

### 4. Verify in MySQL

You can inspect the generated tables at any time:
```bash
mysql -u root -p crypton_db -e "SHOW TABLES; DESCRIBE users; DESCRIBE transactions;"
```
