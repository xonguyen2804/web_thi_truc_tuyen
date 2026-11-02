<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng Nhập | OnlineExam - Hệ Thống Thi Trực Tuyến</title>
    
    <!-- Tailwind CSS CDN -->
    <script src="https://cdn.tailwindcss.com"></script>
    <script>
        tailwind.config = {
            theme: {
                extend: {
                    colors: {
                        brand: {
                            50: '#eff6ff',
                            100: '#dbeafe',
                            500: '#3b82f6',
                            600: '#2563eb',
                            700: '#1d4ed8',
                            900: '#1e3a8a'
                        }
                    }
                }
            }
        }
    </script>
    
    <!-- React & ReactDOM CDN -->
    <script crossorigin src="https://unpkg.com/react@18/umd/react.production.min.js"></script>
    <script crossorigin src="https://unpkg.com/react-dom@18/umd/react-dom.production.min.js"></script>
    <!-- Babel standalone để biên dịch JSX trên trình duyệt -->
    <script src="https://unpkg.com/@babel/standalone/babel.min.js"></script>
    
    <!-- Google Fonts Inter -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    <style>
        body { font-family: 'Inter', sans-serif; }
    </style>
</head>
<body class="bg-gradient-to-br from-slate-900 via-blue-950 to-indigo-950 min-h-screen flex items-center justify-center p-4">

    <!-- Container render React Component -->
    <!-- Truyền lỗi an toàn qua thuộc tính data của HTML -->
    <div id="react-root" 
        data-error="${requestScope.error != null ? requestScope.error : ''}" 
        class="w-full max-w-md"></div>

    <!-- React JSX Script -->
    <script type="text/babel">
        const { useState } = React;

        function LoginForm() {
            
            const [username, setUsername] = useState("");
            const [password, setPassword] = useState("");
            const [showPassword, setShowPassword] = useState(false);
            const [isLoading, setIsLoading] = useState(false);

            // Nhận thông báo lỗi từ request của Servlet nếu có
            const serverError = document.getElementById('react-root')?.dataset?.error || "";

            const handleSubmit = (e) => {
                if (!username.trim() || !password.trim()) {
                    e.preventDefault();
                    return;
                }
                setIsLoading(true);
                // Cho phép form HTML submit bình thường về AuthServlet
            };

            return (
                <div className="bg-white/10 backdrop-blur-xl border border-white/20 rounded-2xl shadow-2xl p-8 text-white transition-all duration-300 hover:shadow-blue-500/10">
                    
                    {/* Header Logo */}
                    <div className="text-center mb-8">
                        <div className="inline-flex items-center justify-center w-14 h-14 rounded-2xl bg-gradient-to-tr from-blue-600 to-indigo-500 shadow-lg shadow-blue-500/30 mb-4">
                            <span className="text-2xl font-black tracking-wider text-white">OE</span>
                        </div>
                        <h1 className="text-2xl font-bold tracking-tight text-white mb-1">OnlineExam Portal</h1>
                        <p className="text-sm text-slate-300">Hệ thống khảo sát & luyện thi trực tuyến THPT QG</p>
                    </div>

                    {/* Server Error Message */}
                    {serverError && (
                        <div className="mb-6 p-3.5 bg-rose-500/20 border border-rose-500/40 rounded-xl text-rose-200 text-sm flex items-center gap-3 animate-shake">
                            <svg className="w-5 h-5 flex-shrink-0 text-rose-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                            </svg>
                            <span>{serverError}</span>
                        </div>
                    )}

                    {/* Login Form */}
                    <form method="POST" action="${pageContext.request.contextPath}/auth/login" onSubmit={handleSubmit} className="space-y-5">
                        
                        {/* Username Input */}
                        <div>
                            <label className="block text-xs font-semibold uppercase tracking-wider text-slate-300 mb-2">
                                Tên đăng nhập
                            </label>
                            <div className="relative">
                                <input
                                    id="username"
                                    name="username"
                                    type="text"
                                    value={username}
                                    onChange={(e) => setUsername(e.target.value)}
                                    placeholder="Nhập username hoặc mã học sinh..."
                                    required
                                    className="w-full bg-slate-900/50 border border-slate-700/60 rounded-xl px-4 py-3 pl-11 text-white placeholder-slate-400 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
                                />
                                <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                                    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                                    </svg>
                                </div>
                            </div>
                        </div>

                        {/* Password Input */}
                        <div>
                            <div className="flex justify-between items-center mb-2">
                                <label className="text-xs font-semibold uppercase tracking-wider text-slate-300">
                                    Mật khẩu
                                </label>
                            </div>
                            <div className="relative">
                                <input
                                    id="password"
                                    name="password"
                                    type={showPassword ? "text" : "password"}
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                    placeholder="Nhập mật khẩu..."
                                    required
                                    className="w-full bg-slate-900/50 border border-slate-700/60 rounded-xl px-4 py-3 pl-11 pr-11 text-white placeholder-slate-400 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
                                />
                                <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                                    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                                    </svg>
                                </div>
                                <button
                                    type="button"
                                    onClick={() => setShowPassword(!showPassword)}
                                    className="absolute inset-y-0 right-0 pr-3.5 flex items-center text-slate-400 hover:text-white transition-colors"
                                >
                                    {showPassword ? (
                                        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l18 18" />
                                        </svg>
                                    ) : (
                                        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                                        </svg>
                                    )}
                                </button>
                            </div>
                        </div>

                        {/* Submit Button with Loading State */}
                        <button
                            type="submit"
                            disabled={isLoading}
                            className="w-full bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-500 hover:to-indigo-500 text-white font-semibold py-3 px-4 rounded-xl shadow-lg shadow-blue-500/25 transition-all duration-200 transform active:scale-[0.98] flex items-center justify-center gap-2"
                        >
                            {isLoading ? (
                                <>
                                    <svg className="animate-spin h-5 w-5 text-white" fill="none" viewBox="0 0 24 24">
                                        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                                        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                                    </svg>
                                    <span>Đang xác thực...</span>
                                </>
                            ) : (
                                <span>Đăng nhập hệ thống &rarr;</span>
                            )}
                        </button>
                    </form>

                    {/* Footer Guide */}
                    <div className="mt-8 pt-6 border-t border-white/10 text-center text-xs text-slate-400">
                        © 2026 OnlineExam System &bull; Bảo mật thông tin thí sinh
                    </div>
                </div>
            );
        }

        const root = ReactDOM.createRoot(document.getElementById('react-root'));
        root.render(<LoginForm />);
    </script>
</body>
</html>