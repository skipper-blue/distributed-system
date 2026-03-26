# Distributed Drinks System - COMPLETE TESTING GUIDE

## 📋 Table of Contents
1. Prerequisites Check
2. Database Setup
3. Build Project
4. Run Components
5. Test Scenarios
6. Troubleshooting

---

## 1️⃣ PREREQUISITES CHECK

### Check Java Version
```powershell
java -version
```
✅ **Expected**: Java 11 or higher (e.g., openjdk 11.0.x or later)

### Check Maven Installation
```powershell
mvn -version
```
✅ **Expected**: Apache Maven 3.6.0 or higher

### Check MySQL
```powershell
mysql --version
```
✅ **Expected**: MySQL 8.0 or higher installed and running

---

## 2️⃣ DATABASE SETUP

### Step 1: Start MySQL Server
```powershell
# On Windows, MySQL should auto-start
# Or start manually:
net start MySQL80  # Windows
# or brew services start mysql  # Mac
# or sudo systemctl start mysql  # Linux
```

### Step 2: Verify MySQL is Running
```powershell
mysql -u root -p -e "SELECT VERSION();"
```
Enter your MySQL root password when prompted.

### Step 3: Create Database & User
```powershell
mysql -u root -p
```

Then paste these commands:
```sql
CREATE DATABASE drinks_system;
CREATE USER 'drinks_user'@'localhost' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON drinks_system.* TO 'drinks_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

### Step 4: Load Database Schema
```powershell
cd c:\Users\maina\Downloads\DistributedDrinksSystem

mysql -u drinks_user -p drinks_system < database/setup.sql
```
Enter password: `password`

### Step 5: Verify Database Content
```powershell
mysql -u drinks_user -p drinks_system
```
```sql
SHOW TABLES;
SELECT * FROM drinks;
SELECT * FROM stock;
EXIT;
```

---

## 3️⃣ BUILD PROJECT

### Navigate to Project Root
```powershell
cd c:\Users\maina\Downloads\DistributedDrinksSystem
```

### Clean and Compile
```powershell
mvn clean compile
```

✅ **Expected Output**: `BUILD SUCCESS`

If you see `BUILD FAILURE`:
- Check error messages
- Ensure Java and Maven are properly installed
- Clear Maven cache: `mvn clean -DskipTests`

---

## 4️⃣ RUN COMPONENTS

### IMPORTANT: Order of Execution
1. Start MySQL (if not running)
2. Start Server first
3. Then start Clients
4. Open multiple clients to test concurrency

---

### 📡 START SERVER (Terminal 1)

```powershell
cd c:\Users\maina\Downloads\DistributedDrinksSystem

mvn exec:java -Dexec.mainClass="server.ServerMain" -pl server
```

✅ **Expected Output**:
```
[INFO] Building server 1.0-SNAPSHOT
[INFO] --- exec-maven-plugin:3.1.0:java (default-cli) @ server ---
Server running...
```

⚠️ **Keep this terminal open!** The server must run continuously.

**Potential Errors**:
- `RemoteException`: Database connection failed - check MySQL
- `Port already in use`: Another app using port 1099
- `Connection refused`: MySQL not running

---

### 👤 START CUSTOMER CLIENT (Terminal 2)

Open a NEW terminal while keeping server running.

```powershell
cd c:\Users\maina\Downloads\DistributedDrinksSystem

mvn exec:java -Dexec.mainClass="client.CustomerClient" -pl client
```

✅ **Expected**: A Swing GUI window opens with:
- Branch dropdown (NAIROBI, NAKURU, MOMBASA, KISUMU)
- Customer Name field
- Drink dropdown (Coke, Fanta, Pepsi)
- Quantity field
- Buttons: Add Item, Clear Order, Place Order

---

### 📊 START ADMIN CLIENT (Terminal 3)

```powershell
cd c:\Users\maina\Downloads\DistributedDrinksSystem

mvn exec:java -Dexec.mainClass="client.AdminClient" -pl client
```

✅ **Expected**: A professional dashboard opens with:
- Sidebar with buttons
- Main content area showing reports
- Auto-refreshes every 30 seconds

---

## 5️⃣ TEST SCENARIOS

### TEST 1: Place a Single Order

1. **Customer Client**:
   - Branch: Select "NAIROBI"
   - Customer Name: Enter "John Doe"
   - Drink: Select "Coke"
   - Quantity: Enter "2"
   - Click **"Add Item"**
   - Click **"Place Order"**

2. **Expected Result**:
   - Message: "Order placed successfully for John Doe at NAIROBI. Total: 100.00 KSH"
   - Order area clears

3. **Admin Client**:
   - Click **"View Customers"** → Should show "John Doe"
   - Click **"Branch Orders"** → Should show "NAIROBI: 1 orders"
   - Click **"Revenue per Branch"** → Should show "NAIROBI: 100.00 KSH"
   - Click **"Total Revenue"** → Should show "100.00 KSH"

---

### TEST 2: Multiple Items in One Order

1. **Customer Client**:
   - Branch: "NAKURU"
   - Customer Name: "Jane Smith"
   - Add Item 1: Coke × 3
   - Add Item 2: Fanta × 2
   - Add Item 3: Pepsi × 1
   - Click **"Place Order"**

2. **Expected**:
   - Total: 209.00 KSH (Coke: 50×3=150, Fanta: 45×2=90, Pepsi: 48)
   - Admin shows "Jane Smith" as customer
   - NAKURU branch revenue updated

---

### TEST 3: Concurrent Orders (Test Locking)

1. Open **TWO Customer Clients** (Terminal 2 and another Terminal 4)

2. **Client 1**: Place order at MOMBASA
3. **Client 2**: Simultaneously place order at MOMBASA
4. **Expected**: Both orders process without data corruption

5. **Admin Client**: Verify both orders appear

---

### TEST 4: Insufficient Stock

1. **Customer Client**:
   - Branch: "KISUMU"
   - Customer Name: "Test User"
   - Drink: "Coke"
   - Quantity: "150" (More than initial 100)
   - Click **"Place Order"**

2. **Expected Error**:
   ```
   Error: Insufficient stock for Coke at KISUMU.
   Available: 100
   ```

---

### TEST 5: Stock Deduction Verification

1. **Before**: Admin Client → Click "View Low Stock Alerts" → Should show nothing

2. **Customer Client**: Place order
   - Branch: "MOMBASA"
   - Customer: "Bulk Order"
   - Coke × 92 (100 - 92 = 8 remaining, < 10 threshold)

3. **Expected**:
   - Order succeeds
   - Stock reduced to 8

4. **After**: Admin Client → Click "View Low Stock Alerts"
   - Should show: "LOW STOCK: MOMBASA - Coke (Quantity: 8, Threshold: 10)"

---

### TEST 6: Multiple Branches Report

1. **Customer Client**: Place orders from different branches
   - Branch: NAIROBI, Customer: "Alice"
   - Branch: NAKURU, Customer: "Bob"
   - Branch: MOMBASA, Customer: "Charlie"
   - Branch: KISUMU, Customer: "David"

2. **Admin Client**:
   - Click **"Branch Orders"** → Should show all 4 branches
   - Click **"Revenue per Branch"** → Should have entries for each

---

### TEST 7: Dashboard Auto-Refresh

1. **Admin Client**: Keep dashboard open
2. **Customer Client**: Place an order
3. **Admin Client**: 
   - Dashboard should auto-update after 30 seconds
   - Or click **"Refresh All"** immediately

---

## 6️⃣ TROUBLESHOOTING

### ❌ Server won't start

**Error**: `Connection refused` or `No connection to database`

**Solution**:
```powershell
# 1. Verify MySQL is running
mysql -u drinks_user -p drinks_system -e "SELECT 1;"

# 2. Check database exists
mysql -u root -p -e "SHOW DATABASES;"

# 3. Check if setup.sql was loaded
mysql -u drinks_user -p drinks_system -e "SHOW TABLES;"
```

---

### ❌ Client can't connect to server

**Error**: `Lookup failed` or `Connection refused`

**Solution**:
```powershell
# 1. Verify server is running
# Look for "Server running..." in Terminal 1

# 2. Check port 1099 is available
netstat -an | findstr :1099

# 3. Restart server
# Stop (Ctrl+C in server terminal)
# Then start again
```

---

### ❌ Maven build fails

**Error**: `BUILD FAILURE`

**Solution**:
```powershell
# 1. Clean cache
mvn clean

# 2. Update Maven
mvn -version

# 3. Rebuild
mvn clean compile

# 4. Check Java version
java -version
```

---

### ❌ GUI doesn't appear

**Solution**:
```powershell
# Try with explicit Maven settings:
mvn exec:java -Dexec.mainClass="client.CustomerClient" -pl client -X
```

---

### ❌ "Low stock alert" not showing

**Solution**:
- Place large orders to test
- Verify stock threshold is 10 in database:
```sql
mysql -u drinks_user -p drinks_system
SELECT * FROM stock WHERE quantity < threshold;
```

---

## 📊 FULL INTEGRATION TEST CHECKLIST

- [ ] MySQL started and verified
- [ ] Database created with `drinks_user` account
- [ ] `setup.sql` loaded successfully
- [ ] Project builds with `mvn clean compile`
- [ ] Server starts without errors
- [ ] Customer Client GUI opens
- [ ] Admin Client Dashboard opens
- [ ] Can place order successfully
- [ ] Stock decreases after order
- [ ] Admin reports update
- [ ] Multiple concurrent clients work
- [ ] Insufficient stock handled gracefully
- [ ] Low stock alerts appear when needed
- [ ] Auto-refresh works in admin dashboard

---

## 🚀 QUICK START (Summary)

```powershell
# Terminal 1: Start Server
cd c:\Users\maina\Downloads\DistributedDrinksSystem
mvn exec:java -Dexec.mainClass="server.ServerMain" -pl server

# Terminal 2: Start Customer Client
cd c:\Users\maina\Downloads\DistributedDrinksSystem
mvn exec:java -Dexec.mainClass="client.CustomerClient" -pl client

# Terminal 3: Start Admin Client
cd c:\Users\maina\Downloads\DistributedDrinksSystem
mvn exec:java -Dexec.mainClass="client.AdminClient" -pl client
```

---

## 📞 Support

If you encounter issues:
1. Check the error message carefully
2. Verify all prerequisites are installed
3. Ensure database is running
4. Check server logs for detailed errors
5. Review troubleshooting section above
