package edu.dut.model.bean;

public class UploadedFile {
    private String originalFileName;
    private String savedFileName;
    private String filePath;
    private long fileSize;
    private String contentType;
    
    public UploadedFile() {
    }
    
    public UploadedFile(String originalFileName, String savedFileName, String filePath, long fileSize, String contentType) {
        this.originalFileName = originalFileName;
        this.savedFileName = savedFileName;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.contentType = contentType;
    }
    
    public String getOriginalFileName() {
        return originalFileName;
    }
    
    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }
    
    public String getSavedFileName() {
        return savedFileName;
    }
    
    public void setSavedFileName(String savedFileName) {
        this.savedFileName = savedFileName;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
    
    public String getContentType() {
        return contentType;
    }
    
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
