package edu.dut.distributed.master;

import edu.dut.distributed.model.WorkerNode;
import edu.dut.model.bean.ConversionRequest;
import edu.dut.model.dao.ConversionRequestDAO;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Manages all connected workers and job distribution
 */
public class WorkerManager {
    
    private static WorkerManager instance;
    
    private Map<String, WorkerNode> workers;
    private Map<String, WorkerConnection> connections;
    private ReadWriteLock lock;
    
    private ConversionRequestDAO requestDAO;
    
    private WorkerManager() {
        this.workers = new ConcurrentHashMap<>();
        this.connections = new ConcurrentHashMap<>();
        this.lock = new ReentrantReadWriteLock();
        this.requestDAO = new ConversionRequestDAO();
    }
    
    public static synchronized WorkerManager getInstance() {
        if (instance == null) {
            instance = new WorkerManager();
        }
        return instance;
    }
    
    public void registerWorker(WorkerNode worker, WorkerConnection connection) {
        lock.writeLock().lock();
        try {
            workers.put(worker.getWorkerId(), worker);
            connections.put(worker.getWorkerId(), connection);
            System.out.println("✓ Worker pool: " + workers.size() + " workers connected");
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public void unregisterWorker(String workerId) {
        lock.writeLock().lock();
        try {
            WorkerNode worker = workers.remove(workerId);
            connections.remove(workerId);
            
            if (worker != null) {
                System.out.println("✗ Worker removed: " + workerId);
                System.out.println("  Remaining workers: " + workers.size());
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Try to assign job to an available worker
     * @return true if job was assigned to a worker, false if no worker available
     */
    public boolean tryAssignJob(ConversionRequest request) {
        lock.readLock().lock();
        try {
            // Find available worker
            WorkerNode availableWorker = null;
            WorkerConnection connection = null;
            
            for (Map.Entry<String, WorkerNode> entry : workers.entrySet()) {
                WorkerNode worker = entry.getValue();
                
                if (worker.isAvailable()) {
                    availableWorker = worker;
                    connection = connections.get(worker.getWorkerId());
                    break;
                }
            }
            
            if (availableWorker != null && connection != null) {
                // Note: Status already updated to PROCESSING by getNextPendingRequest()
                
                // Assign job to worker
                connection.assignJob(
                    request.getRequestId(),
                    request.getSavedFilename(),
                    request.getOriginalFilename()
                );
                return true;
            }
            
            return false;
            
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Handle job result from worker
     */
    public void handleJobResult(int requestId, String pdfFilename, boolean success, 
                                String errorMessage, WorkerNode worker) {
        try {
            if (success) {
                requestDAO.updateCompleted(requestId, pdfFilename);
                System.out.println("✓ Job #" + requestId + " completed by " + worker.getWorkerId());
            } else {
                requestDAO.updateFailed(requestId, errorMessage != null ? errorMessage : "Unknown error");
                System.err.println("✗ Job #" + requestId + " failed on " + worker.getWorkerId() + 
                                 ": " + errorMessage);
            }
            
            worker.completeJob(success);
            
        } catch (SQLException e) {
            System.err.println("Failed to update job result in database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Get statistics about all workers
     */
    public Map<String, Object> getStatistics() {
        lock.readLock().lock();
        try {
            Map<String, Object> stats = new HashMap<>();
            
            int totalWorkers = workers.size();
            int idleWorkers = 0;
            int busyWorkers = 0;
            int offlineWorkers = 0;
            int totalJobs = 0;
            int completedJobs = 0;
            int failedJobs = 0;
            
            for (WorkerNode worker : workers.values()) {
                switch (worker.getStatus()) {
                    case IDLE: idleWorkers++; break;
                    case BUSY: busyWorkers++; break;
                    case OFFLINE: offlineWorkers++; break;
                    case CONNECTING: break; // Skip connecting workers
                }
                
                totalJobs += worker.getCurrentJobCount();
                completedJobs += worker.getTotalJobsCompleted();
                failedJobs += worker.getTotalJobsFailed();
            }
            
            stats.put("totalWorkers", totalWorkers);
            stats.put("idleWorkers", idleWorkers);
            stats.put("busyWorkers", busyWorkers);
            stats.put("offlineWorkers", offlineWorkers);
            stats.put("totalJobs", totalJobs);
            stats.put("completedJobs", completedJobs);
            stats.put("failedJobs", failedJobs);
            
            return stats;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Get list of all workers
     */
    public List<WorkerNode> getAllWorkers() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(workers.values());
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Check if any worker is available
     */
    public boolean hasAvailableWorker() {
        lock.readLock().lock();
        try {
            for (WorkerNode worker : workers.values()) {
                if (worker.isAvailable()) {
                    return true;
                }
            }
            return false;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Shutdown all workers
     */
    public void shutdownAll() {
        lock.writeLock().lock();
        try {
            for (WorkerConnection connection : connections.values()) {
                connection.shutdown();
            }
            workers.clear();
            connections.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }
}
