
package org.example;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        System.out.println(username);
        System.out.println(password);
        // 检查用户名和密码是否正确
        if ("admin".equals(username) && "123456".equals(password)) {
            HttpSession session = req.getSession(); // 获取或创建session
            session.setAttribute("user", username); // 将用户名存储在session中
            System.out.println("密码正确");
            // 重定向到欢迎页面
            resp.sendRedirect(req.getContextPath() + "/welcome.jsp");
        } else {
            // 用户名或密码错误，重定向回登录页面并附带错误信息
            // 注意：这里使用查询参数"error"来指示登录失败
            System.out.println("密码错误");
            resp.sendRedirect(req.getContextPath() + "/Login.html");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 如果是GET请求，直接重定向到登录页面
        resp.sendRedirect(req.getContextPath() + "/index-servlet");
    }
}
