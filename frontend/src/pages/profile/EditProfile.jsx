import { useState, useEffect, useRef, useMemo } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Formik, Form, Field } from "formik";
import { FaCamera } from "react-icons/fa";
import {
    getCurrentUserProfile,
    updateProfile,
    uploadAvatar,
    getApiErrorMessage,
} from "../../services/apiService";
import { editProfileSchema } from "../../validation/profileSchemas";
import Logo from "../../components/Logo";
import AvatarImage from "../../components/AvatarImage";
import Popup from "../../components/Popup";

const showFieldError = (errors, touched, submitCount, field) =>
    errors[field] && (touched[field] || submitCount > 0);

export default function EditProfile() {
    const [savedImageUrl, setSavedImageUrl] = useState("");
    const [previewImage, setPreviewImage] = useState("");
    const [avatarFile, setAvatarFile] = useState(null);
    const [originalPhone, setOriginalPhone] = useState("");
    const [initialValues, setInitialValues] = useState(null);
    const [loading, setLoading] = useState(true);
    const [popup, setPopup] = useState({ open: false, type: "info", title: "", message: "" });

    const fileInputRef = useRef(null);
    const previewUrlRef = useRef("");
    const navigate = useNavigate();

    const validationSchema = useMemo(
        () => editProfileSchema(originalPhone),
        [originalPhone]
    );

    useEffect(() => {
        return () => {
            if (previewUrlRef.current) {
                URL.revokeObjectURL(previewUrlRef.current);
            }
        };
    }, []);

    useEffect(() => {
        const fetchProfile = async () => {
            try {
                const res = await getCurrentUserProfile();
                const data = res.data;
                const phone = data.phone || "";

                setInitialValues({
                    fullName: data.fullName || "",
                    email: data.email || "",
                    phoneNumber: phone,
                    dateOfBirth: data.dateOfBirth ? data.dateOfBirth.split("T")[0] : "",
                    gender: data.gender || "MALE",
                    address: data.address || "",
                });
                setOriginalPhone(phone);
                setSavedImageUrl(data.imageUrl || "");
            } catch (err) {
                setPopup({
                    open: true,
                    type: "error",
                    title: "Lỗi",
                    message: getApiErrorMessage(err, "Không thể tải thông tin hồ sơ."),
                });
            } finally {
                setLoading(false);
            }
        };
        fetchProfile();
    }, []);

    const handleImageChange = (e) => {
        const file = e.target.files?.[0];
        if (!file) return;

        if (previewUrlRef.current) {
            URL.revokeObjectURL(previewUrlRef.current);
        }

        const objectUrl = URL.createObjectURL(file);
        previewUrlRef.current = objectUrl;
        setAvatarFile(file);
        setPreviewImage(objectUrl);
    };

    const handleSubmit = async (values, { setSubmitting, setFieldError }) => {
        try {
            let currentImageUrl = savedImageUrl;
            if (avatarFile) {
                const uploadRes = await uploadAvatar(avatarFile);
                currentImageUrl = uploadRes.data.imageUrl;
            }

            const updateData = {
                fullName: values.fullName.trim(),
                email: values.email.trim(),
                phoneNumber: values.phoneNumber.trim(),
                dateOfBirth: values.dateOfBirth || null,
                gender: values.gender,
                address: values.address?.trim() || "",
                imageUrl: currentImageUrl,
            };

            await updateProfile(updateData);
            navigate("/profile", { state: { successMsg: "Cập nhật thông tin thành công!" } });
        } catch (err) {
            const message = getApiErrorMessage(err, "Cập nhật thất bại.");
            if (message.toLowerCase().includes("phone") || message.includes("điện thoại")) {
                setFieldError("phoneNumber", message);
            }
            setPopup({ open: true, type: "error", title: "Cập nhật thất bại", message });
        } finally {
            setSubmitting(false);
        }
    };

    if (loading || !initialValues) {
        return <div className="min-h-screen flex justify-center items-center">Đang tải...</div>;
    }

    const inputStyle = (hasError) =>
        `w-full h-14 rounded-2xl border ${hasError ? "border-red-400" : "border-[#E5E5E5]"} bg-[#FAFAFA] px-5 outline-none focus:border-[#C89A63] focus:ring-4 focus:ring-[#C89A63]/20`;

    return (
        <div className="min-h-screen bg-gradient-to-br from-[#F8F4EF] to-[#EFE2D3]">
            <header className="bg-white shadow-sm">
                <div className="max-w-7xl mx-auto h-20 flex items-center justify-between px-8">
                    <Link to="/profile">
                        <Logo className="h-14 w-14" />
                    </Link>
                    <AvatarImage
                        src={savedImageUrl}
                        alt="Avatar"
                        className="w-12 h-12 rounded-full object-cover border-2 border-[#C89A63]"
                        size="sm"
                    />
                </div>
            </header>

            <div className="max-w-5xl mx-auto py-12 px-6">
                <div className="bg-white rounded-[32px] shadow-[0_20px_60px_rgba(0,0,0,.08)] p-10">
                    <h1 className="text-3xl font-bold text-[#5A3726] mb-10 text-center md:text-left">
                        Chỉnh sửa hồ sơ
                    </h1>

                    <Formik
                        initialValues={initialValues}
                        validationSchema={validationSchema}
                        validateOnChange={false}
                        validateOnBlur={false}
                        onSubmit={handleSubmit}
                    >
                        {({ isSubmitting, errors, touched, submitCount, validateForm, setTouched, values, setSubmitting, setFieldError }) => (
                            <Form
                                noValidate
                                onSubmit={async (e) => {
                                    e.preventDefault();
                                    const formErrors = await validateForm();
                                    if (Object.keys(formErrors).length > 0) {
                                        setTouched(
                                            Object.keys(formErrors).reduce(
                                                (acc, key) => ({ ...acc, [key]: true }),
                                                {}
                                            )
                                        );
                                        setPopup({
                                            open: true,
                                            type: "warning",
                                            title: "Vui lòng kiểm tra lại",
                                            message: Object.values(formErrors)[0],
                                        });
                                        return;
                                    }
                                    await handleSubmit(values, { setSubmitting, setFieldError });
                                }}
                            >
                                <div className="flex justify-center mb-12">
                                    <div className="relative">
                                        {avatarFile ? (
                                            <img
                                                src={previewImage}
                                                alt="Preview"
                                                className="w-40 h-40 rounded-full object-cover border-4 border-[#C89A63]"
                                            />
                                        ) : (
                                            <AvatarImage
                                                src={savedImageUrl}
                                                alt="Preview"
                                                className="w-40 h-40 rounded-full border-4 border-[#C89A63] object-cover"
                                                size="lg"
                                            />
                                        )}
                                        <input
                                            type="file"
                                            accept="image/*"
                                            className="hidden"
                                            ref={fileInputRef}
                                            onChange={handleImageChange}
                                        />
                                        <button
                                            type="button"
                                            onClick={() => fileInputRef.current?.click()}
                                            className="absolute bottom-2 right-2 w-12 h-12 rounded-full bg-[#C89A63] text-white flex items-center justify-center hover:bg-[#B78350] transition shadow-lg"
                                            title="Thay đổi ảnh đại diện"
                                        >
                                            <FaCamera size={20} />
                                        </button>
                                    </div>
                                </div>

                                <div className="grid md:grid-cols-2 gap-6">
                                    <div>
                                        <label className="block mb-2 font-semibold text-[#5A3726]">Họ và tên</label>
                                        <Field name="fullName" className={inputStyle(showFieldError(errors, touched, submitCount, "fullName"))} placeholder="Nhập họ và tên" />
                                        {showFieldError(errors, touched, submitCount, "fullName") && (
                                            <p className="text-red-500 text-sm mt-1">{errors.fullName}</p>
                                        )}
                                    </div>

                                    <div>
                                        <label className="block mb-2 font-semibold text-[#5A3726]">Email</label>
                                        <Field name="email" type="email" className={inputStyle(showFieldError(errors, touched, submitCount, "email"))} placeholder="Nhập email" />
                                        {showFieldError(errors, touched, submitCount, "email") && (
                                            <p className="text-red-500 text-sm mt-1">{errors.email}</p>
                                        )}
                                    </div>

                                    <div>
                                        <label className="block mb-2 font-semibold text-[#5A3726]">Số điện thoại</label>
                                        <Field name="phoneNumber" className={inputStyle(showFieldError(errors, touched, submitCount, "phoneNumber"))} placeholder="VD: 0901234567" />
                                        {showFieldError(errors, touched, submitCount, "phoneNumber") && (
                                            <p className="text-red-500 text-sm mt-1">{errors.phoneNumber}</p>
                                        )}
                                        {!originalPhone && (
                                            <p className="text-gray-400 text-sm mt-1">Bắt buộc nhập số điện thoại để lưu hồ sơ</p>
                                        )}
                                    </div>

                                    <div>
                                        <label className="block mb-2 font-semibold text-[#5A3726]">Địa chỉ</label>
                                        <Field name="address" className={inputStyle(showFieldError(errors, touched, submitCount, "address"))} placeholder="Nhập địa chỉ" />
                                        {showFieldError(errors, touched, submitCount, "address") && (
                                            <p className="text-red-500 text-sm mt-1">{errors.address}</p>
                                        )}
                                    </div>

                                    <div>
                                        <label className="block mb-2 font-semibold text-[#5A3726]">Ngày sinh</label>
                                        <Field name="dateOfBirth" type="date" className={inputStyle(false)} />
                                    </div>

                                    <div>
                                        <label className="block mb-2 font-semibold text-[#5A3726]">Giới tính</label>
                                        <Field as="select" name="gender" className={inputStyle(false)}>
                                            <option value="MALE">Nam</option>
                                            <option value="FEMALE">Nữ</option>
                                        </Field>
                                    </div>
                                </div>

                                <div className="grid md:grid-cols-2 gap-5 mt-10">
                                    <button
                                        type="submit"
                                        disabled={isSubmitting}
                                        className="h-14 rounded-2xl bg-[#C89A63] hover:bg-[#B78350] text-white font-semibold transition disabled:opacity-70 disabled:cursor-not-allowed shadow-md"
                                    >
                                        {isSubmitting ? "Đang lưu..." : "Lưu thay đổi"}
                                    </button>

                                    <Link
                                        to="/profile"
                                        className="h-14 rounded-2xl border-2 border-[#C89A63] text-[#C89A63] font-semibold flex items-center justify-center hover:bg-[#FFF7EF] transition"
                                    >
                                        Hủy
                                    </Link>
                                </div>
                            </Form>
                        )}
                    </Formik>
                </div>
            </div>

            <Popup
                open={popup.open}
                type={popup.type}
                title={popup.title}
                message={popup.message}
                onClose={() => {
                    setPopup((prev) => ({ ...prev, open: false }));
                    if (popup.type === "error" && popup.message.includes("Không thể tải")) {
                        navigate("/profile");
                    }
                }}
            />
        </div>
    );
}
