// package com.onlineexam.controller;

// import com.onlineexam.dao.AttemptDAO;
// import com.onlineexam.dao.ExamSessionDAO;
// import com.onlineexam.model.ExamSession;
// import com.onlineexam.model.User;
// import com.onlineexam.service.GradingService;
// import jakarta.servlet.ServletException;
// import jakarta.servlet.annotation.WebServlet;
// import jakarta.servlet.http.HttpServlet;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;
// import jakarta.servlet.http.HttpSession;

// import java.io.IOException;
// import java.sql.SQLException;
// import java.util.List;

// @WebServlet({ "/student/exams", "/student/check-pass", "/student/take-exam", "/student/submit-exam" })
// public class AttemptServlet extends HttpServlet {

//     private final AttemptDAO attemptDAO = new AttemptDAO();
//     private final ExamSessionDAO sessionDAO = new ExamSessionDAO();
//     private final GradingService gradingService = new GradingService();
    

//     @Override
//     protected void doGet(HttpServletRequest request, HttpServletResponse response)
//             throws ServletException, IOException {

//         // 1. Kiểm tra đăng nhập
//         HttpSession session = request.getSession(false);
//         User user = (session != null) ? (User) session.getAttribute("user") : null;

//         if (user == null) {
//             response.sendRedirect(request.getContextPath() + "/auth/login");
//             return;
//         }

//         // 2. Kiểm tra quyền STUDENT
//         String roleName = (user.getRole() != null) ? user.getRole().getRoleName() : "";
//         if (!"STUDENT".equalsIgnoreCase(roleName)) {
//             response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập chức năng này");
//             return;
//         }

//         String servletPath = request.getServletPath();
//         if ("/student/take-exam".equals(servletPath)) {
//             handleTakeExamPage(request, response, user);
//         } else {
//             handleAvailableExamsPage(request, response);
//         }
//     }

//     @Override
//     protected void doPost(
//         HttpServletRequest request,
//         HttpServletResponse response
//     ) throws ServletException, IOException {

//         HttpSession session = request.getSession(false);

//         if (session == null ||
//             !(session.getAttribute("user") instanceof User)) {

//             response.sendRedirect(
//                 request.getContextPath() + "/login"
//             );
//             return;
//         }

//         User user = (User) session.getAttribute("user");

//         if (!"STUDENT".equalsIgnoreCase(user.getRole().getRoleName())) {
//             response.sendError(HttpServletResponse.SC_FORBIDDEN);
//             return;
//         }

//         String action = request.getParameter("action");

//         if ("check-pass".equals(action)) {
//             handleVerifyPassAndStart(request, response);
//         } else if ("submit".equals(action)) {
//             handleSubmitExam(request, response, user);
//         } else {
//             response.sendError(HttpServletResponse.SC_BAD_REQUEST);
//         }
//     }

//     // 1. Hiển thị danh sách ca thi khả dụng
//     private void handleAvailableExamsPage(HttpServletRequest request, HttpServletResponse response)
//             throws ServletException, IOException {
//         try {
//             List<AttemptDAO.SessionViewDTO> sessions = attemptDAO.findAvailableSessions();
//             request.setAttribute("sessions", sessions);
//             request.getRequestDispatcher("/WEB-INF/views/student/available-exams.jsp").forward(request, response);
//         } catch (SQLException e) {
//             e.printStackTrace();
//             response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi lấy danh sách ca thi: " + e.getMessage());
//         }
//     }

//     // 2. Xác thực mật khẩu phòng thi và tạo/lấy lượt làm bài (Attempt)
//     private void handleVerifyPassAndStart(HttpServletRequest request, HttpServletResponse response)
//             throws IOException {
//         HttpSession sessionHttp = request.getSession(false);
//         User user = (sessionHttp != null) ? (User) sessionHttp.getAttribute("user") : null;

//         if (user == null) {
//             response.sendRedirect(request.getContextPath() + "/auth/login");
//             return;
//         }

//         int sessionId = Integer.parseInt(request.getParameter("sessionId"));
//         String inputPassword = request.getParameter("sessionPassword");

//         try {
//             ExamSession session = sessionDAO.findById(sessionId);
//             if (session == null || !"OPEN".equalsIgnoreCase(session.getStatus())) {
//                 response.sendRedirect(request.getContextPath() + "/student/exams?error=Ca+thi+chua+duoc+giao+vien+mo+phong!");
//                 return;
//             }

//             // Kiểm tra mật khẩu nếu ca thi yêu cầu
//             if (session.getSessionPassword() != null && !session.getSessionPassword().isBlank()) {
//                 if (inputPassword == null || !session.getSessionPassword().trim().equals(inputPassword.trim())) {
//                     response.sendRedirect(request.getContextPath()
//                             + "/student/exams?error=Mat+khau+phong+thi+khong+dung!&invalidSessionId=" + sessionId);
//                     return;
//                 }
//             }

//             // Tạo hoặc lấy bài làm IN_PROGRESS hiện tại
//             int attemptId = attemptDAO.getOrCreateActiveAttempt(sessionId, user.getUserId());
//             response.sendRedirect(request.getContextPath() + "/student/take-exam?attemptId=" + attemptId);

//         } catch (SQLException e) {
//             e.printStackTrace();
//             response.sendRedirect(request.getContextPath() + "/student/exams?error=" + e.getMessage());
//         }
//     }

//     // 3. Tải câu hỏi hiển thị lên màn hình làm bài
//     private void handleTakeExamPage(HttpServletRequest request, HttpServletResponse response, User user)
//             throws ServletException, IOException {

//         String attemptIdParam = request.getParameter("attemptId");
//         if (attemptIdParam == null || attemptIdParam.trim().isEmpty()) {
//             response.sendRedirect(request.getContextPath() + "/student/exams");
//             return;
//         }

//         int attemptId;
//         try {
//             attemptId = Integer.parseInt(attemptIdParam.trim());
//         } catch (NumberFormatException e) {
//             response.sendRedirect(request.getContextPath() + "/student/exams");
//             return;
//         }

//         try {
//             AttemptDAO.TakeExamDetailDTO detail = attemptDAO.getTakeExamDetail(attemptId, user.getUserId());

//             if (detail == null) {
//                 response.sendError(
//                     HttpServletResponse.SC_FORBIDDEN,
//                     "Bạn không có quyền truy cập lượt thi này."
//                 );
//                 return;
//             }

//             request.setAttribute("attemptId", detail.getAttemptId());
//             request.setAttribute("examTitle", detail.getExamTitle());
//             request.setAttribute("sessionName", detail.getSessionName());
//             request.setAttribute("durationMinutes", detail.getDurationMinutes());
//             request.setAttribute("questions", detail.getQuestions());

//             request.getRequestDispatcher("/WEB-INF/views/student/take-exam.jsp").forward(request, response);

//         } catch (SQLException e) {
//             e.printStackTrace();
//             response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi tải bài thi: " + e.getMessage());
//         }
//     }

//     // 4. Học sinh nộp bài: Lưu đáp án và tự động chấm điểm
//     private void handleSubmitExam(HttpServletRequest request, HttpServletResponse response, User user)
//             throws ServletException, IOException {
//         int attemptId = Integer.parseInt(request.getParameter("attemptId"));

//         try {
//             // Đẩy toàn bộ parameter sang DAO để lưu câu trả lời bằng Transaction
//             attemptDAO.submitAttemptAnswers(attemptId, user.getUserId(), request.getParameterMap());

//             // Tự động chấm điểm
//             gradingService.gradeAttempt(attemptId);

//             response.sendRedirect(request.getContextPath() + "/student/results?attemptId=" + attemptId);

//         } catch (Exception e) {
//             e.printStackTrace();
//             response.sendRedirect(request.getContextPath() + "/student/exams?error=" + e.getMessage());
//         }
//     }
// }


package com.onlineexam.controller;

import java.time.Duration;
import java.time.LocalDateTime;
import java.sql.Timestamp;
import com.onlineexam.dao.AttemptDAO;
import com.onlineexam.dao.ExamSessionDAO;
import com.onlineexam.model.ExamSession;
import com.onlineexam.model.User;
import com.onlineexam.service.GradingService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet({
    "/student/exams",
    "/student/check-pass",
    "/student/take-exam",
    "/student/start-exam",
    "/student/submit-exam"
})
public class AttemptServlet extends HttpServlet {

    private final AttemptDAO attemptDAO = new AttemptDAO();
    private final ExamSessionDAO sessionDAO = new ExamSessionDAO();
    private final GradingService gradingService = new GradingService();

    // =========================================================
    // GET
    // =========================================================

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        User user = getLoggedInUser(request);

        if (user == null) {
            response.sendRedirect(
                request.getContextPath() + "/auth/login"
            );
            return;
        }

        if (!isStudent(user)) {
            response.sendError(
                HttpServletResponse.SC_FORBIDDEN,
                "Bạn không có quyền truy cập chức năng này."
            );
            return;
        }

        String path = request.getServletPath();

        if ("/student/take-exam".equals(path)) {
            handleTakeExamPage(request, response, user);
        } else {
            handleAvailableExamsPage(request, response);
        }
    }

    // =========================================================
    // POST
    // =========================================================

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        User user = getLoggedInUser(request);

        if (user == null) {
            response.sendRedirect(
                request.getContextPath() + "/auth/login"
            );
            return;
        }

        if (!isStudent(user)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String path = request.getServletPath();

        if ("/student/check-pass".equals(path)) {
            handleVerifyPassAndStart(request, response, user);

        } else if ("/student/submit-exam".equals(path)) {
            handleSubmitExam(request, response, user);

        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    // =========================================================
    // COMMON
    // =========================================================

    private User getLoggedInUser(HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session == null) {
            return null;
        }

        Object obj = session.getAttribute("user");

        return (obj instanceof User) ? (User) obj : null;
    }

    private boolean isStudent(User user) {

        return user.getRole() != null
            && "STUDENT".equalsIgnoreCase(
                user.getRole().getRoleName()
            );
    }

    private Integer parsePositiveInt(String value) {

        try {
            int number = Integer.parseInt(value);

            return number > 0 ? number : null;

        } catch (NumberFormatException e) {
            return null;
        }
    }

    // =========================================================
    // 1. AVAILABLE EXAMS
    // =========================================================

    private void handleAvailableExamsPage(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        try {

            List<AttemptDAO.SessionViewDTO> sessions =
                attemptDAO.findAvailableSessions();

            request.setAttribute("sessions", sessions);

            request.getRequestDispatcher(
                "/WEB-INF/views/student/available-exams.jsp"
            ).forward(request, response);

        } catch (SQLException e) {

            e.printStackTrace();

            response.sendError(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Lỗi lấy danh sách ca thi."
            );
        }
    }

    // =========================================================
    // 3. TAKE EXAM PAGE
    // Nếu chưa bắt đầu: hiển thị màn hình chờ
    // Nếu đã bắt đầu: khôi phục bài thi
    // =========================================================

    private void handleTakeExamPage(
        HttpServletRequest request,
        HttpServletResponse response,
        User user
    ) throws ServletException, IOException {

        Integer attemptId = parsePositiveInt(
            request.getParameter("attemptId")
        );

        if (attemptId == null) {
            response.sendRedirect(
                request.getContextPath() + "/student/exams"
            );
            return;
        }

        try {
            // Kiểm tra Attempt thuộc học sinh hiện tại
            AttemptDAO.ExamWaitingDTO examInfo =
                attemptDAO.getExamWaitingInfo(
                    attemptId,
                    user.getUserId()
                );

            if (examInfo == null) {
                response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Bạn không có quyền truy cập lượt thi này."
                );
                return;
            }

            // Chưa bắt đầu thì không cho truy cập trực tiếp đề thi
            if (examInfo.getStartedAt() == null) {
                response.sendRedirect(
                    request.getContextPath() + "/student/exams"
                );
                return;
            }

            long remainingSeconds = examInfo.getRemainingSeconds();

            if (remainingSeconds <= 0) {
                response.sendError(
                    HttpServletResponse.SC_GONE,
                    "Bài thi đã hết thời gian."
                );
                return;
            }

            // Lấy câu hỏi, tiếp tục xác minh quyền sở hữu
            AttemptDAO.TakeExamDetailDTO detail =
                attemptDAO.getTakeExamDetail(
                    attemptId,
                    user.getUserId()
                );

            if (detail == null) {
                response.sendError(
                    HttpServletResponse.SC_FORBIDDEN
                );
                return;
            }

            request.setAttribute("attemptId", detail.getAttemptId());
            request.setAttribute("examTitle", detail.getExamTitle());
            request.setAttribute("sessionName", detail.getSessionName());
            request.setAttribute("durationMinutes", detail.getDurationMinutes());
            request.setAttribute("questions", detail.getQuestions());
            request.setAttribute("remainingSeconds", remainingSeconds);

            request.getRequestDispatcher(
                "/WEB-INF/views/student/take-exam.jsp"
            ).forward(request, response);

        } catch (SQLException e) {
            e.printStackTrace();

            response.sendError(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Lỗi tải bài thi."
            );
        }
    }

    // Xác thực mật khẩu và bắt đầu thi ngay lập tức
    private void handleVerifyPassAndStart(
            HttpServletRequest request,
            HttpServletResponse response,
            User user
    ) throws ServletException, IOException {

        Integer sessionId = parsePositiveInt(
            request.getParameter("sessionId")
        );

        String inputPassword = request.getParameter("sessionPassword");

        if (sessionId == null) {
            response.sendRedirect(
                request.getContextPath() + "/student/exams"
            );
            return;
        }

        try {
            // 1. Lấy thông tin ca thi
            ExamSession examSession = sessionDAO.findById(sessionId);

            if (examSession == null) {
                response.sendRedirect(
                    request.getContextPath() + "/student/exams?error=Ca+thi+khong+ton+tai"
                );
                return;
            }

            // 2. Tạo hoặc lấy Attempt của học sinh
            int attemptId = attemptDAO.getOrCreateActiveAttempt(
                sessionId,
                user.getUserId()
            );

            // 3. Kiểm tra Attempt có thuộc học sinh hiện tại không
            AttemptDAO.ExamWaitingDTO examInfo =
                attemptDAO.getExamWaitingInfo(
                    attemptId,
                    user.getUserId()
                );

            if (examInfo == null) {
                response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Bạn không có quyền truy cập lượt thi này."
                );
                return;
            }

            /*
            * 4. Chỉ xác thực mật khẩu và ghi started_at
            * nếu đây là lượt thi chưa bắt đầu.
            *
            * Nếu đã bắt đầu trước đó:
            * - Không yêu cầu nhập lại mật khẩu.
            * - Không cập nhật lại started_at.
            */
            if (examInfo.getStartedAt() == null) {

                if (!"OPEN".equalsIgnoreCase(examSession.getStatus())) {
                    response.sendRedirect(
                        request.getContextPath()
                        + "/student/exams?error=Ca+thi+chua+duoc+mo"
                    );
                    return;
                }

                String correctPassword = examSession.getSessionPassword();

                if (correctPassword != null && !correctPassword.isBlank()) {

                    if (inputPassword == null
                            || !correctPassword.trim().equals(inputPassword.trim())) {

                        response.sendRedirect(
                            request.getContextPath()
                            + "/student/exams?error=Mat+khau+phong+thi+khong+dung"
                        );
                        return;
                    }
                }

                // Ghi nhận thời điểm bắt đầu đúng một lần
                boolean started = attemptDAO.startExam(
                    attemptId,
                    user.getUserId()
                );

                if (!started) {
                    response.sendError(
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Không thể bắt đầu bài thi."
                    );
                    return;
                }
            }

            // 5. Đọc lại thời gian còn lại sau khi bắt đầu
            AttemptDAO.ExamWaitingDTO updatedInfo =
                attemptDAO.getExamWaitingInfo(
                    attemptId,
                    user.getUserId()
                );

            if (updatedInfo == null || updatedInfo.getStartedAt() == null) {
                response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Không thể xác định thời điểm bắt đầu thi."
                );
                return;
            }

            long remainingSeconds = updatedInfo.getRemainingSeconds();

            if (remainingSeconds <= 0) {
                response.sendError(
                    HttpServletResponse.SC_GONE,
                    "Bài thi đã hết thời gian."
                );
                return;
            }

            // 6. Tải câu hỏi
            AttemptDAO.TakeExamDetailDTO detail =
                attemptDAO.getTakeExamDetail(
                    attemptId,
                    user.getUserId()
                );

            if (detail == null) {
                response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Không thể truy cập bài thi này."
                );
                return;
            }

            // 7. Đưa dữ liệu sang JSP
            request.setAttribute("attemptId", detail.getAttemptId());
            request.setAttribute("examTitle", detail.getExamTitle());
            request.setAttribute("sessionName", detail.getSessionName());
            request.setAttribute("durationMinutes", detail.getDurationMinutes());
            request.setAttribute("questions", detail.getQuestions());
            request.setAttribute("remainingSeconds", remainingSeconds);

            // 8. Hiển thị đề thi ngay, không qua trang chờ
            request.getRequestDispatcher(
                "/WEB-INF/views/student/take-exam.jsp"
            ).forward(request, response);

        } catch (SQLException e) {
            e.printStackTrace();

            response.sendError(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Lỗi khi xác thực và bắt đầu bài thi."
            );
        }
    }

    // =========================================================
    // 5. CALCULATE REMAINING TIME
    // =========================================================

    private long calculateRemainingSeconds(
            Timestamp startedAt,
            int durationMinutes
    ) {

        LocalDateTime startTime = startedAt.toLocalDateTime();

        LocalDateTime endTime = startTime.plusMinutes(durationMinutes);

        long remaining = Duration.between(
            LocalDateTime.now(),
            endTime
        ).getSeconds();

        return Math.max(remaining, 0);
    }

    // =========================================================
    // 6. SUBMIT EXAM
    // =========================================================

    private void handleSubmitExam(
            HttpServletRequest request,
            HttpServletResponse response,
            User user
    ) throws ServletException, IOException {

        Integer attemptId = parsePositiveInt(
            request.getParameter("attemptId")
        );

        if (attemptId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {

            // DAO phải xác thực quyền sở hữu và thời hạn làm bài
            attemptDAO.submitAttemptAnswers(
                attemptId,
                user.getUserId(),
                request.getParameterMap()
            );

            gradingService.gradeAttempt(attemptId);

            response.sendRedirect(
                request.getContextPath()
                + "/student/results?attemptId=" + attemptId
            );

        } catch (Exception e) {

            e.printStackTrace();

            response.sendRedirect(
                request.getContextPath()
                + "/student/exams?error=Khong+the+nop+bai"
            );
        }
    }
}
