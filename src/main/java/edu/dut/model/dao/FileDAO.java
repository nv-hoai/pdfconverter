package edu.dut.model.dao;

import edu.dut.model.bean.UploadedFile;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;

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
        FileInputStream fis = null;
        XWPFDocument document = null;
        OutputStream out = null;
        
        try {
            fis = new FileInputStream(wordFile);
            document = new XWPFDocument(fis);
            out = new FileOutputStream(pdfFile);
            
            PdfOptions options = PdfOptions.create();
            PdfConverter.getInstance().convert(document, out, options);
            
        } finally {
            if (document != null) {
                try {
                    document.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (fis != null) {
                try {
                    fis.close();
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
