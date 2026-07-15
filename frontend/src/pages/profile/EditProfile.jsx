import { Link } from "react-router-dom";
import { FaCamera } from "react-icons/fa";

export default function EditProfile() {
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

                    <img
                        src="/images/avatar.png"
                        alt=""
                        className="w-12 h-12 rounded-full border-2 border-[#C89A63]"
                    />

                </div>

            </header>

            {/* Content */}

            <div className="max-w-5xl mx-auto py-12 px-6">

                <div className="bg-white rounded-[32px] shadow-[0_20px_60px_rgba(0,0,0,.08)] p-10">

                    <h1 className="text-3xl font-bold text-[#5A3726] mb-10">
                        Chỉnh sửa hồ sơ
                    </h1>

                    {/* Avatar */}

                    <div className="flex justify-center mb-12">

                        <div className="relative">

                            <img
                                src="/images/avatar.png"
                                alt=""
                                className="w-40 h-40 rounded-full object-cover border-4 border-[#C89A63]"
                            />

                            <button
                                className="
                                    absolute
                                    bottom-2
                                    right-2
                                    w-12
                                    h-12
                                    rounded-full
                                    bg-[#C89A63]
                                    text-white
                                    flex
                                    items-center
                                    justify-center
                                    hover:bg-[#B78350]
                                    transition
                                "
                            >
                                <FaCamera />
                            </button>

                        </div>

                    </div>

                    {/* Form */}

                    <div className="grid md:grid-cols-2 gap-6">

                        <div>

                            <label className="block mb-2 font-semibold text-[#5A3726]">
                                Họ và tên
                            </label>

                            <input
                                defaultValue="Nguyễn Văn A"
                                className="
                                    w-full
                                    h-14
                                    rounded-2xl
                                    border
                                    border-[#E5E5E5]
                                    bg-[#FAFAFA]
                                    px-5
                                    outline-none
                                    focus:border-[#C89A63]
                                    focus:ring-4
                                    focus:ring-[#C89A63]/20
                                "
                            />

                        </div>

                        <div>

                            <label className="block mb-2 font-semibold text-[#5A3726]">
                                Email
                            </label>

                            <input
                                defaultValue="admin@gmail.com"
                                className="
                                    w-full
                                    h-14
                                    rounded-2xl
                                    border
                                    border-[#E5E5E5]
                                    bg-[#FAFAFA]
                                    px-5
                                    outline-none
                                    focus:border-[#C89A63]
                                    focus:ring-4
                                    focus:ring-[#C89A63]/20
                                "
                            />

                        </div>

                        <div>

                            <label className="block mb-2 font-semibold text-[#5A3726]">
                                Số điện thoại
                            </label>

                            <input
                                defaultValue="0123456789"
                                className="
                                    w-full
                                    h-14
                                    rounded-2xl
                                    border
                                    border-[#E5E5E5]
                                    bg-[#FAFAFA]
                                    px-5
                                    outline-none
                                    focus:border-[#C89A63]
                                    focus:ring-4
                                    focus:ring-[#C89A63]/20
                                "
                            />

                        </div>

                        <div>

                            <label className="block mb-2 font-semibold text-[#5A3726]">
                                Ngày sinh
                            </label>

                            <input
                                type="date"
                                className="
                                    w-full
                                    h-14
                                    rounded-2xl
                                    border
                                    border-[#E5E5E5]
                                    bg-[#FAFAFA]
                                    px-5
                                    outline-none
                                    focus:border-[#C89A63]
                                    focus:ring-4
                                    focus:ring-[#C89A63]/20
                                "
                            />

                        </div>

                        <div>

                            <label className="block mb-2 font-semibold text-[#5A3726]">
                                Giới tính
                            </label>

                            <select
                                className="
                                    w-full
                                    h-14
                                    rounded-2xl
                                    border
                                    border-[#E5E5E5]
                                    bg-[#FAFAFA]
                                    px-5
                                    outline-none
                                    focus:border-[#C89A63]
                                    focus:ring-4
                                    focus:ring-[#C89A63]/20
                                "
                            >
                                <option>Nam</option>
                                <option>Nữ</option>
                            </select>

                        </div>

                    </div>

                    {/* Button */}

                    <div className="grid md:grid-cols-2 gap-5 mt-10">

                        <button
                            className="
                                h-14
                                rounded-2xl
                                bg-[#C89A63]
                                hover:bg-[#B78350]
                                text-white
                                font-semibold
                                transition
                            "
                        >
                            Lưu thay đổi
                        </button>

                        <Link
                            to="/profile"
                            className="
                                h-14
                                rounded-2xl
                                border-2
                                border-[#C89A63]
                                text-[#C89A63]
                                font-semibold
                                flex
                                items-center
                                justify-center
                                hover:bg-[#FFF7EF]
                                transition
                            "
                        >
                            Hủy
                        </Link>

                    </div>

                </div>

            </div>

        </div>
    );
}