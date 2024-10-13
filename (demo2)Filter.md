# Java Web 应用程序之Filter简单练习

本文档详细介绍了通过Java Web应用程序组件来实现通过Filter过滤器实现验证登录的简单实现。

项目一开始进入`IndexServlet.java`类，然后重定向到`Login.html`页面，这时用户可以输入用户名和密码来进行登录，如果密码错误将会再次重定向到`Login.html`页面，如果密码正确则重定向到`welcome.jsp`页面(由于此项目没有数据库等技术的引入只是Java web的简单练习，所以使用简单的密码验证，可以引入数据库来实现更复杂的登录验证)

## IndexServlet.java

### 功能

- 处理对`/index-servlet`路径的GET请求。
- 请求时将客户端重定向到`Login.html`页面。

### 实现

这个`IndexServlet`类最开始项目起始运行的类，写这个类主要是引导到`Login.html`页面以及验证`Filter`的拦截过滤功能

```java
package org.example;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/index-servlet")
public class IndexServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("demo8正在运行...");
        resp.sendRedirect("Login.html");
    }
}
```

## LoginFilter.java

### 功能

- 拦截所有路径`/*`。
- 创建一个名为 LoginFilter 的类, 实现 javax.servlet.Filter 接口。
- 使用 @WebFilter 注解配置过滤器,使其应用于所有 URL 路径 ("/*")。
- 在 doFilter 方法中实现以下逻辑: 

  a. 检查当前请求是否是对登录页面、注册页面或公共资源的请求。如果是,则允许请求通过。 

  b. 如果不是上述情况,检查用户的 session 中是否存在表示已登录的属性(如 "user" 属性)。

  c. 如果用户已登录,允许请求继续。 

  d. 如果用户未登录,将请求重定向到登录页面。
- 创建一个排除列表,包含不需要登录就能访问的路径("/Login.html", "/index.html", "/index-servlet", "/", "/login")。
- 实现一个方法`isExcluded`来检查当前请求路径是否在排除列表中。

### 实现

```java

package org.example;
import java.io.IOException;
import java.nio.file.FileStore;
import java.util.Arrays;
import java.util.List;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebFilter(filterName = "LoginFilter", urlPatterns = "/*") // 应用过滤器到所有URL路径
public class LoginFilter implements Filter {


    // 创建一个排除列表，包含不需要登录就能访问的路径
    private static final List<String> EXCLUDE_PATHS = Arrays.asList("/Login.html", "/index.html", "/index-servlet","/","/login");

/**
     * 检查当前请求路径是否在排除列表中
     * @param requestPath 当前请求的路径
     * @return 如果在排除列表中，返回true；否则返回false
     */

    private boolean isExcluded(String requestPath) {
        for (String path : EXCLUDE_PATHS) {
            if (requestPath.equals("/demo2"+path) || requestPath.startsWith("/demo2"+path + "/")) {

                return true;
            }
        }
        return false;
    }
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        HttpSession session = request.getSession(false);
        // 获取当前请求的路径
        String requestPath = request.getRequestURI();
        if (isExcluded(requestPath)) {
            filterChain.doFilter(request, response);
        }
        else if (session != null && session.getAttribute("user") != null) {
            filterChain.doFilter(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/Login.html");

        }
    }
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }
    @Override
    public void destroy() {

    }
}
```

## LoginServlet.java

### 功能

- 通过`Login.html`表单中获取到的用户名和密码。

- 在 doPost 方法中实现以下逻辑: 

1. 从请求中获取用户名和密码。
2. 打印用户名和密码到服务器控制台（通常用于调试）。
3. 检查用户名和密码是否与预设的凭据匹配。
4. 如果凭据正确，创建或获取一个HTTP会话（session），并将用户名存储在会话中，以便跟踪用户的登录状态。
5. 重定向用户到欢迎页面（`welcome.jsp`），显示登录成功的消息。
6. 如果凭据错误，重定向用户回到登录页面（`Login.html`），并附带错误信息。

### 实现

```java

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

```

## Login.html

### 功能

- 收集用户输入的用户名和密码，并将其通过POST方法发送到服务器上的`login`路径。
- `<input type="text" id="username" name="username" required>`: 用户名输入框，必填项。
- `<input type="password" id="password" name="password" required>`: 密码输入框，必填项。
- `<button type="submit" value="submit">Login</button>`: 提交按钮，用户点击后将表单数据发送到服务器。

### 实现

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login Page</title>
    <style>
        body {
            font-family: 'Arial', sans-serif;
            background-color: #f0f0f0;
            margin: 0;
            padding: 0;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
        }
        .login-container {
            background-color: white;
            padding: 20px;
            border-radius: 5px;
            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
            width: 300px;
            max-width: 100%;
        }
        .login-container h2 {
            text-align: center;
            color: #333;
        }
        .login-container form {
            display: flex;
            flex-direction: column;
        }
        .login-container label {
            margin-top: 10px;
        }
        .login-container input[type='text'],
        .login-container input[type='password'] {
            padding: 10px;
            margin-top: 5px;
            border: 1px solid #ddd;
            border-radius: 4px;
            font-size: 16px;
        }
        .login-container button {
            background-color: #5cb85c;
            color: white;
            padding: 10px 20px;
            margin-top: 20px;
            border: none;
            border-radius: 4px;
            font-size: 16px;
            cursor: pointer;
            transition: background-color 0.3s ease;
        }
        .login-container button:hover {
            background-color: #4cae4c;
        }
        .login-container p {
            margin-top: 15px;
            font-size: 14px;
            text-align: center;
        }
        .login-container a {
            color: #337ab7;
            text-decoration: none;
        }
    </style>
</head>
<body>
<div class="login-container">
    <h2>Login</h2>
    <form action="login" method="post">
        <label for="username">Username:</label>
        <input type="text" id="username" name="username" required>

        <label for="password">Password:</label>
        <input type="password" id="password" name="password" required>

        <button type="submit" value="submit">Login</button>
    </form>
</div>
</body>
</html>
```

## welcome.jsp

### 功能
- `<h1>Welcome!</h1>`: 显示欢迎标题。
- `<p>Hello, <%= session.getAttribute("user") %> . You have successfully logged in.</p>`: 显示用户的用户名，并告知用户已成功登录。这里使用了服务器端脚本（如JSP）来动态插入用户名。
- `<p>Enjoy your personalized experience on our website.</p>`: 欢迎信息，告知用户可以享受个性化的体验。
- `<p>If you need any assistance, feel free to contact our support team.</p>`: 提供帮助信息，告知用户如需要帮助可以联系支持团队

### 实现

```jsp
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Welcome Page</title>
    <style>
        body {
            font-family: 'Arial', sans-serif;
            background-color: #f7f7f7;
            margin: 0;
            padding: 0;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
        }
        .container {
            background-color: white;
            padding: 20px;
            border-radius: 5px;
            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
            text-align: center;
            max-width: 400px;
            margin: auto;
        }
        h1 {
            color: #333;
        }
        p {
            color: #666;
            font-size: 16px;
        }
    </style>
</head>
<body>
<div class="container">
    <h1>Welcome!</h1>
    <p>Hello, <%= session.getAttribute("user") %> . You have successfully logged in.</p>
    <p>Enjoy your personalized experience on our website.</p>
    <p>If you need any assistance, feel free to contact our support team.</p>
</div>
</body>
</html>
```
