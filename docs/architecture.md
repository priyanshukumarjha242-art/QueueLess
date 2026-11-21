# QueueLess Architecture & Database Design

## 1. System Overview
QueueLess is an enterprise-grade real-time queue management platform catering to universities, hospitals, banking institutions, and civic/government offices. It bridges customers, service counters, and administrators via low-latency STOMP WebSockets and an ACID-compliant transactional queue engine.

```
┌────────────────────────────────────────────────────────────────────────┐
│                          QueueLess Monorepo                            │
├───────────────────────────────────┬────────────────────────────────────┤
│         Frontend (Vite)           │          Backend (Spring)          │
│  React 18 + Tailwind + Recharts   │ Java 21, Spring Boot 3.3.4, JPA    │
│  - Customer Portal                │ - JWT Stateless Auth (BCrypt)      │
│  - Staff Counter Terminal         │ - Concurrency-Safe Queue Engine    │
│  - Executive Admin Suite          │ - Real-Time STOMP Broker (/ws)     │
│  - Live Public Display Board      │ - SQLite + Hibernate 6 Community   │
└───────────────────────────────────┴────────────────────────────────────┘
```

## 2. Core Entities & Relationships

1. **User** (`users`):
   - `id` (PK, Long)
   - `email` (Unique, String)
   - `password` (Hashed BCrypt, String)
   - `fullName` (String)
   - `phoneNumber` (String)
   - `role` (Enum: `CUSTOMER`, `STAFF`, `ADMIN`)
   - `organization_id` (FK -> `organizations.id`, Nullable for global users/customers)
   - `active` (Boolean)
   - `createdAt` (Instant)

2. **Organization** (`organizations`):
   - `id` (PK, Long)
   - `name` (String)
   - `code` (Unique slug, e.g. "METRO-BANK")
   - `type` (Enum: `COLLEGE`, `HOSPITAL`, `BANK`, `GOVERNMENT`, `OTHER`)
   - `description` (Text)
   - `address` (String)
   - `active` (Boolean)
   - `createdAt` (Instant)

3. **Department** (`departments`):
   - `id` (PK, Long)
   - `organization_id` (FK -> `organizations.id`)
   - `name` (String)
   - `code` (String, e.g. "CARD", "OPD", "REG")
   - `description` (Text)
   - `active` (Boolean)

4. **Service** (`services`):
   - `id` (PK, Long)
   - `department_id` (FK -> `departments.id`)
   - `name` (String)
   - `code` (String)
   - `prefix` (String, e.g. "A", "VIP", "DOC")
   - `estimatedDurationMinutes` (Integer, default e.g. 10)
   - `priorityLevel` (Integer, 1 = Highest, 5 = Lowest)
   - `active` (Boolean)

5. **ServiceCounter** (`service_counters`):
   - `id` (PK, Long)
   - `department_id` (FK -> `departments.id`)
   - `counterNumber` (String, e.g. "Counter 1", "Window B")
   - `currentStaff_id` (FK -> `users.id`, Nullable)
   - `active` (Boolean)

6. **StaffAssignment** (`staff_assignments`):
   - `id` (PK, Long)
   - `staff_id` (FK -> `users.id`)
   - `counter_id` (FK -> `service_counters.id`)
   - `service_id` (FK -> `services.id`, Nullable if counter supports all department services)
   - `assignedAt` (Instant)
   - `active` (Boolean)

7. **QueueToken** (`queue_tokens`):
   - `id` (PK, Long)
   - `tokenNumber` (String, e.g. "METRO-CARD-A042")
   - `organization_id` (FK -> `organizations.id`)
   - `department_id` (FK -> `departments.id`)
   - `service_id` (FK -> `services.id`)
   - `counter_id` (FK -> `service_counters.id`, Nullable until called)
   - `customer_id` (FK -> `users.id`, Nullable for walk-ins)
   - `customerName` (String)
   - `customerPhone` (String)
   - `priority` (Enum: `VIP`, `PRIORITY`, `REGULAR`)
   - `status` (Enum: `WAITING`, `CALLED`, `SERVING`, `COMPLETED`, `CANCELLED`, `NO_SHOW`)
   - `sequenceNumber` (Integer)
   - `createdAt` (Instant)
   - `calledAt` (Instant)
   - `servedAt` (Instant)
   - `completedAt` (Instant)
   - `estimatedWaitMinutes` (Integer)

8. **TokenHistory** (`token_history`):
   - `id` (PK, Long)
   - `token_id` (FK -> `queue_tokens.id`)
   - `fromStatus` (Enum TokenStatus)
   - `toStatus` (Enum TokenStatus)
   - `actionBy_id` (FK -> `users.id`)
   - `notes` (String)
   - `timestamp` (Instant)

9. **ServiceStatistics** (`service_statistics`):
   - `id` (PK, Long)
   - `service_id` (FK -> `services.id`)
   - `statDate` (LocalDate)
   - `totalGenerated` (Integer)
   - `totalServed` (Integer)
   - `totalCancelled` (Integer)
   - `totalNoShow` (Integer)
   - `avgWaitSeconds` (Long)
   - `avgServiceSeconds` (Long)

## 3. Concurrency & Queue Ordering Design
- **Priority-Weighted FIFO Algorithm**:
  Queue ordering sort: `priority DESC, createdAt ASC`. VIP tokens have priority level 3, PRIORITY level 2, REGULAR level 1.
- **Race Condition Prevention**:
  `callNextToken` uses atomic transitions within a `@Transactional(isolation = Isolation.READ_COMMITTED)` boundary. An atomic `UPDATE queue_tokens SET status = 'CALLED', counter_id = :counterId, calledAt = :now WHERE id = :tokenId AND status = 'WAITING'` guarantees that two staff members clicking "Call Next" cannot claim the same token.
- **Dynamic Waiting Time Calculation**:
  `estimatedWait = (number_of_people_ahead * avg_service_duration_minutes) / max(1, active_counters_for_service)`
