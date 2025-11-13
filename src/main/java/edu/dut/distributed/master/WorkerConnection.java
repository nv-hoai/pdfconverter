package edu.dut.distributed.master;

import edu.dut.distributed.model.WorkerNode;
import edu.dut.distributed.protocol.MessageType;
import edu.dut.distributed.protocol.WorkerMessage;
import edu.dut.util.AppConfig;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Handles communication with a single worker
 */
public class WorkerConnection implements Runnable {
    private Socket socket;
    private WorkerNode worker;
    private WorkerManager manager;
    
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private BlockingQueue<WorkerMessage> messageQueue;
    
    private volatile boolean running = false;
    private Thread senderThread;
    
    public WorkerConnection(Socket socket, WorkerManager manager) {
        this.socket = socket;
        this.manager = manager;
        this.messageQueue = new LinkedBlockingQueue<>();
    }
    
    @Override
    public void run() {
        try {
            // Setup streams
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
            
            running = true;
            
            // Start message sender thread
            senderThread = new Thread(this::messageSender, "WorkerSender-" + socket.getInetAddress());
            senderThread.setDaemon(true);
            senderThread.start();
            
            // Main message receiver loop
            while (running) {
                try {
                    WorkerMessage message = (WorkerMessage) in.readObject();
                    handleMessage(message);
                } catch (EOFException e) {
                    // Normal disconnection
                    String id = (worker != null) ? worker.getWorkerId() : "unknown";
                    System.out.println("ℹ Worker disconnected normally: " + id);
                    break;
                } catch (java.net.SocketException e) {
                    // Connection reset - normal when worker shuts down
                    String id = (worker != null) ? worker.getWorkerId() : "unknown";
                    if (e.getMessage().contains("Connection reset")) {
                        System.out.println("ℹ Worker disconnected: " + id);
                    } else {
                        System.err.println("✗ Socket error with worker " + id + ": " + e.getMessage());
                    }
                    break;
                } catch (ClassNotFoundException e) {
                    System.err.println("✗ Protocol mismatch - ClassNotFoundException: " + e.getMessage());
                    e.printStackTrace();
                    break;
                } catch (IOException e) {
                    String id = (worker != null) ? worker.getWorkerId() : "unknown";
                    if (!running) {
                        // Expected - we're shutting down
                        System.out.println("ℹ Worker connection closed: " + id);
                    } else {
                        System.err.println("✗ IO error reading from worker " + id + ": " + e.getMessage());
                    }
                    break;
                }
            }
            
        } catch (IOException e) {
            System.err.println("Worker connection error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            cleanup();
        }
    }
    
    private void handleMessage(WorkerMessage message) {
        System.out.println("Received from worker: " + message.getType() + " - " + message.getWorkerId());
        
        switch (message.getType()) {
            case WORKER_REGISTER:
                handleRegister(message);
                break;
                
            case WORKER_HEARTBEAT:
                handleHeartbeat(message);
                break;
                
            case JOB_RESULT_SUCCESS:
            case JOB_RESULT_FAILED:
                handleJobResult(message);
                break;
                
            default:
                System.err.println("Unknown message type: " + message.getType());
        }
    }
    
    private void handleRegister(WorkerMessage message) {
        String workerId = message.getWorkerId();
        String hostname = message.getString("hostname");
        Integer cores = message.getInt("cores");
        Integer memoryMB = message.getInt("memoryMB");
        
        // Create worker node
        worker = new WorkerNode(
            workerId,
            hostname,
            socket.getInetAddress().getHostAddress(),
            socket.getPort()
        );
        
        if (cores != null) worker.setCores(cores);
        if (memoryMB != null) worker.setMemoryMB(memoryMB);
        
        worker.setStatus(WorkerNode.WorkerStatus.IDLE);
        
        // Register with manager
        manager.registerWorker(worker, this);
        
        // Send confirmation
        WorkerMessage response = new WorkerMessage(MessageType.WORKER_REGISTERED, workerId);
        response.put("message", "Successfully registered");
        sendMessage(response);
        
        System.out.println("✓ Worker registered: " + worker);
    }
    
    private void handleHeartbeat(WorkerMessage message) {
        if (worker != null) {
            worker.updateHeartbeat();
            
            // Send ACK
            WorkerMessage ack = new WorkerMessage(MessageType.HEARTBEAT_ACK, worker.getWorkerId());
            sendMessage(ack);
        }
    }
    
    private void handleJobResult(WorkerMessage message) {
        if (worker == null) return;
        
        Integer requestId = message.getInt("requestId");
        String pdfFilename = message.getString("pdfFilename");
        Boolean success = message.getBoolean("success");
        String errorMessage = message.getString("errorMessage");
        byte[] pdfData = message.getFileData();
        
        if (requestId == null) {
            System.err.println("Job result missing requestId");
            return;
        }
        
        // If success, save PDF file to disk
        if (success != null && success && pdfData != null && pdfFilename != null) {
            try {
                File pdfFile = new File(AppConfig.getOutputPath() + File.separator + pdfFilename);
                Files.write(pdfFile.toPath(), pdfData);
                
                long pdfSizeKB = pdfData.length / 1024;
                System.out.println("✓ Saved PDF: " + pdfFilename + " (" + pdfSizeKB + " KB)");
                
            } catch (IOException e) {
                System.err.println("✗ Failed to save PDF file: " + e.getMessage());
                e.printStackTrace();
                success = false;
                errorMessage = "Failed to save PDF: " + e.getMessage();
            }
        }
        
        // Notify manager
        manager.handleJobResult(
            requestId,
            pdfFilename,
            success != null && success,
            errorMessage,
            worker
        );
        
        System.out.println("✓ Job #" + requestId + " result from " + worker.getWorkerId() + 
                         ": " + (success ? "SUCCESS" : "FAILED"));
    }
    
    public void sendMessage(WorkerMessage message) {
        try {
            messageQueue.put(message);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private void messageSender() {
        while (running) {
            try {
                WorkerMessage message = messageQueue.take();
                out.writeObject(message);
                out.flush();
            } catch (InterruptedException e) {
                break;
            } catch (IOException e) {
                System.err.println("Failed to send message: " + e.getMessage());
                running = false;
                break;
            }
        }
    }
    
    public void assignJob(int requestId, String savedFilename, String originalFilename) {
        try {
            // Read Word file from disk
            File wordFile = new File(AppConfig.getUploadPath() + File.separator + savedFilename);
            
            if (!wordFile.exists()) {
                System.err.println("✗ Word file not found: " + wordFile.getAbsolutePath());
                return;
            }
            
            byte[] fileData = Files.readAllBytes(wordFile.toPath());
            
            // Create job assignment message with file data
            WorkerMessage message = new WorkerMessage(MessageType.JOB_ASSIGN, worker.getWorkerId());
            message.setJobData(requestId, savedFilename, originalFilename);
            message.setFileData(fileData, fileData.length);
            
            sendMessage(message);
            
            worker.assignJob();
            
            long fileSizeKB = fileData.length / 1024;
            System.out.println("→ Assigned job #" + requestId + " to " + worker.getWorkerId() + 
                             " (" + fileSizeKB + " KB)");
            
        } catch (IOException e) {
            System.err.println("✗ Failed to read file for job #" + requestId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public WorkerNode getWorker() {
        return worker;
    }
    
    public void shutdown() {
        running = false;
        if (worker != null) {
            WorkerMessage shutdownMsg = new WorkerMessage(MessageType.WORKER_SHUTDOWN, worker.getWorkerId());
            sendMessage(shutdownMsg);
        }
    }
    
    private void cleanup() {
        running = false;
        
        // Interrupt sender thread
        if (senderThread != null && senderThread.isAlive()) {
            senderThread.interrupt();
            try {
                senderThread.join(1000); // Wait max 1 second
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        if (worker != null) {
            manager.unregisterWorker(worker.getWorkerId());
            // Disconnection message already logged in exception handler
        }
        
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            // Ignore cleanup errors
        }
    }
}
