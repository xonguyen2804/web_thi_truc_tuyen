package com.onlineexam.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import java.io.IOException;

/**
 * Authentication filter scaffold.
 * TODO: Check HttpSession for an authenticated user and redirect unauthenticated requests.
 */
@WebFilter("/*")
public class AuthenticationFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // TODO: Exclude public resources and the login endpoint before enforcing authentication.
        chain.doFilter(request, response);
    }
}
