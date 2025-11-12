package edu.dut.service;

import edu.dut.model.bean.ConversionRequest;
import edu.dut.model.dao.ConversionRequestDAO;
import edu.dut.model.dao.FileDAO;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.File;

@WebListener
public class ConversionQueueProcessor implements ServletContextListener {
    
    private Thread processorThread;
    private volatile boolean running = false;
    private ConversionRequestDAO requestDAO;
    private FileDAO fileDAO;
    private String uploadPath;
    private String outputPath;
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("Starting Conversion Queue Processor...");
        
        requestDAO = new ConversionRequestDAO();
        fileDAO = new FileDAO();
        
        // Get paths from context
        String applicationPath = sce.getServletContext().getRealPath("");
        uploadPath = applicationPath + File.separator + "uploads";
        outputPath = applicationPath + File.separator + "outputs";
        
        // Ensure directories exist
        fileDAO.ensureDirectoryExists(uploadPath);
        fileDAO.ensureDirectoryExists(outputPath);
        
        // Start processor thread
        running = true;
        processorThread = new Thread(new QueueProcessor());
        processorThread.setDaemon(true);
        processorThread.start();
        
        System.out.println("Conversion Queue Processor started successfully");
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("Stopping Conversion Queue Processor...");
        running = false;
        
        if (processorThread != null) {
            try {
                processorThread.interrupt();
                processorThread.join(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        System.out.println("Conversion Queue Processor stopped");
    }
    
    private class QueueProcessor implements Runnable {
        @Override
        public void run() {
            while (running) {
                try {
                    // Get next pending request
                    ConversionRequest request = requestDAO.getNextPendingRequest();
                    
                    if (request != null) {
                        processRequest(request);
                    } else {
                        // No pending requests, sleep for a while
                        Thread.sleep(2000);
                    }
                    
                } catch (InterruptedException e) {
                    if (!running) {
                        break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        break;
                    }
                }
            }
        }
        
        private void processRequest(ConversionRequest request) {
            System.out.println("Processing request #" + request.getRequestId() + 
                             " - File: " + request.getOriginalFilename());
            
            try {
                // Update status to PROCESSING
                requestDAO.updateStatus(request.getRequestId(), 
                    ConversionRequest.RequestStatus.PROCESSING);
                
                // Get word file
                File wordFile = new File(uploadPath + File.separator + request.getSavedFilename());
                
                if (!wordFile.exists()) {
                    throw new Exception("File không tồn tại: " + wordFile.getAbsolutePath());
                }
                
                // Generate PDF filename
                String pdfFileName = request.getSavedFilename();
                pdfFileName = pdfFileName.substring(0, pdfFileName.lastIndexOf('.')) + ".pdf";
                
                File pdfFile = new File(outputPath + File.separator + pdfFileName);
                
                // Convert
                fileDAO.convertWordToPdf(wordFile, pdfFile);
                
                // Delete word file
                wordFile.delete();
                
                // Update status to COMPLETED
                requestDAO.updateCompleted(request.getRequestId(), pdfFileName);
                
                System.out.println("Successfully processed request #" + request.getRequestId());
                
            } catch (Exception e) {
                System.err.println("Failed to process request #" + request.getRequestId() + 
                                 ": " + e.getMessage());
                e.printStackTrace();
                
                try {
                    requestDAO.updateFailed(request.getRequestId(), e.getMessage());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
