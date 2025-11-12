package edu.dut.controller;

import edu.dut.model.bean.ConversionRequest;
import edu.dut.model.bean.User;
import edu.dut.model.bo.ConversionRequestBO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/results")
public class ResultServlet extends HttpServlet {
    
    private ConversionRequestBO requestBO;
    
    @Override
    public void init() throws ServletException {
        super.init();
        requestBO = new ConversionRequestBO();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Check login
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        User user = (User) session.getAttribute("user");
        
        try {
            // Get user's requests
            List<ConversionRequest> requests = requestBO.getUserRequests(user.getUserId());
            request.setAttribute("requests", requests);
            
            request.getRequestDispatcher("/WEB-INF/views/results.jsp").forward(request, response);
            
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Lỗi khi tải danh sách yêu cầu: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/results.jsp").forward(request, response);
        }
    }
}
