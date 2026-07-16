import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { FaLock, FaEye, FaEyeSlash } from "react-icons/fa";
import { changePassword, getApiErrorMessage } from "../../services/apiService";

export default function ChangePassword() {
    const [showOld, setShowOld] = useState(false);
    const [showNew, setShowNew] = useState(false);
    const [showConfirm, setShowConfirm] = useState(false);

    const [oldPassword, setOldPassword] = useState("");
    const [newPassword, setNewPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const handleUpdatePassword = async () => {
        if (!oldPassword || !newPassword || !confirmPassword) {
            alert("Vui lòng điền đầy đủ các trường.");
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
            await changePassword(oldPassword, newPassword);
            alert("Đổi mật khẩu thành công! Vui lòng đăng nhập lại.");
            localStorage.clear();
            navigate("/");
        } catch (err) {
            alert(getApiErrorMessage(err, "Đổi mật khẩu thất bại."));
        } finally {
            setLoading(false);
        }
    };

    const inputStyle = `w-full h-14 rounded-2xl border border-[#E5E5E5] bg-[#FAFAFA] pl-14 pr-14 outline-none focus:border-[#C89A63] focus:ring-4 focus:ring-[#C89A63]/20`;

    return (
        <div className="min-h-screen bg-gradient-to-br from-[#F8F4EF] to-[#EFE2D3] flex items-center justify-center p-6">
            <div className="bg-white rounded-[32px] shadow-[0_20px_60px_rgba(0,0,0,.08)] w-full max-w-xl p-10">
                <h1 className="text-3xl font-bold text-[#5A3726] text-center">Đổi mật khẩu</h1>
                <p className="text-center text-gray-400 mt-2 mb-10">Vui lòng nhập đầy đủ thông tin bên dưới.</p>

                {/* Old Password */}
                <div className="mb-6">
                    <label className="block mb-2 font-semibold text-[#5A3726]">Mật khẩu hiện tại</label>
                    <div className="relative">
                        <FaLock className="absolute left-5 top-1/2 -translate-y-1/2 text-gray-400" />
                        <input
                            type={showOld ? "text" : "password"}
                            value={oldPassword}
                            onChange={(e) => setOldPassword(e.target.value)}
                            className={inputStyle}
                            placeholder="Nhập mật khẩu hiện tại"
                        />
                        <button type="button" onClick={() => setShowOld(!showOld)} className="absolute right-5 top-1/2 -translate-y-1/2 text-gray-400">
                            {showOld ? <FaEyeSlash /> : <FaEye />}
                        </button>
                    </div>
                </div>

                {/* New Password */}
                <div className="mb-6">
                    <label className="block mb-2 font-semibold text-[#5A3726]">Mật khẩu mới</label>
                    <div className="relative">
                        <FaLock className="absolute left-5 top-1/2 -translate-y-1/2 text-gray-400" />
                        <input
                            type={showNew ? "text" : "password"}
                            value={newPassword}
                            onChange={(e) => setNewPassword(e.target.value)}
                            className={inputStyle}
                            placeholder="Nhập mật khẩu mới"
                        />
                        <button type="button" onClick={() => setShowNew(!showNew)} className="absolute right-5 top-1/2 -translate-y-1/2 text-gray-400">
                            {showNew ? <FaEyeSlash /> : <FaEye />}
                        </button>
                    </div>
                </div>

                {/* Confirm Password */}
                <div className="mb-6">
                    <label className="block mb-2 font-semibold text-[#5A3726]">Xác nhận mật khẩu</label>
                    <div className="relative">
                        <FaLock className="absolute left-5 top-1/2 -translate-y-1/2 text-gray-400" />
                        <input
                            type={showConfirm ? "text" : "password"}
                            value={confirmPassword}
                            onChange={(e) => setConfirmPassword(e.target.value)}
                            className={inputStyle}
                            placeholder="Nhập lại mật khẩu mới"
                        />
                        <button type="button" onClick={() => setShowConfirm(!showConfirm)} className="absolute right-5 top-1/2 -translate-y-1/2 text-gray-400">
                            {showConfirm ? <FaEyeSlash /> : <FaEye />}
                        </button>
                    </div>
                </div>

                <div className="grid grid-cols-2 gap-5 mt-8">
                    <button
                        onClick={handleUpdatePassword}
                        disabled={loading}
                        className="h-14 rounded-2xl bg-[#C89A63] text-white font-semibold hover:bg-[#B78350] transition disabled:opacity-70"
                    >
                        {loading ? "Đang lưu..." : "Cập nhật"}
                    </button>
                    <Link to="/profile" className="h-14 rounded-2xl border-2 border-[#C89A63] text-[#C89A63] flex items-center justify-center font-semibold hover:bg-[#FFF7EF]">
                        Hủy
                    </Link>
                </div>
            </div>
        </div>
    );
}