package org.example.demo3;

import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.ServletRequestListener;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpServletRequest;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

@WebListener
public class RequestLoggingListener implements ServletRequestListener {

    // 获取Logger实例，使用当前类的全限定名作为logger的名字
    private static final Logger logger = Logger.getLogger(RequestLoggingListener.class.getName());

    // 线程安全的日志存储
    private static final ConcurrentHashMap<String, String> logMap = new ConcurrentHashMap<>();

    @Override
    public void requestInitialized(ServletRequestEvent sre) {
        HttpServletRequest request = (HttpServletRequest) sre.getServletRequest();
        long startTime = System.currentTimeMillis();

        // 将开始时间存储在请求的属性中，以便在请求结束时使用
        request.setAttribute("startTime", startTime);
        request.setAttribute("logId", logMap.size()); // 使用日志ID作为key

        // 记录请求开始的时间
        String logEntry = String.format("Request started at: %s", startTime);
        logMap.put(String.valueOf(logMap.size()), logEntry);
        logger.info(logEntry);
    }

    @Override
    public void requestDestroyed(ServletRequestEvent sre) {
        HttpServletRequest request = (HttpServletRequest) sre.getServletRequest();
        Long startTime = (Long) request.getAttribute("startTime");
        long endTime = System.currentTimeMillis();

        if (startTime == null) {
            // 如果开始时间未被设置，记录警告信息
            String logEntry = "Warning: Start time not set for request";
            logMap.put(String.valueOf(logMap.size()), logEntry);
            logger.warning(logEntry);
            return;
        }

        // 记录请求的详细信息和处理时间
        String logEntry = String.format(
                "Request from IP: %s, Method: %s, URI: %s, Query: %s, User-Agent: %s, Time taken: %d ms",
                request.getRemoteAddr(),
                request.getMethod(),
                request.getRequestURI(),
                request.getQueryString(),
                request.getHeader("User-Agent"),
                endTime - startTime
        );
        Integer logId = (Integer) request.getAttribute("logId");
        logMap.put(logId.toString(), logEntry);
        logger.info(logEntry);
    }

    // 获取所有日志条目的方法
    public static ConcurrentHashMap<String, String> getAllLogEntries() {
        return new ConcurrentHashMap<>(logMap);
    }
}