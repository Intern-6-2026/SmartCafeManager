import { Link } from "react-router-dom";
import { FaArrowLeft } from "react-icons/fa";
import { useRef } from "react";

export default function Otp() {
    const inputRefs = useRef([]);

    const handleChange = (e, index) => {
        if (e.target.value.length === 1 && index < 5) {
            inputRefs.current[index + 1].focus();
        }
    };

    const handleKeyDown = (e, index) => {
        if (e.key === "Backspace" && !e.target.value && index > 0) {
            inputRefs.current[index - 1].focus();
        }
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-[#F8F4EF] to-[#EFE2D3] flex items-center justify-center p-6">

            <div className="w-full max-w-6xl bg-white rounded-[36px] shadow-[0_20px_60px_rgba(0,0,0,0.08)] overflow-hidden">

                {/* Logo */}

                <div className="flex justify-center pt-8">

                    <img
                        src="/images/logo.jpg"
                        alt=""
                        className="w-20 h-20 object-contain"
                    />

                </div>

                {/* Body */}

                <div className="grid lg:grid-cols-[55%_45%]">

                    {/* Left */}

                    <div className="flex items-center justify-center px-10 pb-10">

                        <img
                            src="/images/otp.png"
                            alt=""
                            className="
                                w-full
                                max-w-[420px]
                                object-contain
                                hover:scale-105
                                duration-500
                            "
                        />

                    </div>

                    {/* Right */}

                    <div className="flex items-center justify-center px-12 pb-12">

                        <div className="w-full max-w-md">

                            <h1 className="text-4xl font-bold text-[#5A3726]">
                                Xác thực OTP
                            </h1>

                            <p className="text-gray-400 mt-2 mb-10">
                                Nhập mã OTP gồm 6 số đã gửi đến email của bạn.
                            </p>

                            {/* OTP */}

                            <div className="flex justify-between mb-8">

                                {[...Array(6)].map((_, index) => (

                                    <input
                                        key={index}
                                        ref={(el) => inputRefs.current[index] = el}
                                        maxLength={1}
                                        onChange={(e) => handleChange(e, index)}
                                        onKeyDown={(e) => handleKeyDown(e, index)}
                                        className="
                                            w-16
                                            h-16
                                            rounded-2xl
                                            border
                                            border-[#E5E5E5]
                                            bg-[#FAFAFA]
                                            text-center
                                            text-xl
                                            font-bold
                                            outline-none
                                            transition
                                            focus:bg-white
                                            focus:border-[#C89A63]
                                            focus:ring-4
                                            focus:ring-[#C89A63]/20
                                            tracking-widest
                                        "
                                    />

                                ))}

                            </div>

                            {/* Button */}

                            <button
                                className="
                                    w-full
                                    h-14
                                    rounded-2xl
                                    bg-[#C89A63]
                                    hover:bg-[#B78350]
                                    hover:shadow-xl
                                    transition
                                    duration-300
                                    text-white
                                    font-semibold
                                    text-lg
                                "
                            >
                                Xác nhận
                            </button>

                            {/* Resend */}

                            <div className="text-center mt-8">

                                <p className="text-gray-500 text-sm">
                                    Chưa nhận được mã?
                                </p>

                                <button
                                    className="
                                        mt-2
                                        text-[#B78350]
                                        font-semibold
                                        hover:underline
                                    "
                                >
                                    Gửi lại OTP
                                </button>

                            </div>

                            {/* Back */}

                            <div className="mt-8 text-center">

                                <Link
                                    to="/forgot-password"
                                    className="
                                        inline-flex
                                        items-center
                                        gap-2
                                        text-[#B78350]
                                        hover:underline
                                    "
                                >
                                    <FaArrowLeft />
                                    Quay lại
                                </Link>

                            </div>

                        </div>

                    </div>

                </div>

            </div>

        </div>
    );
}