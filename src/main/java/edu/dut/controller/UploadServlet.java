package edu.dut.controller;

import edu.dut.model.bean.User;
import edu.dut.model.bo.ConversionRequestBO;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@WebServlet("/upload")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,      // 1MB
    maxFileSize = 1024 * 1024 * 20,       // 20MB
    maxRequestSize = 1024 * 1024 * 25     // 25MB
)
public class UploadServlet extends HttpServlet {
    
    private static final String UPLOAD_DIR = "uploads";
    private static final long MAX_FILE_SIZE = 20 * 1024 * 1024; // 20MB
    private static final int MAX_REQUESTS_PER_USER = 50;
    
    private ConversionRequestBO requestBO;
    
    @Override
    public void init() throws ServletException {
        super.init();
        requestBO = new ConversionRequestBO();
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Check login
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        User user = (User) session.getAttribute("user");
        
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        // Get the file part from the request
        Part filePart = request.getPart("file");
        
        if (filePart == null || filePart.getSize() == 0) {
            request.setAttribute("error", "Vui lòng chọn file để upload!");
            request.getRequestDispatcher("/WEB-INF/views/upload.jsp").forward(request, response);
            return;
        }
        
        String fileName = getFileName(filePart);
        long fileSize = filePart.getSize();
        
        // Validate file size
        if (fileSize > MAX_FILE_SIZE) {
            request.setAttribute("messageType", "error");
            request.setAttribute("message", "File quá lớn! Kích thước tối đa: 20MB");
            request.getRequestDispatcher("/WEB-INF/views/upload.jsp").forward(request, response);
            return;
        }
        
        // Validate file extension
        if (!fileName.toLowerCase().endsWith(".doc") && !fileName.toLowerCase().endsWith(".docx")) {
            request.setAttribute("messageType", "error");
            request.setAttribute("message", "Chỉ hỗ trợ file Word (.doc, .docx)");
            request.getRequestDispatcher("/WEB-INF/views/upload.jsp").forward(request, response);
            return;
        }
        
        try {
            // Check user's request limit
            int userRequestCount = requestBO.countUserRequests(user.getUserId());
            if (userRequestCount >= MAX_REQUESTS_PER_USER) {
                request.setAttribute("messageType", "error");
                request.setAttribute("message", "Bạn đã đạt giới hạn " + MAX_REQUESTS_PER_USER + " yêu cầu. Vui lòng xóa các yêu cầu cũ!");
                request.getRequestDispatcher("/WEB-INF/views/upload.jsp").forward(request, response);
                return;
            }
            
            // Get paths
            String applicationPath = getServletContext().getRealPath("");
            String uploadPath = applicationPath + File.separator + UPLOAD_DIR;
            
            // Ensure directory exists
            requestBO.ensureDirectoryExists(uploadPath);
            
            // Submit request to queue
            InputStream inputStream = filePart.getInputStream();
            int requestId = requestBO.submitRequest(user.getUserId(), inputStream, fileName, fileSize, uploadPath);
            inputStream.close();
            
            request.setAttribute("messageType", "success");
            request.setAttribute("message", "Yêu cầu #" + requestId + " đã được gửi! Kiểm tra ở trang Kết quả.");
            
        } catch (IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Lỗi không xác định: " + e.getMessage());
        }
        
        request.getRequestDispatcher("/WEB-INF/views/upload.jsp").forward(request, response);
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Check login
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        request.getRequestDispatcher("/WEB-INF/views/upload.jsp").forward(request, response);
    }
    
    private String getFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        String[] tokens = contentDisp.split(";");
        for (String token : tokens) {
            if (token.trim().startsWith("filename")) {
                return token.substring(token.indexOf("=") + 2, token.length() - 1);
            }
        }
        return "";
    }
}
