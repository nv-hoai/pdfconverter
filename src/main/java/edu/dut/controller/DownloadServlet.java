package edu.dut.controller;

import edu.dut.model.bo.ConversionBO;

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
    
    private ConversionBO conversionBO;
    
    @Override
    public void init() throws ServletException {
        super.init();
        conversionBO = new ConversionBO();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String fileName = request.getParameter("file");
        
        if (fileName == null || fileName.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Tên file không hợp lệ!");
            return;
        }
        
        try {
            // Get file via BO
            String applicationPath = getServletContext().getRealPath("");
            String outputPath = applicationPath + File.separator + OUTPUT_DIR;
            
            File downloadFile = conversionBO.getPdfFile(fileName, outputPath);
            
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
            
        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (java.io.FileNotFoundException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "Lỗi khi tải file: " + e.getMessage());
        }
    }
}
