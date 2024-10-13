
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
    private static final List<String> EXCLUDE_PATHS = Arrays.asList("/Login.html", "/index-servlet","/","/login");

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
