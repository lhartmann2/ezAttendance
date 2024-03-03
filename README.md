# ezAttendance
A lightweight, customizable, and mobile-friendly management suite for small schools and programs.

## Main Features
- Easily take attendance, including on a mobile device
- Quickly setup and track teachers, parents/guardians, classes
- Customize app name, colors, and social links
- See attendance history and student absence reports
- Generate attendance reports and contact lists in CSV or text format
- Setup upcoming events to be shown to all users
- Options for administrators to create new users, change passwords, and lock/unlock accounts
- User security - passwords encrypted with BCrypt and accounts are locked after 5 failed logins
- Logging and health metrics enabled with Spring Actuator and Spring Logging

## Requirements
- Java 17+
- MariaDB
- Domain and SSL key (for HTTPS support)

## Initial Setup
### Database
Execute 'attendance.sql' on your MariaDB instance to setup the required schema and tables.

Then execute 'authorities.sql' (after optionally editing) to setup an administrator user with the username 'admin' and password 'changeme' encrypted with BCrypt.

>[!NOTE]
>The app will not start if there are no users nor administrators.

The roles available to users: 
1. ROLE_EMPLOYEE (Not used, can be used for extended functionality)
2. ROLE_MANAGER (a.k.a. normal users)
3. ROLE_ADMIN (must also have ROLE_MANAGER authority)
>[!TIP]
>If not using the default password, use an online BCrypt encoding tool to encrypt your password to paste into the SQL script.

### App Configuration

Before launching, edit the following values in 'application.properties' in `/src/main/resources/` to match your requirements:

**Database Configuration**

Add your MariaDB URL, username and password to:
- `app.datasource.jdbc-url`
- `app.datasource.username`
- `app.datasource.password`
- (Optional) `app.datasource.driver-class-name` (_Change if NOT using MariaDB_)
- (Optional) `spring.jpa.properties.hibernate.dialect` (_Change if NOT using MariaDB_)

If not using a separate database for logins, add the same MariaDB information to:
- `security.datasource.jdbc-url`
- `security.datasource.username`
- `security.datasource.password`

**Customization - Main**

- `attendance.app.name`: The name to be displayed in the menu bar and browser title
- `attendance.app.email`: The email admins see for help/questions
- `attendance.app.credits`: Show small app credits link in page footer
- `attendance.app.welcome`: Welcome message shown on the main page

**Customization - Socials**

Edit the following values to display links to your socials in the page footer:
- `attendance.app.fbEnabled` and `attendance.app.fbURL`: Show/Hide Facebook page link to specified URL
- `attendance.app.igEnabled` and `attendance.app.igURL`: Show/Hide Instagram page link to specified URL
- `attendance.app.twEnabled` and `attendance.app.twURL`: Show/Hide Twitter/X page link to specified URL
- `attendance.app.ytEnabled` and `attendance.app.ytURL`: Show/Hide YouTube channel link to specified URL
- `attendance.app.ttEnabled` and `attendance.app.ttURL`: Show/Hide TikTok page link to specified URL
- `attendance.app.ptEnabled` and `attendance.app.ptURL`: Show/Hide Pinterest page link to specified URL

**Customization - Themes**

Themes are provided by [Bootswatch](https://bootswatch.com/).

To change themes, replace 'bootstrap.min.css' in `/src/main/resources/static/css/` with a new one downloaded from Bootswatch.

**HTTPS Configuration**

To enable HTTPS, uncomment the lines under 'SSL Settings' and specify path to your 'keystore.p12' and set your password.

### Deploying App

Once 'application.properties' in `/src/main/resources/` is configured to your liking, the executable .jar can be created.
From the project root directory, run `./mvnw package` (`mvnw.cmd package` on Windows).
The output .jar file can be found in the `/target` directory (Ex., 'Attendance-1.5.jar')

Move the .jar to an appropriate directory and launch with `java -jar Attendance-1.5.jar`.

See [The Java Command](https://docs.oracle.com/en/java/javase/17/docs/specs/man/java.html) doc for more launch parameters. 
