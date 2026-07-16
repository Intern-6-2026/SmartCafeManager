import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { FaEnvelope, FaArrowLeft } from "react-icons/fa";
import { forgotPassword, getApiErrorMessage } from "../../services/apiService";

export default function ForgotPassword() {
    const [email, setEmail] = useState("");
    const [loading, setLoading] = useState(false);
    const [errorMsg, setErrorMsg] = useState("");
    const navigate = useNavigate();

    const handleSendOtp = async () => {
        setErrorMsg("");
        if (!email) {
            setErrorMsg("Vui lòng nhập email.");
            return;
        }

        setLoading(true);
        try {
            await forgotPassword(email);
            navigate("/otp", { state: { email } });
        } catch (err) {
            setErrorMsg(getApiErrorMessage(err, "Không thể gửi mã OTP."));
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-[#F8F4EF] to-[#EFE2D3] flex items-center justify-center p-6">
            <div className="w-full max-w-6xl bg-white rounded-[36px] shadow-[0_20px_60px_rgba(0,0,0,0.08)] overflow-hidden">
                <div className="flex justify-center pt-8">
                    <img src="/images/logo.jpg" alt="Logo" className="w-20 h-20 object-contain" />
                </div>
                <div className="grid lg:grid-cols-[55%_45%]">
                    <div className="flex items-center justify-center px-10 pb-10">
                        <img src="/images/forgot-password.png" alt="" className="w-full max-w-[430px] object-contain transition duration-500 hover:scale-105" />
                    </div>
                    <div className="flex items-center justify-center px-12 pb-12">
                        <div className="w-full max-w-md">
                            <h1 className="text-4xl font-bold text-[#5A3726]">Quên mật khẩu</h1>
                            <p className="text-gray-400 mt-2 mb-6 leading-6">
                                Nhập email đã đăng ký để nhận mã OTP đặt lại mật khẩu.
                            </p>

                            {errorMsg && (
                                <div className="mb-4 p-3 bg-red-100 text-red-600 rounded-xl text-sm">
                                    {errorMsg}
                                </div>
                            )}

                            <div>
                                <label className="block mb-2 font-semibold text-[#5A3726]">Email</label>
                                <div className="relative">
                                    <FaEnvelope className="absolute left-5 top-1/2 -translate-y-1/2 text-gray-400" />
                                    <input
                                        type="email"
                                        value={email}
                                        onChange={(e) => setEmail(e.target.value)}
                                        placeholder="Nhập email"
                                        className="w-full h-14 rounded-2xl border border-[#E5E5E5] bg-[#FAFAFA] pl-14 pr-5 outline-none transition focus:bg-white focus:border-[#C89A63] focus:ring-4 focus:ring-[#C89A63]/20"
                                    />
                                </div>
                            </div>

                            <button
                                onClick={handleSendOtp}
                                disabled={loading}
                                className="w-full h-14 mt-8 rounded-2xl bg-[#C89A63] text-white text-lg font-semibold transition duration-300 hover:bg-[#B78350] hover:shadow-xl hover:scale-[1.02] disabled:opacity-70 disabled:cursor-not-allowed"
                            >
                                {loading ? "Đang gửi..." : "Gửi mã OTP"}
                            </button>

                            <div className="mt-8 text-center">
                                <Link to="/" className="inline-flex items-center gap-2 text-[#B78350] font-medium hover:underline">
                                    <FaArrowLeft /> Quay lại đăng nhập
                                </Link>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}