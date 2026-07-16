import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import {
    FaEnvelope,
    FaPhone,
    FaUser,
    FaBirthdayCake,
    FaVenusMars,
    FaKey,
    FaEdit,
    FaSignOutAlt
} from "react-icons/fa";
import { getCurrentUserProfile, getApiErrorMessage } from "../../services/apiService";

export default function Profile() {
    const [profile, setProfile] = useState(null);
    const [loading, setLoading] = useState(true);
    const [errorMsg, setErrorMsg] = useState("");
    const navigate = useNavigate();

    useEffect(() => {
        const fetchProfile = async () => {
            try {
                const res = await getCurrentUserProfile();
                setProfile(res.data);
            } catch (err) {
                setErrorMsg(getApiErrorMessage(err, "Không tải được thông tin tài khoản."));
                if (err?.response?.status === 401 || err?.response?.status === 403) {
                    localStorage.clear();
                    navigate("/");
                }
            } finally {
                setLoading(false);
            }
        };
        fetchProfile();
    }, [navigate]);

    const handleLogout = () => {
        localStorage.clear();
        navigate("/");
    };

    const formatDate = (dateString) => {
        if (!dateString) return "Chưa cập nhật";
        const date = new Date(dateString);
        return date.toLocaleDateString("vi-VN");
    };

    if (loading) {
        return <div className="min-h-screen flex items-center justify-center">Đang tải thông tin...</div>;
    }

    if (errorMsg && !profile) {
        return (
            <div className="min-h-screen flex flex-col items-center justify-center">
                <p className="text-red-500 mb-4">{errorMsg}</p>
                <Link to="/" className="text-[#C89A63] underline">Quay lại đăng nhập</Link>
            </div>
        );
    }

    const defaultAvatar = "/images/avatar.png";

    return (
        <div className="min-h-screen bg-gradient-to-br from-[#F8F4EF] to-[#EFE2D3]">
            {/* Header */}
            <header className="bg-white shadow-sm">
                <div className="max-w-7xl mx-auto h-20 flex items-center justify-between px-8">
                    <img src="/images/logo.jpg" alt="Logo" className="w-14" />
                    <div className="flex items-center gap-4">
                        <img
                            src={profile?.imageUrl || defaultAvatar}
                            alt="Avatar"
                            className="w-12 h-12 rounded-full object-cover border-2 border-[#C89A63]"
                        />
                        <div>
                            <h3 className="font-semibold text-[#5A3726]">
                                {profile?.fullName || profile?.username}
                            </h3>
                            <p className="text-sm text-gray-500">
                                {profile?.roleName}
                            </p>
                        </div>
                        <button onClick={handleLogout} className="ml-4 text-gray-500 hover:text-red-500" title="Đăng xuất">
                            <FaSignOutAlt size={20} />
                        </button>
                    </div>
                </div>
            </header>

            {/* Body */}
            <div className="max-w-5xl mx-auto py-12 px-6">
                <div className="bg-white rounded-[32px] shadow-[0_20px_60px_rgba(0,0,0,0.08)] p-10">
                    {/* Avatar */}
                    <div className="flex flex-col items-center">
                        <img
                            src={profile?.imageUrl || defaultAvatar}
                            alt="Avatar Big"
                            className="w-40 h-40 rounded-full border-4 border-[#C89A63] object-cover"
                        />
                        <h2 className="text-3xl font-bold text-[#5A3726] mt-5">
                            {profile?.fullName || profile?.username}
                        </h2>
                        <p className="text-gray-500 mt-1">
                             Vai trò: {profile?.roleName}
                        </p>
                    </div>

                    {/* Info */}
                    <div className="grid md:grid-cols-2 gap-6 mt-12">
                        <div className="flex items-center gap-4 bg-[#FAFAFA] rounded-2xl p-5">
                            <FaEnvelope className="text-[#C89A63] text-xl" />
                            <div>
                                <p className="text-gray-400 text-sm">Email</p>
                                <h4 className="font-semibold">{profile?.email || "Chưa cập nhật"}</h4>
                            </div>
                        </div>

                        <div className="flex items-center gap-4 bg-[#FAFAFA] rounded-2xl p-5">
                            <FaPhone className="text-[#C89A63] text-xl" />
                            <div>
                                <p className="text-gray-400 text-sm">Số điện thoại</p>
                                <h4 className="font-semibold">{profile?.phone || "Chưa cập nhật"}</h4>
                            </div>
                        </div>

                        <div className="flex items-center gap-4 bg-[#FAFAFA] rounded-2xl p-5">
                            <FaBirthdayCake className="text-[#C89A63] text-xl" />
                            <div>
                                <p className="text-gray-400 text-sm">Ngày sinh</p>
                                <h4 className="font-semibold">{formatDate(profile?.dateOfBirth)}</h4>
                            </div>
                        </div>

                        <div className="flex items-center gap-4 bg-[#FAFAFA] rounded-2xl p-5">
                            <FaVenusMars className="text-[#C89A63] text-xl" />
                            <div>
                                <p className="text-gray-400 text-sm">Giới tính</p>
                                <h4 className="font-semibold">
                                    {profile?.gender === "MALE" ? "Nam" : profile?.gender === "FEMALE" ? "Nữ" : "Chưa cập nhật"}
                                </h4>
                            </div>
                        </div>

                        {/* Tuỳ chọn hiển thị theo ROLE */}
                        {profile?.roleName === "CUSTOMER" && (
                            <div className="flex items-center gap-4 bg-[#FAFAFA] rounded-2xl p-5 md:col-span-2">
                                <FaUser className="text-[#C89A63] text-xl" />
                                <div>
                                    <p className="text-gray-400 text-sm">Điểm tích lũy (Loyalty Points)</p>
                                    <h4 className="font-semibold text-green-600">{profile?.loyaltyPoints || 0} điểm</h4>
                                </div>
                            </div>
                        )}
                    </div>

                    {/* Button */}
                    <div className="grid md:grid-cols-2 gap-5 mt-12">
                        <Link
                            to="/edit-profile"
                            className="h-14 rounded-2xl bg-[#C89A63] hover:bg-[#B78350] transition text-white font-semibold flex items-center justify-center gap-3"
                        >
                            <FaEdit />
                            Chỉnh sửa hồ sơ
                        </Link>

                        <Link
                            to="/change-password"
                            className="h-14 rounded-2xl border-2 border-[#C89A63] text-[#C89A63] hover:bg-[#FFF7EF] transition font-semibold flex items-center justify-center gap-3"
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