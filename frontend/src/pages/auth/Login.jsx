import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { FaUser, FaLock, FaEye, FaEyeSlash } from "react-icons/fa";
import { loginApi, getApiErrorMessage } from "../../services/apiService";
import AuthCard from "../../components/AuthCard";

/** Trang đăng nhập Smart Cafe */
export default function Login() {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [showPassword, setShowPassword] = useState(false);
    const [loading, setLoading] = useState(false);
    const [errorMsg, setErrorMsg] = useState("");
    
    const navigate = useNavigate();

    const handleLogin = async (e) => {
        e.preventDefault();
        setErrorMsg("");
        if (!username || !password) {
            setErrorMsg("Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu.");
            return;
        }

        setLoading(true);
        try {
            const res = await loginApi(username, password);
            const { token, roleName, userName } = res.data;

            localStorage.setItem("token", token);
            localStorage.setItem("roleName", roleName);
            localStorage.setItem("userName", userName);

            navigate("/profile");
        } catch (err) {
            setErrorMsg(getApiErrorMessage(err, "Đăng nhập thất bại."));
        } finally {
            setLoading(false);
        }
    };

    return (
        <AuthCard title="Đăng nhập" subtitle="Chào mừng bạn quay trở lại SmartCafe">
            {errorMsg && (
                <div className="mb-4 p-3 bg-red-100 text-red-600 rounded-xl text-sm">
                    {errorMsg}
                </div>
            )}

            <form onSubmit={handleLogin}>
                                {/* Username */}
                                <div className="mb-6">
                                    <label className="block mb-2 font-semibold text-[#5A3726]">
                                        Tên đăng nhập
                                    </label>
                                    <div className="relative">
                                        <FaUser className="absolute left-5 top-1/2 -translate-y-1/2 text-gray-400" />
                                        <input
                                            type="text"
                                            value={username}
                                            onChange={(e) => setUsername(e.target.value)}
                                            placeholder="Nhập tên đăng nhập"
                                            className="w-full h-14 rounded-2xl border border-[#E5E5E5] bg-[#FAFAFA] pl-14 pr-5 outline-none transition focus:bg-white focus:border-[#C89A63] focus:ring-4 focus:ring-[#C89A63]/20"
                                        />
                                    </div>
                                </div>

                                {/* Password */}
                                <div>
                                    <label className="block mb-2 font-semibold text-[#5A3726]">
                                        Mật khẩu
                                    </label>
                                    <div className="relative">
                                        <FaLock className="absolute left-5 top-1/2 -translate-y-1/2 text-gray-400" />
                                        <input
                                            type={showPassword ? "text" : "password"}
                                            value={password}
                                            onChange={(e) => setPassword(e.target.value)}
                                            placeholder="Nhập mật khẩu"
                                            className="w-full h-14 rounded-2xl border border-[#E5E5E5] bg-[#FAFAFA] pl-14 pr-14 outline-none transition focus:bg-white focus:border-[#C89A63] focus:ring-4 focus:ring-[#C89A63]/20"
                                        />
                                        <button 
                                            type="button" 
                                            onClick={() => setShowPassword(!showPassword)}
                                            className="absolute right-5 top-1/2 -translate-y-1/2 text-gray-400 cursor-pointer"
                                        >
                                            {showPassword ? <FaEyeSlash /> : <FaEye />}
                                        </button>
                                    </div>
                                </div>

                                {/* Forgot */}
                                <div className="flex justify-end mt-5">
                                    <Link
                                        to="/forgot-password"
                                        className="text-[#B78350] font-medium hover:text-[#9B6735] hover:underline transition"
                                    >
                                        Quên mật khẩu?
                                    </Link>
                                </div>

                                {/* Login Button */}
                                <button
                                    type="submit"
                                    disabled={loading}
                                    className="w-full h-14 mt-8 rounded-2xl bg-[#C89A63] text-white text-lg font-semibold transition duration-300 hover:bg-[#B78350] hover:shadow-xl hover:scale-[1.02] active:scale-95 disabled:opacity-70 disabled:cursor-not-allowed"
                                >
                                    {loading ? "Đang xử lý..." : "Đăng nhập"}
                                </button>
                            </form>
        </AuthCard>
    );
}