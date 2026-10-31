package com.onlineexam.controller;

import com.onlineexam.service.SystemMonitor;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Locale;

@WebServlet("/api/metrics")
public class MetricsServlet extends HttpServlet {

    private final SystemMonitor systemMonitor = new SystemMonitor();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        String cmd = request.getParameter("cmd");

        if (!"METRICS".equals(cmd)) {
            response.sendError(400, "Invalid command");
            return;
        }

        SystemMonitor.Metrics metrics = systemMonitor.getCurrentMetrics();

        response.setContentType("text/plain;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");

        response.getWriter().printf(
                java.util.Locale.US,
                "%.2f %.2f%n",
                metrics.cpuPercent(),
                metrics.ramPercent());
    }
}