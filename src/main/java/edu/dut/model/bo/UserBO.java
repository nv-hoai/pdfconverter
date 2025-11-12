package edu.dut.model.bo;

import edu.dut.model.bean.User;
import edu.dut.model.dao.UserDAO;

import java.sql.SQLException;

public class UserBO {
    private UserDAO userDAO;
    
    public UserBO() {
        this.userDAO = new UserDAO();
    }
    
    public User login(String username, String password) throws SQLException {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên đăng nhập không được để trống");
        }
        
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Mật khẩu không được để trống");
        }
        
        User user = userDAO.authenticate(username.trim(), password);
        
        if (user == null) {
            throw new IllegalArgumentException("Tên đăng nhập hoặc mật khẩu không đúng");
        }
        
        return user;
    }
    
    public User getUserById(int userId) throws SQLException {
        return userDAO.getUserById(userId);
    }
}
