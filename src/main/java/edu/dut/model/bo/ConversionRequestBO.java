package edu.dut.model.bo;

import edu.dut.model.bean.ConversionRequest;
import edu.dut.model.bean.UploadedFile;
import edu.dut.model.dao.ConversionRequestDAO;
import edu.dut.model.dao.FileDAO;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;

public class ConversionRequestBO {
    private ConversionRequestDAO requestDAO;
    private FileDAO fileDAO;
    
    public ConversionRequestBO() {
        this.requestDAO = new ConversionRequestDAO();
        this.fileDAO = new FileDAO();
    }
    
    public int submitRequest(int userId, InputStream inputStream, String originalFileName, 
                            long fileSize, String uploadPath) throws Exception {
        // Validate file
        if (!isValidWordFile(originalFileName)) {
            throw new IllegalArgumentException("Chỉ chấp nhận file Word (.doc hoặc .docx)");
        }
        
        if (fileSize > 20 * 1024 * 1024) { // 20MB
            throw new IllegalArgumentException("File quá lớn. Kích thước tối đa: 20MB");
        }
        
        // Save uploaded file
        UploadedFile uploadedFile = fileDAO.saveUploadedFile(inputStream, originalFileName, uploadPath);
        
        // Create request record
        ConversionRequest request = new ConversionRequest();
        request.setUserId(userId);
        request.setOriginalFilename(originalFileName);
        request.setSavedFilename(uploadedFile.getSavedFileName());
        request.setFileSize(fileSize);
        request.setStatus(ConversionRequest.RequestStatus.PENDING);
        
        return requestDAO.createRequest(request);
    }
    
    public List<ConversionRequest> getUserRequests(int userId) throws SQLException {
        return requestDAO.getRequestsByUser(userId);
    }
    
    public ConversionRequest getRequestById(int requestId) throws SQLException {
        return requestDAO.getRequestById(requestId);
    }
    
    public int countUserRequests(int userId) throws SQLException {
        return requestDAO.countRequestsByUser(userId);
    }
    
    private boolean isValidWordFile(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }
        String lowerFileName = fileName.toLowerCase();
        return lowerFileName.endsWith(".doc") || lowerFileName.endsWith(".docx");
    }
    
    public void ensureDirectoryExists(String path) {
        fileDAO.ensureDirectoryExists(path);
    }
}
