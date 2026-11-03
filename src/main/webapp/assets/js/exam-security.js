document.addEventListener("DOMContentLoaded", function () {

    const startExamBtn = document.getElementById("startExamBtn");

    let examStarted = false;
    let violationCount = 0;
    let lastViolationTime = 0;

    // ==========================================
    // 1. BẮT ĐẦU THI VÀ BẬT FULLSCREEN
    // ==========================================

    startExamBtn.addEventListener("click", async function () {

        try {
            await document.documentElement.requestFullscreen();

            examStarted = true;

            startExamBtn.disabled = true;
            startExamBtn.textContent = "ĐANG TRONG PHÒNG THI";
            startExamBtn.classList.add("exam-active");

            // Khóa một số phím nếu trình duyệt hỗ trợ
            if (navigator.keyboard && navigator.keyboard.lock) {
                try {
                    await navigator.keyboard.lock([
                        "Tab",
                        "Escape"
                    ]);
                } catch (error) {
                    console.log("Không thể khóa bàn phím:", error);
                }
            }

        } catch (error) {
            alert("Không thể bật chế độ toàn màn hình. Vui lòng thử lại!");
        }
    });


    // ==========================================
    // 2. GHI NHẬN VI PHẠM
    // ==========================================

    function recordViolation(message) {

        const now = Date.now();

        // Tránh ghi nhận 2 lần cho cùng một hành động
        if (now - lastViolationTime < 1500) {
            return;
        }

        lastViolationTime = now;
        violationCount++;

        alert(
            "CẢNH BÁO: " + message +
            "\nSố lần vi phạm: " + violationCount
        );

        console.warn("Vi phạm thi:", message);
    }


    // ==========================================
    // 3. KIỂM SOÁT BÀN PHÍM
    // ==========================================

    document.addEventListener("keydown", function (e) {

        if (!examStarted) return;

        const isAnswerInput =
            e.target.classList.contains("short-answer-input");

        // Chặn các phím tắt thông dụng
        const blockedShortcut =
            e.key === "F12" ||
            (e.ctrlKey && ["u", "s", "p", "c", "v", "x", "a"].includes(e.key.toLowerCase())) ||
            (e.ctrlKey && e.shiftKey && ["i", "j", "c"].includes(e.key.toLowerCase())) ||
            (e.altKey && ["ArrowLeft", "ArrowRight"].includes(e.key));

        if (blockedShortcut) {
            e.preventDefault();
            recordViolation("Phát hiện sử dụng phím tắt không được phép!");
            return;
        }

        // Nếu không phải ô trả lời ngắn thì khóa phím
        if (!isAnswerInput) {
            e.preventDefault();
            return;
        }

        // Trong ô trả lời: cho phép phím xóa, điều hướng và thao tác cơ bản
        const allowedKeys = [
            "Backspace",
            "Delete",
            "ArrowLeft",
            "ArrowRight",
            "Home",
            "End",
            "Tab"
        ];

        if (allowedKeys.includes(e.key)) {
            return;
        }

        // Chặn Space
        if (e.key === " ") {
            e.preventDefault();
            return;
        }

        // Cho phép chữ cái, chữ số và dấu chấm
        if (!/^[a-zA-Z0-9.]$/.test(e.key)) {
            e.preventDefault();
        }

        // Không cho nhập nhiều hơn một dấu chấm
        if (e.key === "." && e.target.value.includes(".")) {
            e.preventDefault();
        }
    });


    // ==========================================
    // 4. KIỂM SOÁT DỮ LIỆU NHẬP
    // ==========================================

    document.querySelectorAll(".short-answer-input").forEach(input => {

        // Lọc dữ liệu, kể cả khi dán hoặc nhập bằng cách khác
        input.addEventListener("input", function () {

            let value = this.value;

            // Chỉ giữ chữ cái, chữ số và dấu chấm
            value = value.replace(/[^a-zA-Z0-9.]/g, "");

            // Chỉ cho phép một dấu chấm
            let parts = value.split(".");

            if (parts.length > 2) {
                value = parts[0] + "." + parts.slice(1).join("");
            }

            // Giới hạn tối đa 4 ký tự
            value = value.slice(0, 4);

            this.value = value;
        });

        // Chặn dán
        input.addEventListener("paste", function (e) {
            e.preventDefault();
        });
    });


    // ==========================================
    // 5. CHẶN CHUỘT PHẢI, COPY, CUT, PASTE
    // ==========================================

    document.addEventListener("contextmenu", function (e) {
        if (examStarted) {
            e.preventDefault();
        }
    });

    ["copy", "cut", "paste", "dragstart"].forEach(eventName => {

        document.addEventListener(eventName, function (e) {
            if (examStarted) {
                e.preventDefault();
            }
        });

    });


    // ==========================================
    // 6. CHẶN BÔI ĐEN NỘI DUNG
    // ==========================================

    document.addEventListener("selectstart", function (e) {

        if (
            examStarted &&
            !e.target.classList.contains("short-answer-input")
        ) {
            e.preventDefault();
        }

    });


    // ==========================================
    // 7. PHÁT HIỆN CHUYỂN TAB
    // ==========================================

    document.addEventListener("visibilitychange", function () {

        if (examStarted && document.hidden) {
            recordViolation("Bạn đã chuyển sang tab khác!");
        }

    });


    // ==========================================
    // 8. PHÁT HIỆN RỜI KHỎI CỬA SỔ
    // ==========================================

    window.addEventListener("blur", function () {

        if (examStarted && !document.hidden) {
            recordViolation("Bạn đã rời khỏi cửa sổ làm bài!");
        }

    });


    // ==========================================
    // 9. PHÁT HIỆN THOÁT FULLSCREEN
    // ==========================================

    document.addEventListener("fullscreenchange", function () {

        if (examStarted && !document.fullscreenElement) {
            recordViolation("Bạn đã thoát chế độ toàn màn hình!");
        }

    });

});
