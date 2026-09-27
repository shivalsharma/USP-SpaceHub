USP SpaceHub

USP SpaceHub is a smart classroom, computer-lab, and event-room booking
system. It allows users to search for suitable rooms, submit booking
requests, view calendars and notifications, and manage their profiles.
Administrators can manage users, rooms, courses, bookings, approvals,
reports, settings, maintenance, and audit information.

The system uses optimized room search and booking rules to help select
suitable rooms based on availability, class size, required facilities,
lecturer schedules, booking limits, fairness, room utilization, and
other constraints. The supported booking types are LAB, TUTORIAL, and
ECA.

Main Features

-   User login and role-based access control (RBAC)
-   Admin, IT Admin, Lecturer, and Student user roles
-   User and profile management
-   Room and laboratory management
-   Course management
-   Optimized room search and recommendation
-   Booking request submission and validation
-   Booking approval and conflict handling
-   Alternative room/time suggestions
-   Calendar and timetable display
-   Dashboard notifications
-   Booking reminders
-   Room maintenance management
-   Reports and audit logs
-   Configurable booking rules and system settings

Technologies Used

-   Java
-   Spring Boot
-   Spring Security
-   Spring Data JPA
-   Thymeleaf
-   MySQL
-   HTML/CSS
-   Maven/IntelliJ IDEA development environment

Windows Setup

1. Required Software

Install the following on Windows:

-   Java JDK compatible with the project
-   IntelliJ IDEA
-   MySQL Server
-   MySQL Workbench
-   Git (optional, if cloning from GitHub)

Make sure Java and MySQL are installed and MySQL Server is running.

2. Get the Project

If you downloaded the project as a ZIP file:

1.  Extract the ZIP file.
2.  Open IntelliJ IDEA.
3.  Select Open.
4.  Select the extracted USP-SpaceHub-main folder.
5.  Allow IntelliJ to load and index the project.

If you are using Git, clone the repository and open the cloned folder in
IntelliJ IDEA.

3. Configure MySQL

The application is configured to use MySQL on:

-   Host: localhost
-   Port: 3306
-   Database: usp_spacehub
-   Username: root
-   Password: blank by default

The main database configuration is located at:

src/main/resources/application.properties

Default configuration:

    spring.datasource.url=jdbc:mysql://localhost:3306/usp_spacehub?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Pacific/Fiji
    spring.datasource.username=root
    spring.datasource.password=

If your MySQL root account has a password, change:

    spring.datasource.password=

to:

    spring.datasource.password=YOUR_MYSQL_PASSWORD

The application is configured with createDatabaseIfNotExist=true, so it
can create the usp_spacehub database when the MySQL user has the
required permission.

A SQL file is also included in the project at:

src/main/java/fj/ac/usp/spacehub/usp_spacehub (1).sql

This can be opened in MySQL Workbench if you need to manually import the
supplied database data.

4. Run the Application

In IntelliJ IDEA:

1.  Open:

src/main/java/fj/ac/usp/spacehub/SpaceHubApplication.java

2.  Run SpaceHubApplication.
3.  Wait for the Spring Boot application to start.
4.  Open a web browser.
5.  Go to:

http://localhost:8080

The application uses port 8080.

Demo Login Accounts

The project includes seeded demonstration accounts:

  Role       Email                 Password
  ---------- --------------------- --------------
  Admin      admin@usp.ac.fj       Admin@123
  IT Admin   itadmin@usp.ac.fj     ITAdmin@123
  Lecturer   lecturer1@usp.ac.fj   Lecturer@123
  Lecturer   lecturer2@usp.ac.fj   Lecturer@123
  Student    student1@usp.ac.fj    Student@123

These accounts are intended for development and demonstration use.

Booking Rules

The system supports three booking types:

-   LAB
-   TUTORIAL
-   ECA

LAB and TUTORIAL have equal academic priority and both have higher
priority than ECA.

Bookings are normally allowed between 8:00 AM and 10:00 PM.

Before a booking is accepted, the system checks rules such as:

-   Room availability
-   Room status and maintenance
-   Room capacity
-   Required facilities
-   Operational equipment
-   Room access restrictions
-   Lecturer timetable clashes
-   Lecturer-course assignment
-   Course booking limits
-   Lecturer booking limits
-   Prime-time limits
-   Duplicate booking requests

Valid room options can then be ranked using factors such as room
suitability, capacity efficiency, time fairness, room fairness, course
booking need, room utilization, and alternative availability.

Project Structure

    src/main/java/fj/ac/usp/spacehub/
    ├── config/        Application configuration, security and data seeding
    ├── controller/    Web controllers
    ├── dto/           Data transfer and form objects
    ├── model/         Database entities and enums
    ├── repository/    Database repositories
    └── service/       Business logic, validation, search and scoring

    src/main/resources/
    ├── templates/     Thymeleaf HTML pages
    ├── static/        Static resources
    ├── application.properties
    └── application-mysql.properties

Important Configuration

Main application settings:

src/main/resources/application.properties

Optional MySQL environment-based settings:

src/main/resources/application-mysql.properties

The MySQL profile supports these environment variables:

-   DB_HOST
-   DB_PORT
-   DB_NAME
-   DB_USER
-   DB_PASSWORD

Notes

-   MySQL Server must be running before starting the application.
-   If port 8080 is already being used, stop the other application or
    change server.port in application.properties.
-   If database login fails, check the MySQL username and password in
    application.properties.
-   Email sending is currently disabled by default with
    app.mail.enabled=false.
