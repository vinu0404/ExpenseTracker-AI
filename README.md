# Kharcha — AI-Powered Expense Tracker

A microservices-based expense tracking application with AI-powered expense detection, admin dashboard, and centralized logging — all behind a Kong API Gateway.

---

## Architecture

```mermaid
flowchart TB
    subgraph Client
        FE["Frontend<br/>(kharcha.html)"]
        ADMIN["Admin Dashboard<br/>(admin_dashboard.html)"]
    end

    subgraph Gateway
        KONG["🦍 Kong API Gateway<br/>:9000"]
    end

    FE -- "All API calls" --> KONG
    ADMIN -- "All API calls" --> KONG

    subgraph AuthLayer["Authentication"]
        AUTH["Auth Service<br/>(Spring Boot :8080)"]
        AUTH_DB[("authservice<br/>MySQL")]
    end

    KONG -- "/auth/**, /ping" --> AUTH
    KONG -- "pre-function: /ping → validate JWT<br/>returns X-User-Id, X-User-Roles" -.-> AUTH
    AUTH --- AUTH_DB

    subgraph ExpenseLayer["Expense Management"]
        EXPENSE["Expense Store<br/>(Spring Boot :8083)"]
        EXPENSE_DB[("expenseService<br/>MySQL")]
    end

    KONG -- "/saveExpense, /expenses/admin" --> EXPENSE
    EXPENSE --- EXPENSE_DB

    subgraph AILayer["AI Processing"]
        AI["AI Service<br/>(FastAPI + Python :8000)"]
        LLM["OpenAI GPT-4o-mini"]
    end

    KONG -- "/api/v1/analyze" --> AI
    AI -- "structured extraction" --> LLM

    subgraph UserLayer["User Management"]
        USER["User Service<br/>(Spring Boot :8081)"]
        USER_DB[("userservice<br/>MySQL")]
    end

    USER --- USER_DB

    subgraph Messaging["Event Bus"]
        KAFKA["Kafka + Zookeeper"]
    end

    AUTH -- "produce: user-created-topic" --> KAFKA
    AI -- "produce: ai_service" --> KAFKA
    KAFKA -- "consume: user-created-topic" --> USER
    KAFKA -- "consume: ai_service" --> EXPENSE

    subgraph Monitoring["Observability"]
        PROMTAIL["Promtail"]
        LOKI["Loki"]
        GRAFANA["Grafana<br/>:3000"]
    end

    PROMTAIL -- "ship logs" --> LOKI
    LOKI --> GRAFANA

    style Gateway fill:#1a1a2e,stroke:#e94560,color:#fff
    style AuthLayer fill:#16213e,stroke:#0f3460,color:#fff
    style ExpenseLayer fill:#16213e,stroke:#0f3460,color:#fff
    style AILayer fill:#16213e,stroke:#0f3460,color:#fff
    style UserLayer fill:#16213e,stroke:#0f3460,color:#fff
    style Messaging fill:#1a1a2e,stroke:#e94560,color:#fff
    style Monitoring fill:#0a0a1a,stroke:#533483,color:#fff
```

---

## Request Flow

```mermaid
sequenceDiagram
    participant C as Client
    participant K as Kong :9000
    participant A as Auth Service :8080
    participant E as Expense Store :8083
    participant AI as AI Service :8000
    participant KF as Kafka
    participant U as User Service :8081

    Note over C,K: 1. Signup / Login (public)
    C->>K: POST /auth/signup or /auth/login
    K->>A: forward request
    A-->>K: JWT tokens
    K-->>C: tokens

    Note over C,K: 2. Any protected request
    C->>K: GET /saveExpense (+ Authorization header)
    K->>A: pre-function → GET /ping
    A-->>K: 200 + X-User-Id, X-User-Roles
    K->>E: forward + user-id header
    E-->>K: expenses (paginated)
    K-->>C: response

    Note over C,K: 3. AI Expense Detection
    C->>K: POST /api/v1/analyze (+ Authorization)
    K->>A: pre-function → GET /ping
    A-->>K: 200 + X-User-Id
    K->>AI: forward + user-id header
    AI->>AI: Call OpenAI GPT-4o-mini
    AI->>KF: produce to "ai_service" topic
    AI-->>K: parsed expense JSON
    K-->>C: response
    KF->>E: consume → save to DB

    Note over A,U: 4. User sync (async via Kafka)
    A->>KF: produce to "user-created-topic"
    KF->>U: consume → save user to user DB
```

---

## Services & Ports

| Service | Tech Stack | Internal Port | External Port | Description |
|---------|-----------|---------------|---------------|-------------|
| **Kong Gateway** | Kong 3.9 OSS (DB-less) | 8000 | **9000** | API gateway — single entry point for all client traffic. Pre-function Lua plugins validate JWT via `/ping`, inject `user-id` header, enforce admin role. |
| **Auth Service** | Spring Boot 3.4.3, Java 21 | 8080 | 8080 | Signup, login, JWT generation/validation, refresh tokens, admin user management. Seeds default `ROLE_USER` + `ROLE_ADMIN` roles. |
| **User Service** | Spring Boot 3.4.3, Java 21 | 8081 | 8081 | Stores user profile data. Consumes `user-created-topic` from Kafka to sync users created in Auth Service. |
| **Expense Store** | Spring Boot 3.4.3, Java 21 | 8083 | 8083 | CRUD for expenses. Consumes `ai_service` Kafka topic for AI-detected expenses. Admin endpoint for viewing any user's expenses. |
| **AI Service** | FastAPI, Python 3.11, OpenAI | 8000 | 8000 | Accepts natural language text, calls GPT-4o-mini with structured output to extract expense data (amount, merchant, currency, description, date). Produces to Kafka. |
| **MySQL** | MySQL 8.0 | 3306 | 3306 | Shared database for all Java services (single instance, separate tables). |
| **Kafka** | Confluent 7.6.0 | 9092 | 9092 | Event bus. Topics: `user-created-topic`, `ai_service`. |
| **Zookeeper** | Confluent 7.6.0 | 2181 | — | Kafka coordination. |
| **Loki** | Grafana Loki 3.4.2 | 3100 | 3100 | Log aggregation backend (TSDB storage, 7-day retention). |
| **Promtail** | Grafana Promtail 3.4.2 | — | — | Collects Docker container logs via socket and ships to Loki. |
| **Grafana** | Grafana 11.5.2 | 3000 | **3000** | Dashboards & log exploration. Pre-provisioned Kharcha Logs dashboard. |

---

## Kafka Topics

| Topic | Producer | Consumer | Payload |
|-------|----------|----------|---------|
| `user-created-topic` | Auth Service | User Service | `{ email, name, userName, userId }` |
| `ai_service` | AI Service | Expense Store | `{ amount, merchant, currency, description, created_at, user_id }` |

---

## Quick Start

```bash
# 1. Set your OpenAI API key
export OPENAI_API_KEY=sk-...

# 2. Build and start everything
docker compose up --build -d

# 3. Wait for Java services to boot, then open:
#    App:       http://localhost:9000  (via kharcha.html)
#    Admin:     http://localhost:9000  (via admin_dashboard.html)
#    Grafana:   http://localhost:3000  (admin / kharcha)
```

### Default Admin Credentials
| Field | Value |
|-------|-------|
| Username | `admin` |
| Password | `admin` |

The admin user is seeded automatically on first boot by `DataSeeder.java`.

---

## Project Structure

```
Kharcha/
├── AuthService/          # JWT auth, user registration, admin endpoints
├── UserService/          # User profile storage (Kafka consumer)
├── ExpenseStore/         # Expense CRUD + AI expense consumer
├── ai-service/           # FastAPI + OpenAI LLM integration
├── kong-gateway/         # Kong declarative config (kong.yml)
├── monitoring/
│   ├── loki/             # Loki log aggregation config
│   ├── promtail/         # Promtail log collector config
│   └── grafana/          # Grafana provisioning (datasource + dashboard)
├── kharcha.html          # Main expense tracker frontend
├── admin_dashboard.html  # Admin panel frontend
├── docker-compose.yml    # Full stack orchestration
└── README.md
```
