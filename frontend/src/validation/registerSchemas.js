import * as Yup from "yup";

export const registerSchema = Yup.object({
  username: Yup.string()
    .trim()
    .min(3, "Tên đăng nhập tối thiểu 3 ký tự")
    .max(50, "Tên đăng nhập tối đa 50 ký tự")
    .required("Vui lòng nhập tên đăng nhập"),
  password: Yup.string()
    .min(6, "Mật khẩu tối thiểu 6 ký tự")
    .required("Vui lòng nhập mật khẩu"),
  confirmPassword: Yup.string()
    .oneOf([Yup.ref("password")], "Mật khẩu xác nhận không khớp")
    .required("Vui lòng xác nhận mật khẩu"),
  email: Yup.string().trim().email("Email không hợp lệ").required("Vui lòng nhập email"),
  fullName: Yup.string().trim().required("Vui lòng nhập họ tên"),
  phoneNumber: Yup.string()
    .trim()
    .matches(/^(0[0-9]{9,10})?$/, "Số điện thoại không hợp lệ")
    .nullable(),
});
