# Microservices-Based-ticketing-system
A distributed, event-driven full-stack ticketing engine engineered with Java and Spring Boot to handle intense traffic spikes during flash sales. The system leverages Redis for atomic distributed in-memory seat locks, PostgreSQL for a durable relational transaction ledger, and Apache Kafka to handle booking confirmations asynchronously.

---

## 🏗️ System Architecture

The platform is built as a decoupled, asynchronous microservices architecture to segregate high-speed inventory reservations from heavy, blocking I/O processing operations.

```text
               ┌────────────────────────────────────────┐
               │          Interactive Client SPA        │
               │       (HTML5 / Tailwind CSS / JS)      │
               └───────────────────┬────────────────────┘
                                   │
                      HTTP REST    │   CORS Enabled
                     API Requests  │   (Ports 8081 / 8084)
                                   ▼
               ┌────────────────────────────────────────┐
               │             Booking Service            │
               │          (Spring Boot on 8081)         │
               └───────────┬────────────────┬───────────┘
                           │                │
       Distributed Locks   │                │   Relational Schema
      Atomic SETNX (10m)   │                │   Data Persistence
                           ▼                ▼
               ┌───────────────┐        ┌───────────────┐
               │  Redis Cache  │        │  PostgreSQL   │
               │  (Port 6379)  │        │  (Port 5433)  │
               └───────────────┘        └───────────────┘
                                   │
                                   │ Event-Driven Ingestion
                                   │ (booking-initiated-topic)
                                   ▼
               ┌────────────────────────────────────────┐
               │              Apache Kafka              │
               │         Message Broker (Port 9092)     │
               └───────────────────┬────────────────────┘
                                   │
                                   │ Asynchronous Pull Stream
                                   ▼
               ┌────────────────────────────────────────┐
               │          Notification Service          │
               │          (Spring Boot on 8084)         │
               └───────────────────┬────────────────────┘
                                   │
                                   │ Secure SMTP Handshake
                                   ▼
               ┌────────────────────────────────────────┐
               │           Google SMTP Server           │
               │       (Live Email Ticket Delivery)     │
               └────────────────────────────────────────┘
```
              
### 🔹 Components:
* **Booking Service (`booking-service`):** Exposes high-throughput RESTful endpoints handling core seating metrics, dynamic surge calculations, and lock acquisitions.
* **Notification Service (`notification-service`):** An independent event consumer containing non-blocking Kafka listener threads that execute secure SMTP handshakes to dispatch single-invoice unified summaries to user inboxes.
* **Storage Layer Hierarchy:**
  * **Redis (Transient Memory Node):** Operates as a single-threaded distributed lock coordinator executing fast lookups to isolate concurrent contention pathways.
  * **PostgreSQL (Persistent Ledger):** Records long-term relational database history mappings bound directly to local directories via mounted Docker configurations.

---

## ⚡ Key Technical Features

### 1. High-Concurrency Mitigation (Distributed Locks)
To prevent the critical database bottleneck known as "double booking" under heavy traffic, the core thread bypasses heavy physical disk row locks by acquiring short-lived string keys inside an in-memory Redis cluster. Utilizing atomic `SETNX` (Set If Not Exists) instructions, overlapping reservation commands target identical coordinates and get filtered or rejected in microseconds before hitting the transactional relational layer.

### 2. Microsecond Multi-Seat Transactional Atomicity
To resolve multi-seat checkout vulnerabilities, a strict custom two-phase loop runs inside the Spring Boot Controller wrapped in a `@Transactional` boundary context. The application attempts to secure every single individual seat lock key inside Redis sequentially. If *any single slot* within the requested array is already captured, a transaction conflict exception triggers an immediate self-healing rollback routine that frees previously claimed slots to prevent deadlocks.

### 3. Algorithmic Dynamic Surge Pricing
The pricing engine automatically shifts ticket valuations row-by-row based on instantaneous inventory depletion curves using a real-time mathematical scarcity function:

$$Price = BasePrice \times \left(1 + \alpha \left(1 - \frac{R}{T}\right)\right)$$

Where $BasePrice$ is the section baseline anchor, $T$ is total row capacity (20 seats), $R$ is remaining unbooked units, and $\alpha$ is a configurable surge multiplier intensity knob (set to `0.5`, capping peak markups at exactly 50%).

### 4. Consolidated Message Streaming Pipelines
To reduce notification email spam and preserve connection network bandwidth, the application collapses multiple checked-out seats into a single semicolon-delimited token array payload (`A-1;A-2;A-3`). This array is packed into exactly **one single Kafka message** inside the cluster topic partition, routing a single invoice confirmation receipt to the client.

---

## 🛠️ Tech Stack & Keywords

* **Languages:** Java (Core Java, Multithreading, Streams API), JavaScript (ES6 DOM Parsing), SQL
* **Backend Frameworks:** Spring Boot, Spring Data JPA, Hibernate, RESTful API Controllers
* **Databases & Cache:** PostgreSQL, Redis (Distributed In-Memory Lock Management)
* **Message Broker:** Apache Kafka (Event-Driven Stream Consumer Groups)
* **DevOps Infrastructure:** Docker Compose, Local Directory Volumes (Bind Mounts), Git
* **Frontend UI Framework:** HTML5, CSS3, Tailwind CSS Single Page Architecture

---
<img width="1408" height="657" alt="System design" src="https://github.com/user-attachments/assets/ab49df42-1eac-4949-ac44-f708b9364d00" />

## 📂 Master Folder Layout

```text
ticket-booking-engine/
│
├── docker-compose.yml                     # Multi-container orchestration scaffold
├── index.html                             # Tailwind CSS frontend portal layout
├── postgresql/data/                       # Local directory bind mount for Postgres (Persistent)
│
├── booking-service/
│   └── src/main/java/com/ticket/booking_service/
│       ├── BookingServiceApplication.java # Entrypoint, CORS filters, property injection
│       ├── Booking.java                   # JPA entity relational mapped database table 
│       ├── BookingRepository.java         # Data Access Layer & row configuration interfaces
│       ├── SeatLockingService.java        # Atomic Redis connection client component
│       └── BookingController.java         # REST Engine: locks, surge, and billing
│
└── notification-service/
    └── src/main/java/com/ticket/notification_service/
        ├── NotificationServiceApplication.java # Config initialization & Mail Bean setups
        └── NotificationConsumer.java      # Kafka stream listener & SMTP dispatch node
