package edu.dut.model.dao;

import edu.dut.model.bean.User;
import edu.dut.util.DatabaseUtil;
import edu.dut.util.HashUtil;

import java.sql.*;

public class UserDAO {
    
    public User authenticate(String username, String password) throws SQLException {
        String hashedPassword = HashUtil.sha256(password);
        String sql = "SELECT user_id, username, full_name, email, role, created_at " +
                     "FROM users WHERE username = ? AND password = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setFullName(rs.getString("full_name"));
                user.setEmail(rs.getString("email"));
                user.setRole(rs.getString("role"));
                user.setCreatedAt(rs.getTimestamp("created_at"));
                return user;
            }
            
            return null;
            
        } finally {
            DatabaseUtil.close(rs, stmt, conn);
        }
    }
    
    public User getUserById(int userId) throws SQLException {
        String sql = "SELECT user_id, username, full_name, email, role, created_at " +
                     "FROM users WHERE user_id = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setFullName(rs.getString("full_name"));
                user.setEmail(rs.getString("email"));
                user.setRole(rs.getString("role"));
                user.setCreatedAt(rs.getTimestamp("created_at"));
                return user;
            }
            
            return null;
            
        } finally {
            DatabaseUtil.close(rs, stmt, conn);
        }
    }
}
