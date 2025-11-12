package edu.dut.model.dao;

import edu.dut.model.bean.ConversionRequest;
import edu.dut.model.bean.ConversionRequest.RequestStatus;
import edu.dut.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConversionRequestDAO {
    
    public int createRequest(ConversionRequest request) throws SQLException {
        String sql = "INSERT INTO conversion_requests (user_id, original_filename, saved_filename, file_size, status) " +
                     "VALUES (?, ?, ?, ?, ?)";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, request.getUserId());
            stmt.setString(2, request.getOriginalFilename());
            stmt.setString(3, request.getSavedFilename());
            stmt.setLong(4, request.getFileSize());
            stmt.setString(5, request.getStatus().toString());
            
            stmt.executeUpdate();
            
            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
            
            return -1;
            
        } finally {
            DatabaseUtil.close(rs, stmt, conn);
        }
    }
    
    public void updateStatus(int requestId, RequestStatus status) throws SQLException {
        String sql = "UPDATE conversion_requests SET status = ?, " +
                     (status == RequestStatus.PROCESSING ? "started_at = NOW()" : "") +
                     (status == RequestStatus.COMPLETED || status == RequestStatus.FAILED ? ", completed_at = NOW()" : "") +
                     " WHERE request_id = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, status.toString());
            stmt.setInt(2, requestId);
            stmt.executeUpdate();
            
        } finally {
            DatabaseUtil.close(stmt, conn);
        }
    }
    
    public void updateCompleted(int requestId, String pdfFilename) throws SQLException {
        String sql = "UPDATE conversion_requests SET status = ?, pdf_filename = ?, completed_at = NOW() " +
                     "WHERE request_id = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, RequestStatus.COMPLETED.toString());
            stmt.setString(2, pdfFilename);
            stmt.setInt(3, requestId);
            stmt.executeUpdate();
            
        } finally {
            DatabaseUtil.close(stmt, conn);
        }
    }
    
    public void updateFailed(int requestId, String errorMessage) throws SQLException {
        String sql = "UPDATE conversion_requests SET status = ?, error_message = ?, completed_at = NOW() " +
                     "WHERE request_id = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, RequestStatus.FAILED.toString());
            stmt.setString(2, errorMessage);
            stmt.setInt(3, requestId);
            stmt.executeUpdate();
            
        } finally {
            DatabaseUtil.close(stmt, conn);
        }
    }
    
    public List<ConversionRequest> getRequestsByUser(int userId) throws SQLException {
        String sql = "SELECT * FROM conversion_requests WHERE user_id = ? ORDER BY created_at DESC";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<ConversionRequest> requests = new ArrayList<>();
        
        try {
            conn = DatabaseUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                requests.add(mapResultSet(rs));
            }
            
            return requests;
            
        } finally {
            DatabaseUtil.close(rs, stmt, conn);
        }
    }
    
    public ConversionRequest getNextPendingRequest() throws SQLException {
        String sql = "SELECT * FROM conversion_requests WHERE status = 'PENDING' ORDER BY created_at LIMIT 1";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSet(rs);
            }
            
            return null;
            
        } finally {
            DatabaseUtil.close(rs, stmt, conn);
        }
    }
    
    public ConversionRequest getRequestById(int requestId) throws SQLException {
        String sql = "SELECT * FROM conversion_requests WHERE request_id = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, requestId);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSet(rs);
            }
            
            return null;
            
        } finally {
            DatabaseUtil.close(rs, stmt, conn);
        }
    }
    
    public int countRequestsByUser(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM conversion_requests WHERE user_id = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
            return 0;
            
        } finally {
            DatabaseUtil.close(rs, stmt, conn);
        }
    }
    
    public List<ConversionRequest> getOldRequests(java.util.Date cutoffDate) throws SQLException {
        String sql = "SELECT * FROM conversion_requests WHERE " +
                     "(status = 'COMPLETED' OR status = 'FAILED') AND " +
                     "completed_at < ? " +
                     "ORDER BY completed_at ASC";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<ConversionRequest> requests = new java.util.ArrayList<>();
        
        try {
            conn = DatabaseUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setTimestamp(1, new java.sql.Timestamp(cutoffDate.getTime()));
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                requests.add(mapResultSet(rs));
            }
            
            return requests;
            
        } finally {
            DatabaseUtil.close(rs, stmt, conn);
        }
    }
    
    public void deleteRequest(int requestId) throws SQLException {
        String sql = "DELETE FROM conversion_requests WHERE request_id = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, requestId);
            stmt.executeUpdate();
            
        } finally {
            DatabaseUtil.close(stmt, conn);
        }
    }
    
    private ConversionRequest mapResultSet(ResultSet rs) throws SQLException {
        ConversionRequest request = new ConversionRequest();
        request.setRequestId(rs.getInt("request_id"));
        request.setUserId(rs.getInt("user_id"));
        request.setOriginalFilename(rs.getString("original_filename"));
        request.setSavedFilename(rs.getString("saved_filename"));
        request.setFileSize(rs.getLong("file_size"));
        request.setStatus(RequestStatus.valueOf(rs.getString("status")));
        request.setPdfFilename(rs.getString("pdf_filename"));
        request.setErrorMessage(rs.getString("error_message"));
        request.setCreatedAt(rs.getTimestamp("created_at"));
        request.setStartedAt(rs.getTimestamp("started_at"));
        request.setCompletedAt(rs.getTimestamp("completed_at"));
        return request;
    }
}
