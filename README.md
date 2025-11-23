# Word to PDF Converter - Distributed System

Ứng dụng web chuyển đổi file Word (.docx) sang PDF với kiến trúc phân tán Master-Worker, xử lý bất đồng bộ, và quản lý người dùng.

## Tính năng chính

**Core Features:**
- Xác thực người dùng (Login/Logout)
- Upload file .docx, xử lý bất đồng bộ qua queue
- Theo dõi trạng thái chuyển đổi realtime
- Tự động dọn dẹp file sau 7 ngày
- Giới hạn: 20MB/file, 50 requests/user
- Multi-user support

**Distributed System:**
- Master-Worker Architecture qua TCP
- Truyền file qua mạng (không cần shared storage)
- Dynamic worker pool, tự động cân bằng tải
- Fallback xử lý local nếu không có worker
- Worker monitoring dashboard

**Conversion Engine:**
- Docx4j 11.4.9 + Apache FOP 2.9
- Hỗ trợ Unicode/Tiếng Việt
- Giữ nguyên layout, font, hình ảnh

## Kiến trúc hệ thống

### Architecture Overview

```
Master Server (Tomcat)
├── Web App (Port 8080)
│   ├── User Management
│   ├── File Upload/Download
│   └── Request Queue
├── TCP Server (Port 7777)
│   ├── Worker Pool Manager
│   ├── Job Distribution
│   └── Result Collection
└── Local Converter (Fallback)

Worker Nodes (TCP Clients)
├── Worker 1 → Docx4j + FOP
├── Worker 2 → Docx4j + FOP
└── Worker N → Docx4j + FOP
```

### TCP File Transfer Flow

```
User → Upload .docx
  ↓
Master → Read → byte[]
  ↓
Master → Find Worker → Send {requestId, fileData}
  ↓
Worker → Receive → Save temp → Convert → PDF byte[]
  ↓
Worker → Send {requestId, pdfData}
  ↓
Master → Save outputs/
  ↓
User ← Download PDF
```

## Yêu cầu hệ thống

**Master Server:**
- Java 8+
- Maven 3.6+
- Tomcat 9.x (javax.* namespace)
- MySQL 5.7+ hoặc 8.0+
- RAM: 2GB+
- Port 8080 (HTTP) + Port 7777 (TCP)

**Workers (Optional):**
- Java 8+
- RAM: 512MB+ per worker
- Kết nối được tới Master port 7777

## Technology Stack

- Java Servlet 4.0 + JSP + JSTL
- MySQL + JDBC
- Docx4j 11.4.9 + Apache FOP 2.9
- Java Socket (TCP)
- Maven

## Database Setup

### 1. Cài đặt MySQL
- Download và cài đặt MySQL Server 5.7+ hoặc 8.0+
- Trong quá trình cài đặt, ghi nhớ root password

### 2. Tạo database
Mở MySQL Command Line hoặc MySQL Workbench, chạy:
```sql
CREATE DATABASE word_pdf_converter CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. Import schema
**Cách 1 - Command Line:**
```bash
# Windows
cd D:\Projects\JavaProjects\pdfconverterv9
mysql -u root -p word_pdf_converter < database.sql

# Nhập password khi được yêu cầu
```

**Cách 2 - MySQL Workbench:**
- Mở MySQL Workbench
- Kết nối tới MySQL Server
- File → Run SQL Script
- Chọn file `database.sql` trong thư mục project
- Chọn database: `word_pdf_converter`
- Click Run

### 4. Xác nhận
Kiểm tra tables đã được tạo:
```sql
USE word_pdf_converter;
SHOW TABLES;
-- Kết quả: users, conversion_requests
```

### 5. Tài khoản demo
```
username: admin  | password: 123456
username: user1  | password: 123456
```

### 6. Cấu trúc bảng

**users**: user_id, username, password_sha256, full_name, created_at

**conversion_requests**: request_id, user_id, original_filename, saved_filename, file_size, status (PENDING/PROCESSING/COMPLETED/FAILED), pdf_filename, error_message, created_at, started_at, completed_at

## Hướng dẫn cài đặt

### A. Setup với VS Code

#### Bước 1: Cài đặt phần mềm cần thiết

**1.1. JDK 8 trở lên**
- Download: https://www.oracle.com/java/technologies/downloads/
- Cài đặt và thêm vào PATH
- Kiểm tra: `java -version`

**1.2. Apache Tomcat 9**
- Download: https://tomcat.apache.org/download-90.cgi
- Chọn "Core" → "zip" (Windows)
- Extract vào thư mục (ví dụ: `C:\apache-tomcat-9.0.xx`)

**1.3. Maven**
- Download: https://maven.apache.org/download.cgi
- Extract và thêm `bin` vào PATH
- Kiểm tra: `mvn -version`

**1.4. MySQL**
- Download: https://dev.mysql.com/downloads/mysql/
- Cài đặt MySQL Server (ghi nhớ root password)

#### Bước 2: Cài đặt VS Code Extensions

Mở VS Code, vào Extensions (Ctrl+Shift+X), tìm và cài:

1. **Extension Pack for Java** (Microsoft)
   - Bao gồm: Language Support, Debugger, Test Runner, Maven, Project Manager

2. **Maven for Java** (Microsoft)
   - Hỗ trợ Maven build và dependencies

3. **Community Server Connectors** (Red Hat)
   - Quản lý và deploy lên Tomcat server

#### Bước 3: Clone và mở project

```bash
# Clone hoặc copy project về máy
cd D:\Projects\JavaProjects
git clone <repository-url> pdfconverterv9
# Hoặc giải nén zip nếu có

# Mở trong VS Code
code pdfconverterv9
```

#### Bước 4: Import SQL database

Xem phần [Database Setup](#database-setup) ở trên để import file `database.sql`

#### Bước 5: Cấu hình kết nối Database

Mở file `src/main/java/edu/dut/util/DatabaseUtil.java`:
```java
private static final String URL = "jdbc:mysql://localhost:3306/word_pdf_converter";
private static final String USER = "root";
private static final String PASSWORD = "your_mysql_password"; // Đổi password của bạn
```

#### Bước 6: Cấu hình External Storage (Tùy chọn)

Mở file `src/main/java/edu/dut/util/AppConfig.java`:
```java
// Cách 1: Dùng thư mục riêng ngoài project
private static final String BASE_PATH = "D:/pdfconverter-data";

// Cách 2: Dùng thư mục trong project (mặc định)
private static final String BASE_PATH = "";
```

#### Bước 7: Build project với Maven

**Cách 1 - Dùng Terminal:**
```bash
# Mở Terminal trong VS Code (Ctrl+`)
mvn clean package

# Đợi Maven download dependencies và build
# File WAR sẽ được tạo tại: target/pdfconverterv9.war
```

**Cách 2 - Dùng Maven Sidebar:**
- Mở Maven sidebar (biểu tượng M bên trái)
- Expand project → Lifecycle
- Double-click `clean`
- Double-click `package`

#### Bước 8: Cấu hình Tomcat Server trong VS Code

**8.1. Thêm Tomcat Server:**
- Mở Servers sidebar (biểu tượng server bên trái, hoặc Ctrl+Shift+P → "Servers")
- Click "Create New Server"
- Chọn "Download Server" hoặc "Yes" để tải Tomcat
- Hoặc chọn "No" để dùng Tomcat đã có:
  - Server type: Apache Tomcat
  - Browse và chọn thư mục Tomcat đã extract (ví dụ: `C:\apache-tomcat-9.0.xx`)
- Server xuất hiện trong SERVERS sidebar

**8.2. Start Tomcat Server:**
- Mở SERVERS sidebar (biểu tượng server bên trái)
- Chuột phải vào server → `Start Server`
- Hoặc click nút ▶️ bên cạnh tên server
- Đợi console hiển thị "Server startup in [xxx] milliseconds"

#### Bước 9: Deploy ứng dụng lên Tomcat

**Cách 1 - Qua VS Code (Khuyến nghị):**
- Mở SERVERS sidebar
- Chuột phải vào server đang chạy → `Add Deployment`
- Chọn "Exploded" hoặc "Packaged (Archive)"
- Browse đến `target/pdfconverterv9.war`
- Đợi deploy hoàn tất (xem OUTPUT tab → "Servers")

**Cách 2 - Copy thủ công:**
```bash
# Copy file WAR vào thư mục webapps của Tomcat
copy target\pdfconverterv9.war C:\apache-tomcat-9.0.xx\webapps\

# Tomcat tự động extract và deploy (10-30 giây)
```

#### Bước 10: Truy cập ứng dụng

Mở trình duyệt, truy cập:
```
http://localhost:8080/pdfconverterv9/
```

Đăng nhập bằng tài khoản demo: `admin` / `123456`

---

### B. Setup với Eclipse

#### Bước 1: Cài đặt phần mềm cần thiết

**1.1. Eclipse IDE**
- Download: https://www.eclipse.org/downloads/
- Chọn **Eclipse IDE for Enterprise Java and Web Developers**
- Extract và chạy `eclipse.exe`

**1.2. JDK, Maven, Tomcat, MySQL**
- Cài tương tự như phần VS Code ở trên

#### Bước 2: Import Project vào Eclipse

**2.1. Mở Eclipse:**
- Chọn workspace (ví dụ: `D:\Projects\JavaProjects`)

**2.2. Import Maven Project:**
- Menu: **File** → **Import**
- Chọn: **Maven** → **Existing Maven Projects**
- Click **Next**
- **Root Directory**: Browse đến thư mục `pdfconverterv9`
- Checkbox `pom.xml` sẽ được tự động check
- Click **Finish**
- Eclipse sẽ tự động download dependencies (có thể mất vài phút)

#### Bước 3: Import SQL database

Xem phần [Database Setup](#database-setup) ở trên để import file `database.sql`

#### Bước 4: Cấu hình Database Connection

Mở file `src/main/java/edu/dut/util/DatabaseUtil.java`:
```java
private static final String URL = "jdbc:mysql://localhost:3306/word_pdf_converter";
private static final String USER = "root";
private static final String PASSWORD = "your_mysql_password";
```

#### Bước 5: Cấu hình External Storage

Mở file `src/main/java/edu/dut/util/AppConfig.java`:
```java
private static final String BASE_PATH = "D:/pdfconverter-data";
// Hoặc: private static final String BASE_PATH = ""; // Dùng thư mục project
```

#### Bước 6: Build Project với Maven

**Cách 1 - Maven Build:**
- Chuột phải vào project → **Run As** → **Maven build...**
- **Goals**: Nhập `clean package`
- Click **Run**
- Xem Console tab để theo dõi quá trình build
- Thành công khi thấy: `BUILD SUCCESS`

**Cách 2 - Maven Update:**
- Chuột phải vào project → **Maven** → **Update Project**
- Check **Force Update of Snapshots/Releases**
- Click **OK**

#### Bước 7: Cấu hình Tomcat Server trong Eclipse

**7.1. Thêm Tomcat Runtime:**
- Menu: **Window** → **Preferences**
- Navigate: **Server** → **Runtime Environments**
- Click **Add...**
- Chọn **Apache Tomcat v9.0**
- Click **Next**
- **Tomcat installation directory**: Browse đến thư mục Tomcat (ví dụ: `C:\apache-tomcat-9.0.xx`)
- **JRE**: Chọn JDK đã cài
- Click **Finish**
- Click **Apply and Close**

**7.2. Tạo Server Instance:**
- Menu: **Window** → **Show View** → **Other...**
- Chọn **Server** → **Servers**
- Click **OK**
- Trong Servers tab (dưới cùng), click link **"No servers are available. Click this link to create a new server..."**
- Hoặc chuột phải vào Servers tab → **New** → **Server**

**7.3. Cấu hình Server:**
- **Server type**: Expand **Apache** → Chọn **Tomcat v9.0 Server**
- **Server runtime**: Chọn runtime vừa tạo
- **Server name**: Để mặc định hoặc đặt tên (ví dụ: "Tomcat 9")
- Click **Next**
- **Available projects**: Chọn `pdfconverterv9`
- Click **Add >** để chuyển sang **Configured**
- Click **Finish**

#### Bước 8: Deploy và Run

**8.1. Deploy lên Server:**
- Trong Servers tab, chuột phải vào server
- Chọn **Add and Remove...**
- Chọn project `pdfconverterv9` từ Available
- Click **Add >**
- Click **Finish**

**8.2. Start Server:**
- Chuột phải vào server trong Servers tab
- Chọn **Start**
- Hoặc click nút **Start** (nút Play màu xanh) trên toolbar của Servers tab
- Xem Console tab để theo dõi startup
- Đợi message: "Server startup in [xxx] milliseconds"

**Cách khác - Run trực tiếp:**
- Chuột phải vào project → **Run As** → **Run on Server**
- Chọn server vừa tạo
- Click **Finish**
- Eclipse tự động start server và deploy

#### Bước 9: Truy cập ứng dụng

Mở trình duyệt:
```
http://localhost:8080/pdfconverterv9/
```

Đăng nhập: `admin` / `123456`

---

### C. Deploy Workers (Optional)

Để sử dụng distributed processing, cần deploy worker nodes:

#### C.1. Build Worker
```bash
cd pdf-worker
mvn clean package
```

#### C.2. Run Worker
```bash
# Kết nối tới master server
java -jar target/pdf-worker-1.0.0-jar-with-dependencies.jar <master-ip> 7777

# Ví dụ - Local:
java -jar target/pdf-worker-1.0.0-jar-with-dependencies.jar localhost 7777

# Ví dụ - Remote:
java -jar target/pdf-worker-1.0.0-jar-with-dependencies.jar 192.168.1.100 7777
```

Chi tiết xem: `pdf-worker/README.md`

---

### D. Kiểm tra hoạt động

#### D.1. Kiểm tra Web Application
- Truy cập: http://localhost:8080/pdfconverterv9/
- Login thành công → Trang Upload
- Upload file .docx → Chuyển sang trang Results
- Đợi status COMPLETED → Download PDF

#### D.2. Kiểm tra Workers (Nếu có)
- Truy cập: http://localhost:8080/pdfconverterv9/admin/workers
- Xem danh sách workers đang kết nối
- Xem thống kê: IDLE/BUSY, jobs completed

#### D.3. Kiểm tra Logs
**VS Code:**
- OUTPUT tab (View → Output, hoặc Ctrl+Shift+U)
- Dropdown chọn "Servers"
- Hoặc chuột phải server → `Show in Output`

**Eclipse:**
- Console tab hiển thị logs realtime

**Manual:**
- `<TOMCAT_HOME>/logs/catalina.out` (Linux/Mac)
- `<TOMCAT_HOME>/logs/catalina.yyyy-mm-dd.log` (Windows)

## Hướng dẫn sử dụng

### 1. Đăng nhập
- Truy cập URL ứng dụng
- Nhập username/password (dùng tài khoản demo)

### 2. Upload file
- Kéo file .docx vào vùng upload hoặc click chọn
- Click "Gửi yêu cầu chuyển đổi"
- Yêu cầu được xử lý bất đồng bộ

### 3. Xem kết quả
- Click "Kết quả" trên menu
- Trang tự động refresh mỗi 5 giây
- Trạng thái: PENDING → PROCESSING → COMPLETED/FAILED

### 4. Tải PDF
- Khi status = COMPLETED, click "Tải về"
- File PDF được download
- File tự động xóa sau 7 ngày

## Cấu trúc dự án

```
pdfconverterv9/
├── src/main/
│   ├── java/edu/dut/
│   │   ├── controller/           # Servlets
│   │   ├── model/
│   │   │   ├── bean/            # Entities
│   │   │   ├── bo/              # Business Logic
│   │   │   └── dao/             # Data Access
│   │   ├── service/             # Background Services
│   │   ├── distributed/         # TCP Server, Worker Manager
│   │   ├── filter/              # Authentication Filter
│   │   └── util/                # Utilities
│   └── webapp/
│       ├── WEB-INF/
│       │   ├── views/           # JSP files
│       │   └── web.xml
│       └── workers.jsp          # Worker monitoring
├── target/                      # Build output
├── pom.xml
├── database.sql
└── README.md
```

## Cấu hình

### Database Connection
File: `src/main/java/edu/dut/util/DatabaseUtil.java`
```java
private static final String URL = "jdbc:mysql://localhost:3306/word_pdf_converter";
private static final String USER = "root";
private static final String PASSWORD = "your_password";
```

### External Storage
File: `src/main/java/edu/dut/util/AppConfig.java`
```java
private static final String BASE_PATH = "D:/pdfconverter-data";
// Hoặc để trống dùng thư mục project: private static final String BASE_PATH = "";
```

### Upload Limits
File: `src/main/java/edu/dut/controller/UploadServlet.java`
```java
private static final long MAX_FILE_SIZE = 20 * 1024 * 1024; // 20MB
private static final int MAX_REQUESTS_PER_USER = 50;
```

### File Retention
File: `src/main/java/edu/dut/service/FileCleanupTask.java`
```java
private static final int FILE_RETENTION_DAYS = 7;
```

### Tomcat Port
File: `<TOMCAT_HOME>/conf/server.xml`
```xml
<Connector port="8080" protocol="HTTP/1.1" ... />
```

## System Limits

| Tính năng | Giá trị |
|-----------|---------|
| Max file size | 20 MB |
| Max requests/user | 50 |
| File retention | 7 ngày |
| Cleanup interval | 24 giờ |
| Session timeout | 30 phút |

## Troubleshooting

### Database Connection Error
```
Lỗi: Communications link failure
```
**Giải pháp:**
- Kiểm tra MySQL đang chạy: `mysql -u root -p`
- Kiểm tra port 3306: `netstat -an | findstr 3306`
- Cập nhật thông tin trong DatabaseUtil.java
- Kiểm tra firewall

### 404 Not Found
```
Lỗi: HTTP Status 404
```
**Giải pháp:**
- Kiểm tra Tomcat đang chạy
- Kiểm tra file WAR trong `<TOMCAT>/webapps/`
- Đợi Tomcat extract WAR (10-30s)
- Kiểm tra URL: `http://localhost:8080/pdfconverterv9/`

### Maven Build Failed
```
Lỗi: Failed to execute goal
```
**Giải pháp:**
- Clean cache: `mvn clean`
- Update dependencies: `mvn dependency:resolve`
- Kiểm tra JDK version: `java -version` (>= 8)
- Rebuild: `mvn clean package -U`

### Queue Not Processing
```
Lỗi: Requests stuck in PENDING
```
**Giải pháp:**
- Kiểm tra Tomcat logs
- Tìm: "Conversion Queue Processor started"
- Restart Tomcat
- Kiểm tra exceptions trong logs

### Worker Connection Issues
```
Lỗi: Worker không kết nối được
```
**Giải pháp:**
- Kiểm tra port 7777 không bị firewall chặn
- Kiểm tra MasterTCPServer đã start trong logs
- Kiểm tra worker có thể ping tới master
- Xem worker logs để biết chi tiết lỗi

## License

MIT License

## Contact

**Đại học Bách khoa Đà Nẵng (DUT)**  
Email: support@dut.udn.vn
