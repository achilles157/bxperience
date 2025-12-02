# BXperience Project

## Overview
BXperience is a Java Swing application for managing rental assets and bookings. It supports two main business lines:
1.  **Booking Experience**: Hourly rental of assets (Simulators, Consoles, VR) on-site.
2.  **Play At Home**: Daily rental of assets delivered to the customer's location.

## Architecture
The project follows a layered architecture to separate concerns and improve maintainability:

-   **`tampilan` (Presentation Layer)**: Contains all Swing UI components (`JPanel`, `JFrame`).
    -   `aset`: Asset management UI.
    -   `booking`: Booking and Rental UI.
    -   `laporan`: Reporting UI.
    -   `utama`: Main application shell and navigation.
    -   `util`: UI utilities and styles.
-   **`service` (Data Access Layer)**: Handles all database interactions.
    -   `AsetDAO`: CRUD operations for assets.
    -   `BookingDAO`: Booking transactions and availability checks.
    -   `PlayAtHomeDAO`: Rental transactions and stock management.
-   **`connection` (Infrastructure)**: Database connection management (`DatabaseConnection`).

## Prerequisites
-   Java Development Kit (JDK) 8 or higher.
-   MySQL Database.
-   Required Libraries (should be in classpath):
    -   `mysql-connector-java`
    -   `jcalendar` (for Date Chooser)

## Setup & Run
1.  **Database Setup**:
    -   Import the `bxperience.sql` file into your MySQL database.
    -   Configure database credentials in `src/connection/DatabaseConnection.java`.
2.  **Run Application**:
    -   Compile and run `src/javaapplication1/MainFrame.java`.

## Key Features
-   **Asset Management**: Add, edit, delete, and search assets.
-   **Booking System**: Real-time availability checking for hourly bookings.
-   **Play At Home**: Multi-day rental system with stock management.
-   **Reporting**: View status and category reports.

## Refactoring Notes
Recent refactoring (Dec 2025) focused on:
-   Extracting database logic from UI classes into DAO classes (`BookingDAO`, `PlayAtHomeDAO`).
-   Implementing `SwingWorker` for responsive UI during database operations.
-   Standardizing code structure and adding Javadoc.
