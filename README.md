# 📄 Word to PDF Converter - Tomcat 9

Ứng dụng web chuyển đổi file Word sang PDF với **xử lý bất đồng bộ**, **quản lý người dùng**, và **tự động dọn dẹp file** - sử dụng Java Servlet MVC cho **Tomcat 9.x**.

## ✨ Tính năng chính

- 🔐 **Xác thực người dùng** - Đăng nhập/đăng xuất an toàn
- ⚡ **Xử lý bất đồng bộ** - Upload không cần chờ đợi, xử lý background queue
- 📊 **Theo dõi tiến trình** - Xem trạng thái realtime của yêu cầu chuyển đổi
- 🗑️ **Tự động dọn dẹp** - Xóa file sau download và tự động xóa file cũ > 7 ngày
- 🛡️ **Bảo vệ hệ thống** - Giới hạn 20MB/file, 50 requests/user
- 🎨 **Giao diện hiện đại** - Responsive, drag & drop, progress indicator
- 🔄 **Multi-user support** - Nhiều người dùng đồng thời, phân quyền theo user

## 📋 Yêu cầu hệ thống

- **Java**: 8 trở lên
- **Maven**: 3.6+
- **Tomcat**: 9.x (Java EE 8 - javax.* namespace)
- **MySQL**: 5.7+ hoặc 8.0+
- **RAM**: Tối thiểu 2GB
- **Disk**: 100MB+ cho ứng dụng + dung lượng file upload

## 🗄️ Cấu trúc Database

### 1. Tạo database
```sql
CREATE DATABASE word_pdf_converter CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE word_pdf_converter;
```

### 2. Import schema
Chạy file SQL đã cung cấp:
```bash
mysql -u root -p word_pdf_converter < database.sql
```

### 3. Cấu trúc bảng

**Bảng `users`:**
- `user_id` - ID người dùng (auto increment)
- `username` - Tên đăng nhập (unique)
- `password_sha256` - Mật khẩu đã hash SHA-256
- `full_name` - Họ tên đầy đủ
- `created_at` - Thời gian tạo tài khoản

**Bảng `conversion_requests`:**
- `request_id` - ID yêu cầu chuyển đổi
- `user_id` - ID người dùng
- `original_filename` - Tên file gốc
- `saved_filename` - Tên file đã lưu
- `file_size` - Kích thước file (bytes)
- `status` - Trạng thái: PENDING/PROCESSING/COMPLETED/FAILED
- `pdf_filename` - Tên file PDF sau chuyển đổi
- `error_message` - Thông báo lỗi (nếu có)
- `created_at` - Thời gian tạo yêu cầu
- `started_at` - Thời gian bắt đầu xử lý
- `completed_at` - Thời gian hoàn thành

### 4. Tài khoản demo
```
Username: admin  | Password: 123456
Username: user1  | Password: 123456
Username: user2  | Password: 123456
```

## 🚀 Hướng dẫn cài đặt

### A. Với VS Code

#### 1. Cài đặt Extensions
- **Extension Pack for Java** (Microsoft)
- **Maven for Java** (Microsoft)
- **Tomcat for Java** (Wei Shen)
- **MySQL** (cweijan) - tùy chọn, để quản lý database

#### 2. Clone/Mở project
```bash
cd d:\Projects\JavaProjects
git clone <repository-url> pdfconverterv9
code pdfconverterv9
```

#### 3. Cấu hình database
Mở `src/main/java/edu/dut/util/DatabaseUtil.java` và kiểm tra:
```java
private static final String URL = "jdbc:mysql://localhost:3306/word_pdf_converter";
private static final String USER = "root";
private static final String PASSWORD = ""; // Đổi nếu có password
```

#### 4. Build project
- Mở Terminal trong VS Code: `Ctrl + `` `
- Chạy lệnh:
```bash
mvn clean package
```

#### 5. Cấu hình Tomcat trong VS Code
- Mở **Command Palette**: `Ctrl+Shift+P`
- Gõ: `Tomcat: Add Tomcat Server`
- Chọn thư mục cài đặt Tomcat 9
- Click chuột phải vào server → `Start`

#### 6. Deploy ứng dụng
**Cách 1: Qua VS Code**
- Click chuột phải vào file `target/pdfconverterv9-1.0.war`
- Chọn `Run on Tomcat Server`

**Cách 2: Copy thủ công**
```bash
copy target\pdfconverterv9-1.0.war %CATALINA_HOME%\webapps\
```

#### 7. Truy cập ứng dụng
```
http://localhost:8080/pdfconverterv9-1.0/
```

---

### B. Với Eclipse

#### 1. Cài đặt Eclipse IDE
- Download **Eclipse IDE for Enterprise Java and Web Developers**
- Đảm bảo đã cài JDK 8+ và Maven

#### 2. Import project
- **File** → **Import** → **Maven** → **Existing Maven Projects**
- Chọn thư mục `pdfconverterv9`
- Click **Finish**

#### 3. Cấu hình database
- Mở `src/main/java/edu/dut/util/DatabaseUtil.java`
- Cập nhật thông tin kết nối MySQL:
```java
private static final String URL = "jdbc:mysql://localhost:3306/word_pdf_converter";
private static final String USER = "root";
private static final String PASSWORD = "your_password";
```

#### 4. Build project
- Chuột phải vào project → **Run As** → **Maven build...**
- Nhập Goals: `clean package`
- Click **Run**

#### 5. Cấu hình Tomcat Server
- **Window** → **Preferences** → **Server** → **Runtime Environments**
- Click **Add** → Chọn **Apache Tomcat v9.0**
- Browse đến thư mục cài Tomcat
- Click **Finish**

#### 6. Tạo Server trong Eclipse
- **Window** → **Show View** → **Servers**
- Chuột phải → **New** → **Server**
- Chọn **Tomcat v9.0 Server**
- Click **Next**, chọn project `pdfconverterv9`
- Click **Finish**

#### 7. Deploy và chạy
- Chuột phải vào project → **Run As** → **Run on Server**
- Chọn Tomcat server đã tạo
- Click **Finish**

#### 8. Truy cập ứng dụng
```
http://localhost:8080/pdfconverterv9-1.0/
```

---

## 🎯 Hướng dẫn sử dụng

### 1. Đăng nhập
- Truy cập URL ứng dụng
- Nhập username/password (dùng tài khoản demo ở trên)
- Click **Đăng nhập**

### 2. Upload file Word
- Click vào vùng **"Kéo thả file vào đây"** hoặc kéo file trực tiếp
- Chọn file Word (.doc hoặc .docx)
- Click **"Gửi yêu cầu chuyển đổi"**
- Yêu cầu được gửi ngay, không cần chờ

### 3. Xem kết quả
- Click nút **"Kết quả"** trên thanh menu
- Trang tự động refresh mỗi 5 giây
- Theo dõi trạng thái:
  - 🟡 **PENDING** - Đang chờ xử lý
  - 🔵 **PROCESSING** - Đang chuyển đổi
  - 🟢 **COMPLETED** - Hoàn thành
  - 🔴 **FAILED** - Thất bại

### 4. Tải PDF
- Khi status = **COMPLETED**, click nút **"Tải về"**
- File PDF tự động download
- ⚠️ File sẽ bị xóa sau khi tải thành công

### 5. Đăng xuất
- Click nút **"Đăng xuất"** trên thanh menu

---

## 🏗️ Kiến trúc hệ thống

### Mô hình MVC cổ điển

```
┌─────────────────────────────────────────────────────┐
│                   CLIENT (Browser)                   │
└────────────────────┬────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────┐
│              CONTROLLER (Servlets)                   │
│  • LoginServlet      • UploadServlet                │
│  • LogoutServlet     • ResultServlet                │
│  • DownloadServlet                                  │
└────────────────────┬────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────┐
│           BUSINESS OBJECT (BO Layer)                │
│  • UserBO                                           │
│  • ConversionRequestBO                              │
└────────────────────┬────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────┐
│         DATA ACCESS OBJECT (DAO Layer)              │
│  • UserDAO                                          │
│  • ConversionRequestDAO                             │
│  • FileDAO                                          │
└────────────────────┬────────────────────────────────┘
                     │
         ┌───────────┴───────────┐
         ▼                       ▼
┌─────────────────┐    ┌─────────────────┐
│  DATABASE       │    │  FILE SYSTEM    │
│  (MySQL)        │    │  (uploads/      │
│                 │    │   outputs/)     │
└─────────────────┘    └─────────────────┘

         ┌────────────────────────────┐
         │  BACKGROUND SERVICES       │
         │  • ConversionQueueProcessor│
         │  • FileCleanupTask         │
         └────────────────────────────┘
```

### Cấu trúc thư mục

```
pdfconverterv9/
├── src/
│   ├── main/
│   │   ├── java/edu/dut/
│   │   │   ├── controller/          # Servlets (Controller)
│   │   │   │   ├── LoginServlet.java
│   │   │   │   ├── LogoutServlet.java
│   │   │   │   ├── UploadServlet.java
│   │   │   │   ├── ResultServlet.java
│   │   │   │   └── DownloadServlet.java
│   │   │   ├── model/
│   │   │   │   ├── bean/           # Entities (Model)
│   │   │   │   │   ├── User.java
│   │   │   │   │   ├── ConversionRequest.java
│   │   │   │   │   ├── UploadedFile.java
│   │   │   │   │   └── ConversionResult.java
│   │   │   │   ├── bo/             # Business Logic
│   │   │   │   │   ├── UserBO.java
│   │   │   │   │   └── ConversionRequestBO.java
│   │   │   │   └── dao/            # Data Access
│   │   │   │       ├── UserDAO.java
│   │   │   │       ├── ConversionRequestDAO.java
│   │   │   │       └── FileDAO.java
│   │   │   ├── service/            # Background Services
│   │   │   │   ├── ConversionQueueProcessor.java
│   │   │   │   └── FileCleanupTask.java
│   │   │   └── util/               # Utilities
│   │   │       ├── DatabaseUtil.java
│   │   │       └── HashUtil.java
│   │   └── webapp/
│   │       ├── WEB-INF/
│   │       │   ├── views/          # JSP Views
│   │       │   │   ├── login.jsp
│   │       │   │   ├── upload.jsp
│   │       │   │   └── results.jsp
│   │       │   └── web.xml
│   │       └── index.html
│   └── test/                       # Unit tests
├── target/                         # Build output
├── pom.xml                         # Maven config
├── database.sql                    # Database schema
└── README.md
```

---

## 🔧 Cấu hình nâng cao

### Thay đổi thông tin database

**File:** `src/main/java/edu/dut/util/DatabaseUtil.java`
```java
private static final String URL = "jdbc:mysql://localhost:3306/word_pdf_converter";
private static final String USER = "root";
private static final String PASSWORD = "your_password";
```

### Điều chỉnh giới hạn

**File:** `src/main/java/edu/dut/controller/UploadServlet.java`
```java
private static final long MAX_FILE_SIZE = 20 * 1024 * 1024; // 20MB
private static final int MAX_REQUESTS_PER_USER = 50;
```

**File:** `src/main/java/edu/dut/service/FileCleanupTask.java`
```java
private static final int FILE_RETENTION_DAYS = 7; // Giữ file 7 ngày
```

### Thay đổi port Tomcat

**File:** `<TOMCAT_HOME>/conf/server.xml`
```xml
<Connector port="8080" protocol="HTTP/1.1" ... />
<!-- Đổi 8080 thành port mong muốn -->
```

---

## 🛡️ Bảo vệ & Giới hạn

| Tính năng | Giá trị | Mô tả |
|-----------|---------|-------|
| **Max file size** | 20 MB | Kích thước tối đa mỗi file upload |
| **Max requests/user** | 50 | Số lượng yêu cầu tối đa mỗi user |
| **File retention** | 7 ngày | PDF tự động xóa sau 7 ngày (file gốc xóa ngay sau convert) |
| **Auto cleanup** | 24 giờ | Task tự động chạy mỗi ngày |
| **Delete policy** | Sau convert | Xóa file Word gốc (.docx), giữ PDF cho user download nhiều lần |
| **Session timeout** | 30 phút | Timeout phiên đăng nhập |
| **Auto cleanup** | 24 giờ | Task tự động chạy mỗi ngày |
| **Delete after download** | Ngay lập tức | Xóa file sau khi download thành công |
| **Session timeout** | 30 phút | Timeout phiên đăng nhập |

---

## 🔍 Troubleshooting

### Lỗi kết nối database

**Lỗi:** `Communications link failure`

**Giải pháp:**
1. Kiểm tra MySQL đang chạy: `mysql -u root -p`
2. Kiểm tra port: `netstat -an | findstr 3306`
3. Cập nhật URL trong `DatabaseUtil.java`
4. Kiểm tra firewall không chặn port 3306

### Lỗi 404 Not Found

**Lỗi:** `HTTP Status 404 - Not Found`

**Giải pháp:**
1. Kiểm tra Tomcat đang chạy
2. Kiểm tra file WAR đã deploy: `<TOMCAT>/webapps/pdfconverterv9-1.0.war`
3. Đợi Tomcat extract file WAR (10-30 giây)
4. Kiểm tra URL đúng: `http://localhost:8080/pdfconverterv9-1.0/`

### Lỗi Maven build

**Lỗi:** `Failed to execute goal`

**Giải pháp:**
1. Xóa cache Maven: `mvn clean`
2. Update dependencies: `mvn dependency:resolve`
3. Kiểm tra JDK version: `java -version` (phải >= 8)
4. Rebuild: `mvn clean package -U`

### File không tải được

**Lỗi:** Download không hoạt động

**Giải pháp:**
1. Kiểm tra file tồn tại trong `outputs/`
2. Kiểm tra quyền truy cập thư mục
3. Xem log Tomcat: `<TOMCAT>/logs/catalina.out`
4. Kiểm tra status request = COMPLETED

### Queue processor không chạy

**Lỗi:** Requests luôn ở PENDING

**Giải pháp:**
1. Kiểm tra log Tomcat startup
2. Tìm dòng: `"Conversion Queue Processor started"`
3. Restart Tomcat
4. Kiểm tra có exception trong log không

---

## 📚 Công nghệ sử dụng

### Backend
- **Java Servlet 4.0** - Controller layer
- **JSP 2.3 + JSTL 1.2** - View layer
- **MySQL 8.0** - Database
- **Apache Commons DBCP2** - Connection pooling

### Document Processing
- **Apache POI 5.2.3** - Đọc file Word
- **Apache PDFBox 2.0.29** - Tạo PDF
- **XDocReport 2.0.4** - Document conversion

### Utilities
- **Log4j2 2.20.0** - Logging
- **Maven 3.x** - Build tool

---

## 📊 Luồng xử lý

### 1. Luồng đăng nhập
```
User nhập login → LoginServlet 
  → UserBO.validateLogin() 
  → UserDAO.getUserByUsername() 
  → Check password SHA-256 
  → Tạo session 
  → Redirect /upload
```

### 2. Luồng upload
```
User chọn file → UploadServlet
  → Validate (size, extension, user limit)
  → FileDAO.saveUploadedFile()
  → ConversionRequestBO.submitRequest()
  → ConversionRequestDAO.createRequest()
  → Insert DB với status PENDING
  → Redirect /upload với message success
```

### 3. Luồng background processing
```
ConversionQueueProcessor (daemon thread)
  → Sleep 2 seconds
  → ConversionRequestDAO.getPendingRequests()
  → For each request:
      → Update status = PROCESSING
      → FileDAO.convertWordToPdf()
      → Delete original Word file (.docx)
      → Update status = COMPLETED/FAILED
  → Repeat
```

### 4. Luồng download
```
User click Download → DownloadServlet
  → ConversionRequestDAO.getRequestById()
  → Validate (ownership, status = COMPLETED)
  → Check file tồn tại
  → Read file từ outputs/
  → Stream file to response
  → File PDF vẫn giữ lại (cho phép download nhiều lần)
  → File tự động xóa sau 7 ngày bởi FileCleanupTask
```

### 5. Luồng cleanup
```
FileCleanupTask (timer)
  → Run every 24 hours
  → ConversionRequestDAO.getOldRequests(7 days ago)
  → For each old request (COMPLETED/FAILED > 7 days):
      → Delete PDF files
      → ConversionRequestDAO.deleteRequest()
```

---

## 🤝 Đóng góp

Mọi đóng góp đều được chào đón! Vui lòng:
1. Fork repository
2. Tạo branch mới: `git checkout -b feature/TenTinhNang`
3. Commit changes: `git commit -m 'Thêm tính năng XYZ'`
4. Push: `git push origin feature/TenTinhNang`
5. Tạo Pull Request

---

## 📝 License

Dự án này được phát hành dưới [MIT License](LICENSE).

---

## 👨‍💻 Tác giả

**Đại học Bách khoa Đà nẵng (DUT)**  
Email: support@dut.udn.vn

---

## 📞 Hỗ trợ

Nếu gặp vấn đề, vui lòng:
1. Kiểm tra phần [Troubleshooting](#-troubleshooting)
2. Xem log Tomcat: `<TOMCAT_HOME>/logs/catalina.out`
3. Tạo issue trên GitHub
4. Liên hệ email hỗ trợ

---

**🎉 Chúc bạn sử dụng thành công!**
