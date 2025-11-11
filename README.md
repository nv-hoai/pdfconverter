# Word to PDF Converter - Tomcat 9

Ứng dụng web chuyển đổi file Word sang PDF sử dụng Java Servlet theo mô hình MVC cho **Tomcat 9.x**.

## Yêu cầu

- Java 8+
- Maven 3.x
- Apache Tomcat 9.x

## Hướng dẫn sử dụng

### 1. Build ứng dụng
```bash
mvn clean package
```

### 2. Deploy ứng dụng
- Copy file `target/pdfconverterv9-1.0-SNAPSHOT.war` vào thư mục `webapps` của Tomcat 9
- Hoặc deploy trực tiếp từ IDE

### 3. Truy cập ứng dụng
```
http://localhost:8080/pdfconverterv9-1.0-SNAPSHOT/
```

### 4. Sử dụng
1. Click **"Chọn file Word"** hoặc kéo thả file
2. Chọn file Word (.doc hoặc .docx)
3. Click **"Chuyển đổi sang PDF"**
4. Click **"Tải xuống PDF"**

## Đặc điểm

✅ Tương thích với **Tomcat 9.x** (Java EE 8)  
✅ Sử dụng `javax.servlet.*` namespace  
✅ Upload file Word (.doc, .docx)  
✅ Chuyển đổi sang PDF tự động  
✅ Giao diện đẹp, responsive  
✅ Kích thước file tối đa: 10MB  

## Công nghệ

- Java Servlet 4.0
- JSP & JSTL
- Apache POI 5.2.3
- Apache PDFBox 2.0.29
- XDocReport 2.0.4
- Log4j2 2.20.0
