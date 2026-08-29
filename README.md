# 📦 Rally Inventory Service

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-blue.svg)](https://www.postgresql.org/)
[![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-Distributed%20Events-black.svg)](https://kafka.apache.org/)
[![Docker](https://img.shields.io/badge/Docker-Containerized-2496ED.svg)](https://www.docker.com/)

The **Rally Inventory Service** is a high-throughput, event-driven microservice responsible for real-time stock management, atomic stock reservation/release workflows, and complete audit history tracking within the **Rally** group-buying and e-commerce platform.

---

## 📑 Table of Contents

- [Architecture & Core Concepts](#-architecture--core-concepts)
- [Key Features](#-key-features)
- [Tech Stack](#-tech-stack)
- [Database Schema & Migrations](#-database-schema--migrations)
- [Event-Driven Architecture (Kafka)](#-event-driven-architecture-kafka)
- [REST API Reference](#-rest-api-reference)
- [Configuration & Environment Variables](#-configuration--environment-variables)
- [Getting Started](#-getting-started)
  - [Prerequisites](#prerequisites)
  - [Running Locally](#running-locally)
  - [Running with Docker Compose](#running-with-docker-compose)
- [Testing](#-testing)

---

## 🏛 Architecture & Core Concepts

The Inventory Service maintains a strict stock consistency model with database-enforced integrity constraints and optimistic locking (`@Version`) to prevent race conditions during high-volume flash sales and group deal reservations.

### 📐 Stock Invariant
$$\text{Total Stock} = \text{Available Stock} + \text{Reserved Stock}$$

- **Available Stock**: Stock free to be reserved by incoming deals or orders.
- **Reserved Stock**: Stock temporarily held during active deal pooling or pending order checkout.
- **Total Stock**: Physical stock present in the warehouse.

```
                  ┌───────────────────────────────┐
                  │        Product Created        │
                  └──────────────┬────────────────┘
                                 │
                                 ▼
                     [Available: +N, Total: +N]
                                 │
                ┌────────────────┴────────────────┐
                │                                 │
     (Deal / Order Reserved)             (Direct Restock / Adjust)
                │                                 │
                ▼                                 ▼
      [Available: -N, Reserved: +N]      [Available: ±N, Total: ±N]
                │
        ┌───────┴────────┐
        │                │
(Completed/Success)  (Failed/Cancelled/Expired)
        │                │
        ▼                ▼
[Total: -N, Reserved: -N] [Available: +N, Reserved: -N]
```

---

## ✨ Key Features

- **Atomic Stock Reservation**: Supports multi-item atomic reservations (`/inventory/order-reserve`) ensuring all-or-nothing stock locking for cart checkouts.
- **Dual-Flow Support**: Handles both individual regular orders and collective group-deal lifecycles.
- **Audit History**: Every inventory modification (`CREATE`, `RESERVE`, `RELEASE`, `DEDUCT`, `RESTOCK`, `UPDATE`, `DELETE`) is recorded in `inventory_history` with timestamps.
- **Optimistic Concurrency Control**: Uses JPA `@Version` column to prevent lost updates under heavy concurrent requests.
- **Automated Flyway Migrations**: Ensures schema versioning and deterministic database initialization.

---

## 🛠 Tech Stack

| Component | Technology |
| :--- | :--- |
| **Language & Runtime** | Java 21 (Eclipse Temurin) |
| **Framework** | Spring Boot 4.1.0 (Web MVC, Data JPA, Actuator, Validation) |
| **Database** | PostgreSQL 17 |
| **Database Migrations** | Flyway |
| **Event Broker** | Apache Kafka |
| **JSON Deserialization** | Jackson |
| **Build Tool** | Apache Maven |
| **Containerization** | Docker, Docker Compose |

---

## 🗄 Database Schema & Migrations

Flyway automatically applies SQL migrations located in `src/main/resources/db/migration/`.

### `inventory` Table
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `product_id` | `UUID` | `PRIMARY KEY` | Product unique identifier |
| `total_stock` | `INTEGER` | `NOT NULL, CHECK (>= 0)` | Overall physical stock |
| `reserved_stock` | `INTEGER` | `NOT NULL, CHECK (>= 0)` | Currently reserved units |
| `available_stock` | `INTEGER` | `NOT NULL, CHECK (>= 0)` | Units available for sale |
| `version` | `BIGINT` | `NOT NULL DEFAULT 0` | Optimistic locking version |
| `updated_at` | `TIMESTAMP` | `NOT NULL DEFAULT NOW()` | Last update timestamp |

### `inventory_history` Table
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | `BIGSERIAL` | `PRIMARY KEY` | History entry unique ID |
| `product_id` | `UUID` | `NOT NULL, INDEXED` | Referenced product ID |
| `operation_type` | `VARCHAR(50)` | `NOT NULL` | Operation enum value |
| `quantity` | `INTEGER` | `NOT NULL, CHECK (> 0)` | Quantity affected |
| `created_at` | `TIMESTAMP` | `NOT NULL DEFAULT NOW()` | Entry creation timestamp |

---

## ⚡ Event-Driven Architecture (Kafka)

The service consumes events across multiple topics to react in real time to product, deal, and order state transitions:

| Topic | Event Class | Action Triggered |
| :--- | :--- | :--- |
| `product-created` | `ProductCreatedEvent` | Creates new inventory record (`CREATE`) |
| `product-deleted` | `ProductDeletedEvent` | Logs soft deletion in history (`DELETE`) |
| `deal-created` | `DealCreatedEvent` | Reserves stock for deal (`RESERVE`) |
| `deal-succeeded` | `DealSucceededEvent` | Deducts reserved stock upon deal completion (`DEDUCT`) |
| `deal-cancelled` | `DealCancelledEvent` | Releases reserved stock back to available (`RELEASE`) |
| `deal-expired` | `DealExpiredEvent` | Releases reserved stock back to available (`RELEASE`) |
| `deal-failed` | `DealFailedEvent` | Releases reserved stock back to available (`RELEASE`) |
| `order.lifecycle` | `OrderCreatedEvent` | Deducts reserved stock upon order confirmation (`DEDUCT`) |
| `order.lifecycle` | `OrderNormalCancelledEvent` | Releases reserved stock upon order cancellation / payment failure (`RELEASE`) |
| `order-completed` | `OrderCompletedEvent` | Legacy single-product order completion (`DEDUCT`) |
| `order-cancelled` | `OrderCancelledEvent` | Legacy single-product order cancellation (`RELEASE`) |

---

## 🚀 REST API Reference

Base path: `/inventory` (Default Port: `8087`)

### 1. Get Single Inventory
- **Endpoint**: `GET /inventory/{productId}`
- **Response**: `200 OK`
```json
{
  "productId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "totalStock": 100,
  "reservedStock": 20,
  "availableStock": 80,
  "version": 1,
  "updatedAt": "2026-08-28T16:00:00Z"
}
```

---

### 2. Bulk Get Inventory
- **Endpoint**: `POST /inventory/bulk`
- **Request Body**:
```json
[
  "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d"
]
```
- **Response**: `200 OK`
```json
{
  "3fa85f64-5717-4562-b3fc-2c963f66afa6": {
    "productId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "totalStock": 100,
    "reservedStock": 20,
    "availableStock": 80,
    "version": 1,
    "updatedAt": "2026-08-28T16:00:00Z"
  }
}
```

---

### 3. Multi-Item Order Reservation (Batch Call)
- **Endpoint**: `POST /inventory/order-reserve`
- **Behavior**: Atomic multi-item batch check. If all items have sufficient stock, reserves the stock and returns `reserved: true`. If any item is unavailable, leaves the database completely untouched (no partial reservation) and returns `reserved: false` for the unavailable item(s) to allow Order Service to cancel the order.
- **Request Body**:
```json
{
  "orderId": "550e8400-e29b-41d4-a716-446655440000",
  "items": [
    {
      "productId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "quantity": 2
    },
    {
      "productId": "c0911222-5717-4562-b3fc-2c963f66afa6",
      "quantity": 1
    }
  ]
}
```
- **Response — 200 OK (Success)**:
```json
{
  "orderId": "550e8400-e29b-41d4-a716-446655440000",
  "items": [
    {
      "productId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "available": 80,
      "reserved": true
    },
    {
      "productId": "c0911222-5717-4562-b3fc-2c963f66afa6",
      "available": 10,
      "reserved": true
    }
  ]
}
```
- **Response — 200 OK (Shortage / Atomic Rollback)**:
```json
{
  "orderId": "550e8400-e29b-41d4-a716-446655440000",
  "items": [
    {
      "productId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "available": 80,
      "reserved": true
    },
    {
      "productId": "c0911222-5717-4562-b3fc-2c963f66afa6",
      "available": 0,
      "reserved": false
    }
  ]
}
```


---

### 4. Deal Stock Operations
- **Reserve Deal Stock**: `POST /inventory/{productId}/reserve-deal`
- **Release Deal Stock**: `POST /inventory/{productId}/release-deal`
- **Request Body**:
```json
{
  "quantity": 10
}
```
- **Response**: `200 OK`

---

### 5. Order Stock Operations
- **Reserve Order Stock**: `POST /inventory/{productId}/reserve-order`
- **Release Order Stock**: `POST /inventory/{productId}/release-order`
- **Request Body**:
```json
{
  "quantity": 1
}
```
- **Response**: `200 OK`

---

### 6. Restock Inventory
- **Endpoint**: `PATCH /inventory/{productId}/restock`
- **Request Body**:
```json
{
  "quantity": 50
}
```
- **Response**: `200 OK`
```json
{
  "message": "Inventory restocked successfully."
}
```

---

### 7. Adjust Inventory
- **Endpoint**: `PATCH /inventory/{productId}/adjust`
- **Request Body**:
```json
{
  "adjustment": -5,
  "reason": "Damaged goods in transit"
}
```
- **Response**: `200 OK`
```json
{
  "message": "Inventory adjusted successfully."
}
```

---

### 8. Delete Inventory
- **Endpoint**: `DELETE /inventory/{productId}`
- **Response**: `204 No Content`

---

## ⚙ Configuration & Environment Variables

| Variable | Description | Default Value | Docker Compose Value |
| :--- | :--- | :--- | :--- |
| `DB_HOST` | PostgreSQL hostname | `localhost` | `inventory-postgres` |
| `DB_PORT` | PostgreSQL port | `5436` | `5432` |
| `DB_NAME` | Database name | `inventory` | `inventory` |
| `DB_USER` | Database username | `inventory_user` | `inventory_user` |
| `DB_PASSWORD` | Database password | `inventory_password` | `inventory_password` |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka broker address | `localhost:9092` | `kafka:29092` |
| `server.port` | Application HTTP port | `8087` | `8087` |

---

## 🏁 Getting Started

### Prerequisites
- **Java 21 JDK** installed
- **Maven 3.9+** (or use the bundled `./mvnw`)
- **Docker & Docker Compose**
- Active external Docker network (if running within Rally ecosystem):
  ```bash
  docker network create groupdeal-net
  ```

---

### Running Locally

1. **Start the backing PostgreSQL database**:
   ```bash
   docker run -d \
     --name inventory-postgres \
     -e POSTGRES_DB=inventory \
     -e POSTGRES_USER=inventory_user \
     -e POSTGRES_PASSWORD=inventory_password \
     -p 5436:5432 \
     postgres:17
   ```

2. **Start Kafka** (if not already running).

3. **Run the Spring Boot application**:
   ```bash
   # Windows (PowerShell / CMD)
   .\mvnw spring-boot:run

   # Linux / macOS
   ./mvnw spring-boot:run
   ```

---

### Running with Docker Compose

Ensure the `groupdeal-net` shared network is created:
```bash
docker network create groupdeal-net
```

Start the services:
```bash
docker compose up -d --build
```

View logs:
```bash
docker compose logs -f inventory-service
```

Stop services:
```bash
docker compose down
```

---

## 🧪 Testing

Run automated tests via the Maven wrapper:

```bash
# Windows
.\mvnw test

# Linux / macOS
./mvnw test
```
