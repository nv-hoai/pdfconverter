package edu.dut.model.bo;

import edu.dut.model.bean.ConversionResult;
import edu.dut.model.bean.UploadedFile;
import edu.dut.model.dao.FileDAO;

import java.io.File;
import java.io.InputStream;

/**
 * Business Object for file conversion operations
 */
public class ConversionBO {
    
    private FileDAO fileDAO;
    
    public ConversionBO() {
        this.fileDAO = new FileDAO();
    }
    
    /**
     * Process file upload
     */
    public UploadedFile processUpload(InputStream inputStream, String originalFileName, 
                                      long fileSize, String uploadPath) throws Exception {
        // Validate file
        if (!isValidWordFile(originalFileName)) {
            throw new IllegalArgumentException("Chỉ chấp nhận file Word (.doc hoặc .docx)");
        }
        
        if (fileSize > 10 * 1024 * 1024) { // 10MB
            throw new IllegalArgumentException("File quá lớn. Kích thước tối đa: 10MB");
        }
        
        // Save uploaded file
        return fileDAO.saveUploadedFile(inputStream, originalFileName, uploadPath);
    }
    
    /**
     * Convert Word file to PDF
     */
    public ConversionResult convertToPdf(UploadedFile uploadedFile, String outputPath) {
        try {
            // Convert using DAO
            String pdfFileName = fileDAO.convertWordToPdf(uploadedFile, outputPath);
            
            // Delete original uploaded file
            fileDAO.deleteFile(uploadedFile.getFilePath());
            
            return new ConversionResult(true, 
                "Chuyển đổi thành công! File: " + uploadedFile.getOriginalFileName(), 
                pdfFileName);
                
        } catch (Exception e) {
            e.printStackTrace();
            return new ConversionResult(false, 
                "Lỗi khi chuyển đổi file: " + e.getMessage());
        }
    }
    
    /**
     * Get PDF file for download
     */
    public File getPdfFile(String fileName, String outputPath) throws Exception {
        return fileDAO.getFile(fileName, outputPath);
    }
    
    /**
     * Validate if file is Word document
     */
    private boolean isValidWordFile(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }
        String lowerFileName = fileName.toLowerCase();
        return lowerFileName.endsWith(".doc") || lowerFileName.endsWith(".docx");
    }
    
    /**
     * Ensure directory exists
     */
    public void ensureDirectoryExists(String path) {
        fileDAO.ensureDirectoryExists(path);
    }
}
