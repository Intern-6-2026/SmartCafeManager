import { useState } from "react";
import { Link } from "react-router-dom";
import { FaLock, FaEye, FaEyeSlash } from "react-icons/fa";

export default function ChangePassword() {

    const [showOld, setShowOld] = useState(false);
    const [showNew, setShowNew] = useState(false);
    const [showConfirm, setShowConfirm] = useState(false);

    const inputStyle = `
        w-full
        h-14
        rounded-2xl
        border
        border-[#E5E5E5]
        bg-[#FAFAFA]
        pl-14
        pr-14
        outline-none
        focus:border-[#C89A63]
        focus:ring-4
        focus:ring-[#C89A63]/20
    `;

    return (
        <div className="min-h-screen bg-gradient-to-br from-[#F8F4EF] to-[#EFE2D3] flex items-center justify-center p-6">

            <div className="bg-white rounded-[32px] shadow-[0_20px_60px_rgba(0,0,0,.08)] w-full max-w-xl p-10">

                <h1 className="text-3xl font-bold text-[#5A3726] text-center">
                    Đổi mật khẩu
                </h1>

                <p className="text-center text-gray-400 mt-2 mb-10">
                    Vui lòng nhập đầy đủ thông tin bên dưới.
                </p>

                {[
                    {
                        label: "Mật khẩu hiện tại",
                        show: showOld,
                        setShow: setShowOld,
                    },
                    {
                        label: "Mật khẩu mới",
                        show: showNew,
                        setShow: setShowNew,
                    },
                    {
                        label: "Xác nhận mật khẩu",
                        show: showConfirm,
                        setShow: setShowConfirm,
                    },
                ].map((item, index) => (

                    <div key={index} className="mb-6">

                        <label className="block mb-2 font-semibold text-[#5A3726]">
                            {item.label}
                        </label>

                        <div className="relative">

                            <FaLock className="absolute left-5 top-1/2 -translate-y-1/2 text-gray-400" />

                            <input
                                type={item.show ? "text" : "password"}
                                className={inputStyle}
                                placeholder={item.label}
                            />

                            <button
                                type="button"
                                onClick={() => item.setShow(!item.show)}
                                className="absolute right-5 top-1/2 -translate-y-1/2 text-gray-400"
                            >
                                {item.show ? <FaEyeSlash /> : <FaEye />}
                            </button>

                        </div>

                    </div>

                ))}

                <div className="grid grid-cols-2 gap-5 mt-8">

                    <button
                        className="
                            h-14
                            rounded-2xl
                            bg-[#C89A63]
                            text-white
                            font-semibold
                            hover:bg-[#B78350]
                            transition
                        "
                    >
                        Cập nhật
                    </button>

                    <Link
                        to="/profile"
                        className="
                            h-14
                            rounded-2xl
                            border-2
                            border-[#C89A63]
                            text-[#C89A63]
                            flex
                            items-center
                            justify-center
                            font-semibold
                            hover:bg-[#FFF7EF]
                        "
                    >
                        Hủy
                    </Link>

                </div>

            </div>

        </div>
    );
}