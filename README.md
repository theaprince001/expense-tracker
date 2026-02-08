# Expense Tracker – Backend

A production-ready **Expense Tracking Backend API** built with **Spring Boot**, designed to demonstrate real-world backend engineering practices such as authentication, authorization, budgeting, reporting, notifications, and system observability.

This project focuses on **clean architecture, security, and scalability**, not just basic CRUD operations.

---

## Features

### Authentication & Security
- User registration with email activation
- JWT-based authentication
- Secure login and logout with token blacklisting
- Role-based access control (USER / ADMIN)
- Rate limiting using Redis
- Global exception handling with structured API errors
- Correlation ID logging for request tracing

### Expense & Budget Management
- Income and expense tracking
- Category-based transactions
- Monthly and category-wise budgets
- Automatic budget alerts at 80%, 90%, and 100%
- Budget progress tracking with Redis-backed alert state

### Real-Time Notifications
- WebSocket-based real-time notifications
- Offline notification support using Redis
- Budget alerts delivered via:
  - WebSocket
  - Email

### Reporting & Analytics
- Monthly financial dashboard summary
- Category-wise spending breakdown
- Monthly trends analysis
- PDF monthly reports
- CSV export of transactions
- Email delivery of monthly reports

### File Handling
- Receipt upload and storage
- Secure access to uploaded files
- File type and size validation

### Admin Capabilities
- View all users
- Activate and deactivate users
- View all transactions
- System-level statistics

---

## Tech Stack

### Backend
- Java 17
- Spring Boot 3
- Spring Security
- Spring Data JPA
- Hibernate
- Flyway (database migrations)

### Database
- PostgreSQL

### Caching & Messaging
- Redis

### Real-Time
- WebSockets (Spring WebSocket)

### Security
- JWT authentication
- BCrypt password hashing
- Rate limiting
- Token blacklist

### Reporting
- Apache PDFBox
- CSV generation

### DevOps & Tooling
- Docker & Docker Compose
- Maven
- Git & GitHub

---


This project follows production-grade practices:
- No secrets committed to source control
- Environment-based configuration
- Centralized exception handling
- Stateless authentication
- Database versioning with Flyway
- Redis-backed rate limiting
- Secure file handling
- Structured logging


## Author

Theaprince001

Backend-focused developer building real-world, production-ready systems.
