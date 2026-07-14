import { Link } from "react-router-dom";
import { FaEnvelope, FaArrowLeft } from "react-icons/fa";

export default function ForgotPassword() {
    return (
        <div className="min-h-screen bg-gradient-to-br from-[#F8F4EF] to-[#EFE2D3] flex items-center justify-center p-6">

            <div className="w-full max-w-6xl bg-white rounded-[36px] shadow-[0_20px_60px_rgba(0,0,0,0.08)] overflow-hidden">

                {/* Logo */}

                <div className="flex justify-center pt-8">

                    <img
                        src="/images/logo.jpg"
                        alt="Logo"
                        className="w-20 h-20 object-contain"
                    />

                </div>

                {/* Content */}

                <div className="grid lg:grid-cols-[55%_45%]">

                    {/* Left */}

                    <div className="flex items-center justify-center px-10 pb-10">

                        <img
                            src="/images/forgot-password.png"
                            alt=""
                            className="
                                w-full
                                max-w-[430px]
                                object-contain
                                transition
                                duration-500
                                hover:scale-105
                            "
                        />

                    </div>

                    {/* Right */}

                    <div className="flex items-center justify-center px-12 pb-12">

                        <div className="w-full max-w-md">

                            <h1 className="text-4xl font-bold text-[#5A3726]">
                                Quên mật khẩu
                            </h1>

                            <p className="text-gray-400 mt-2 mb-10 leading-6">
                                Nhập email đã đăng ký để nhận mã OTP đặt lại mật khẩu.
                            </p>

                            {/* Email */}

                            <div>

                                <label className="block mb-2 font-semibold text-[#5A3726]">
                                    Email
                                </label>

                                <div className="relative">

                                    <FaEnvelope
                                        className="
                                            absolute
                                            left-5
                                            top-1/2
                                            -translate-y-1/2
                                            text-gray-400
                                        "
                                    />

                                    <input
                                        type="email"
                                        placeholder="Nhập email"
                                        className="
                                            w-full
                                            h-14
                                            rounded-2xl
                                            border
                                            border-[#E5E5E5]
                                            bg-[#FAFAFA]
                                            pl-14
                                            pr-5
                                            outline-none
                                            transition
                                            focus:bg-white
                                            focus:border-[#C89A63]
                                            focus:ring-4
                                            focus:ring-[#C89A63]/20
                                        "
                                    />

                                </div>

                            </div>

                            {/* Button */}

                            <button
                                className="
                                    w-full
                                    h-14
                                    mt-8
                                    rounded-2xl
                                    bg-[#C89A63]
                                    text-white
                                    text-lg
                                    font-semibold
                                    transition
                                    duration-300
                                    hover:bg-[#B78350]
                                    hover:shadow-xl
                                    hover:scale-[1.02]
                                "
                            >
                                Gửi mã OTP
                            </button>

                            {/* Back */}

                            <div className="mt-8 text-center">

                                <Link
                                    to="/"
                                    className="
                                        inline-flex
                                        items-center
                                        gap-2
                                        text-[#B78350]
                                        font-medium
                                        hover:underline
                                    "
                                >
                                    <FaArrowLeft />
                                    Quay lại đăng nhập
                                </Link>

                            </div>

                        </div>

                    </div>

                </div>

            </div>

        </div>
    );
}