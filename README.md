# Distributed Drinks System

A production-quality distributed Java application for managing drink orders across multiple branches using pure Java RMI and MySQL.

## Architecture

- **Server**: RMI server with business logic and database operations
- **Clients**: Swing GUI clients for customers and admins
- **Database**: MySQL with tables for drinks, stock, orders
- **Communication**: Java RMI (Remote Method Invocation)

## Branches

- NAIROBI (Headquarters)
- NAKURU
- MOMBASA
- KISUMU

## Features

- Customer order placement from any branch
- Real-time stock management with automatic deduction
- Low stock alerts (threshold: 10 units)
- Admin dashboard with comprehensive reports
- Thread-safe concurrent operations
- Full transaction support

## Prerequisites

- Java 11 or higher
- MySQL 8.0 or higher
- Maven 3.6 or higher

## Setup Instructions

### 1. MySQL Database Setup

1. Install MySQL server
2. Create database and user:
```sql
CREATE DATABASE drinks_system;
CREATE USER 'drinks_user'@'localhost' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON drinks_system.* TO 'drinks_user'@'localhost';
FLUSH PRIVILEGES;
```

3. Run the setup script:
```bash
mysql -u drinks_user -p drinks_system < database/setup.sql
```

### 2. Build the Project

```bash
mvn clean compile
```

### 3. Start the Server

```bash
mvn exec:java -Dexec.mainClass="server.ServerMain" -pl server
```

Or from the server directory:
```bash
cd server
mvn exec:java -Dexec.mainClass="server.ServerMain"
```

### 4. Run Clients

#### Admin Client (Dashboard)
```bash
mvn exec:java -Dexec.mainClass="client.AdminClient" -pl client
```

#### Customer Client
```bash
mvn exec:java -Dexec.mainClass="client.CustomerClient" -pl client
```

## Testing Distributed Behavior

1. Start the server
2. Run multiple customer clients simultaneously
3. Place orders from different branches
4. Monitor stock levels through admin dashboard
5. Test low stock alerts by placing large orders

## Database Schema

- **drinks**: Product catalog with prices
- **stock**: Branch-specific inventory
- **orders**: Order headers
- **order_items**: Order line items

## Admin Reports

- All customers who made orders
- Orders per branch
- Revenue per branch
- Total business revenue
- Low stock alerts

## Security Notes

- Database credentials are hardcoded for demo purposes
- In production, use environment variables or secure config
- RMI communication is not encrypted
- Add SSL/TLS for production use

## Troubleshooting

### Connection Issues
- Ensure MySQL is running on localhost:3306
- Check database credentials in DatabaseConnection.java
- Verify RMI registry is accessible on port 1099

### Build Issues
- Ensure Java 11+ is installed
- Check Maven version
- Clean and rebuild: `mvn clean compile`

### Runtime Issues
- Check server logs for database connection errors
- Verify all modules are compiled
- Ensure no port conflicts (RMI uses 1099)