# QueueLess API Specification

This document details the REST endpoints and WebSocket protocols exposed by the QueueLess backend platform.

---

## Base URLs
- **REST Endpoints:** `http://localhost:8080/api`
- **WebSocket STOMP Broker:** `ws://localhost:8080/ws` (or fallback via SockJS `http://localhost:8080/ws`)

---

## 1. Authentication (`/api/auth`)

### 1.1 Register User
- **Method:** `POST /api/auth/register`
- **Access:** Public
- **Request Body:**
```json
{
  "fullName": "Dr. Victoria Vance",
  "email": "admin@queueless.com",
  "password": "securepassword",
  "phoneNumber": "+1-555-0199",
  "role": "ROLE_ADMIN",
  "organizationId": 1
}
```
- **Response:** `200 OK`
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "id": 1,
  "email": "admin@queueless.com",
  "fullName": "Dr. Victoria Vance",
  "role": "ROLE_ADMIN",
  "organizationId": 1,
  "organizationName": "City General Hospital"
}
```

### 1.2 User Login
- **Method:** `POST /api/auth/login`
- **Access:** Public
- **Request Body:**
```json
{
  "email": "admin@queueless.com",
  "password": "admin123"
}
```
- **Response:** `200 OK` (returns JWT token and user profile)

### 1.3 Current User Profile
- **Method:** `GET /api/auth/me`
- **Access:** Authenticated (`Bearer <token>`)

---

## 2. Public Endpoints (`/api/public`)

### 2.1 List Active Organizations
- **Method:** `GET /api/public/organizations`
- **Access:** Public

### 2.2 Organization Details
- **Method:** `GET /api/public/organizations/{id}`
- **Access:** Public

### 2.3 List Departments by Organization
- **Method:** `GET /api/public/organizations/{orgId}/departments`
- **Access:** Public

### 2.4 List Services by Organization
- **Method:** `GET /api/public/organizations/{orgId}/services`
- **Access:** Public

### 2.5 Live Public Queue Display Board
- **Method:** `GET /api/public/board/{organizationId}`
- **Access:** Public
- **Description:** Real-time snapshot of active calling tokens, waiting queue, and recently completed tokens.

---

## 3. Customer Operations (`/api/customer`)

### 3.1 Generate Queue Token
- **Method:** `POST /api/customer/tokens`
- **Access:** Authenticated Customer / Guest
- **Request Body:**
```json
{
  "serviceId": 1,
  "customerName": "Marcus Brody",
  "customerPhone": "+1-555-0344",
  "priority": "REGULAR" // Options: REGULAR, PRIORITY, VIP
}
```
- **Response:** `200 OK`
```json
{
  "id": 11,
  "tokenNumber": "OPD-GP-011",
  "organizationId": 1,
  "organizationName": "City General Hospital",
  "departmentName": "Outpatient Department",
  "serviceName": "General Physician Consultation",
  "priority": "VIP",
  "status": "WAITING",
  "estimatedWaitMinutes": 10,
  "peopleAhead": 0
}
```

### 3.2 List Customer's Personal Tokens
- **Method:** `GET /api/customer/tokens`
- **Access:** Authenticated Customer

### 3.3 Cancel Active Token
- **Method:** `POST /api/customer/tokens/{id}/cancel`
- **Access:** Authenticated Customer / Admin
- **Request Body:** `{"notes": "Changed schedule"}`

---

## 4. Staff Operations (`/api/staff`)

### 4.1 Get Assigned Counter
- **Method:** `GET /api/staff/my-counter`
- **Access:** Authenticated Staff

### 4.2 Get Department Waiting Queue
- **Method:** `GET /api/staff/waiting-tokens?departmentId={id}`
- **Access:** Authenticated Staff

### 4.3 Call Next Customer
- **Method:** `POST /api/staff/call-next?counterId={counterId}`
- **Access:** Authenticated Staff
- **Description:** Atomically fetches and claims the highest-priority, earliest-waiting token. Throws `409 Conflict` if counter is occupied.

### 4.4 Recall Customer
- **Method:** `POST /api/staff/tokens/{id}/recall?counterId={counterId}`
- **Access:** Authenticated Staff

### 4.5 Start Serving Customer
- **Method:** `POST /api/staff/tokens/{id}/start-serving?counterId={counterId}`
- **Access:** Authenticated Staff

### 4.6 Complete Service
- **Method:** `POST /api/staff/tokens/{id}/complete?counterId={counterId}`
- **Access:** Authenticated Staff
- **Request Body:** `{"notes": "Prescription issued"}`

### 4.7 Mark No-Show / Absent
- **Method:** `POST /api/staff/tokens/{id}/no-show?counterId={counterId}`
- **Access:** Authenticated Staff
- **Request Body:** `{"notes": "Customer absent after 3 announcements"}`

---

## 5. Executive Administration (`/api/admin`)

### 5.1 Real-Time Analytics & SLA Metrics
- **Method:** `GET /api/admin/analytics/{orgId}?days={1|7|30}`
- **Access:** Role: `ADMIN`
- **Response:**
  - Total tokens generated
  - Currently waiting count
  - Average wait time (minutes)
  - Average service time (minutes)
  - Completed tokens, cancelled tokens, no-show rate (%)
  - Hourly demand distribution (08:00 - 18:00)
  - Breakdown by service and counter

### 5.2 Department & Service Management
- `POST /api/admin/departments`: Create department
- `PUT /api/admin/departments/{id}`: Update department
- `POST /api/admin/services`: Create service with priority and estimated duration
- `PUT /api/admin/services/{id}`: Update service settings

### 5.3 Counter & Staff Assignment
- `POST /api/admin/counters`: Create counter
- `POST /api/admin/counters/assign`: Assign staff member to counter
- `POST /api/admin/counters/{id}/unassign`: Unassign staff from counter
- `POST /api/admin/staff`: Create new staff account
- `PATCH /api/admin/users/{userId}/toggle-status`: Activate/deactivate user

---

## 6. Real-Time WebSockets (STOMP Topics)

| Destination | Payload | Purpose |
|-------------|---------|---------|
| `/topic/org/{orgId}` | `TokenDto` | Organization-wide board updates (monitor screen) |
| `/topic/service/{serviceId}` | `TokenDto` | Service line movements |
| `/topic/token/{tokenId}` | `TokenDto` | Individual customer live queue tracking |
| `/topic/counter/{counterId}` | `TokenDto` | Counter desk updates |
