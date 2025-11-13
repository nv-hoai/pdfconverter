package edu.dut.model.dao;

import edu.dut.model.bean.UploadedFile;
import org.docx4j.Docx4J;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Data Access Object for file operations
 */
public class FileDAO {
    
    /**
     * Save uploaded file to disk
     */
    public UploadedFile saveUploadedFile(InputStream inputStream, String originalFileName, 
                                         String uploadPath) throws IOException {
        // Generate unique filename
        String timestamp = String.valueOf(System.currentTimeMillis());
        String savedFileName = timestamp + "_" + originalFileName;
        String filePath = uploadPath + File.separator + savedFileName;
        
        // Save file
        File file = new File(filePath);
        Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        
        // Create bean
        UploadedFile uploadedFile = new UploadedFile();
        uploadedFile.setOriginalFileName(originalFileName);
        uploadedFile.setSavedFileName(savedFileName);
        uploadedFile.setFilePath(filePath);
        uploadedFile.setFileSize(file.length());
        
        return uploadedFile;
    }
    
    /**
     * Convert Word document to PDF
     */
    public String convertWordToPdf(UploadedFile uploadedFile, String outputPath) throws Exception {
        File wordFile = new File(uploadedFile.getFilePath());
        
        // Generate PDF filename
        String pdfFileName = uploadedFile.getSavedFileName();
        pdfFileName = pdfFileName.substring(0, pdfFileName.lastIndexOf('.')) + ".pdf";
        
        File pdfFile = new File(outputPath + File.separator + pdfFileName);
        
        // Convert
        convertWordToPdf(wordFile, pdfFile);
        
        return pdfFileName;
    }
    
    /**
     * Convert Word file to PDF file
     */
    public void convertWordToPdf(File wordFile, File pdfFile) throws Exception {
        OutputStream out = null;
        
        try {
            // Load Word document
            WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(wordFile);
            
            // Generate PDF directly
            out = new FileOutputStream(pdfFile);
            Docx4J.toPDF(wordMLPackage, out);
            
        } catch (Exception e) {
            throw new Exception("Conversion failed: " + e.getMessage(), e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Get file from path
     */
    public File getFile(String fileName, String directoryPath) throws FileNotFoundException {
        // Security check
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            throw new IllegalArgumentException("Tên file không hợp lệ!");
        }
        
        File file = new File(directoryPath + File.separator + fileName);
        
        if (!file.exists()) {
            throw new FileNotFoundException("File không tồn tại!");
        }
        
        return file;
    }
    
    /**
     * Delete file
     */
    public boolean deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }
    
    /**
     * Ensure directory exists, create if not
     */
    public void ensureDirectoryExists(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
}
