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