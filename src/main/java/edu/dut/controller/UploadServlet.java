package edu.dut.controller;

import edu.dut.model.ConversionResult;
import edu.dut.service.WordToPdfConverter;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@WebServlet("/upload")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,  // 2MB
    maxFileSize = 1024 * 1024 * 10,       // 10MB
    maxRequestSize = 1024 * 1024 * 50     // 50MB
)
public class UploadServlet extends HttpServlet {
    
    private static final String UPLOAD_DIR = "uploads";
    private static final String OUTPUT_DIR = "outputs";
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        // Get the file part from the request
        Part filePart = request.getPart("file");
        
        if (filePart == null || filePart.getSize() == 0) {
            ConversionResult result = new ConversionResult(false, "Vui lòng chọn file để upload!");
            request.setAttribute("result", result);
            request.getRequestDispatcher("/WEB-INF/views/upload.jsp").forward(request, response);
            return;
        }
        
        String fileName = getFileName(filePart);
        
        // Check if file is Word document
        if (!fileName.toLowerCase().endsWith(".docx") && !fileName.toLowerCase().endsWith(".doc")) {
            ConversionResult result = new ConversionResult(false, "Chỉ chấp nhận file Word (.doc hoặc .docx)!");
            request.setAttribute("result", result);
            request.getRequestDispatcher("/WEB-INF/views/upload.jsp").forward(request, response);
            return;
        }
        
        try {
            // Create upload and output directories if they don't exist
            String applicationPath = getServletContext().getRealPath("");
            String uploadPath = applicationPath + File.separator + UPLOAD_DIR;
            String outputPath = applicationPath + File.separator + OUTPUT_DIR;
            
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            
            File outputDir = new File(outputPath);
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            
            // Save uploaded file
            String timestamp = String.valueOf(System.currentTimeMillis());
            String uploadedFileName = timestamp + "_" + fileName;
            File uploadedFile = new File(uploadPath + File.separator + uploadedFileName);
            
            InputStream inputStream = filePart.getInputStream();
            java.nio.file.Files.copy(inputStream, uploadedFile.toPath(), 
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            inputStream.close();
            
            // Convert to PDF
            String pdfFileName = uploadedFileName.substring(0, uploadedFileName.lastIndexOf('.')) + ".pdf";
            File pdfFile = new File(outputPath + File.separator + pdfFileName);
            
            WordToPdfConverter.convert(uploadedFile, pdfFile);
            
            // Delete uploaded Word file
            uploadedFile.delete();
            
            // Set result
            ConversionResult result = new ConversionResult(true, 
                "Chuyển đổi thành công! File: " + fileName, pdfFileName);
            request.setAttribute("result", result);
            
        } catch (Exception e) {
            e.printStackTrace();
            ConversionResult result = new ConversionResult(false, 
                "Lỗi khi chuyển đổi file: " + e.getMessage());
            request.setAttribute("result", result);
        }
        
        request.getRequestDispatcher("/WEB-INF/views/upload.jsp").forward(request, response);
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
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
