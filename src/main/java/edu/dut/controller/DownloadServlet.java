package edu.dut.controller;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@WebServlet("/download")
public class DownloadServlet extends HttpServlet {
    
    private static final String OUTPUT_DIR = "outputs";
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String fileName = request.getParameter("file");
        
        if (fileName == null || fileName.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Tên file không hợp lệ!");
            return;
        }
        
        // Security check: prevent directory traversal
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Tên file không hợp lệ!");
            return;
        }
        
        String applicationPath = getServletContext().getRealPath("");
        String filePath = applicationPath + File.separator + OUTPUT_DIR + File.separator + fileName;
        
        File downloadFile = new File(filePath);
        
        if (!downloadFile.exists()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "File không tồn tại!");
            return;
        }
        
        // Set content type
        response.setContentType("application/pdf");
        response.setContentLength((int) downloadFile.length());
        
        // Set headers for download
        String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename=\"%s\"", 
            new String(fileName.getBytes("UTF-8"), "ISO-8859-1"));
        response.setHeader(headerKey, headerValue);
        
        // Write file to response
        FileInputStream inputStream = new FileInputStream(downloadFile);
        OutputStream outStream = response.getOutputStream();
        
        byte[] buffer = new byte[4096];
        int bytesRead;
        
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, bytesRead);
        }
        
        inputStream.close();
        outStream.close();
        
        // Optional: Delete file after download
        // downloadFile.delete();
    }
}
