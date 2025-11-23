package edu.dut.controller;

import edu.dut.distributed.master.WorkerManager;
import edu.dut.distributed.model.WorkerNode;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/admin/workers")
public class WorkerMonitorServlet extends HttpServlet {
    
    private WorkerManager workerManager;
    
    @Override
    public void init() throws ServletException {
        super.init();
        workerManager = WorkerManager.getInstance();
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
        
        // Get worker statistics
        Map<String, Object> stats = workerManager.getStatistics();
        request.setAttribute("stats", stats);
        
        // Get worker list
        List<WorkerNode> workers = workerManager.getAllWorkers();
        request.setAttribute("workers", workers);
        
        // Forward to JSP
        request.getRequestDispatcher("/workers.jsp").forward(request, response);
    }
}
