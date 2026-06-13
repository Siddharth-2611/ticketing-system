# Microservices-Based-ticketing-system
A distributed, event-driven full-stack ticketing engine engineered with Java and Spring Boot to handle intense traffic spikes during flash sales. The system leverages Redis for atomic distributed in-memory seat locks, PostgreSQL for a durable relational transaction ledger, and Apache Kafka to handle booking confirmations asynchronously.

---

## 🏗️ System Architecture

The platform is built as a decoupled, asynchronous microservices architecture to segregate high-speed inventory reservations from heavy, blocking I/O processing operations.
