# DISTRIBUTED DRINKS SYSTEM - COMPLETE SETUP GUIDE

## ✅ **STATUS: FULLY COMPILED AND READY TO RUN**

All Java files have been compiled successfully. The project is ready to execute!

---

## 🚀 **QUICK START (5 MINUTES)**

### **Option 1: Automatic Startup (EASIEST)**

```cmd
cd c:\Users\maina\Downloads\DistributedDrinksSystem
run.bat
```

This opens an interactive menu where you can:
- ✅ Start Server only
- ✅ Start Customer Client
- ✅ Start Admin Dashboard
- ✅ Start All at Once

---

### **Option 2: Manual Commands**

**Terminal 1 - Start Server:**
```cmd
cd c:\Users\maina\Downloads\DistributedDrinksSystem
java -cp out server.ServerMain
```

✅ **Expected Output:**
```
═══════════════════════════════════════════════════════════════════════════════
  DISTRIBUTED DRINKS SYSTEM - SERVER STARTUP
═══════════════════════════════════════════════════════════════════════════════

[STARTUP] Testing database connection...
[DB] Connecting to: jdbc:mysql://localhost:3306/drinks_system?useSSL=false as root
[DB] ✓ Connection successful
[STARTUP] Creating RMI service implementation...
[STARTUP] ✓ Service created

[STARTUP] Initializing RMI Registry on port 1099...
[STARTUP] ✓ New RMI Registry created
[STARTUP] Binding service as 'DrinkService'...
[STARTUP] ✓ Service bound successfully

═══════════════════════════════════════════════════════════════════════════════
  ✓ SERVER RUNNING SUCCESSFULLY
  RMI Registry: localhost:1099
  Service Name: DrinkService
  Ready to accept client connections...
═══════════════════════════════════════════════════════════════════════════════
```

**Terminal 2 - Start Customer Client:**
```cmd
cd c:\Users\maina\Downloads\DistributedDrinksSystem
java -cp out client.CustomerClient
```

✅ A Swing GUI window opens with:
- Branch dropdown
- Customer name input
- Drink selection
- Order management

**Terminal 3 - Start Admin Dashboard:**
```cmd
cd c:\Users\maina\Downloads\DistributedDrinksSystem
java -cp out client.AdminClient
```

✅ A professional dashboard opens showing:
- Real-time customer list
- Orders per branch
- Revenue analytics
- Low stock alerts
- Auto-refresh every 30 seconds

---

## 🎯 **WHAT'S BEEN IMPROVED**

### **1. Database Connection (XAMPP Compatible)**
- ✅ Updated to use XAMPP default credentials (root user, no password)
- ✅ Better error messages if MySQL is not running
- ✅ Auto-retry logic for connection failures

### **2. Server (ServerMain.java)**
- ✅ **Professional startup messages** with progress indicators
- ✅ **Database verification** before starting
- ✅ **Detailed error reporting** for troubleshooting
- ✅ **Port conflict detection** with helpful messages

### **3. Admin Dashboard (AdminClient.java)**  
- ✅ **Beautiful modern UI** with colored buttons and icons
- ✅ **Connection status indicator** (green/red/orange)
- ✅ **Auto-reconnection logic** if server goes down
- ✅ **Threaded UI operations** for smooth performance
- ✅ **Professional data formatting** with box drawings
- ✅ **Real-time timestamp** on updates
- ✅ **Better error handling** with user guidance

### **4. Startup Scripts**
- ✅ **run.bat** - Interactive menu for easy startup
- ✅ **setup_db.bat** - Database verification and setup
- ✅ Automatic compilation checking
- ✅ Clean error messages

---

## 📋 **PRE-RUN CHECKLIST**

Before running, ensure:

- [ ] **XAMPP is running**
  ```cmd
  net start MySQL80
  ```
  OR use XAMPP Control Panel → Start MySQL

- [ ] **Database exists** (run once)
  ```cmd
  cd c:\Users\maina\Downloads\DistributedDrinksSystem
  setup_db.bat
  ```

- [ ] **All files compiled**
  ```cmd
  dir out\common\*.class
  dir out\server\*.class
  dir out\client\*.class
  ```

---

## 🧪 **TESTING WORKFLOW**

### **Test 1: Basic Order**
1. **Customer Client:** Order NAIROBI branch, 2x Coke
2. **Admin Dashboard:** Click "View Customers" → Should show customer
3. **Admin Dashboard:** Click "Total Revenue" → Should show 100.00 KSH

### **Test 2: Multiple Orders**
1. **Customer Client:** Place 3 different orders from different branches
2. **Admin Dashboard:** Click "Branch Orders" → Should show all branches
3. **Admin Dashboard:** Click "Revenue by Branch" → Should show distribution

### **Test 3: Low Stock Alert**
1. **Customer Client:** Order 92x Coke (leaves 8 < threshold 10)
2. **Admin Dashboard:** Click "Low Stock" → Should show alert

### **Test 4: Concurrent Clients**
1. Open **2 Customer Clients** simultaneously
2. Place orders from each at same time
3. **Admin Dashboard:** Verify both orders recorded without errors

---

## 🔧 **TROUBLESHOOTING**

### **❌ "MySQL Driver not found"**
**Solution:**
- Go to [maven.apache.org](https://dev.mysql.com/downloads/connector/j/)
- Download `mysql-connector-java-8.0.33.jar`
- Place it in project's `lib/` folder (create if needed)
- Recompile

### **❌ "Connection refused" (Server won't start)**
**Steps:**
1. Check MySQL is running
2. Verify database exists: `mysql -u root -e "USE drinks_system;"`
3. If database missing, run: `setup_db.bat`
4. Check port 1099 is free: `netstat -an | findstr 1099`

### **❌ "Cannot connect to server" (Client error)**
1. Verify Server terminal shows "Server running..."
2. Check port 1099 with: `netstat -an | findstr 1099`
3. Click "Refresh All" button to retry
4. Client auto-retries every 5 seconds

### **❌ GUI doesn't appear**
Try the manual command with verbose output:
```cmd
java -cp out -verbose:class client.AdminClient 2>&1 | more
```

### **❌ UI looks scaled/fuzzy**
Set display scaling in Java:
```cmd
java -Dsun.java2d.uiScale=1.0 -cp out client.AdminClient
```

---

## 📊 **PROJECT STRUCTURE**

```
DistributedDrinksSystem/
├── out/                           ← Compiled .class files
│   ├── common/
│   ├── server/
│   └── client/
├── common/src/main/java/common/   ← Source code
│   ├── RemoteService.java         ← RMI Interface
│   ├── Order.java
│   ├── OrderItem.java
│   ├── Customer.java
│   └── Drink.java
├── server/src/main/java/server/
│   ├── ServerMain.java            ← Start here first
│   ├── RemoteServiceImpl.java      ← Business logic
│   └── DatabaseConnection.java    ← MySQL config
├── client/src/main/java/client/
│   ├── CustomerClient.java        ← Place orders
│   └── AdminClient.java           ← Dashboard
├── database/
│   └── setup.sql                  ← Database schema
├── run.bat                        ← ⭐ MAIN STARTUP SCRIPT
├── setup_db.bat                   ← Database setup
└── README.md
```

---

## 🎓 **HOW IT WORKS**

```
┌─────────────────────────────────────────────────────────────────┐
│                                                                 │
│  XAMPP MySQL ◄────────┐                                        │
│                      │                                         │
│                   [SERVER]                                     │
│            RMI Registry :1099                                  │
│       - Database Connection                                    │
│       - Stock Management                                       │
│       - Order Processing                                       │
│           ▲                                                    │
│           │ RMI Calls                                          │
│        ┌──┴──┬──────────┐                                      │
│        │     │          │                                      │
│   [CUSTOMER] [ADMIN]  [CUSTOMER²]                              │
│   - GUI    - Dashboard  - GUI                                  │
│   - Orders - Reports    - Orders                               │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 📞 **QUICK REFERENCE**

| Task | Command |
|------|---------|
| Start Everything | `run.bat` |
| Setup Database | `setup_db.bat` |
| Start Server | `java -cp out server.ServerMain` |
| Start Customer | `java -cp out client.CustomerClient` |
| Start Admin | `java -cp out client.AdminClient` |
| Recompile All | `javac -d out common\src\main\java\common\*.java && javac -cp out -d out server\src\main\java\server\*.java && javac -cp out -d out client\src\main\java\client\*.java` |

---

## ✨ **KEY FEATURES**

✅ **Pure Java** - No Spring Boot, pure RMI  
✅ **MySQL Integration** - Real database with JDBC  
✅ **Thread-Safe** - ReentrantLock for concurrent orders  
✅ **Professional UI** - Modern Swing with colors and styling  
✅ **Auto-Refresh** - Dashboard updates every 30 seconds  
✅ **Error Handling** - User-friendly messages  
✅ **XAMPP Compatible** - Works with local MySQL installations  
✅ **Concurrent Clients** - Multiple users simultaneously  
✅ **Stock Management** - Automatic deduction and alerts  
✅ **Reporting** - Revenue, customers, orders by branch  

---

## 🎯 **EXPECTED BEHAVIOR**

### **Perfect Startup Sequence:**
1. **run.bat** → Select "Start All in Separate Windows"
2. **Small delay** (Windows creates processes)
3. **Server window** → Shows "✓ SERVER RUNNING SUCCESSFULLY"
4. **Admin window** → Shows "✓ Connected to Server" (green)
5. **Customer window** → GUI opens, ready to place orders
6. **Success!** ✅ System is fully operational

### **Normal Operation:**
- Customer places order → Immediately shown in Admin
- Stock decreases automatically
- Low stock triggers alert when < 10 units
- Multiple clients work simultaneously without conflicts
- Dashboard auto-refreshes, shows latest data

---

## 📝 **NOTES**

- **DO NOT close server window** while using clients
- **Clients auto-retry** if server goes down
- **Database persists** between runs
- Compatible with Windows 10/11, Java 11+
- MySQL 8.0+ required (XAMPP includes it)

---

## 🎉 **YOU'RE ALL SET!**

Everything is compiled and ready. Just run:

```cmd
cd c:\Users\maina\Downloads\DistributedDrinksSystem
run.bat
```

Then choose "Option 4: Start All in Separate Windows" and enjoy! 🚀
