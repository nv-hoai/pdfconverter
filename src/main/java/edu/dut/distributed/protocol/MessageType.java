package edu.dut.distributed.protocol;

/**
 * Message types for communication between Master Server and Workers
 */
public enum MessageType {
    // Worker -> Master
    WORKER_REGISTER,      // Worker đăng ký với Master
    WORKER_HEARTBEAT,     // Worker gửi heartbeat
    JOB_RESULT_SUCCESS,   // Worker gửi kết quả thành công
    JOB_RESULT_FAILED,    // Worker gửi kết quả thất bại
    
    // Master -> Worker
    WORKER_REGISTERED,    // Master xác nhận đăng ký
    JOB_ASSIGN,          // Master gửi job cho Worker
    WORKER_SHUTDOWN,     // Master yêu cầu Worker shutdown
    HEARTBEAT_ACK        // Master xác nhận heartbeat
}
