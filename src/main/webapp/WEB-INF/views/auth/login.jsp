<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%!
    // Mã hóa thông báo trước khi đưa vào HTML để tránh chèn mã HTML/JavaScript.
    private String escapeHtml(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#x27;");
    }
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="theme-color" content="#172554">
    <title>Đăng nhập | OnlineExam</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/auth.css">
</head>
<body>
    <main class="login-shell">
        <section class="login-card" aria-labelledby="login-title">
            <header class="login-header">
                <div class="brand-mark" aria-hidden="true">OE</div>
                <h1 id="login-title">OnlineExam Portal</h1>
                <p>Hệ thống khảo sát &amp; luyện thi trực tuyến THPT QG</p>
            </header>

            <%
                String errorMessage = (String) request.getAttribute("error");
                if (errorMessage != null && !errorMessage.isBlank()) {
            %>
                <div class="login-error" role="alert">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor"
                         stroke-width="2" stroke-linecap="round" stroke-linejoin="round"
                         aria-hidden="true">
                        <path d="M12 9v4m0 4h.01M10.3 3.9 1.8 18a2 2 0 0 0 1.7 3h17a2 2 0 0 0 1.7-3L13.7 3.9a2 2 0 0 0-3.4 0Z"/>
                    </svg>
                    <span><%= escapeHtml(errorMessage) %></span>
                </div>
            <% } %>

            <form id="login-form" method="POST"
                  action="${pageContext.request.contextPath}/auth/login">
                <div class="form-group">
                    <label class="form-label" for="username">Tên đăng nhập</label>
                    <div class="input-wrap">
                        <svg class="input-icon" viewBox="0 0 24 24" fill="none"
                             stroke="currentColor" stroke-width="1.8" aria-hidden="true">
                            <circle cx="12" cy="8" r="4"/>
                            <path d="M5 21v-2a7 7 0 0 1 14 0v2"/>
                        </svg>
                        <input class="form-input" id="username" name="username"
                               type="text" placeholder="Nhập username hoặc mã học sinh..."
                               autocomplete="username" autocapitalize="none"
                               spellcheck="false" required autofocus>
                    </div>
                </div>

                <div class="form-group">
                    <label class="form-label" for="password">Mật khẩu</label>
                    <div class="input-wrap">
                        <svg class="input-icon" viewBox="0 0 24 24" fill="none"
                             stroke="currentColor" stroke-width="1.8" aria-hidden="true">
                            <rect x="3" y="10" width="18" height="11" rx="2"/>
                            <path d="M7 10V7a5 5 0 0 1 10 0v3"/>
                        </svg>
                        <input class="form-input" id="password" name="password"
                               type="password" placeholder="Nhập mật khẩu..."
                               autocomplete="current-password" required>
                        <button class="password-toggle" id="toggle-password"
                                type="button" aria-label="Hiện mật khẩu"
                                aria-controls="password" aria-pressed="false">
                            <svg id="eye-icon" viewBox="0 0 24 24" fill="none"
                                 stroke="currentColor" stroke-width="1.8"
                                 stroke-linecap="round" stroke-linejoin="round"
                                 aria-hidden="true">
                                <path d="M2 12s3.6-7 10-7 10 7 10 7-3.6 7-10 7S2 12 2 12Z"/>
                                <circle cx="12" cy="12" r="3"/>
                            </svg>
                        </button>
                    </div>
                </div>

                <button class="submit-button" id="submit-button" type="submit">
                    <span id="submit-label">Đăng nhập hệ thống <span aria-hidden="true">→</span></span>
                </button>
            </form>

            <footer class="login-footer">
                © 2026 OnlineExam System &bull; Bảo mật thông tin thí sinh
            </footer>
        </section>
    </main>

    <script>
        const passwordInput = document.getElementById("password");
        const togglePassword = document.getElementById("toggle-password");
        const eyeIcon = document.getElementById("eye-icon");
        const loginForm = document.getElementById("login-form");
        const submitButton = document.getElementById("submit-button");

        togglePassword.addEventListener("click", function () {
            const willShow = passwordInput.type === "password";
            passwordInput.type = willShow ? "text" : "password";
            togglePassword.setAttribute("aria-label", willShow ? "Ẩn mật khẩu" : "Hiện mật khẩu");
            togglePassword.setAttribute("aria-pressed", String(willShow));

            eyeIcon.innerHTML = willShow
                ? '<path d="M3 3l18 18M10.6 10.6a2 2 0 0 0 2.8 2.8"/><path d="M9.9 5.2A10.8 10.8 0 0 1 12 5c6.4 0 10 7 10 7a15.8 15.8 0 0 1-3 3.8M6.2 6.2C3.5 8 2 12 2 12s3.6 7 10 7a10.5 10.5 0 0 0 4-.8"/>'
                : '<path d="M2 12s3.6-7 10-7 10 7 10 7-3.6 7-10 7S2 12 2 12Z"/><circle cx="12" cy="12" r="3"/>';
        });

        loginForm.addEventListener("submit", function (event) {
            if (!loginForm.checkValidity()) {
                event.preventDefault();
                loginForm.reportValidity();
                return;
            }
            submitButton.disabled = true;
            submitButton.innerHTML = '<span class="spinner" aria-hidden="true"></span><span>Đang xác thực...</span>';
        });
    </script>
</body>
</html>
