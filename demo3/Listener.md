# Java Web 应用程序之Listener简单练习

本文档详细介绍了通过Java Web应用程序组件来实现通过Listener记录每个 HTTP 请求的详细信息。

通过初始界面的`<a href="test">logViewer</a>`这个超链接来到`TestServlet`，模拟处理时间：使用 `Thread.sleep(1000)` 使当前线程暂停1000毫秒（1秒），模拟处理时间，通过 `response.sendRedirect` 方法，重定向到`LogViewerServlet`来输出日志信息

## RequestLoggingListener.java

`RequestLoggingListener` 是一个实现了 `ServletRequestListener` 接口的Java类，用于监听和记录Servlet请求的初始化和销毁事件。通过这个监听器，我们可以捕获请求的开始和结束时间，并记录相关的请求信息。

### 功能

- `private static final Logger logger`: 使用当前类的全限定名作为logger的名字，用于记录日志信息。
- `private static final ConcurrentHashMap<String, String> logMap`: 一个线程安全的日志存储，用于存储请求的日志条目。

1. 获取请求对象 `HttpServletRequest`。
2. 记录请求开始的时间，并将其存储在请求属性中。
3. 使用日志ID作为key，将开始时间记录到日志存储中。
4. 使用Logger记录请求开始的日志信息。

-`requestDestroyed`方法，这个方法提供了一个获取所有日志条目的快照的方式 

### 实现

```java
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
```

## LogViewerServlet.java

`LogViewerServlet` 是一个继承自 `HttpServlet` 的Java类，用于处理对日志查看器的HTTP GET请求。它从 `RequestLoggingListener` 中获取所有请求日志条目，并将它们以HTML表格的形式展示给用户。

### 功能

- 设置响应的内容类型为HTML，并指定字符集为UTF-8。
- 获取 `PrintWriter` 对象用于向客户端发送HTML响应。
- 输出HTML头部和内联CSS样式。
- 输出HTML表格的标题和表头。
- 从 `RequestLoggingListener` 获取所有日志条目。
- 遍历日志条目，解析并输出到HTML表格中。
- 输出HTML表格的尾部和页面结束标签。

### 实现

```java
package org.example.demo3;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;

@WebServlet("/logViewer")
public class LogViewerServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        // 输出HTML头部和样式
        out.println("<html><head><title>Request Log Viewer</title><style>"
                + "table { width: 100%; border-collapse: collapse; }"
                + "th, td { border: 1px solid #ddd; padding: 8px; }"
                + "th { background-color: #f2f2f2; }"
                + "tr:nth-child(even) { background-color: #f9f9f9; }"
                + "tr:hover { background-color: #f1f1f1; }"
                + "</style></head><body>");
        out.println("<h1>Request Log</h1>");
        out.println("<table>");
        out.println("<tr><th>Time</th><th>IP Address</th><th>Method</th><th>URI</th><th>Query</th><th>User-Agent</th><th>Time Taken (ms)</th></tr>");

        // 获取所有日志条目并输出
        Collection<String> logEntries = RequestLoggingListener.getAllLogEntries().values();
        for (String entry : logEntries) {
            // 解析日志条目并输出到表格中
            // 假设日志格式已经是 "Time/IP Address/Method/URI/Query/User-Agent/Time Taken"
            String[] parts = entry.split(", ");
            out.println("<tr>");
            out.printf("<td>%s</td>", parts[0]);
            out.printf("<td>%s</td>", parts[1]);
            out.printf("<td>%s</td>", parts[2]);
            out.printf("<td>%s</td>", parts[3]);
            out.printf("<td>%s</td>", parts[4]);
            out.printf("<td>%s</td>", parts[5]);
            out.printf("<td>%s</td>", parts.length > 6 ? parts[6] : "");
            out.println("</tr>");
        }

        // 输出HTML尾部
        out.println("</table></body></html>");
    }
}
```

## TestServlet.java 

### 功能

- 设置请求开始时间属性：通过 `request.setAttribute` 方法，将当前时间（毫秒值）存储在请求属性中，键为 `"startTime"`。
- 模拟处理时间：使用 `Thread.sleep(1000)` 使当前线程暂停1000毫秒（1秒），模拟处理时间。
- 返回响应消息：通过 `response.sendRedirect` 方法，重定向到`LogViewerServlet`来输出日志信息。

### 实现

```java
package org.example.demo3;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/test")
public class TestServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 设置请求开始时间属性
        request.setAttribute("startTime", System.currentTimeMillis());

        // 模拟一些处理时间
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        response.sendRedirect(request.getContextPath() + "/logViewer");
    }
}

```

## index.jsp

初始界面

```jsp
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
  <title>JSP - Hello World</title>
</head>
<body>
<h1><%= "Hello World!" %></h1>
<br/>
<a href="test">logViewer</a>
</body>
</html>
```
