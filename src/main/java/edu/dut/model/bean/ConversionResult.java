package edu.dut.model.bean;

public class ConversionResult {
    private boolean success;
    private String message;
    private String downloadFileName;
    
    public ConversionResult() {
    }
    
    public ConversionResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public ConversionResult(boolean success, String message, String downloadFileName) {
        this.success = success;
        this.message = message;
        this.downloadFileName = downloadFileName;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getDownloadFileName() {
        return downloadFileName;
    }
    
    public void setDownloadFileName(String downloadFileName) {
        this.downloadFileName = downloadFileName;
    }
}
