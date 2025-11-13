<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="edu.dut.distributed.model.WorkerNode" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Worker Monitor - PDF Converter</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.8.0/font/bootstrap-icons.css">
    <style>
        .worker-card {
            transition: all 0.3s ease;
            border-left: 4px solid #6c757d;
        }
        .worker-card.idle {
            border-left-color: #198754;
        }
        .worker-card.busy {
            border-left-color: #ffc107;
        }
        .worker-card.offline {
            border-left-color: #dc3545;
        }
        .stat-card {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border-radius: 10px;
            padding: 20px;
            margin-bottom: 20px;
        }
        .stat-card h3 {
            font-size: 2.5rem;
            font-weight: bold;
            margin-bottom: 5px;
        }
        .refresh-btn {
            position: fixed;
            bottom: 30px;
            right: 30px;
            width: 60px;
            height: 60px;
            border-radius: 50%;
            box-shadow: 0 4px 12px rgba(0,0,0,0.3);
        }
    </style>
</head>
<body>
    <%
        @SuppressWarnings("unchecked")
        Map<String, Object> stats = (Map<String, Object>) request.getAttribute("stats");
        @SuppressWarnings("unchecked")
        List<WorkerNode> workers = (List<WorkerNode>) request.getAttribute("workers");
    %>
    
    <!-- Navigation -->
    <nav class="navbar navbar-expand-lg navbar-dark bg-primary">
        <div class="container-fluid">
            <a class="navbar-brand" href="<%= request.getContextPath() %>/home">
                <i class="bi bi-file-earmark-pdf"></i> PDF Converter
            </a>
            <div class="navbar-nav ms-auto">
                <a class="nav-link" href="<%= request.getContextPath() %>/home">
                    <i class="bi bi-house"></i> Home
                </a>
                <a class="nav-link active" href="<%= request.getContextPath() %>/admin/workers">
                    <i class="bi bi-server"></i> Workers
                </a>
            </div>
        </div>
    </nav>

    <div class="container mt-4">
        <h1 class="mb-4">
            <i class="bi bi-hdd-network"></i> Worker Monitor
            <span class="badge bg-secondary"><%= stats.get("totalWorkers") %></span>
        </h1>

        <!-- Statistics Cards -->
        <div class="row">
            <div class="col-md-3 col-sm-6">
                <div class="stat-card" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
                    <h3><%= stats.get("totalWorkers") %></h3>
                    <p class="mb-0"><i class="bi bi-hdd-rack"></i> Total Workers</p>
                </div>
            </div>
            <div class="col-md-3 col-sm-6">
                <div class="stat-card" style="background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);">
                    <h3><%= stats.get("idleWorkers") %></h3>
                    <p class="mb-0"><i class="bi bi-check-circle"></i> Idle Workers</p>
                </div>
            </div>
            <div class="col-md-3 col-sm-6">
                <div class="stat-card" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
                    <h3><%= stats.get("busyWorkers") %></h3>
                    <p class="mb-0"><i class="bi bi-lightning"></i> Busy Workers</p>
                </div>
            </div>
            <div class="col-md-3 col-sm-6">
                <div class="stat-card" style="background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);">
                    <h3><%= stats.get("completedJobs") %></h3>
                    <p class="mb-0"><i class="bi bi-check2-all"></i> Completed Jobs</p>
                </div>
            </div>
        </div>

        <!-- Worker List -->
        <div class="row mt-4">
            <% if (workers.isEmpty()) { %>
                <div class="col-12">
                    <div class="alert alert-warning">
                        <i class="bi bi-exclamation-triangle"></i>
                        <strong>No workers connected!</strong>
                        <p class="mb-0 mt-2">
                            Start workers by running: <code>java -jar pdf-worker.jar &lt;server-ip&gt; 7777</code>
                        </p>
                    </div>
                </div>
            <% } else { %>
                <% for (WorkerNode worker : workers) { 
                    String statusClass = worker.getStatus().toString().toLowerCase();
                    String statusBadge = "secondary";
                    String statusIcon = "bi-question-circle";
                    
                    switch (worker.getStatus()) {
                        case IDLE:
                            statusBadge = "success";
                            statusIcon = "bi-check-circle-fill";
                            break;
                        case BUSY:
                            statusBadge = "warning";
                            statusIcon = "bi-lightning-charge-fill";
                            break;
                        case OFFLINE:
                            statusBadge = "danger";
                            statusIcon = "bi-x-circle-fill";
                            break;
                    }
                    
                    long uptimeSeconds = worker.getUptimeSeconds();
                    long hours = uptimeSeconds / 3600;
                    long minutes = (uptimeSeconds % 3600) / 60;
                    long seconds = uptimeSeconds % 60;
                    String uptime = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                %>
                    <div class="col-md-6 col-lg-4 mb-3">
                        <div class="card worker-card <%= statusClass %>">
                            <div class="card-body">
                                <h5 class="card-title">
                                    <i class="bi bi-cpu"></i> <%= worker.getWorkerId() %>
                                    <span class="badge bg-<%= statusBadge %> float-end">
                                        <i class="<%= statusIcon %>"></i> <%= worker.getStatus() %>
                                    </span>
                                </h5>
                                
                                <hr>
                                
                                <p class="mb-2">
                                    <i class="bi bi-pc-display"></i>
                                    <strong>Hostname:</strong> <%= worker.getHostname() %>
                                </p>
                                <p class="mb-2">
                                    <i class="bi bi-router"></i>
                                    <strong>IP:</strong> <%= worker.getIpAddress() %>:<%= worker.getPort() %>
                                </p>
                                <p class="mb-2">
                                    <i class="bi bi-memory"></i>
                                    <strong>Resources:</strong> <%= worker.getCores() %> cores, <%= worker.getMemoryMB() %> MB
                                </p>
                                <p class="mb-2">
                                    <i class="bi bi-clock-history"></i>
                                    <strong>Uptime:</strong> <%= uptime %>
                                </p>
                                
                                <hr>
                                
                                <div class="row text-center">
                                    <div class="col-4">
                                        <h6 class="mb-0"><%= worker.getCurrentJobCount() %></h6>
                                        <small class="text-muted">Current</small>
                                    </div>
                                    <div class="col-4">
                                        <h6 class="mb-0 text-success"><%= worker.getTotalJobsCompleted() %></h6>
                                        <small class="text-muted">Completed</small>
                                    </div>
                                    <div class="col-4">
                                        <h6 class="mb-0 text-danger"><%= worker.getTotalJobsFailed() %></h6>
                                        <small class="text-muted">Failed</small>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                <% } %>
            <% } %>
        </div>
    </div>

    <!-- Refresh Button -->
    <button class="btn btn-primary refresh-btn" onclick="location.reload()">
        <i class="bi bi-arrow-clockwise fs-4"></i>
    </button>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        // Auto refresh every 5 seconds
        setTimeout(function() {
            location.reload();
        }, 5000);
    </script>
</body>
</html>
