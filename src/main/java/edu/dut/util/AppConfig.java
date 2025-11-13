package edu.dut.util;

import java.io.File;

/**
 * Configuration class for managing application data paths.
 * 
 * Priority order:
 * 1. Environment variable: PDF_DATA_PATH
 * 2. User home directory: ~/.pdfconverter
 * 3. Default fallback: D:/data/pdfconverter (Windows) or /var/lib/pdfconverter (Linux)
 * 
 * Folders are automatically created if they don't exist.
 */
public class AppConfig {
    
    private static final String BASE_DATA_PATH;
    private static final String UPLOAD_FOLDER = "uploads";
    private static final String OUTPUT_FOLDER = "outputs";
    
    static {
        BASE_DATA_PATH = determineBasePath();
        ensureDirectoriesExist();
        logConfiguration();
    }
    
    /**
     * Determine base path with priority: ENV → User Home → Default
     */
    private static String determineBasePath() {
        // Priority 1: Environment variable
        String envPath = System.getenv("PDF_DATA_PATH");
        if (envPath != null && !envPath.trim().isEmpty()) {
            return normalizePathSeparators(envPath.trim());
        }
        
        // Priority 2: User home directory
        String userHome = System.getProperty("user.home");
        if (userHome != null) {
            return normalizePathSeparators(userHome + "/.pdfconverter");
        }
        
        // Priority 3: Default fallback based on OS
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("win")) {
            return "D:/data/pdfconverter";
        } else {
            return "/var/lib/pdfconverter";
        }
    }
    
    /**
     * Normalize path separators to forward slash for consistency
     */
    private static String normalizePathSeparators(String path) {
        return path.replace("\\", "/");
    }
    
    /**
     * Ensure all required directories exist, create if necessary
     */
    private static void ensureDirectoriesExist() {
        createDirectoryIfNotExists(BASE_DATA_PATH);
        createDirectoryIfNotExists(getUploadPath());
        createDirectoryIfNotExists(getOutputPath());
    }
    
    /**
     * Create directory if it doesn't exist
     */
    private static void createDirectoryIfNotExists(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                System.out.println("✓ Created directory: " + path);
            } else {
                System.err.println("⚠ Failed to create directory: " + path);
            }
        }
    }
    
    /**
     * Log configuration to console
     */
    private static void logConfiguration() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("📁 PDF Converter - Storage Configuration");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        String envPath = System.getenv("PDF_DATA_PATH");
        if (envPath != null && !envPath.trim().isEmpty()) {
            System.out.println("✓ Using path from environment variable PDF_DATA_PATH");
        } else {
            System.out.println("ℹ No PDF_DATA_PATH environment variable found");
            System.out.println("✓ Using fallback path");
        }
        
        System.out.println("Base path:   " + BASE_DATA_PATH);
        System.out.println("Upload path: " + getUploadPath());
        System.out.println("Output path: " + getOutputPath());
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }
    
    /**
     * Get base data path
     */
    public static String getBasePath() {
        return BASE_DATA_PATH;
    }
    
    /**
     * Get upload directory path (for Word files)
     */
    public static String getUploadPath() {
        return BASE_DATA_PATH + "/" + UPLOAD_FOLDER;
    }
    
    /**
     * Get output directory path (for PDF files)
     */
    public static String getOutputPath() {
        return BASE_DATA_PATH + "/" + OUTPUT_FOLDER;
    }
    
    /**
     * Check if storage is ready
     */
    public static boolean isStorageReady() {
        File baseDir = new File(BASE_DATA_PATH);
        File uploadDir = new File(getUploadPath());
        File outputDir = new File(getOutputPath());
        
        return baseDir.exists() && baseDir.isDirectory() &&
               uploadDir.exists() && uploadDir.isDirectory() &&
               outputDir.exists() && outputDir.isDirectory();
    }
}
