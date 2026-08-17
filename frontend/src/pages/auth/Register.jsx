import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Formik, Form, Field, ErrorMessage } from "formik";
import { FaUser, FaLock, FaEnvelope, FaIdCard, FaPhone } from "react-icons/fa";
import AuthCard from "../../components/AuthCard";
import { registerApi, getApiErrorMessage } from "../../services/apiService";
import { registerSchema } from "../../validation/registerSchemas";
import { notifySuccess, notifyError } from "../../utils/toast";

/** Trang đăng ký khách hàng — trang mới, không thay Login cũ. */
export default function Register() {
  const navigate = useNavigate();
  const [serverError, setServerError] = useState("");

  return (
    <AuthCard title="Đăng ký" subtitle="Tạo tài khoản khách hàng NEOCAFÉ">
      {serverError && (
        <div className="mb-4 p-3 bg-red-100 text-red-600 rounded-xl text-sm">{serverError}</div>
      )}

      <Formik
        initialValues={{
          username: "",
          password: "",
          confirmPassword: "",
          email: "",
          fullName: "",
          phoneNumber: "",
        }}
        validationSchema={registerSchema}
        onSubmit={async (values, { setSubmitting }) => {
          setServerError("");
          try {
            const { confirmPassword, ...payload } = values;
            const res = await registerApi(payload);
            notifySuccess(res.data?.message || "Đăng ký thành công!");
            navigate("/", { replace: true });
          } catch (err) {
            const msg = getApiErrorMessage(err, "Đăng ký thất bại.");
            setServerError(msg);
            notifyError(msg);
          } finally {
            setSubmitting(false);
          }
        }}
      >
        {({ isSubmitting }) => (
          <Form className="space-y-4">
            <FieldBlock name="fullName" label="Họ và tên" icon={<FaIdCard />} placeholder="Nguyễn Văn A" />
            <FieldBlock name="username" label="Tên đăng nhập" icon={<FaUser />} placeholder="ten_dang_nhap" />
            <FieldBlock name="email" label="Email" icon={<FaEnvelope />} type="email" placeholder="email@gmail.com" />
            <FieldBlock name="phoneNumber" label="Số điện thoại (tuỳ chọn)" icon={<FaPhone />} placeholder="09xxxxxxxx" />
            <FieldBlock name="password" label="Mật khẩu" icon={<FaLock />} type="password" placeholder="Tối thiểu 6 ký tự" />
            <FieldBlock name="confirmPassword" label="Xác nhận mật khẩu" icon={<FaLock />} type="password" placeholder="Nhập lại mật khẩu" />

            <button
              type="submit"
              disabled={isSubmitting}
              className="w-full h-14 mt-4 rounded-2xl bg-[#C89A63] text-white text-lg font-semibold transition duration-300 hover:bg-[#B78350] hover:shadow-xl hover:scale-[1.02] active:scale-95 disabled:opacity-70"
            >
              {isSubmitting ? "Đang đăng ký..." : "Đăng ký"}
            </button>

            <p className="text-center text-sm text-gray-500 mt-4">
              Đã có tài khoản?{" "}
              <Link to="/" className="text-[#B78350] font-semibold hover:underline">
                Đăng nhập
              </Link>
            </p>
          </Form>
        )}
      </Formik>
    </AuthCard>
  );
}

function FieldBlock({ name, label, icon, type = "text", placeholder }) {
  return (
    <div>
      <label className="block mb-2 font-semibold text-[#5A3726]">{label}</label>
      <div className="relative">
        <span className="absolute left-5 top-1/2 -translate-y-1/2 text-gray-400">{icon}</span>
        <Field
          name={name}
          type={type}
          placeholder={placeholder}
          className="w-full h-14 rounded-2xl border border-[#E5E5E5] bg-[#FAFAFA] pl-14 pr-5 outline-none transition focus:bg-white focus:border-[#C89A63] focus:ring-4 focus:ring-[#C89A63]/20"
        />
      </div>
      <ErrorMessage name={name} component="div" className="text-red-500 text-sm mt-1" />
    </div>
  );
}
