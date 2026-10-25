package com.onlineexam.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import java.io.IOException;

/**
 * Authorization filter scaffold.
 * TODO: Enforce ADMIN / TEACHER / STUDENT access rules.
 */
@WebFilter("/admin/*")
public class AuthorizationFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // TODO: Check the user's role and resource-level permissions.
        chain.doFilter(request, response);
    }
}
