package edu.dut.controller;

import edu.dut.model.bean.ConversionResult;
import edu.dut.model.bean.UploadedFile;
import edu.dut.model.bo.ConversionBO;

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
    
    private ConversionBO conversionBO;
    
    @Override
    public void init() throws ServletException {
        super.init();
        conversionBO = new ConversionBO();
    }
    
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
        long fileSize = filePart.getSize();
        
        try {
            // Get paths
            String applicationPath = getServletContext().getRealPath("");
            String uploadPath = applicationPath + File.separator + UPLOAD_DIR;
            String outputPath = applicationPath + File.separator + OUTPUT_DIR;
            
            // Ensure directories exist
            conversionBO.ensureDirectoryExists(uploadPath);
            conversionBO.ensureDirectoryExists(outputPath);
            
            // Process upload via BO
            InputStream inputStream = filePart.getInputStream();
            UploadedFile uploadedFile = conversionBO.processUpload(inputStream, fileName, fileSize, uploadPath);
            inputStream.close();
            
            // Convert to PDF via BO
            ConversionResult result = conversionBO.convertToPdf(uploadedFile, outputPath);
            request.setAttribute("result", result);
            
        } catch (IllegalArgumentException e) {
            // Validation error
            ConversionResult result = new ConversionResult(false, e.getMessage());
            request.setAttribute("result", result);
        } catch (Exception e) {
            e.printStackTrace();
            ConversionResult result = new ConversionResult(false, 
                "Lỗi không xác định: " + e.getMessage());
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
