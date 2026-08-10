import { useState, useEffect, useCallback, useRef } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Formik, Form, Field } from "formik";
import { FaLock, FaEye, FaEyeSlash } from "react-icons/fa";
import { changePassword, getApiErrorMessage } from "../../services/apiService";
import { changePasswordSchema } from "../../validation/profileSchemas";
import Popup from "../../components/Popup";

const initialValues = {
    oldPassword: "",
    newPassword: "",
    confirmPassword: "",
};

const showFieldError = (errors, touched, submitCount, field) =>
    errors[field] && (touched[field] || submitCount > 0);

export default function ChangePassword() {
    const [showOld, setShowOld] = useState(false);
    const [showNew, setShowNew] = useState(false);
    const [showConfirm, setShowConfirm] = useState(false);
    const [popup, setPopup] = useState({ open: false, type: "info", title: "", message: "" });
    const navigate = useNavigate();

    const handlePopupClose = () => {
        const shouldLogout =
            popup.type === "success" ||
            popup.message.includes("Phiên đăng nhập") ||
            popup.message.includes("đăng nhập để");
        setPopup((prev) => ({ ...prev, open: false }));
        if (shouldLogout) {
            localStorage.clear();
            navigate("/");
        }
    };

    const showValidationPopup = useCallback((message) => {
        setPopup({
            open: true,
            type: "warning",
            title: "Vui lòng kiểm tra lại",
            message,
        });
    }, []);

    const inputStyle = (hasError) =>
        `w-full h-14 rounded-2xl border ${hasError ? "border-red-400" : "border-[#E5E5E5]"} bg-[#FAFAFA] pl-14 pr-14 outline-none focus:border-[#C89A63] focus:ring-4 focus:ring-[#C89A63]/20`;

    return (
        <div className="min-h-screen bg-gradient-to-br from-[#F8F4EF] to-[#EFE2D3] flex items-center justify-center p-6">
            <div className="bg-white rounded-[32px] shadow-[0_20px_60px_rgba(0,0,0,.08)] w-full max-w-xl p-10">
                <h1 className="text-3xl font-bold text-[#5A3726] text-center">Đổi mật khẩu</h1>
                <p className="text-center text-gray-400 mt-2 mb-10">Vui lòng nhập đầy đủ thông tin bên dưới.</p>

                <Formik
                    initialValues={initialValues}
                    validationSchema={changePasswordSchema}
                    validateOnChange={false}
                    validateOnBlur={false}
                    onSubmit={async (values, { setSubmitting, resetForm }) => {
                        if (!localStorage.getItem("token")) {
                            setPopup({
                                open: true,
                                type: "error",
                                title: "Chưa đăng nhập",
                                message: "Vui lòng đăng nhập để đổi mật khẩu.",
                            });
                            setSubmitting(false);
                            return;
                        }

                        setSubmitting(true);
                        try {
                            await changePassword(values.oldPassword, values.newPassword);
                            resetForm();
                            setPopup({
                                open: true,
                                type: "success",
                                title: "Thành công",
                                message: "Đổi mật khẩu thành công! Vui lòng đăng nhập lại.",
                            });
                        } catch (err) {
                            const status = err?.response?.status;
                            if (status === 401 || status === 403) {
                                setPopup({
                                    open: true,
                                    type: "error",
                                    title: "Phiên đăng nhập hết hạn",
                                    message: "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.",
                                });
                                return;
                            }
                            setPopup({
                                open: true,
                                type: "error",
                                title: "Đổi mật khẩu thất bại",
                                message: getApiErrorMessage(err, "Đổi mật khẩu thất bại."),
                            });
                        } finally {
                            setSubmitting(false);
                        }
                    }}
                >
                    {({ isSubmitting, errors, touched, submitCount }) => (
                        <ValidationPopupWatcher
                            submitCount={submitCount}
                            errors={errors}
                            onShow={showValidationPopup}
                        >
                            <Form noValidate>
                                <div className="mb-6">
                                    <label className="block mb-2 font-semibold text-[#5A3726]">Mật khẩu hiện tại</label>
                                    <div className="relative">
                                        <FaLock className="absolute left-5 top-1/2 -translate-y-1/2 text-gray-400" />
                                        <Field
                                            name="oldPassword"
                                            type={showOld ? "text" : "password"}
                                            className={inputStyle(showFieldError(errors, touched, submitCount, "oldPassword"))}
                                            placeholder="Nhập mật khẩu hiện tại"
                                        />
                                        <button type="button" onClick={() => setShowOld(!showOld)} className="absolute right-5 top-1/2 -translate-y-1/2 text-gray-400">
                                            {showOld ? <FaEyeSlash /> : <FaEye />}
                                        </button>
                                    </div>
                                    {showFieldError(errors, touched, submitCount, "oldPassword") && (
                                        <p className="text-red-500 text-sm mt-1">{errors.oldPassword}</p>
                                    )}
                                </div>

                                <div className="mb-6">
                                    <label className="block mb-2 font-semibold text-[#5A3726]">Mật khẩu mới</label>
                                    <div className="relative">
                                        <FaLock className="absolute left-5 top-1/2 -translate-y-1/2 text-gray-400" />
                                        <Field
                                            name="newPassword"
                                            type={showNew ? "text" : "password"}
                                            className={inputStyle(showFieldError(errors, touched, submitCount, "newPassword"))}
                                            placeholder="Nhập mật khẩu mới (tối thiểu 6 ký tự)"
                                        />
                                        <button type="button" onClick={() => setShowNew(!showNew)} className="absolute right-5 top-1/2 -translate-y-1/2 text-gray-400">
                                            {showNew ? <FaEyeSlash /> : <FaEye />}
                                        </button>
                                    </div>
                                    {showFieldError(errors, touched, submitCount, "newPassword") && (
                                        <p className="text-red-500 text-sm mt-1">{errors.newPassword}</p>
                                    )}
                                </div>

                                <div className="mb-6">
                                    <label className="block mb-2 font-semibold text-[#5A3726]">Xác nhận mật khẩu</label>
                                    <div className="relative">
                                        <FaLock className="absolute left-5 top-1/2 -translate-y-1/2 text-gray-400" />
                                        <Field
                                            name="confirmPassword"
                                            type={showConfirm ? "text" : "password"}
                                            className={inputStyle(showFieldError(errors, touched, submitCount, "confirmPassword"))}
                                            placeholder="Nhập lại mật khẩu mới"
                                        />
                                        <button type="button" onClick={() => setShowConfirm(!showConfirm)} className="absolute right-5 top-1/2 -translate-y-1/2 text-gray-400">
                                            {showConfirm ? <FaEyeSlash /> : <FaEye />}
                                        </button>
                                    </div>
                                    {showFieldError(errors, touched, submitCount, "confirmPassword") && (
                                        <p className="text-red-500 text-sm mt-1">{errors.confirmPassword}</p>
                                    )}
                                </div>

                                <div className="grid grid-cols-2 gap-5 mt-8">
                                    <button
                                        type="submit"
                                        disabled={isSubmitting}
                                        className="h-14 rounded-2xl bg-[#C89A63] text-white font-semibold hover:bg-[#B78350] transition disabled:opacity-70"
                                    >
                                        {isSubmitting ? "Đang lưu..." : "Cập nhật"}
                                    </button>
                                    <Link to="/profile" className="h-14 rounded-2xl border-2 border-[#C89A63] text-[#C89A63] flex items-center justify-center font-semibold hover:bg-[#FFF7EF]">
                                        Hủy
                                    </Link>
                                </div>
                            </Form>
                        </ValidationPopupWatcher>
                    )}
                </Formik>
            </div>

            <Popup
                open={popup.open}
                type={popup.type}
                title={popup.title}
                message={popup.message}
                onClose={handlePopupClose}
            />
        </div>
    );
}

function ValidationPopupWatcher({ submitCount, errors, onShow, children }) {
    const lastShownSubmit = useRef(0);

    useEffect(() => {
        if (submitCount > lastShownSubmit.current && Object.keys(errors).length > 0) {
            lastShownSubmit.current = submitCount;
            onShow(Object.values(errors)[0]);
        }
    }, [submitCount, errors, onShow]);

    return children;
}
