<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="edu.dut.model.bean.User" %>
<%@ page import="edu.dut.model.bean.ConversionRequest" %>
<%@ page import="java.util.List" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
    
    List<ConversionRequest> requests = (List<ConversionRequest>) request.getAttribute("requests");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Kết quả chuyển đổi - Word to PDF Converter</title>
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
            flex-direction: column;
        }

        .navbar {
            background: rgba(255, 255, 255, 0.95);
            padding: 15px 30px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .navbar-brand {
            font-size: 1.5rem;
            font-weight: bold;
            color: #667eea;
        }

        .navbar-right {
            display: flex;
            align-items: center;
            gap: 20px;
        }

        .user-info {
            display: flex;
            align-items: center;
            gap: 10px;
            padding: 8px 15px;
            background: #f0f0f0;
            border-radius: 20px;
        }

        .user-icon {
            width: 32px;
            height: 32px;
            background: #667eea;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            color: white;
            font-weight: bold;
        }

        .nav-btn {
            padding: 10px 20px;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            font-size: 1rem;
            text-decoration: none;
            transition: all 0.3s ease;
            display: inline-block;
        }

        .btn-upload {
            background: #28a745;
            color: white;
        }

        .btn-upload:hover {
            background: #218838;
            transform: translateY(-2px);
        }

        .btn-logout {
            background: #dc3545;
            color: white;
        }

        .btn-logout:hover {
            background: #c82333;
            transform: translateY(-2px);
        }

        .container {
            flex: 1;
            padding: 40px 20px;
            max-width: 1200px;
            margin: 0 auto;
            width: 100%;
        }

        .results-card {
            background: white;
            border-radius: 20px;
            box-shadow: 0 20px 60px rgba(0,0,0,0.3);
            padding: 40px;
        }

        h1 {
            color: #333;
            margin-bottom: 10px;
            font-size: 2rem;
        }

        .subtitle {
            color: #666;
            margin-bottom: 30px;
            font-size: 0.95rem;
        }

        .refresh-info {
            background: #d1ecf1;
            color: #0c5460;
            padding: 10px 15px;
            border-radius: 8px;
            margin-bottom: 20px;
            text-align: center;
            border: 1px solid #bee5eb;
        }

        .table-container {
            overflow-x: auto;
        }

        table {
            width: 100%;
            border-collapse: collapse;
            margin-bottom: 20px;
        }

        th {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 15px;
            text-align: left;
            font-weight: 600;
        }

        th:first-child {
            border-top-left-radius: 10px;
        }

        th:last-child {
            border-top-right-radius: 10px;
        }

        td {
            padding: 15px;
            border-bottom: 1px solid #e0e0e0;
        }

        tr:hover {
            background: #f8f9ff;
        }

        .status-badge {
            padding: 6px 12px;
            border-radius: 20px;
            font-size: 0.85rem;
            font-weight: 600;
            display: inline-block;
        }

        .status-pending {
            background: #fff3cd;
            color: #856404;
        }

        .status-processing {
            background: #cce5ff;
            color: #004085;
        }

        .status-completed {
            background: #d4edda;
            color: #155724;
        }

        .status-deleted {
            background: #e2e3e5;
            color: #383d41;
        }

        .status-failed {
            background: #f8d7da;
            color: #721c24;
        }

        .btn-download {
            background: #667eea;
            color: white;
            padding: 8px 16px;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            text-decoration: none;
            font-size: 0.9rem;
            transition: all 0.3s ease;
            display: inline-block;
        }

        .btn-download:hover {
            background: #5568d3;
            transform: translateY(-2px);
        }

        .btn-download:disabled {
            background: #ccc;
            cursor: not-allowed;
            transform: none;
        }

        .empty-state {
            text-align: center;
            padding: 60px 20px;
        }

        .empty-icon {
            font-size: 5rem;
            margin-bottom: 20px;
            opacity: 0.5;
        }

        .empty-text {
            color: #666;
            font-size: 1.2rem;
            margin-bottom: 30px;
        }

        @media (max-width: 768px) {
            .navbar {
                flex-direction: column;
                gap: 15px;
            }

            .navbar-right {
                width: 100%;
                justify-content: center;
                flex-wrap: wrap;
            }

            .results-card {
                padding: 20px;
            }

            h1 {
                font-size: 1.5rem;
            }

            table {
                font-size: 0.9rem;
            }

            th, td {
                padding: 10px 8px;
            }
        }
    </style>
</head>
<body>
    <nav class="navbar">
        <div class="navbar-brand">📄 Word to PDF Converter</div>
        <div class="navbar-right">
            <div class="user-info">
                <div class="user-icon"><%= user.getFullName().substring(0, 1).toUpperCase() %></div>
                <span><%= user.getFullName() %></span>
            </div>
            <a href="<%= request.getContextPath() %>/upload" class="nav-btn btn-upload">📤 Upload mới</a>
            <form method="post" action="<%= request.getContextPath() %>/logout" style="display: inline;">
                <button type="submit" class="nav-btn btn-logout">🚪 Đăng xuất</button>
            </form>
        </div>
    </nav>

    <div class="container">
        <div class="results-card">
            <h1>📋 Kết quả chuyển đổi</h1>
            <p class="subtitle">Danh sách các yêu cầu chuyển đổi của bạn</p>

            <div class="refresh-info">
                ℹ️ Trang tự động làm mới mỗi 5 giây để cập nhật trạng thái
            </div>

            <% if (requests != null && !requests.isEmpty()) { %>
                <div class="table-container">
                    <table>
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>File gốc</th>
                                <th>Trạng thái</th>
                                <th>Thời gian gửi</th>
                                <th>Thời gian hoàn thành</th>
                                <th>Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% for (ConversionRequest req : requests) { %>
                                <tr>
                                    <td>#<%= req.getRequestId() %></td>
                                    <td><%= req.getOriginalFilename() %></td>
                                    <td>
                                        <span class="status-badge status-<%= req.getStatus().name().toLowerCase() %>">
                                            <%= req.getStatusDisplay() %>
                                        </span>
                                    </td>
                                    <td><%= req.getCreatedAt() != null ? new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(req.getCreatedAt()) : "-" %></td>
                                    <td><%= req.getCompletedAt() != null ? new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(req.getCompletedAt()) : "-" %></td>
                                    <td>
                                        <% if (req.getStatus() == ConversionRequest.RequestStatus.COMPLETED) { %>
                                            <a href="<%= request.getContextPath() %>/download?id=<%= req.getRequestId() %>" 
                                               class="btn-download">
                                                📥 Tải về
                                            </a>
                                        <% } else if (req.getStatus() == ConversionRequest.RequestStatus.DELETED) { %>
                                            <span style="color: #6c757d;">🗑️ Đã xóa (quá 7 ngày)</span>
                                        <% } else if (req.getStatus() == ConversionRequest.RequestStatus.FAILED) { %>
                                            <span style="color: #dc3545;">❌ Thất bại</span>
                                        <% } else { %>
                                            <span style="color: #999;">⏳ Đang xử lý...</span>
                                        <% } %>
                                    </td>
                                </tr>
                            <% } %>
                        </tbody>
                    </table>
                </div>
            <% } else { %>
                <div class="empty-state">
                    <div class="empty-icon">📭</div>
                    <div class="empty-text">Bạn chưa có yêu cầu chuyển đổi nào</div>
                    <a href="<%= request.getContextPath() %>/upload" class="btn-download">
                        📤 Upload file ngay
                    </a>
                </div>
            <% } %>
        </div>
    </div>

    <script>
        // Auto refresh every 5 seconds to update status
        setTimeout(function() {
            location.reload();
        }, 5000);
    </script>
</body>
</html>
