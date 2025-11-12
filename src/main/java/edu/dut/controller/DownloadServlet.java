package edu.dut.controller;

import edu.dut.model.bean.ConversionRequest;
import edu.dut.model.bean.User;
import edu.dut.model.bo.ConversionRequestBO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@WebServlet("/download")
public class DownloadServlet extends HttpServlet {
    
    private static final String OUTPUT_DIR = "outputs";
    
    private ConversionRequestBO requestBO;
    
    @Override
    public void init() throws ServletException {
        super.init();
        requestBO = new ConversionRequestBO();
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
        
        User user = (User) session.getAttribute("user");
        
        String requestIdStr = request.getParameter("id");
        
        if (requestIdStr == null || requestIdStr.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu mã yêu cầu!");
            return;
        }
        
        try {
            int requestId = Integer.parseInt(requestIdStr);
            
            // Get request
            ConversionRequest convRequest = requestBO.getRequestById(requestId);
            
            if (convRequest == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Yêu cầu không tồn tại!");
                return;
            }
            
            // Check ownership
            if (convRequest.getUserId() != user.getUserId()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền tải file này!");
                return;
            }
            
            // Check if completed
            if (convRequest.getStatus() != ConversionRequest.RequestStatus.COMPLETED) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Yêu cầu chưa hoàn thành!");
                return;
            }
            
            // Get PDF file
            String applicationPath = getServletContext().getRealPath("");
            String filePath = applicationPath + File.separator + OUTPUT_DIR + File.separator + convRequest.getPdfFilename();
            File downloadFile = new File(filePath);
            
            if (!downloadFile.exists()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "File không tồn tại!");
                return;
            }
            
            // Set response headers BEFORE getting output stream
            response.setContentType("application/pdf");
            response.setContentLengthLong(downloadFile.length());
            response.setBufferSize(8192); // Set buffer size
            
            // Set headers for download
            String originalName = convRequest.getOriginalFilename();
            String pdfName = originalName.substring(0, originalName.lastIndexOf('.')) + ".pdf";
            // Encode filename properly for Vietnamese characters
            String encodedFilename = java.net.URLEncoder.encode(pdfName, "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", 
                "attachment; filename*=UTF-8''" + encodedFilename);
            
            // Write file to response with proper resource management
            FileInputStream inputStream = null;
            OutputStream outStream = null;
            
            try {
                inputStream = new FileInputStream(downloadFile);
                outStream = response.getOutputStream();
                
                byte[] buffer = new byte[8192]; // Increase buffer size to 8KB
                int bytesRead;
                
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outStream.write(buffer, 0, bytesRead);
                }
                
                outStream.flush(); // Flush before closing
                
                // Delete files after successful download
                try {
                    File originalFile = new File(applicationPath, "uploads" + File.separator + convRequest.getSavedFilename());
                    if (originalFile.exists()) {
                        originalFile.delete();
                    }
                    downloadFile.delete();
                    System.out.println("Đã xóa file sau khi download: " + pdfName);
                } catch (Exception delEx) {
                    System.err.println("Không thể xóa file: " + delEx.getMessage());
                }
                
            } finally {
                // Always close streams in finally block
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        // Log but don't throw
                    }
                }
                // Don't close response output stream - container handles it
            }
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Mã yêu cầu không hợp lệ!");
        } catch (IOException e) {
            // Check if it's a client abort (connection closed by client)
            String errorMsg = e.getMessage();
            String exceptionName = e.getClass().getName();
            
            if (exceptionName.contains("ClientAbortException") ||
                (errorMsg != null && 
                (errorMsg.contains("Broken pipe") || 
                 errorMsg.contains("Connection reset") ||
                 errorMsg.contains("Connection was aborted") ||
                 errorMsg.contains("Stream closed")))) {
                // Client disconnected - this is normal, don't log as error
                System.out.println("Client cancelled/disconnected download for request: " + requestIdStr);
            } else {
                e.printStackTrace();
                if (!response.isCommitted()) {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                        "Lỗi khi tải file!");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (!response.isCommitted()) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                    "Lỗi khi tải file: " + e.getMessage());
            }
        }
    }
}
