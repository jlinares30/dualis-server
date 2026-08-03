# Dualis API - Backend Financial Platform

Dualis API is an enterprise-grade RESTful backend service built for personal and couple financial management. The system enables individual users and partners to track accounts, manage categorized transactions, automate shared expense splitting using multiple algorithms, compute real-time budget limits, monitor debt balances, analyze financial health metrics, and synchronize data for offline-capable clients.

---

## Technology Stack

- Language: Java 21 (LTS)
- Framework: Spring Boot 3.4.2
- Security: Spring Security, JSON Web Tokens (JJWT 0.12.6), BCrypt Password Encoder
- Persistence: Spring Data JPA, Hibernate ORM
- Database: PostgreSQL
- Database Migrations: Flyway
- API Documentation: SpringDoc OpenAPI 3.0, Swagger UI
- Build Tool: Apache Maven
- Testing: JUnit 5, Mockito, Spring MockMvc

---

## System Architecture and Key Modules

### 1. Authentication and Security Module
- User registration with BCrypt password hashing.
- Stateless authentication issuing signed HMAC-SHA256 JSON Web Tokens (JWT).
- Bearer token authentication filter integrated into Spring Security filter chain.
- User profile management and currency preference settings.

### 2. Workspaces and Partner Linking Module
- Multi-tenant workspace architecture supporting `INDIVIDUAL` and `COUPLE` workspace types.
- Unique 6-character alphanumeric invitation code generator (e.g., `DUAL8X`).
- Role-based membership management (`OWNER`, `PARTNER`, `MEMBER`).
- Partner joining workflow via invitation code.

### 3. Accounts Management Module
- Support for financial account types: `DEBIT`, `CREDIT`, `CASH`, `INVESTMENT`.
- Initial balance tracking, active/inactive status management, and soft-delete capabilities.
- Inclusion flags for global workspace liquidity calculations.

### 4. Transaction Engine
- Support for `INCOME`, `EXPENSE`, and `TRANSFER` transaction types.
- Atomic balance impact calculation and balance reversal upon transaction update or deletion.
- Multi-criteria dynamic filtering using JPA Specifications (date range, type, category, account, search term).
- Pageable pagination and sorting.

### 5. Couple Split Engine
- Automated shared expense breakdown using four distinct strategies:
  - `EQUAL`: 50/50 equal distribution.
  - `PROPORTIONAL`: Dynamic distribution based on partner income ratios.
  - `CUSTOM_PERCENTAGE`: User-defined percentage split.
  - `FIXED_AMOUNT`: Fixed currency contribution by Partner A.
- Default split rule assignment per workspace.

### 6. Monthly Budgets Engine
- Category-level and workspace-level monthly spending limits.
- Real-time expense progress aggregation based on actual `EXPENSE` transactions within target month and year.
- Dynamic budget status thresholds:
  - `ON_TRACK`: Spent percentage < 80%
  - `WARNING`: Spent percentage between 80% and 100%
  - `EXCEEDED`: Spent percentage > 100%

### 7. Categories Management Module
- Global system default categories seeded via Flyway migrations (Housing, Groceries, Transport, Health, Entertainment, Utilities, Restaurants, Salary, Investments).
- Custom workspace category creation with custom icons, hex color codes, and type (`INCOME` / `EXPENSE`).
- Financial intelligence classification (`ESSENTIAL` vs `NON_ESSENTIAL`).
- Immutable system category protection rules.

### 8. Debt Settlement Engine
- Cumulative shared expense aggregation across partner transactions.
- Calculation of net partner debt balance factoring in active split rules.
- Settlement payment registration (`PENDING`, `COMPLETED`, `CANCELLED`) to offset debt balances.

### 9. Financial Health Dashboard Module
- Unified financial metrics endpoint for client dashboard visualization:
  - Total workspace liquidity balance across active accounts.
  - Monthly cash flow (Total Income vs Total Expenses).
  - Net savings and savings rate percentage (`(NetSavings / Income) * 100`).
  - Essential vs Non-Essential spending ratios.
  - Ranked category expense breakdown by spend volume.
  - Active and exceeded budget counts.

### 10. Data Synchronization Module
- Delta Pull Sync (`POST /api/v1/sync/pull`): Returns entities modified or created after client `lastSyncedAt` timestamp.
- Offline Push Sync (`POST /api/v1/sync/push`): Batch upload of transactions recorded while offline with balance adjustment execution.
- Conflict resolution strategy: Server-Last-Write-Wins using `updatedAt` timestamps.

---

## Database Migrations Schema (Flyway)

- `V1__create_accounts_table.sql`: Creates `accounts` schema and indexes.
- `V2__create_transactions_table.sql`: Creates `transactions` schema and search indexes.
- `V3__create_split_rules_table.sql`: Creates `split_rules` table for partner expense rules.
- `V4__create_budgets_table.sql`: Creates `budgets` table for monthly limit tracking.
- `V5__create_categories_table.sql`: Creates `categories` table and seeds default categories.
- `V6__create_workspaces_table.sql`: Creates `workspaces` and `workspace_members` tables.
- `V7__create_settlements_table.sql`: Creates `settlements` table for debt payments.
- `V8__create_users_table.sql`: Creates `users` table for security credentials.

---

## API Endpoints Reference

### Authentication (Public)
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/auth/register` | Register a new user account |
| POST | `/api/v1/auth/login` | Authenticate user credentials and issue JWT token |

### User Profile (Protected)
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/v1/auth/me` | Retrieve authenticated user profile |
| PUT | `/api/v1/auth/me` | Update user profile preferences |

### Workspaces (Protected)
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/workspaces` | Create individual or couple workspace |
| GET | `/api/v1/workspaces?userEmail={email}` | List workspaces for user |
| GET | `/api/v1/workspaces/{id}` | Get workspace details and members |
| PUT | `/api/v1/workspaces/{id}` | Update workspace details |
| DELETE | `/api/v1/workspaces/{id}` | Deactivate workspace (soft delete) |
| POST | `/api/v1/workspaces/{id}/invite` | Generate partner invitation code |
| POST | `/api/v1/workspaces/join` | Join couple workspace via invitation code |

### Accounts (Protected)
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/accounts` | Create financial account |
| GET | `/api/v1/accounts?workspaceId={id}` | List workspace accounts |
| GET | `/api/v1/accounts/{id}` | Get account details |
| PUT | `/api/v1/accounts/{id}` | Update account configuration |
| DELETE | `/api/v1/accounts/{id}` | Soft delete account |

### Transactions (Protected)
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/transactions` | Create income, expense, or transfer transaction |
| GET | `/api/v1/transactions` | Paginated search and specification filter |
| GET | `/api/v1/transactions/{id}` | Get transaction details |
| PUT | `/api/v1/transactions/{id}` | Update transaction and recalculate balance |
| DELETE | `/api/v1/transactions/{id}` | Delete transaction and revert balance |

### Split Rules (Protected)
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/split-rules` | Create split rule |
| GET | `/api/v1/split-rules?workspaceId={id}` | List workspace split rules |
| GET | `/api/v1/split-rules/{id}` | Get split rule details |
| PUT | `/api/v1/split-rules/{id}` | Update split rule |
| DELETE | `/api/v1/split-rules/{id}` | Delete split rule |
| POST | `/api/v1/split-rules/calculate` | Compute expense breakdown and debt summary |

### Budgets (Protected)
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/budgets` | Create monthly budget limit |
| GET | `/api/v1/budgets?workspaceId={id}` | List workspace monthly budgets |
| GET | `/api/v1/budgets/{id}` | Get budget details |
| GET | `/api/v1/budgets/{id}/progress` | Calculate real-time spending progress and status |
| PUT | `/api/v1/budgets/{id}` | Update budget limit |
| DELETE | `/api/v1/budgets/{id}` | Delete budget |

### Categories (Protected)
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/categories` | Create custom category |
| GET | `/api/v1/categories?workspaceId={id}` | List system defaults and custom categories |
| GET | `/api/v1/categories/{id}` | Get category details |
| PUT | `/api/v1/categories/{id}` | Update custom category |
| DELETE | `/api/v1/categories/{id}` | Delete custom category |

### Debt Settlements (Protected)
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/v1/settlements/balance-summary` | Get cumulative debt balance summary |
| POST | `/api/v1/settlements` | Register settlement payment |
| GET | `/api/v1/settlements?workspaceId={id}` | List settlement records |
| PATCH | `/api/v1/settlements/{id}/complete` | Mark settlement payment completed |
| DELETE | `/api/v1/settlements/{id}` | Cancel settlement record |

### Dashboard (Protected)
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/v1/dashboard/summary` | Get financial health metrics and analytics |

### Data Synchronization (Protected)
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/sync/pull` | Pull delta data modified since lastSyncedAt |
| POST | `/api/v1/sync/push` | Batch upload offline recorded transactions |

---

## Security Authorization Header

Protected endpoints require a valid JWT Access Token passed in the HTTP Authorization header:

```http
Authorization: Bearer <your_jwt_access_token>
```

Swagger UI documentation includes interactive JWT authorization at:
`http://localhost:8080/swagger-ui.html`

---

## Local Setup and Build Instructions

### Prerequisites
- JDK 21
- PostgreSQL 15+
- Apache Maven 3.9+

### Environment Configuration (`application.yaml`)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/dualis_db
    username: postgres
    password: postgres_password
  jpa:
    hibernate:
      ddl-auto: update
  flyway:
    enabled: true
    validate-on-migrate: false
```

### Build Commands
```bash
# Compile and run unit/integration tests
./mvnw clean test

# Build executable JAR package
./mvnw clean package -DskipTests

# Execute Spring Boot application
./mvnw spring-boot:run
```

---

## Testing Architecture

The project contains unit and integration tests covering all service logic and REST controllers:

- Service Unit Tests: `AccountServiceImplTest`, `TransactionServiceImplTest`, `SplitRuleServiceImplTest`, `BudgetServiceImplTest`, `CategoryServiceImplTest`, `WorkspaceServiceImplTest`, `SettlementServiceImplTest`, `DashboardServiceImplTest`, `AuthServiceImplTest`, `SyncServiceImplTest`.
- Controller Integration Tests: `AccountControllerTest`, `TransactionControllerTest`, `SplitRuleControllerTest`, `BudgetControllerTest`, `CategoryControllerTest`, `WorkspaceControllerTest`, `SettlementControllerTest`, `DashboardControllerTest`, `AuthControllerTest`, `SyncControllerTest`.

Run tests via command line:
```bash
./mvnw test
```
