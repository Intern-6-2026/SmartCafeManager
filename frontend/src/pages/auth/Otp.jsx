import { Link, useNavigate, useLocation } from "react-router-dom";
import { FaArrowLeft } from "react-icons/fa";
import { useRef, useState } from "react";

export default function Otp() {
    const inputRefs = useRef([]);
    const [otpValues, setOtpValues] = useState(["", "", "", "", "", ""]);
    const navigate = useNavigate();
    const location = useLocation();
    const email = location.state?.email || "email của bạn";

    const handleChange = (e, index) => {
        const val = e.target.value;
        if (isNaN(val)) return; 

        const newOtpValues = [...otpValues];
        newOtpValues[index] = val;
        setOtpValues(newOtpValues);

        if (val !== "" && index < 5) {
            inputRefs.current[index + 1].focus();
        }
    };

    const handleKeyDown = (e, index) => {
        if (e.key === "Backspace" && !e.target.value && index > 0) {
            inputRefs.current[index - 1].focus();
        }
    };

    const handleConfirmOtp = () => {
        const fullOtp = otpValues.join("");
        if (fullOtp.length < 6) {
            alert("Vui lòng nhập đủ 6 số OTP.");
            return;
        }
        navigate("/new-password", { state: { token: fullOtp } });
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-[#F8F4EF] to-[#EFE2D3] flex items-center justify-center p-6">
            <div className="w-full max-w-6xl bg-white rounded-[36px] shadow-[0_20px_60px_rgba(0,0,0,0.08)] overflow-hidden">
                <div className="flex justify-center pt-8">
                    <img src="/images/logo.jpg" alt="" className="w-20 h-20 object-contain" />
                </div>
                <div className="grid lg:grid-cols-[55%_45%]">
                    <div className="flex items-center justify-center px-10 pb-10">
                        <img src="/images/otp.png" alt="" className="w-full max-w-[420px] object-contain hover:scale-105 duration-500" />
                    </div>
                    <div className="flex items-center justify-center px-12 pb-12">
                        <div className="w-full max-w-md">
                            <h1 className="text-4xl font-bold text-[#5A3726]">Xác thực OTP</h1>
                            <p className="text-gray-400 mt-2 mb-10">
                                Nhập mã OTP gồm 6 số đã gửi đến {email}.
                            </p>

                            <div className="flex justify-between mb-8">
                                {otpValues.map((val, index) => (
                                    <input
                                        key={index}
                                        ref={(el) => (inputRefs.current[index] = el)}
                                        maxLength={1}
                                        value={val}
                                        onChange={(e) => handleChange(e, index)}
                                        onKeyDown={(e) => handleKeyDown(e, index)}
                                        className="w-16 h-16 rounded-2xl border border-[#E5E5E5] bg-[#FAFAFA] text-center text-xl font-bold outline-none transition focus:bg-white focus:border-[#C89A63] focus:ring-4 focus:ring-[#C89A63]/20 tracking-widest"
                                    />
                                ))}
                            </div>

                            <button
                                onClick={handleConfirmOtp}
                                className="w-full h-14 rounded-2xl bg-[#C89A63] hover:bg-[#B78350] hover:shadow-xl transition duration-300 text-white font-semibold text-lg"
                            >
                                Xác nhận
                            </button>

                            <div className="text-center mt-8">
                                <Link to="/forgot-password" className="inline-flex items-center gap-2 text-[#B78350] hover:underline">
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