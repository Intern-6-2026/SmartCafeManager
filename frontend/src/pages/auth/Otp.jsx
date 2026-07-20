import { Link, useNavigate, useLocation } from "react-router-dom";
import { FaArrowLeft } from "react-icons/fa";
import { useRef, useState, useEffect } from "react";
import Popup from "../../components/Popup";
import AuthCard from "../../components/AuthCard";

const RESET_OTP_KEY = "resetOtpToken";
const RESET_EMAIL_KEY = "resetEmail";

export default function Otp() {
    const inputRefs = useRef([]);
    const [otpValues, setOtpValues] = useState(["", "", "", "", "", ""]);
    const [popup, setPopup] = useState({ open: false, type: "info", title: "", message: "" });
    const navigate = useNavigate();
    const location = useLocation();
    const email = location.state?.email || sessionStorage.getItem(RESET_EMAIL_KEY) || "email của bạn";

    useEffect(() => {
        if (location.state?.email) {
            sessionStorage.setItem(RESET_EMAIL_KEY, location.state.email);
        }
    }, [location.state?.email]);

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
            setPopup({
                open: true,
                type: "warning",
                title: "Thiếu mã OTP",
                message: "Vui lòng nhập đủ 6 số OTP.",
            });
            return;
        }
        sessionStorage.setItem(RESET_OTP_KEY, fullOtp);
        navigate("/new-password", { state: { token: fullOtp, email } });
    };

    return (
        <>
            <AuthCard title="Xác thực OTP" subtitle={`Nhập mã OTP gồm 6 số đã gửi đến ${email}.`}>
                <div className="flex justify-between gap-2 mb-8">
                    {otpValues.map((val, index) => (
                        <input
                            key={index}
                            ref={(el) => (inputRefs.current[index] = el)}
                            maxLength={1}
                            value={val}
                            onChange={(e) => handleChange(e, index)}
                            onKeyDown={(e) => handleKeyDown(e, index)}
                            className="w-full max-w-[3rem] h-14 rounded-2xl border border-[#E5E5E5] bg-[#FAFAFA] text-center text-xl font-bold outline-none transition focus:bg-white focus:border-[#C89A63] focus:ring-4 focus:ring-[#C89A63]/20"
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
            </AuthCard>

            <Popup
                open={popup.open}
                type={popup.type}
                title={popup.title}
                message={popup.message}
                onClose={() => setPopup((prev) => ({ ...prev, open: false }))}
            />
        </>
    );
}
