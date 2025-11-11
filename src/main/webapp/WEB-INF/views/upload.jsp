<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chuyển đổi Word sang PDF</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            display: flex;
            justify-content: center;
            align-items: center;
            padding: 20px;
        }
        
        .container {
            background: white;
            border-radius: 20px;
            box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
            padding: 40px;
            max-width: 600px;
            width: 100%;
        }
        
        h1 {
            color: #333;
            text-align: center;
            margin-bottom: 10px;
            font-size: 28px;
        }
        
        .subtitle {
            text-align: center;
            color: #666;
            margin-bottom: 30px;
            font-size: 14px;
        }
        
        .upload-area {
            border: 2px dashed #667eea;
            border-radius: 10px;
            padding: 40px;
            text-align: center;
            background: #f8f9ff;
            margin-bottom: 20px;
            transition: all 0.3s ease;
        }
        
        .upload-area:hover {
            border-color: #764ba2;
            background: #f0f1ff;
        }
        
        .upload-icon {
            font-size: 48px;
            color: #667eea;
            margin-bottom: 15px;
        }
        
        .file-input {
            display: none;
        }
        
        .file-label {
            display: inline-block;
            padding: 12px 30px;
            background: #667eea;
            color: white;
            border-radius: 25px;
            cursor: pointer;
            transition: all 0.3s ease;
            font-weight: 500;
        }
        
        .file-label:hover {
            background: #764ba2;
            transform: translateY(-2px);
            box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
        }
        
        .file-name {
            margin-top: 15px;
            color: #333;
            font-weight: 500;
        }
        
        .submit-btn {
            width: 100%;
            padding: 15px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border: none;
            border-radius: 25px;
            font-size: 16px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s ease;
            text-transform: uppercase;
            letter-spacing: 1px;
        }
        
        .submit-btn:hover {
            transform: translateY(-2px);
            box-shadow: 0 10px 25px rgba(102, 126, 234, 0.4);
        }
        
        .submit-btn:active {
            transform: translateY(0);
        }
        
        .result {
            margin-top: 25px;
            padding: 20px;
            border-radius: 10px;
            text-align: center;
        }
        
        .result.success {
            background: #d4edda;
            border: 1px solid #c3e6cb;
            color: #155724;
        }
        
        .result.error {
            background: #f8d7da;
            border: 1px solid #f5c6cb;
            color: #721c24;
        }
        
        .result-icon {
            font-size: 40px;
            margin-bottom: 10px;
        }
        
        .result-message {
            font-size: 16px;
            margin-bottom: 15px;
        }
        
        .download-btn {
            display: inline-block;
            padding: 12px 30px;
            background: #28a745;
            color: white;
            text-decoration: none;
            border-radius: 25px;
            transition: all 0.3s ease;
            font-weight: 500;
            margin-top: 10px;
        }
        
        .download-btn:hover {
            background: #218838;
            transform: translateY(-2px);
            box-shadow: 0 5px 15px rgba(40, 167, 69, 0.4);
        }
        
        .info-box {
            background: #e7f3ff;
            border-left: 4px solid #667eea;
            padding: 15px;
            margin-top: 25px;
            border-radius: 5px;
        }
        
        .info-box h3 {
            color: #333;
            margin-bottom: 10px;
            font-size: 16px;
        }
        
        .info-box ul {
            list-style: none;
            color: #555;
            font-size: 14px;
            line-height: 1.8;
        }
        
        .info-box li:before {
            content: "✓ ";
            color: #28a745;
            font-weight: bold;
            margin-right: 5px;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>📄 Chuyển đổi Word sang PDF</h1>
        <p class="subtitle">Chuyển đổi file Word (.doc, .docx) thành PDF dễ dàng - Tomcat 9</p>
        
        <form action="${pageContext.request.contextPath}/upload" method="post" 
              enctype="multipart/form-data" id="uploadForm">
            
            <div class="upload-area" id="uploadArea">
                <div class="upload-icon">📁</div>
                <label for="fileInput" class="file-label">Chọn file Word</label>
                <input type="file" name="file" id="fileInput" class="file-input" 
                       accept=".doc,.docx" required>
                <div class="file-name" id="fileName"></div>
            </div>
            
            <button type="submit" class="submit-btn">Chuyển đổi sang PDF</button>
        </form>
        
        <c:if test="${not empty result}">
            <div class="result ${result.success ? 'success' : 'error'}">
                <div class="result-icon">${result.success ? '✅' : '❌'}</div>
                <div class="result-message">${result.message}</div>
                <c:if test="${result.success && not empty result.downloadFileName}">
                    <a href="${pageContext.request.contextPath}/download?file=${result.downloadFileName}" 
                       class="download-btn">📥 Tải xuống PDF</a>
                </c:if>
            </div>
        </c:if>
        
        <div class="info-box">
            <h3>📋 Hướng dẫn sử dụng:</h3>
            <ul>
                <li>Chọn file Word (.doc hoặc .docx)</li>
                <li>Nhấn nút "Chuyển đổi sang PDF"</li>
                <li>Tải file PDF về máy</li>
                <li>Kích thước tối đa: 10MB</li>
            </ul>
        </div>
    </div>
    
    <script>
        const fileInput = document.getElementById('fileInput');
        const fileName = document.getElementById('fileName');
        const uploadArea = document.getElementById('uploadArea');
        
        fileInput.addEventListener('change', function(e) {
            if (this.files && this.files[0]) {
                fileName.textContent = '📎 ' + this.files[0].name;
            } else {
                fileName.textContent = '';
            }
        });
        
        // Drag and drop support
        uploadArea.addEventListener('dragover', function(e) {
            e.preventDefault();
            this.style.borderColor = '#764ba2';
            this.style.background = '#f0f1ff';
        });
        
        uploadArea.addEventListener('dragleave', function(e) {
            e.preventDefault();
            this.style.borderColor = '#667eea';
            this.style.background = '#f8f9ff';
        });
        
        uploadArea.addEventListener('drop', function(e) {
            e.preventDefault();
            this.style.borderColor = '#667eea';
            this.style.background = '#f8f9ff';
            
            const files = e.dataTransfer.files;
            if (files.length > 0) {
                fileInput.files = files;
                fileName.textContent = '📎 ' + files[0].name;
            }
        });
    </script>
</body>
</html>
