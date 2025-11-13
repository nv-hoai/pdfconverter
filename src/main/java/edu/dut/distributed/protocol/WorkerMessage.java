package edu.dut.distributed.protocol;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Protocol message for Master-Worker communication
 */
public class WorkerMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private MessageType type;
    private String workerId;
    private long timestamp;
    private Map<String, Object> data;
    
    // File transfer fields
    private byte[] fileData;
    private long fileSize;
    
    public WorkerMessage() {
        this.timestamp = System.currentTimeMillis();
        this.data = new HashMap<>();
    }
    
    public WorkerMessage(MessageType type, String workerId) {
        this();
        this.type = type;
        this.workerId = workerId;
    }
    
    // Job assignment data
    public void setJobData(int requestId, String savedFilename, String originalFilename) {
        data.put("requestId", requestId);
        data.put("savedFilename", savedFilename);
        data.put("originalFilename", originalFilename);
    }
    
    // Job result data
    public void setResultData(int requestId, String pdfFilename, boolean success, String errorMessage) {
        data.put("requestId", requestId);
        data.put("pdfFilename", pdfFilename);
        data.put("success", success);
        data.put("errorMessage", errorMessage);
    }
    
    // Worker info
    public void setWorkerInfo(String hostname, int cores, long memoryMB) {
        data.put("hostname", hostname);
        data.put("cores", cores);
        data.put("memoryMB", memoryMB);
    }
    
    public void put(String key, Object value) {
        data.put(key, value);
    }
    
    public Object get(String key) {
        return data.get(key);
    }
    
    public Integer getInt(String key) {
        Object value = data.get(key);
        if (value instanceof Integer) {
            return (Integer) value;
        }
        return null;
    }
    
    public String getString(String key) {
        Object value = data.get(key);
        return value != null ? value.toString() : null;
    }
    
    public Boolean getBoolean(String key) {
        Object value = data.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return null;
    }
    
    // File transfer methods
    public void setFileData(byte[] fileData, long fileSize) {
        this.fileData = fileData;
        this.fileSize = fileSize;
    }
    
    public byte[] getFileData() {
        return fileData;
    }
    
    public long getFileSize() {
        return fileSize;
    }
    
    // Getters and Setters
    public MessageType getType() {
        return type;
    }
    
    public void setType(MessageType type) {
        this.type = type;
    }
    
    public String getWorkerId() {
        return workerId;
    }
    
    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public Map<String, Object> getData() {
        return data;
    }
    
    public void setData(Map<String, Object> data) {
        this.data = data;
    }
    
    @Override
    public String toString() {
        return "WorkerMessage{" +
                "type=" + type +
                ", workerId='" + workerId + '\'' +
                ", timestamp=" + timestamp +
                ", data=" + data +
                '}';
    }
}
