# QueueLess — Real-Time Queue Management Platform

QueueLess is an enterprise-grade, full-stack real-time queue management and customer routing platform built for colleges, hospitals, retail banks, and government service centers.

---

## Architecture Overview

```
                                      QueueLess Platform
 ┌────────────────────────────────────────────────────────────────────────────────────────┐
 │                               Frontend (React 18 + Vite)                               │
 │                                                                                        │
 │  ┌───────────────────────┐  ┌───────────────────────┐  ┌────────────────────────────┐  │
 │  │   Customer Portal     │  │ Staff Counter Station │  │ Executive Admin Suite      │  │
 │  │ - Service directory   │  │ - Atomic 'Call Next'  │  │ - SLA Analytics & Charts   │  │
 │  │ - Token Generation    │  │ - Recall customer     │  │ - Org & Dept Provisioning  │  │
 │  │ - Live Wait Estimator │  │ - Start consultation  │  │ - Counter & Staff Routing  │  │
 │  │ - Dynamic Tracker     │  │ - Resolution audit    │  │ - Peak Hour Distributions  │  │
 │  └───────────┬───────────┘  └───────────┬───────────┘  └─────────────┬──────────────┘  │
 │              │                          │                            │                 │
 │              └──────────────────────────┼────────────────────────────┘                 │
 │                                         ▼                                              │
 │                   STOMP WebSockets (/ws) + REST APIs (/api)                            │
 └─────────────────────────────────────────┬──────────────────────────────────────────────┘
                                           │
 ┌─────────────────────────────────────────▼──────────────────────────────────────────────┐
 │                              Backend (Spring Boot 3.3.4)                               │
 │                                                                                        │
 │  ┌───────────────────────┐  ┌───────────────────────┐  ┌────────────────────────────┐  │
 │  │  Security & Auth      │  │ Queue Engine Service  │  │ Real-Time STOMP Broker     │  │
 │  │ - JWT Stateless Auth  │  │ - Concurrency control │  │ - /topic/org/{orgId}       │  │
 │  │ - BCrypt Password     │  │ - Priority FIFO       │  │ - /topic/service/{svcId}   │  │
 │  │ - Role Guards         │  │ - Dynamic Wait Math   │  │ - /topic/token/{tokenId}   │  │
 │  └───────────────────────┘  └───────────┬───────────┘  └────────────────────────────┘  │
 │                                         │                                              │
 │                                         ▼                                              │
 │                   Spring Data JPA + Hibernate 6 Community Dialect                      │
 └─────────────────────────────────────────┬──────────────────────────────────────────────┘
                                           │
                                           ▼
                                 SQLite Local Database
```

---

## Key Features

1. **Transactional Queue Engine & Concurrency Safety:**
   - **Priority-Weighted FIFO:** VIP and fast-track priority queues are elevated without starving regular patrons.
   - **Race Condition Prevention:** Atomic updates (`atomicCallToken`) ensure multiple staff clicking "Call Next" simultaneously can never double-allocate a token.
   - **Duplicate Protection:** Customers cannot spam multiple active tokens for the same service.
   - **Adaptive Wait Time Estimator:** Dynamically calculates estimated wait time based on queue depth and active counter headcount.

2. **Real-Time Synchronous Updates (Zero Polling):**
   - Embedded Spring WebSocket message broker broadcasts ticket transitions over STOMP topics.
   - Customer devices, staff desks, and waiting room screens automatically re-render without page refreshes.

3. **Three Specialized Persona Portals:**
   - **Customer Portal:** Browse service centers, select departments, generate digital tokens, monitor queue position in real-time, or cancel tickets.
   - **Staff Terminal:** View assigned counter, inspect waiting line, call next patient/client, broadcast voice recall alerts, start consultations, and record completion notes.
   - **Executive Administration Suite:** Full interactive analytics powered by Recharts (hourly volume, average wait time, average consultation time, no-show rate, counter throughput), organization provisioning, and staff scheduling.

4. **Public Waiting Room Monitor:**
   - Designed for high-resolution display monitors and TVs in waiting areas with clear "Now Calling" tiles and audible announcement notifications.

---

## Technology Stack

| Layer | Technology |
|---|---|
| **Backend** | Java 21, Spring Boot 3.3.4, Spring Security, Spring Data JPA, Hibernate 6 |
| **Real-Time** | Spring WebSockets, STOMP protocol, SockJS |
| **Database** | SQLite, SQLite-JDBC, Hibernate Community Dialects |
| **Security** | JWT (JSON Web Tokens), BCrypt password hashing |
| **Frontend** | React 18, Vite, Tailwind CSS, Lucide React, Recharts |
| **HTTP & WS Client** | Axios, @stomp/stompjs, sockjs-client |

---

## Demo Accounts & Seed Data

The application automatically seeds demonstration organizations, departments, counters, and users upon first boot:

| Role | Email | Password | Assigned Organization / Counter |
|---|---|---|---|
| **Admin** | `admin@queueless.com` | `admin123` | City General Hospital / Global Admin |
| **Staff** | `staff.sarah@hospital.com` | `staff123` | City General Hospital — Counter 1 (Primary Care) |
| **Staff** | `staff.alex@hospital.com` | `staff123` | City General Hospital — Counter 2 (Pediatrics) |
| **Staff** | `staff.david@college.com` | `staff123` | Apex Metropolitan University — Window 1 (Registrar) |
| **Customer** | `customer@queueless.com` | `customer123` | General Citizen / Patient |
| **Student** | `student.priya@college.com` | `student123` | Apex Metropolitan University Student |

---

## Running Locally

### Prerequisites
- **Java 21+** (`java -version`)
- **Maven 3.8+** (`mvn -version`)
- **Node.js 18+** (`node -v`) & **npm** (`npm -v`)

### 1. Start the Spring Boot Backend
```bash
cd backend
mvn clean package -DskipTests
java -jar target/queueless-backend-1.0.0.jar
```
*The backend starts at `http://localhost:8080`. SQLite database `queueless.db` is initialized automatically.*

### 2. Start the React Frontend
```bash
cd frontend
npm install
npm run dev
```
*The frontend development server opens at `http://localhost:5173` with proxy routing to the backend.*

---

## Running Tests

### Backend Unit & Concurrency Tests
```bash
cd backend
mvn test
```
Executes JUnit 5 and Mockito test suites covering authentication, token generation, concurrency race condition safeguards, lifecycle state transitions, and cancellation logic.

### Frontend Production Build
```bash
cd frontend
npm run build
```
Generates a minified, production-optimized Vite bundle into `frontend/dist/`.

---

## Project Structure

```
QueueLess/
├── backend/
│   ├── pom.xml
│   ├── src/main/java/com/queueless/
│   │   ├── QueueLessApplication.java
│   │   ├── config/          # Security, JWT, WebSockets, CORS
│   │   ├── controller/      # REST API Controllers (Auth, Customer, Staff, Admin, Public)
│   │   ├── dto/             # Data Transfer Objects & Request Validations
│   │   ├── entity/          # JPA Entities (User, Org, Dept, Counter, Token, History, Stats)
│   │   ├── exception/       # Centralized Global Exception Handlers
│   │   ├── loader/          # Seed Data Loader
│   │   ├── repository/      # Spring Data JPA Repositories
│   │   └── service/         # Queue Engine, Auth, Analytics, WebSocket Publisher
│   └── src/test/java/       # Mockito & JUnit 5 Test Suites
├── frontend/
│   ├── package.json
│   ├── vite.config.js       # Vite configuration with API & WS proxy
│   ├── tailwind.config.js   # Tailwind styling & dark mode configuration
│   └── src/
│       ├── api/             # Axios client & Interceptors
│       ├── context/         # Auth, Dark Mode, and STOMP WebSocket Providers
│       ├── components/      # UI components (Badges, StatCards, Modals, EmptyStates)
│       └── pages/           # Landing, Login, Register, Customer, Staff, Admin, PublicBoard
├── docs/
│   ├── architecture.md      # Architecture design, entity diagrams, concurrency models
│   └── api_spec.md          # REST API endpoints & WebSocket topic specifications
├── .gitignore
└── README.md
```
