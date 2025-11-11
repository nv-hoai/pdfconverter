package edu.dut.service;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.File;

public class WordToPdfConverter {
    
    /**
     * Convert Word document to PDF
     * @param inputStream Input stream of Word document
     * @param outputFile Output PDF file
     * @throws Exception if conversion fails
     */
    public static void convert(InputStream inputStream, File outputFile) throws Exception {
        XWPFDocument document = null;
        OutputStream out = null;
        
        try {
            // Load Word document
            document = new XWPFDocument(inputStream);
            
            // Create output stream
            out = new FileOutputStream(outputFile);
            
            // Create PDF options
            PdfOptions options = PdfOptions.create();
            
            // Convert to PDF
            PdfConverter.getInstance().convert(document, out, options);
            
        } finally {
            if (document != null) {
                try {
                    document.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Convert Word file to PDF file
     * @param inputFile Input Word file
     * @param outputFile Output PDF file
     * @throws Exception if conversion fails
     */
    public static void convert(File inputFile, File outputFile) throws Exception {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(inputFile);
            convert(fis, outputFile);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
