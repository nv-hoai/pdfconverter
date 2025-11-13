package edu.dut.distributed.model;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents a connected worker node
 */
public class WorkerNode {
    private String workerId;
    private String hostname;
    private String ipAddress;
    private int port;
    private int cores;
    private long memoryMB;
    
    private volatile WorkerStatus status;
    private AtomicLong lastHeartbeat;
    private AtomicInteger currentJobCount;
    private AtomicInteger totalJobsCompleted;
    private AtomicInteger totalJobsFailed;
    
    private long connectedTime;
    
    public enum WorkerStatus {
        CONNECTING,
        IDLE,
        BUSY,
        OFFLINE
    }
    
    public WorkerNode(String workerId, String hostname, String ipAddress, int port) {
        this.workerId = workerId;
        this.hostname = hostname;
        this.ipAddress = ipAddress;
        this.port = port;
        this.status = WorkerStatus.CONNECTING;
        this.lastHeartbeat = new AtomicLong(System.currentTimeMillis());
        this.currentJobCount = new AtomicInteger(0);
        this.totalJobsCompleted = new AtomicInteger(0);
        this.totalJobsFailed = new AtomicInteger(0);
        this.connectedTime = System.currentTimeMillis();
    }
    
    public void updateHeartbeat() {
        lastHeartbeat.set(System.currentTimeMillis());
    }
    
    public boolean isHealthy() {
        long timeSinceLastHeartbeat = System.currentTimeMillis() - lastHeartbeat.get();
        return timeSinceLastHeartbeat < 30000; // 30 seconds timeout
    }
    
    public boolean isAvailable() {
        return status == WorkerStatus.IDLE && isHealthy();
    }
    
    public void assignJob() {
        currentJobCount.incrementAndGet();
        status = WorkerStatus.BUSY;
    }
    
    public void completeJob(boolean success) {
        currentJobCount.decrementAndGet();
        if (success) {
            totalJobsCompleted.incrementAndGet();
        } else {
            totalJobsFailed.incrementAndGet();
        }
        
        if (currentJobCount.get() == 0) {
            status = WorkerStatus.IDLE;
        }
    }
    
    public long getUptimeSeconds() {
        return (System.currentTimeMillis() - connectedTime) / 1000;
    }
    
    // Getters and Setters
    public String getWorkerId() {
        return workerId;
    }
    
    public String getHostname() {
        return hostname;
    }
    
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public int getPort() {
        return port;
    }
    
    public int getCores() {
        return cores;
    }
    
    public void setCores(int cores) {
        this.cores = cores;
    }
    
    public long getMemoryMB() {
        return memoryMB;
    }
    
    public void setMemoryMB(long memoryMB) {
        this.memoryMB = memoryMB;
    }
    
    public WorkerStatus getStatus() {
        return status;
    }
    
    public void setStatus(WorkerStatus status) {
        this.status = status;
    }
    
    public long getLastHeartbeat() {
        return lastHeartbeat.get();
    }
    
    public int getCurrentJobCount() {
        return currentJobCount.get();
    }
    
    public int getTotalJobsCompleted() {
        return totalJobsCompleted.get();
    }
    
    public int getTotalJobsFailed() {
        return totalJobsFailed.get();
    }
    
    @Override
    public String toString() {
        return "WorkerNode{" +
                "workerId='" + workerId + '\'' +
                ", hostname='" + hostname + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", status=" + status +
                ", currentJobs=" + currentJobCount.get() +
                ", completed=" + totalJobsCompleted.get() +
                ", failed=" + totalJobsFailed.get() +
                '}';
    }
}
