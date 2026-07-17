import { useState, useEffect, useRef } from "react";
import { Link, useNavigate } from "react-router-dom";
import { FaCamera } from "react-icons/fa";
import { 
    getCurrentUserProfile, 
    updateProfile, 
    uploadAvatar, 
    getApiErrorMessage 
} from "../../services/apiService";

export default function EditProfile() {
    const [profile, setProfile] = useState({
        fullName: "",
        email: "",
        phoneNumber: "",
        dateOfBirth: "",
        gender: "MALE",
        address: "",
        imageUrl: ""
    });
    
    const [avatarFile, setAvatarFile] = useState(null);
    const [previewImage, setPreviewImage] = useState("/images/avatar.png");
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    
    const fileInputRef = useRef(null);
    const navigate = useNavigate();

    useEffect(() => {
        const fetchProfile = async () => {
            try {
                const res = await getCurrentUserProfile();
                const data = res.data;
                setProfile({
                    fullName: data.fullName || "",
                    email: data.email || "",
                    phoneNumber: data.phone || "",
                    dateOfBirth: data.dateOfBirth ? data.dateOfBirth.split('T')[0] : "", // Cắt lấy YYYY-MM-DD
                    gender: data.gender || "MALE",
                    address: data.address || "",
                    imageUrl: data.imageUrl || ""
                });
                if (data.imageUrl) {
                    setPreviewImage(data.imageUrl);
                }
            } catch (err) {
                alert(getApiErrorMessage(err, "Không thể tải thông tin hồ sơ."));
                navigate("/profile");
            } finally {
                setLoading(false);
            }
        };
        fetchProfile();
    }, [navigate]);

    const handleImageChange = (e) => {
        const file = e.target.files[0];
        if (file) {
            setAvatarFile(file);
            setPreviewImage(URL.createObjectURL(file)); // Hiển thị preview ảnh cục bộ
        }
    };

    const handleChange = (e) => {
        const { name, value } = e.target;
        setProfile(prev => ({ ...prev, [name]: value }));
    };

    const handleSave = async () => {
        setSaving(true);
        try {
            let currentImageUrl = profile.imageUrl;
            if (avatarFile) {
                const uploadRes = await uploadAvatar(avatarFile);
                currentImageUrl = uploadRes.data.imageUrl; // Lấy URL ảnh mới từ server
            }

            const updateData = {
                fullName: profile.fullName,
                email: profile.email,
                phoneNumber: profile.phoneNumber,
                dateOfBirth: profile.dateOfBirth || null,
                gender: profile.gender,
                address: profile.address,
                imageUrl: currentImageUrl
            };

            await updateProfile(updateData);
            alert("Cập nhật thông tin thành công!");
            navigate("/profile");
        } catch (err) {
            alert(getApiErrorMessage(err, "Cập nhật thất bại."));
        } finally {
            setSaving(false);
        }
    };

    if (loading) return <div className="min-h-screen flex justify-center items-center">Đang tải...</div>;

    const inputStyle = `w-full h-14 rounded-2xl border border-[#E5E5E5] bg-[#FAFAFA] px-5 outline-none focus:border-[#C89A63] focus:ring-4 focus:ring-[#C89A63]/20`;

    return (
        <div className="min-h-screen bg-gradient-to-br from-[#F8F4EF] to-[#EFE2D3]">
            {/* Header */}
            <header className="bg-white shadow-sm">
                <div className="max-w-7xl mx-auto h-20 flex items-center justify-between px-8">
                    <img src="/images/logo.jpg" alt="Logo" className="w-14" />
                    <img
                        src={previewImage}
                        alt="Avatar"
                        className="w-12 h-12 rounded-full object-cover border-2 border-[#C89A63]"
                    />
                </div>
            </header>

            {/* Content */}
            <div className="max-w-5xl mx-auto py-12 px-6">
                <div className="bg-white rounded-[32px] shadow-[0_20px_60px_rgba(0,0,0,.08)] p-10">
                    <h1 className="text-3xl font-bold text-[#5A3726] mb-10 text-center md:text-left">
                        Chỉnh sửa hồ sơ
                    </h1>

                    {/* Avatar Upload */}
                    <div className="flex justify-center mb-12">
                        <div className="relative">
                            <img
                                src={previewImage}
                                alt="Preview"
                                className="w-40 h-40 rounded-full object-cover border-4 border-[#C89A63]"
                            />
                            <input 
                                type="file" 
                                accept="image/*" 
                                className="hidden" 
                                ref={fileInputRef}
                                onChange={handleImageChange}
                            />
                            <button
                                onClick={() => fileInputRef.current.click()}
                                className="absolute bottom-2 right-2 w-12 h-12 rounded-full bg-[#C89A63] text-white flex items-center justify-center hover:bg-[#B78350] transition shadow-lg"
                                title="Thay đổi ảnh đại diện"
                            >
                                <FaCamera size={20} />
                            </button>
                        </div>
                    </div>

                    {/* Form */}
                    <div className="grid md:grid-cols-2 gap-6">
                        <div>
                            <label className="block mb-2 font-semibold text-[#5A3726]">Họ và tên</label>
                            <input
                                name="fullName"
                                value={profile.fullName}
                                onChange={handleChange}
                                className={inputStyle}
                                placeholder="Nhập họ và tên"
                            />
                        </div>

                        <div>
                            <label className="block mb-2 font-semibold text-[#5A3726]">Email</label>
                            <input
                                name="email"
                                type="email"
                                value={profile.email}
                                onChange={handleChange}
                                className={inputStyle}
                                placeholder="Nhập email"
                            />
                        </div>

                        <div>
                            <label className="block mb-2 font-semibold text-[#5A3726]">Số điện thoại</label>
                            <input
                                name="phoneNumber"
                                value={profile.phoneNumber}
                                onChange={handleChange}
                                className={inputStyle}
                                placeholder="Nhập số điện thoại"
                            />
                        </div>
                        
                        <div>
                            <label className="block mb-2 font-semibold text-[#5A3726]">Địa chỉ</label>
                            <input
                                name="address"
                                value={profile.address}
                                onChange={handleChange}
                                className={inputStyle}
                                placeholder="Nhập địa chỉ"
                            />
                        </div>

                        <div>
                            <label className="block mb-2 font-semibold text-[#5A3726]">Ngày sinh</label>
                            <input
                                name="dateOfBirth"
                                type="date"
                                value={profile.dateOfBirth}
                                onChange={handleChange}
                                className={inputStyle}
                            />
                        </div>

                        <div>
                            <label className="block mb-2 font-semibold text-[#5A3726]">Giới tính</label>
                            <select
                                name="gender"
                                value={profile.gender}
                                onChange={handleChange}
                                className={inputStyle}
                            >
                                <option value="MALE">Nam</option>
                                <option value="FEMALE">Nữ</option>
                            </select>
                        </div>
                    </div>

                    {/* Buttons */}
                    <div className="grid md:grid-cols-2 gap-5 mt-10">
                        <button
                            onClick={handleSave}
                            disabled={saving}
                            className="h-14 rounded-2xl bg-[#C89A63] hover:bg-[#B78350] text-white font-semibold transition disabled:opacity-70 disabled:cursor-not-allowed shadow-md"
                        >
                            {saving ? "Đang lưu..." : "Lưu thay đổi"}
                        </button>

                        <Link
                            to="/profile"
                            className="h-14 rounded-2xl border-2 border-[#C89A63] text-[#C89A63] font-semibold flex items-center justify-center hover:bg-[#FFF7EF] transition"
                        >
                            Hủy
                        </Link>
                    </div>
                </div>
            </div>
        </div>
    );
}