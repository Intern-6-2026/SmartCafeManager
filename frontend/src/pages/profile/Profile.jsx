import { Link } from "react-router-dom";
import {
    FaEnvelope,
    FaPhone,
    FaUser,
    FaBirthdayCake,
    FaVenusMars,
    FaKey,
    FaEdit,
} from "react-icons/fa";

export default function Profile() {
    return (
        <div className="min-h-screen bg-gradient-to-br from-[#F8F4EF] to-[#EFE2D3]">

            {/* Header */}

            <header className="bg-white shadow-sm">

                <div className="max-w-7xl mx-auto h-20 flex items-center justify-between px-8">

                    <img
                        src="/images/logo.jpg"
                        alt=""
                        className="w-14"
                    />

                    <div className="flex items-center gap-4">

                        <img
                            src="/images/avatar.png"
                            alt=""
                            className="w-12 h-12 rounded-full object-cover border-2 border-[#C89A63]"
                        />

                        <div>

                            <h3 className="font-semibold text-[#5A3726]">
                                Nguyễn Văn A
                            </h3>

                            <p className="text-sm text-gray-500">
                                Quản lý
                            </p>

                        </div>

                    </div>

                </div>

            </header>

            {/* Body */}

            <div className="max-w-5xl mx-auto py-12 px-6">

                <div className="bg-white rounded-[32px] shadow-[0_20px_60px_rgba(0,0,0,0.08)] p-10">

                    {/* Avatar */}

                    <div className="flex flex-col items-center">

                        <img
                            src="/images/avatar.png"
                            alt=""
                            className="w-40 h-40 rounded-full border-4 border-[#C89A63] object-cover"
                        />

                        <h2 className="text-3xl font-bold text-[#5A3726] mt-5">
                            Nguyễn Văn A
                        </h2>

                        <p className="text-gray-500">
                            Quản lý cửa hàng
                        </p>

                    </div>

                    {/* Info */}

                    <div className="grid md:grid-cols-2 gap-6 mt-12">

                        <div className="flex items-center gap-4 bg-[#FAFAFA] rounded-2xl p-5">

                            <FaEnvelope className="text-[#C89A63] text-xl" />

                            <div>

                                <p className="text-gray-400 text-sm">
                                    Email
                                </p>

                                <h4 className="font-semibold">
                                    admin@gmail.com
                                </h4>

                            </div>

                        </div>

                        <div className="flex items-center gap-4 bg-[#FAFAFA] rounded-2xl p-5">

                            <FaPhone className="text-[#C89A63] text-xl" />

                            <div>

                                <p className="text-gray-400 text-sm">
                                    Số điện thoại
                                </p>

                                <h4 className="font-semibold">
                                    0123456789
                                </h4>

                            </div>

                        </div>

                        <div className="flex items-center gap-4 bg-[#FAFAFA] rounded-2xl p-5">

                            <FaBirthdayCake className="text-[#C89A63] text-xl" />

                            <div>

                                <p className="text-gray-400 text-sm">
                                    Ngày sinh
                                </p>

                                <h4 className="font-semibold">
                                    20/10/2004
                                </h4>

                            </div>

                        </div>

                        <div className="flex items-center gap-4 bg-[#FAFAFA] rounded-2xl p-5">

                            <FaVenusMars className="text-[#C89A63] text-xl" />

                            <div>

                                <p className="text-gray-400 text-sm">
                                    Giới tính
                                </p>

                                <h4 className="font-semibold">
                                    Nam
                                </h4>

                            </div>

                        </div>

                        <div className="flex items-center gap-4 bg-[#FAFAFA] rounded-2xl p-5 md:col-span-2">

                            <FaUser className="text-[#C89A63] text-xl" />

                            <div>

                                <p className="text-gray-400 text-sm">
                                    Vai trò
                                </p>

                                <h4 className="font-semibold">
                                    Quản lý cửa hàng
                                </h4>

                            </div>

                        </div>

                    </div>

                    {/* Button */}

                    <div className="grid md:grid-cols-2 gap-5 mt-12">

                        <Link
                            to="/edit-profile"
                            className="
                                h-14
                                rounded-2xl
                                bg-[#C89A63]
                                hover:bg-[#B78350]
                                transition
                                text-white
                                font-semibold
                                flex
                                items-center
                                justify-center
                                gap-3
                            "
                        >
                            <FaEdit />
                            Chỉnh sửa hồ sơ
                        </Link>

                        <Link
                            to="/change-password"
                            className="
                                h-14
                                rounded-2xl
                                border-2
                                border-[#C89A63]
                                text-[#C89A63]
                                hover:bg-[#FFF7EF]
                                transition
                                font-semibold
                                flex
                                items-center
                                justify-center
                                gap-3
                            "
                        >
                            <FaKey />
                            Đổi mật khẩu
                        </Link>

                    </div>

                </div>

            </div>

        </div>
    );
}