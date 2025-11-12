package edu.dut.service;

import edu.dut.model.bean.ConversionRequest;
import edu.dut.model.dao.ConversionRequestDAO;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.File;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@WebListener
public class FileCleanupTask implements ServletContextListener {
    
    private Timer timer;
    private static final long CLEANUP_INTERVAL = 24 * 60 * 60 * 1000; // 24 hours
    private static final int FILE_RETENTION_DAYS = 7; // Keep files for 7 days
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String uploadPath = sce.getServletContext().getRealPath("") + File.separator + "uploads";
        String outputPath = sce.getServletContext().getRealPath("") + File.separator + "outputs";
        
        timer = new Timer("FileCleanupTask", true);
        
        // Run cleanup every 24 hours, starting 1 hour after startup
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                cleanupOldFiles(uploadPath, outputPath);
            }
        }, 60 * 60 * 1000, CLEANUP_INTERVAL); // Start after 1 hour, repeat every 24 hours
        
        System.out.println("File Cleanup Task started - will run every 24 hours");
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (timer != null) {
            timer.cancel();
            System.out.println("File Cleanup Task stopped");
        }
    }
    
    private void cleanupOldFiles(String uploadPath, String outputPath) {
        System.out.println("Starting file cleanup task...");
        
        try {
            ConversionRequestDAO dao = new ConversionRequestDAO();
            
            // Calculate cutoff date (7 days ago)
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -FILE_RETENTION_DAYS);
            Date cutoffDate = calendar.getTime();
            
            // Get old completed/failed requests
            List<ConversionRequest> oldRequests = dao.getOldRequests(cutoffDate);
            
            int deletedFiles = 0;
            int deletedRecords = 0;
            
            for (ConversionRequest request : oldRequests) {
                boolean filesDeleted = true;
                
                // Delete original file
                if (request.getSavedFilename() != null) {
                    File originalFile = new File(uploadPath, request.getSavedFilename());
                    if (originalFile.exists()) {
                        if (originalFile.delete()) {
                            deletedFiles++;
                        } else {
                            filesDeleted = false;
                        }
                    }
                }
                
                // Delete PDF file
                if (request.getPdfFilename() != null) {
                    File pdfFile = new File(outputPath, request.getPdfFilename());
                    if (pdfFile.exists()) {
                        if (pdfFile.delete()) {
                            deletedFiles++;
                        } else {
                            filesDeleted = false;
                        }
                    }
                }
                
                // Delete database record if files were deleted successfully
                if (filesDeleted) {
                    dao.deleteRequest(request.getRequestId());
                    deletedRecords++;
                }
            }
            
            System.out.println("File cleanup completed: " + deletedFiles + " files and " + 
                             deletedRecords + " records deleted");
            
        } catch (SQLException e) {
            System.err.println("Error during file cleanup: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
