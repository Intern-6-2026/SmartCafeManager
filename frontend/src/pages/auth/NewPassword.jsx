import { useState } from "react";
import { Link, useNavigate, useLocation } from "react-router-dom";
import { FaLock, FaEye, FaEyeSlash, FaArrowLeft } from "react-icons/fa";
import { resetPassword, getApiErrorMessage } from "../../services/apiService";

export default function NewPassword() {
    const [showPassword, setShowPassword] = useState(false);
    const [showConfirm, setShowConfirm] = useState(false);
    const [newPassword, setNewPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [loading, setLoading] = useState(false);
    
    const navigate = useNavigate();
    const location = useLocation();
    const token = location.state?.token;

    const handleSubmit = async () => {
        if (!token) {
            alert("Không tìm thấy mã xác thực. Vui lòng thử lại quá trình quên mật khẩu.");
            navigate("/forgot-password");
            return;
        }

        if (newPassword.length < 6) {
            alert("Mật khẩu mới phải có ít nhất 6 ký tự.");
            return;
        }

        if (newPassword !== confirmPassword) {
            alert("Xác nhận mật khẩu không khớp!");
            return;
        }

        setLoading(true);
        try {
            const res = await resetPassword(token, newPassword);
            alert(res.data);
            navigate("/");
        } catch (err) {
            alert(getApiErrorMessage(err, "Đổi mật khẩu thất bại. Mã OTP có thể đã hết hạn."));
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-[#F8F4EF] to-[#EFE2D3] flex items-center justify-center p-6">
            <div className="w-full max-w-6xl bg-white rounded-[36px] shadow-[0_20px_60px_rgba(0,0,0,0.08)] overflow-hidden">
                <div className="flex justify-center pt-8">
                    <img src="/images/logo.jpg" alt="" className="w-20 h-20 object-contain" />
                </div>
                <div className="grid lg:grid-cols-[55%_45%]">
                    <div className="flex items-center justify-center px-10 pb-10">
                        <img src="/images/new-password.png" alt="" className="w-full max-w-[420px] object-contain hover:scale-105 duration-500" />
                    </div>
                    <div className="flex items-center justify-center px-12 pb-12">
                        <div className="w-full max-w-md">
                            <h1 className="text-4xl font-bold text-[#5A3726]">Mật khẩu mới</h1>
                            <p className="text-gray-400 mt-2 mb-10">Tạo mật khẩu mới cho tài khoản của bạn.</p>

                            <div className="mb-6">
                                <label className="block mb-2 font-semibold text-[#5A3726]">Mật khẩu mới</label>
                                <div className="relative">
                                    <FaLock className="absolute left-5 top-1/2 -translate-y-1/2 text-gray-400" />
                                    <input
                                        type={showPassword ? "text" : "password"}
                                        value={newPassword}
                                        onChange={(e) => setNewPassword(e.target.value)}
                                        placeholder="Nhập mật khẩu mới"
                                        className="w-full h-14 rounded-2xl border border-[#E5E5E5] bg-[#FAFAFA] pl-14 pr-14 outline-none transition focus:bg-white focus:border-[#C89A63] focus:ring-4 focus:ring-[#C89A63]/20"
                                    />
                                    <button type="button" onClick={() => setShowPassword(!showPassword)} className="absolute right-5 top-1/2 -translate-y-1/2 text-gray-400">
                                        {showPassword ? <FaEyeSlash /> : <FaEye />}
                                    </button>
                                </div>
                            </div>

                            <div>
                                <label className="block mb-2 font-semibold text-[#5A3726]">Xác nhận mật khẩu</label>
                                <div className="relative">
                                    <FaLock className="absolute left-5 top-1/2 -translate-y-1/2 text-gray-400" />
                                    <input
                                        type={showConfirm ? "text" : "password"}
                                        value={confirmPassword}
                                        onChange={(e) => setConfirmPassword(e.target.value)}
                                        placeholder="Nhập lại mật khẩu"
                                        className="w-full h-14 rounded-2xl border border-[#E5E5E5] bg-[#FAFAFA] pl-14 pr-14 outline-none transition focus:bg-white focus:border-[#C89A63] focus:ring-4 focus:ring-[#C89A63]/20"
                                    />
                                    <button type="button" onClick={() => setShowConfirm(!showConfirm)} className="absolute right-5 top-1/2 -translate-y-1/2 text-gray-400">
                                        {showConfirm ? <FaEyeSlash /> : <FaEye />}
                                    </button>
                                </div>
                            </div>

                            <button
                                onClick={handleSubmit}
                                disabled={loading}
                                className="w-full h-14 mt-8 rounded-2xl bg-[#C89A63] hover:bg-[#B78350] hover:shadow-xl transition duration-300 text-white text-lg font-semibold disabled:opacity-70"
                            >
                                {loading ? "Đang xử lý..." : "Đổi mật khẩu"}
                            </button>

                            <div className="text-center mt-8">
                                <Link to="/otp" className="inline-flex items-center gap-2 text-[#B78350] hover:underline">
                                    <FaArrowLeft /> Quay lại
                                </Link>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}