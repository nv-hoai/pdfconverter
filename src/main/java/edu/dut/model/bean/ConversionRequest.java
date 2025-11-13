package edu.dut.model.bean;

import java.sql.Timestamp;

public class ConversionRequest {
    private int requestId;
    private int userId;
    private String originalFilename;
    private String savedFilename;
    private long fileSize;
    private RequestStatus status;
    private String pdfFilename;
    private String errorMessage;
    private Timestamp createdAt;
    private Timestamp startedAt;
    private Timestamp completedAt;
    
    // For display purposes
    private String username;
    
    public enum RequestStatus {
        PENDING, 
        PROCESSING, 
        COMPLETED, 
        DELETED,    // File đã bị xóa bởi cleanup task
        FAILED
    }
    
    public ConversionRequest() {
        this.status = RequestStatus.PENDING;
    }
    
    public int getRequestId() {
        return requestId;
    }
    
    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getOriginalFilename() {
        return originalFilename;
    }
    
    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }
    
    public String getSavedFilename() {
        return savedFilename;
    }
    
    public void setSavedFilename(String savedFilename) {
        this.savedFilename = savedFilename;
    }
    
    public long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
    
    public RequestStatus getStatus() {
        return status;
    }
    
    public void setStatus(RequestStatus status) {
        this.status = status;
    }
    
    public String getPdfFilename() {
        return pdfFilename;
    }
    
    public void setPdfFilename(String pdfFilename) {
        this.pdfFilename = pdfFilename;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
    public Timestamp getStartedAt() {
        return startedAt;
    }
    
    public void setStartedAt(Timestamp startedAt) {
        this.startedAt = startedAt;
    }
    
    public Timestamp getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(Timestamp completedAt) {
        this.completedAt = completedAt;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getStatusDisplay() {
        switch (status) {
            case PENDING: return "Đang chờ";
            case PROCESSING: return "Đang xử lý";
            case COMPLETED: return "Hoàn thành";
            case DELETED: return "Đã xóa";
            case FAILED: return "Thất bại";
            default: return status.toString();
        }
    }
}
