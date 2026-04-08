# ✈️ AeroSys — Airport Management System

> A full-featured desktop application for managing airport operations, built with **Java Swing** and **MySQL**.

---

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Project Structure](#project-structure)
- [Database Schema](#database-schema)
- [Prerequisites](#prerequisites)
- [Database Setup](#database-setup)
- [Running the Project](#running-the-project)
- [Default Login Credentials](#default-login-credentials)
- [Tech Stack](#tech-stack)
- [Developer](#developer)

---

## Overview

AeroSys is a Java-based Airport Management System with a modern Swing GUI. It supports two user roles — **Employee/Admin** and **Passenger** — each with a dedicated interface.  

Admins can manage flights, employees, passengers, bookings, payments, and generate reports. Passengers can register, search flights, book tickets, make payments, and print boarding passes.

---

## Features

### 🔐 Authentication
- Separate login for Employees (Admin/Staff) and Passengers
- Session management with role-based access control
- Account status enforcement (Active / Suspended / Deactivated)

### 🛠️ Admin Panel
- Dashboard with live statistics
- Flight management (add, edit, cancel, search)
- Employee management (add, edit, deactivate, role control)
- Passenger account management and approval of registration requests
- Booking and payment oversight
- Revenue reports and flight reports (printable)

### 🧳 Passenger Portal
- Self-registration (pending admin approval)
- Flight search with filters
- Seat booking and payment (Cash / Card)
- Ticket viewing and boarding pass printing
- Booking history and cancellation

### 🗄️ Database Layer
- 7 core tables: `Employee`, `Passenger`, `Passenger_Requests`, `Flight`, `Booking`, `Payment`, `Ticket`
- 10 views for clean data access
- 11 stored procedures (login, stats, search, reports, approvals)
- 4 triggers for seat count management and booking integrity

---

## Project Structure

```
Aerosys_db_/
├── Aerosys_db_.sql          ← Full database dump (schema + seed data)
└── src/
    ├── Main/
    │   ├── Main.java            ← Entry point
    │   └── AirportSystem.java   ← Singleton controller / service facade
    ├── Gui/
    │   ├── AeroSysLoginUI.java      ← Login screen
    │   ├── AeroSysAdminUI.java      ← Admin/Employee dashboard
    │   ├── AeroSysPassengerUI.java  ← Passenger portal
    │   ├── AeroSysRegisterUI.java   ← Passenger self-registration
    │   ├── AeroComponents.java      ← Reusable custom UI components
    │   ├── BookingPaymentPrinter.java
    │   ├── FlightReportPrinter.java
    │   └── TicketPrinter.java
    ├── model/
    │   ├── Employee.java
    │   ├── Passenger.java
    │   ├── Flight.java
    │   ├── Booking.java
    │   ├── Payment.java
    │   ├── Ticket.java
    │   ├── PassengerRequest.java
    │   ├── Person.java              ← Base class
    │   ├── SessionManager.java      ← Singleton session holder
    │   └── InputValidator.java
    ├── service/
    │   ├── AuthService.java
    │   ├── BookingService.java
    │   ├── EmployeeService.java
    │   ├── FlightService.java
    │   ├── PaymentService.java
    │   └── TicketService.java
    ├── dao/
    │   ├── DatabaseConnection.java  ← JDBC connection factory
    │   ├── AuthDAO.java
    │   ├── BookingDAO.java
    │   ├── EmployeeDAO.java
    │   ├── FlightDAO.java
    │   ├── PassengerDAO.java
    │   ├── PaymentDAO.java
    │   └── TicketDAO.java
    └── icons/                       ← All UI icons (PNG)
```

---

## Database Schema

| Table                | Description                                      |
|----------------------|--------------------------------------------------|
| `Employee`           | Staff accounts with roles (ADMIN / EMPLOYEE)     |
| `Passenger`          | Registered passenger accounts                    |
| `Passenger_Requests` | Pending self-registration requests               |
| `Flight`             | Flight schedule, pricing, and availability       |
| `Booking`            | Links passengers to flights with seat count      |
| `Payment`            | Payment records tied to bookings                 |
| `Ticket`             | Issued tickets with seat numbers and status      |

---

## Prerequisites

Make sure the following are installed before running the project:

| Requirement         | Version       | Notes                                              |
|---------------------|---------------|----------------------------------------------------|
| Java JDK            | 11 or higher  | [Download](https://www.oracle.com/java/technologies/downloads/) |
| MySQL Server        | 8.0 or higher | [Download](https://dev.mysql.com/downloads/mysql/) |
| MySQL JDBC Driver   | 8.x (`mysql-connector-j`) | Must be on classpath — see below      |
| Any Java IDE        | —             | IntelliJ IDEA, Eclipse, or NetBeans recommended    |

---

## Database Setup

### Step 1 — Start MySQL

Make sure your MySQL server is running locally on port `3306`.

### Step 2 — Import the Database

Open a terminal and run:

```bash
mysql -u root -p < Aerosys_db_.sql
```

Or from inside the MySQL shell:

```sql
SOURCE /path/to/Aerosys_db_.sql;
```

This will:
- Create the `aerosys_db` database
- Create all tables, views, stored procedures, and triggers
- Seed sample data (employees, passengers, flights, bookings)

### Step 3 — Configure the Connection

Open `src/dao/DatabaseConnection.java` and update the credentials if needed:

```java
private static final String URL  = "jdbc:mysql://127.0.0.1:3306/aerosys_db?useSSL=false&serverTimezone=UTC";
private static final String USER = "root";
private static final String PASS = "your_mysql_password";
```

> ⚠️ **Security Note:** Do not commit real credentials to a public repository. Use environment variables or a config file excluded via `.gitignore` for production deployments.

### Step 4 — Add the JDBC Driver

Download `mysql-connector-j-*.jar` from [MySQL Downloads](https://dev.mysql.com/downloads/connector/j/).

- **IntelliJ IDEA:** `File → Project Structure → Libraries → + → Java → select the JAR`
- **Eclipse:** Right-click project → `Build Path → Add External Archives`
- **NetBeans:** Right-click `Libraries → Add JAR/Folder`

---

## Running the Project

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/aerosys.git
   cd aerosys
   ```

2. Open the project in your IDE and add the MySQL JDBC JAR to the classpath (see above).

3. Make sure the database is imported and the credentials in `DatabaseConnection.java` are correct.

4. Run `src/Main/Main.java` as the application entry point.

---

## Default Login Credentials

> These are the seeded credentials from `Aerosys_db_.sql`. Change them after first login in a real deployment.

### Employee / Admin Accounts

| Username | Password | Role     |
|----------|----------|----------|
| `admin`  | `admin`  | ADMIN    |
| `sara`   | `sara`   | EMPLOYEE |
| `khalid` | `khalid` | EMPLOYEE |

### Passenger Accounts

| Username    | Password    |
|-------------|-------------|
| `Mohammed`  | `Mohammed`  |
| `fatima`    | `fatima`    |
| `john`      | `john`      |
| `layla`     | `layla`     |
| `omar`      | `omar`      |

---

## Tech Stack

| Layer       | Technology                          |
|-------------|-------------------------------------|
| Language    | Java (JDK 11+)                      |
| GUI         | Java Swing (custom components)      |
| Database    | MySQL 8.0                           |
| Connectivity| JDBC (`mysql-connector-j`)          |
| Pattern     | MVC — Model / Service / DAO / GUI   |
| Concurrency | Singleton `AirportSystem` controller + `SessionManager` |

---

## Developer

**Asjid Siddique**  
Full-Stack Java Developer  
[GitHub](https://github.com/AsjidSiddique/)

---

> **AeroSys** — Built for the skies. 🛫
