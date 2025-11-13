package edu.dut.distributed.master;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * TCP Server for accepting worker connections
 */
@WebListener
public class MasterTCPServer implements ServletContextListener {
    
    private static final int DEFAULT_PORT = 7777;
    private static final int MAX_WORKER_CONNECTIONS = 50;
    
    private ServerSocket serverSocket;
    private Thread acceptThread;
    private ExecutorService executorService;
    private volatile boolean running = false;
    
    private WorkerManager workerManager;
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("🖥️  Starting Master TCP Server for Workers...");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        workerManager = WorkerManager.getInstance();
        executorService = Executors.newFixedThreadPool(MAX_WORKER_CONNECTIONS);
        
        try {
            serverSocket = new ServerSocket(DEFAULT_PORT);
            running = true;
            
            acceptThread = new Thread(this::acceptConnections);
            acceptThread.setDaemon(true);
            acceptThread.start();
            
            System.out.println("✓ Master TCP Server started on port " + DEFAULT_PORT);
            System.out.println("✓ Workers can connect to: <server-ip>:" + DEFAULT_PORT);
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            
        } catch (IOException e) {
            System.err.println("❌ Failed to start Master TCP Server: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("Stopping Master TCP Server...");
        
        running = false;
        
        // Shutdown all workers
        if (workerManager != null) {
            workerManager.shutdownAll();
        }
        
        // Close server socket
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        // Wait for accept thread
        if (acceptThread != null) {
            try {
                acceptThread.join(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        // Shutdown executor
        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
            }
        }
        
        // Cleanup MySQL JDBC driver
        try {
            com.mysql.cj.jdbc.AbandonedConnectionCleanupThread.checkedShutdown();
        } catch (Exception e) {
            // Ignore if class not found or already shutdown
        }
        
        System.out.println("Master TCP Server stopped");
    }
    
    private void acceptConnections() {
        System.out.println("⏳ Waiting for worker connections...");
        
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                String clientAddress = clientSocket.getInetAddress().getHostAddress();
                
                System.out.println("➤ New connection from: " + clientAddress + ":" + clientSocket.getPort());
                
                // Handle worker connection in separate thread
                WorkerConnection workerConnection = new WorkerConnection(clientSocket, workerManager);
                executorService.submit(workerConnection);
                
            } catch (IOException e) {
                if (running) {
                    System.err.println("Error accepting connection: " + e.getMessage());
                }
            }
        }
    }
}
