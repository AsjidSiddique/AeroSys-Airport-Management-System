-- AeroSys Airport Management System
-- Database: aerosys_db
-- Course: CSC236 | Section: 4MY1
-- Group: Sajjad Alsulaiman (Leader) + 5 members

DROP DATABASE IF EXISTS aerosys_db;
CREATE DATABASE aerosys_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE aerosys_db;


-- ============================================================
-- TABLES
-- ============================================================

CREATE TABLE Employee (
    employee_id    INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name           VARCHAR(100) NOT NULL,
    email          VARCHAR(150) NOT NULL UNIQUE,
    phone          VARCHAR(20)  NOT NULL,
    role           ENUM('ADMIN','EMPLOYEE') NOT NULL DEFAULT 'EMPLOYEE',
    username       VARCHAR(60)  NOT NULL UNIQUE,
    password       VARCHAR(255) NOT NULL,
    account_status ENUM('Active','Deactivated') NOT NULL DEFAULT 'Active',
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_emp_phone CHECK (phone    REGEXP '^[0-9]{10,15}$'),
    CONSTRAINT chk_emp_email CHECK (email    LIKE '%@%.%'),
    CONSTRAINT chk_emp_usr   CHECK (CHAR_LENGTH(username) >= 4)
);

INSERT INTO Employee (name, email, phone, role, username, password, account_status) VALUES
('Admin System',      'admin@aerosys.com',  '0501234567', 'ADMIN',    'admin',  'admin',  'Active'),
('Sara Al-Otaibi',    'sara@aerosys.com',   '0507654321', 'EMPLOYEE', 'sara',   'sara',   'Active'),
('Khalid Al-Zahrani', 'khalid@aerosys.com', '0509876543', 'EMPLOYEE', 'khalid', 'khalid', 'Active');


CREATE TABLE Passenger (
    passenger_id   INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    passport_no    VARCHAR(50)  NOT NULL UNIQUE,
    name           VARCHAR(100) NOT NULL,
    email          VARCHAR(150) NOT NULL UNIQUE,
    phone          VARCHAR(20)  NOT NULL,
    nationality    VARCHAR(80)  NOT NULL,
    date_of_birth  DATE         NULL,
    username       VARCHAR(60)  NOT NULL UNIQUE,
    password       VARCHAR(255) NOT NULL,
    account_status ENUM('Active','Suspended','Deactivated') NOT NULL DEFAULT 'Active',
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_pax_phone CHECK (phone  REGEXP '^[0-9]{10,15}$'),
    CONSTRAINT chk_pax_email CHECK (email  LIKE '%@%.%'),
    CONSTRAINT chk_pax_usr   CHECK (CHAR_LENGTH(username) >= 4)
);

INSERT INTO Passenger (passport_no, name, email, phone, nationality, date_of_birth, username, password, account_status) VALUES
('SA1234567', 'Mohammed Al-Ghamdi', 'mohammed@gmail.com', '0551234567', 'Saudi',    '1995-03-15', 'Mohammed', 'Mohammed', 'Active'),
('SA2345678', 'Fatima Al-Harbi',    'fatima@gmail.com',   '0559876543', 'Saudi',    '1998-07-22', 'fatima',   'fatima',   'Active'),
('US3456789', 'John Smith',         'john@gmail.com',     '0554567890', 'American', '1990-11-05', 'john',     'john',     'Active'),
('SA4567890', 'Layla Al-Shehri',    'layla@gmail.com',    '0556789012', 'Saudi',    '2000-01-30', 'layla',    'layla',    'Active'),
('SA5678901', 'Omar Al-Dosari',     'omar@gmail.com',     '0553456789', 'Saudi',    '1993-06-18', 'omar',     'omar',     'Active');


CREATE TABLE Passenger_Requests (
    request_id     INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name           VARCHAR(100) NOT NULL,
    email          VARCHAR(150) NOT NULL UNIQUE,
    phone          VARCHAR(20)  NOT NULL,
    passport_no    VARCHAR(50)  NOT NULL UNIQUE,
    nationality    VARCHAR(80)  NOT NULL,
    date_of_birth  DATE         NULL,
    username       VARCHAR(60)  NOT NULL UNIQUE,
    password       VARCHAR(255) NOT NULL,
    submitted_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    request_status ENUM('Pending','Approved','Rejected') NOT NULL DEFAULT 'Pending',
    reviewed_by    INT          NULL,
    reviewed_at    TIMESTAMP    NULL,
    CONSTRAINT fk_req_reviewer FOREIGN KEY (reviewed_by)
        REFERENCES Employee(employee_id) ON DELETE SET NULL ON UPDATE CASCADE
);

INSERT INTO Passenger_Requests (name, email, phone, passport_no, nationality, date_of_birth, username, password, request_status) VALUES
('Rayan Al-Qahtani', 'rayan@gmail.com', '0558765432', 'SA6789012', 'Saudi', '1997-04-10', 'rayan', 'rayan', 'Pending'),
('Nora Al-Fayez',    'nora@gmail.com',  '0557654321', 'SA8901234', 'Saudi', '1999-12-01', 'nora',  'nora',  'Pending');


CREATE TABLE Flight (
    flight_id      INT           NOT NULL AUTO_INCREMENT PRIMARY KEY,
    flight_number  VARCHAR(20)   NOT NULL UNIQUE,
    origin         VARCHAR(120)  NOT NULL,
    destination    VARCHAR(120)  NOT NULL,
    departure_time DATETIME      NOT NULL,
    arrival_time   DATETIME      NOT NULL,
    total_seats    INT           NOT NULL DEFAULT 150,
    available_seats INT          NOT NULL DEFAULT 150,
    price_per_seat DECIMAL(10,2) NOT NULL,
    flight_status  ENUM('Scheduled','Delayed','Cancelled','Completed') NOT NULL DEFAULT 'Scheduled',
    managed_by     INT           NOT NULL,
    created_at     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cancelled_at   TIMESTAMP     NULL,
    cancelled_by   INT           NULL,
    cancel_reason  VARCHAR(255)  NULL,
    CONSTRAINT fk_flt_managed   FOREIGN KEY (managed_by)   REFERENCES Employee(employee_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_flt_cancel_by FOREIGN KEY (cancelled_by) REFERENCES Employee(employee_id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT chk_flt_seats    CHECK (total_seats > 0),
    CONSTRAINT chk_flt_avail    CHECK (available_seats >= 0),
    CONSTRAINT chk_flt_avail_mx CHECK (available_seats <= total_seats),
    CONSTRAINT chk_flt_time     CHECK (arrival_time > departure_time),
    CONSTRAINT chk_flt_price    CHECK (price_per_seat > 0)
);

INSERT INTO Flight (flight_number, origin, destination, departure_time, arrival_time, total_seats, available_seats, price_per_seat, flight_status, managed_by) VALUES
('AE101', 'Riyadh', 'Jeddah',   '2025-04-10 08:00:00', '2025-04-10 10:00:00', 150, 148,  299.99,  'Scheduled', 1),
('AE102', 'Jeddah', 'Dubai',    '2025-04-11 10:00:00', '2025-04-11 12:30:00', 200, 197,  499.99,  'Scheduled', 1),
('AE103', 'Riyadh', 'London',   '2025-04-12 14:00:00', '2025-04-12 22:00:00', 300, 297,  1999.99, 'Scheduled', 2),
('AE104', 'Dubai',  'Riyadh',   '2025-04-13 09:00:00', '2025-04-13 11:30:00', 180, 180,  399.99,  'Scheduled', 2),
('AE105', 'Jeddah', 'Cairo',    '2025-04-14 16:00:00', '2025-04-14 18:00:00', 160, 160,  599.99,  'Delayed',   3),
('AE106', 'Riyadh', 'Istanbul', '2025-04-15 07:00:00', '2025-04-15 11:00:00', 250, 245,  899.99,  'Scheduled', 3),
('AE107', 'Dubai',  'London',   '2025-04-16 23:00:00', '2025-04-17 06:00:00', 350, 350,  1499.99, 'Scheduled', 1);


CREATE TABLE Booking (
    booking_id     INT           NOT NULL AUTO_INCREMENT PRIMARY KEY,
    passenger_id   INT           NOT NULL,
    flight_id      INT           NOT NULL,
    booked_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    seat_count     INT           NOT NULL DEFAULT 1,
    total_price    DECIMAL(10,2) NOT NULL,
    booking_status ENUM('Pending','Confirmed','Cancelled') NOT NULL DEFAULT 'Pending',
    processed_by   INT           NULL,
    CONSTRAINT fk_bkg_passenger FOREIGN KEY (passenger_id) REFERENCES Passenger(passenger_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_bkg_flight    FOREIGN KEY (flight_id)    REFERENCES Flight(flight_id)        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_bkg_processed FOREIGN KEY (processed_by) REFERENCES Employee(employee_id)   ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT chk_bkg_seats    CHECK (seat_count >= 1),
    CONSTRAINT chk_bkg_price    CHECK (total_price > 0)
);

INSERT INTO Booking (passenger_id, flight_id, seat_count, total_price, booking_status, processed_by) VALUES
(1, 1, 1,  299.99,  'Confirmed', 2),
(2, 2, 1,  499.99,  'Confirmed', 2),
(3, 3, 1,  1999.99, 'Confirmed', 1),
(4, 1, 1,  299.99,  'Pending',   NULL),
(5, 6, 2,  1799.98, 'Confirmed', 3);


CREATE TABLE Payment (
    payment_id     INT           NOT NULL AUTO_INCREMENT PRIMARY KEY,
    booking_id     INT           NOT NULL UNIQUE,
    amount         DECIMAL(10,2) NOT NULL,
    payment_method ENUM('Credit Card','Debit Card','Cash','Online') NOT NULL,
    payment_status ENUM('Pending','Paid','Refunded') NOT NULL DEFAULT 'Pending',
    paid_at        TIMESTAMP     NULL,
    processed_by   INT           NULL,
    CONSTRAINT fk_pay_booking   FOREIGN KEY (booking_id)   REFERENCES Booking(booking_id)   ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_pay_processed FOREIGN KEY (processed_by) REFERENCES Employee(employee_id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT chk_pay_amount   CHECK (amount > 0)
);

INSERT INTO Payment (booking_id, amount, payment_method, payment_status, paid_at, processed_by) VALUES
(1, 299.99,  'Credit Card', 'Paid',    CURRENT_TIMESTAMP, 2),
(2, 499.99,  'Online',      'Paid',    CURRENT_TIMESTAMP, 2),
(3, 1999.99, 'Debit Card',  'Paid',    CURRENT_TIMESTAMP, 1),
(4, 299.99,  'Cash',        'Pending', NULL,              NULL),
(5, 1799.98, 'Online',      'Paid',    CURRENT_TIMESTAMP, 3);


CREATE TABLE Ticket (
    ticket_id     INT         NOT NULL AUTO_INCREMENT PRIMARY KEY,
    booking_id    INT         NOT NULL,
    passenger_id  INT         NOT NULL,
    flight_id     INT         NOT NULL,
    seat_number   VARCHAR(10) NOT NULL,
    issued_at     TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ticket_status ENUM('Active','Cancelled','Used') NOT NULL DEFAULT 'Active',
    CONSTRAINT fk_tkt_booking   FOREIGN KEY (booking_id)   REFERENCES Booking(booking_id)     ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_tkt_passenger FOREIGN KEY (passenger_id) REFERENCES Passenger(passenger_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_tkt_flight    FOREIGN KEY (flight_id)    REFERENCES Flight(flight_id)        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT uq_seat_per_flight UNIQUE (flight_id, seat_number)
);

INSERT INTO Ticket (booking_id, passenger_id, flight_id, seat_number, ticket_status) VALUES
(1, 1, 1, '12A', 'Active'),
(2, 2, 2, '15B', 'Active'),
(3, 3, 3, '22C', 'Active'),
(5, 5, 6, '5A',  'Active'),
(5, 5, 6, '5B',  'Active');


-- ============================================================
-- VIEWS
-- ============================================================

CREATE VIEW vw_all_flights AS
SELECT
    f.flight_id,
    f.flight_number,
    f.origin,
    f.destination,
    f.departure_time,
    f.arrival_time,
    f.total_seats,
    f.available_seats,
    f.price_per_seat,
    f.flight_status,
    e.name AS managed_by
FROM Flight f
JOIN Employee e ON f.managed_by = e.employee_id
WHERE f.flight_status != 'Cancelled'
ORDER BY f.departure_time;


CREATE VIEW vw_all_bookings AS
SELECT
    b.booking_id,
    p.passenger_id,
    p.name         AS passenger_name,
    p.passport_no,
    f.flight_number,
    f.origin,
    f.destination,
    f.departure_time,
    b.seat_count,
    b.total_price,
    b.booking_status,
    b.booked_at,
    COALESCE(e.name, 'Self') AS processed_by
FROM Booking b
JOIN Passenger p ON b.passenger_id = p.passenger_id
JOIN Flight    f ON b.flight_id    = f.flight_id
LEFT JOIN Employee e ON b.processed_by = e.employee_id
WHERE p.account_status = 'Active'
  AND f.flight_status  != 'Cancelled'
ORDER BY b.booked_at DESC;


CREATE VIEW vw_all_tickets AS
SELECT
    t.ticket_id,
    t.seat_number,
    p.passenger_id,
    p.name         AS passenger_name,
    p.passport_no,
    f.flight_number,
    f.origin,
    f.destination,
    f.departure_time,
    t.issued_at,
    t.ticket_status,
    pay.payment_status
FROM Ticket t
JOIN Passenger p  ON t.passenger_id = p.passenger_id
JOIN Flight    f  ON t.flight_id    = f.flight_id
JOIN Booking   b  ON t.booking_id   = b.booking_id
LEFT JOIN Payment pay ON b.booking_id = pay.booking_id
WHERE p.account_status = 'Active'
  AND f.flight_status  != 'Cancelled'
ORDER BY t.issued_at DESC;


CREATE VIEW vw_all_payments AS
SELECT
    pay.payment_id,
    pay.booking_id,
    p.passenger_id,
    p.name         AS passenger_name,
    f.flight_number,
    f.destination,
    pay.amount,
    pay.payment_method,
    pay.payment_status,
    pay.paid_at,
    COALESCE(e.name, 'Self') AS processed_by
FROM Payment   pay
JOIN Booking   b  ON pay.booking_id  = b.booking_id
JOIN Passenger p  ON b.passenger_id  = p.passenger_id
JOIN Flight    f  ON b.flight_id     = f.flight_id
LEFT JOIN Employee e ON pay.processed_by = e.employee_id
WHERE p.account_status = 'Active'
  AND f.flight_status  != 'Cancelled'
ORDER BY pay.paid_at DESC;


CREATE VIEW vw_passenger_bookings AS
SELECT
    b.booking_id,
    p.passenger_id,
    p.name         AS passenger_name,
    p.passport_no,
    f.flight_number,
    f.origin,
    f.destination,
    f.departure_time,
    f.arrival_time,
    b.seat_count,
    b.total_price,
    b.booking_status,
    t.seat_number,
    t.ticket_status,
    pay.payment_status
FROM Booking   b
JOIN Passenger p   ON b.passenger_id = p.passenger_id
JOIN Flight    f   ON b.flight_id    = f.flight_id
LEFT JOIN Ticket   t   ON b.booking_id  = t.booking_id
LEFT JOIN Payment  pay ON b.booking_id  = pay.booking_id
WHERE p.account_status = 'Active'
  AND f.flight_status  != 'Cancelled'
ORDER BY b.booked_at DESC;


CREATE VIEW vw_available_flights AS
SELECT
    flight_id,
    flight_number,
    origin,
    destination,
    departure_time,
    arrival_time,
    available_seats,
    price_per_seat,
    flight_status
FROM Flight
WHERE available_seats > 0
  AND flight_status IN ('Scheduled','Delayed')
ORDER BY departure_time;


CREATE VIEW vw_pending_requests AS
SELECT
    request_id,
    name,
    email,
    phone,
    passport_no,
    nationality,
    username,
    submitted_at,
    request_status
FROM Passenger_Requests
WHERE request_status = 'Pending'
ORDER BY submitted_at ASC;


CREATE VIEW vw_active_passengers AS
SELECT
    passenger_id,
    passport_no,
    name,
    email,
    phone,
    nationality,
    date_of_birth,
    username,
    account_status,
    created_at
FROM Passenger
ORDER BY created_at DESC;


CREATE VIEW vw_bookings_full AS
SELECT
    b.booking_id,
    b.passenger_id,
    p.name                               AS passenger_name,
    p.passport_no,
    b.flight_id,
    f.flight_number,
    f.origin,
    f.destination,
    f.departure_time,
    b.seat_count,
    b.total_price,
    b.booking_status,
    b.booked_at,
    COALESCE(eb.name, 'Self')            AS processed_by,
    pay.payment_id,
    COALESCE(pay.amount, b.total_price)  AS pay_amount,
    COALESCE(pay.payment_method, 'Not Set') AS pay_method,
    COALESCE(pay.payment_status, 'Pending') AS pay_status,
    pay.paid_at,
    COALESCE(ep.name, 'Self')            AS paid_by
FROM Booking b
JOIN Passenger p   ON b.passenger_id   = p.passenger_id
JOIN Flight    f   ON b.flight_id      = f.flight_id
LEFT JOIN Employee eb  ON b.processed_by   = eb.employee_id
LEFT JOIN Payment  pay ON b.booking_id     = pay.booking_id
LEFT JOIN Employee ep  ON pay.processed_by = ep.employee_id
WHERE p.account_status = 'Active'
  AND f.flight_status  != 'Cancelled'
ORDER BY b.booked_at DESC;


CREATE VIEW vw_active_employees AS
SELECT
    employee_id,
    name,
    email,
    phone,
    role,
    username,
    account_status,
    created_at
FROM Employee
ORDER BY role DESC, created_at ASC;


-- ============================================================
-- STORED PROCEDURES
-- ============================================================

DELIMITER //


CREATE PROCEDURE sp_login(
    IN  p_username  VARCHAR(60),
    IN  p_password  VARCHAR(255),
    IN  p_role_type ENUM('EMPLOYEE','PASSENGER')
)
BEGIN
    IF p_role_type = 'EMPLOYEE' THEN
        SELECT
            employee_id  AS user_id,
            name,
            role         AS user_role,
            'EMPLOYEE'   AS user_type,
            account_status
        FROM Employee
        WHERE username       = p_username
          AND password       = p_password
          AND account_status = 'Active'
        LIMIT 1;
    ELSE
        SELECT
            passenger_id AS user_id,
            passport_no,
            name,
            'PASSENGER'  AS user_role,
            'PASSENGER'  AS user_type,
            account_status
        FROM Passenger
        WHERE username       = p_username
          AND password       = p_password
          AND account_status = 'Active'
        LIMIT 1;
    END IF;
END//


CREATE PROCEDURE sp_dashboard_stats()
BEGIN
    SELECT
        (SELECT COUNT(*) FROM Flight    WHERE flight_status  != 'Cancelled')        AS total_flights,
        (SELECT COUNT(*) FROM Flight    WHERE flight_status   = 'Scheduled')        AS scheduled_flights,
        (SELECT COUNT(*) FROM Flight    WHERE flight_status   = 'Delayed')          AS delayed_flights,
        (SELECT COUNT(*) FROM Passenger WHERE account_status  = 'Active')           AS total_passengers,
        (SELECT COUNT(*) FROM Booking)                                              AS total_bookings,
        (SELECT COUNT(*) FROM Booking   WHERE booking_status  = 'Confirmed')        AS confirmed_bookings,
        (SELECT COUNT(*) FROM Booking   WHERE booking_status  = 'Pending')          AS pending_bookings,
        (SELECT COUNT(*) FROM Ticket    WHERE ticket_status   = 'Active')           AS active_tickets,
        (SELECT COALESCE(SUM(amount),0) FROM Payment WHERE payment_status = 'Paid') AS total_revenue,
        (SELECT COUNT(*) FROM Passenger_Requests WHERE request_status = 'Pending') AS pending_requests;
END//


CREATE PROCEDURE sp_passenger_stats(IN p_passenger_id INT)
BEGIN
    SELECT
        p.passenger_id,
        p.name         AS passenger_name,
        p.passport_no,
        p.nationality,
        (SELECT COUNT(*) FROM Booking
         WHERE passenger_id = p_passenger_id)                                       AS total_bookings,
        (SELECT COUNT(*) FROM Booking
         WHERE passenger_id = p_passenger_id AND booking_status = 'Confirmed')      AS confirmed_bookings,
        (SELECT COUNT(*) FROM Ticket
         WHERE passenger_id = p_passenger_id AND ticket_status  = 'Active')         AS active_tickets,
        (SELECT COALESCE(SUM(pay.amount), 0)
         FROM   Payment pay
         JOIN   Booking b ON pay.booking_id = b.booking_id
         WHERE  b.passenger_id    = p_passenger_id
           AND  pay.payment_status = 'Paid')                                        AS total_spent,
        (SELECT COUNT(*) FROM vw_available_flights)                                 AS available_flights_count
    FROM Passenger p
    WHERE p.passenger_id   = p_passenger_id
      AND p.account_status = 'Active';
END//


CREATE PROCEDURE sp_search_flights(
    IN p_origin      VARCHAR(120),
    IN p_destination VARCHAR(120),
    IN p_travel_date DATE
)
BEGIN
    SELECT
        flight_id, flight_number, origin, destination,
        departure_time, arrival_time, available_seats,
        price_per_seat, flight_status
    FROM Flight
    WHERE (p_origin      IS NULL OR origin      LIKE CONCAT('%', p_origin,      '%'))
      AND (p_destination IS NULL OR destination LIKE CONCAT('%', p_destination, '%'))
      AND (p_travel_date IS NULL OR DATE(departure_time) = p_travel_date)
      AND available_seats > 0
      AND flight_status IN ('Scheduled','Delayed')
    ORDER BY departure_time;
END//


CREATE PROCEDURE sp_deactivate_passenger(
    IN p_passenger_id INT,
    IN p_done_by      INT
)
BEGIN
    DECLARE v_confirmed INT DEFAULT 0;

    SELECT COUNT(*) INTO v_confirmed
    FROM Booking
    WHERE passenger_id = p_passenger_id AND booking_status = 'Confirmed';

    IF v_confirmed > 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Cannot deactivate passenger with active confirmed bookings.';
    END IF;

    UPDATE Passenger
    SET account_status = 'Deactivated'
    WHERE passenger_id = p_passenger_id;

    SELECT 'Passenger account deactivated successfully.' AS result_message;
END//


CREATE PROCEDURE sp_deactivate_employee(
    IN p_employee_id INT,
    IN p_done_by     INT
)
BEGIN
    DECLARE v_role VARCHAR(20);

    SELECT role INTO v_role FROM Employee WHERE employee_id = p_employee_id;

    IF v_role = 'ADMIN' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'ADMIN accounts cannot be deactivated.';
    END IF;

    UPDATE Employee
    SET account_status = 'Deactivated'
    WHERE employee_id = p_employee_id;

    SELECT 'Employee account deactivated successfully.' AS result_message;
END//


CREATE PROCEDURE sp_cancel_flight(
    IN p_flight_id    INT,
    IN p_cancelled_by INT,
    IN p_reason       VARCHAR(255)
)
BEGIN
    DECLARE v_confirmed INT DEFAULT 0;

    SELECT COUNT(*) INTO v_confirmed
    FROM Booking WHERE flight_id = p_flight_id AND booking_status = 'Confirmed';

    IF v_confirmed > 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Cannot cancel flight with confirmed bookings.';
    END IF;

    UPDATE Flight
    SET flight_status = 'Cancelled',
        cancelled_at  = CURRENT_TIMESTAMP,
        cancelled_by  = p_cancelled_by,
        cancel_reason = p_reason
    WHERE flight_id = p_flight_id;

    SELECT 'Flight cancelled successfully.' AS result_message;
END//


CREATE PROCEDURE sp_revenue_report()
BEGIN
    SELECT
        DATE_FORMAT(paid_at, '%Y-%m') AS month,
        COUNT(*)                      AS total_transactions,
        SUM(amount)                   AS total_revenue,
        SUM(CASE WHEN payment_status = 'Refunded' THEN 1 ELSE 0 END) AS total_refunds
    FROM Payment
    WHERE payment_status IN ('Paid','Refunded')
    GROUP BY DATE_FORMAT(paid_at, '%Y-%m')
    ORDER BY month DESC;
END//


CREATE PROCEDURE sp_flight_report()
BEGIN
    SELECT
        f.flight_number,
        f.origin,
        f.destination,
        DATE_FORMAT(f.departure_time, '%Y-%m-%d %H:%i') AS departure,
        f.flight_status,
        f.total_seats,
        f.available_seats,
        (f.total_seats - f.available_seats)             AS seats_sold,
        COUNT(b.booking_id)                             AS total_bookings,
        COALESCE(SUM(pay.amount), 0)                    AS revenue_collected
    FROM Flight f
    LEFT JOIN Booking b   ON f.flight_id  = b.flight_id  AND b.booking_status = 'Confirmed'
    LEFT JOIN Payment pay ON b.booking_id = pay.booking_id AND pay.payment_status = 'Paid'
    WHERE f.flight_status != 'Cancelled'
    GROUP BY f.flight_id
    ORDER BY f.departure_time;
END//


CREATE PROCEDURE sp_approve_request(
    IN  p_request_id     INT,
    IN  p_reviewed_by_id INT,
    OUT p_result         VARCHAR(300)
)
BEGIN
    DECLARE v_status      VARCHAR(50);
    DECLARE v_name        VARCHAR(100);
    DECLARE v_email       VARCHAR(150);
    DECLARE v_phone       VARCHAR(20);
    DECLARE v_passport    VARCHAR(50);
    DECLARE v_nationality VARCHAR(80);
    DECLARE v_dob         DATE;
    DECLARE v_username    VARCHAR(80);
    DECLARE v_password    VARCHAR(255);

    SELECT request_status, name, email, phone,
           passport_no, nationality, date_of_birth, username, password
    INTO   v_status, v_name, v_email, v_phone,
           v_passport, v_nationality, v_dob, v_username, v_password
    FROM   Passenger_Requests
    WHERE  request_id = p_request_id;

    IF v_status IS NULL THEN
        SET p_result = CONCAT('ERROR:Request #', p_request_id, ' not found.');

    ELSEIF v_status != 'Pending' THEN
        SET p_result = CONCAT('ERROR:Request #', p_request_id, ' is already ', v_status, '.');

    ELSE
        UPDATE Passenger_Requests
        SET    request_status = 'Approved',
               reviewed_by   = p_reviewed_by_id,
               reviewed_at   = CURRENT_TIMESTAMP
        WHERE  request_id = p_request_id;

        INSERT INTO Passenger (name, email, phone, passport_no, nationality,
                               date_of_birth, username, password,
                               account_status, created_at)
        VALUES (v_name, v_email, v_phone, v_passport, v_nationality,
                v_dob, v_username, v_password,
                'Active', CURRENT_TIMESTAMP);

        SET p_result = CONCAT('SUCCESS:Request #', p_request_id,
                              ' approved. Passenger account created.');
    END IF;
END//


CREATE PROCEDURE sp_reject_request(
    IN  p_request_id     INT,
    IN  p_reviewed_by_id INT,
    OUT p_result         VARCHAR(300)
)
BEGIN
    DECLARE v_status VARCHAR(50);

    SELECT request_status INTO v_status
    FROM   Passenger_Requests
    WHERE  request_id = p_request_id;

    IF v_status IS NULL THEN
        SET p_result = CONCAT('ERROR:Request #', p_request_id, ' not found.');

    ELSEIF v_status != 'Pending' THEN
        SET p_result = CONCAT('ERROR:Request #', p_request_id, ' is already ', v_status, '.');

    ELSE
        UPDATE Passenger_Requests
        SET    request_status = 'Rejected',
               reviewed_by   = p_reviewed_by_id,
               reviewed_at   = CURRENT_TIMESTAMP
        WHERE  request_id = p_request_id;

        SET p_result = CONCAT('SUCCESS:Request #', p_request_id, ' rejected successfully.');
    END IF;
END//


DELIMITER ;


-- ============================================================
-- TRIGGERS
-- ============================================================

DELIMITER //


CREATE TRIGGER trg_after_booking_insert
AFTER INSERT ON Booking
FOR EACH ROW
BEGIN
    UPDATE Flight
    SET available_seats = available_seats - NEW.seat_count
    WHERE flight_id = NEW.flight_id;
END//


CREATE TRIGGER trg_after_booking_cancelled
AFTER UPDATE ON Booking
FOR EACH ROW
BEGIN
    IF NEW.booking_status = 'Cancelled' AND OLD.booking_status != 'Cancelled' THEN
        UPDATE Flight
        SET available_seats = available_seats + NEW.seat_count
        WHERE flight_id = NEW.flight_id;

        UPDATE Ticket
        SET ticket_status = 'Cancelled'
        WHERE booking_id = NEW.booking_id;
    END IF;
END//


CREATE TRIGGER trg_before_booking_insert
BEFORE INSERT ON Booking
FOR EACH ROW
BEGIN
    DECLARE v_avail   INT;
    DECLARE v_fstatus VARCHAR(30);

    SELECT available_seats, flight_status
    INTO   v_avail, v_fstatus
    FROM Flight WHERE flight_id = NEW.flight_id;

    IF v_fstatus IN ('Cancelled','Completed') THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'This flight is no longer available for booking.';
    END IF;

    IF v_avail < NEW.seat_count THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Not enough available seats on this flight.';
    END IF;

    IF NEW.seat_count < 1 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Seat count must be at least 1.';
    END IF;
END//


CREATE TRIGGER trg_before_ticket_insert
BEFORE INSERT ON Ticket
FOR EACH ROW
BEGIN
    DECLARE v_booking_status VARCHAR(20);
    DECLARE v_payment_status VARCHAR(20);

    SELECT booking_status INTO v_booking_status
    FROM Booking WHERE booking_id = NEW.booking_id;

    SELECT payment_status INTO v_payment_status
    FROM Payment WHERE booking_id = NEW.booking_id;

    IF v_booking_status != 'Confirmed' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Booking must be Confirmed before a ticket can be issued.';
    END IF;

    IF v_payment_status IS NULL OR v_payment_status != 'Paid' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Payment must be completed before a ticket can be issued.';
    END IF;
END//


DELIMITER ;

SELECT * FROM Passenger;