import axios from "axios";

// Đường dẫn cơ sở của Backend Spring Boot
const API_BASE_URL = "/api/v1";

axios.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("token");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

export const loginApi = async (username, password) => {
  return await axios.post(`${API_BASE_URL}/auth/login`, { username, password });
};

export const forgotPassword = async (email) => {
  return await axios.post(`${API_BASE_URL}/auth/forgot-password`, { email });
};

export const verifyOtp = async (token) => {
  return await axios.post(`${API_BASE_URL}/auth/verify-otp`, { token });
};

export const resetPassword = async (token, newPassword) => {
  return await axios.post(`${API_BASE_URL}/auth/reset-password`, { token, newPassword });
};

export const getCurrentUserProfile = async () => {
  return await axios.get(`${API_BASE_URL}/users/profile`);
};

export const checkPhoneAvailable = async (phoneNumber) => {
  return await axios.get(`${API_BASE_URL}/users/check-phone`, {
    params: { phoneNumber },
  });
};

export const updateProfile = async (profileData) => {
  return await axios.put(`${API_BASE_URL}/users/profile`, profileData);
};

export const changePassword = async (oldPassword, newPassword) => {
  return await axios.put(`${API_BASE_URL}/users/change-password`, { oldPassword, newPassword });
};

export const uploadAvatar = async (file) => {
  const formData = new FormData();
  formData.append("image", file);
  return await axios.post(`${API_BASE_URL}/users/profile/avatar`, formData);
};

// Gọi API lấy toàn bộ món ăn (menu đầy đủ, kèm danh mục + ảnh)
export const getAllItems = async () => {
  return await axios.get(`${API_BASE_URL}/items`);
};

export const getLatestItems = async () => {
  return await axios.get(`${API_BASE_URL}/items/latest`);
};

export const getBestSellerItems = async () => {
  return await axios.get(`${API_BASE_URL}/items/best-sellers`);
};

/* Các API gọi món tại bàn — theo tài liệu mới, nằm dưới /api/v1/items */
const CUSTOMER_URL = `${API_BASE_URL}/customer`;
 
// API 1: Thêm món vào giỏ tạm thời (note là tuỳ chọn)
export const addItemToCart = async (tableId, itemId, quantity, note) => {
  const params = { tableId, itemId, quantity, note };
  if (note) params.note = note;
  return await axios.post(`${CUSTOMER_URL}/cart/add`, params, { headers: { "Content-Type": "application/json", }, });
};
 
// API 2: Xem tất cả món trong giỏ hàng tạm thời (PENDING)
export const getCart = async (tableId) => {
  return await axios.get(`${CUSTOMER_URL}/cart/${tableId}`);
};
 
// API 3: [GỌI MÓN] chốt đơn gửi xuống bếp (PENDING -> CONFIRMED)
export const confirmOrder = async (tableId) => {
  return await axios.post(`${CUSTOMER_URL}/confirm-order`, null, {
    params: { tableId },
  });
};
 
// API 4: Xem lịch sử các món đã gọi xuống bếp (CONFIRMED / SERVED / CANCELLED)
export const getOrderHistory = async (tableId) => {
  return await axios.get(`${CUSTOMER_URL}/invoice-summary/${tableId}`);
};
 
// API 5: Xem chi tiết tổng quan hóa đơn
export const getInvoice = async (tableId, tableOrderId) => {
  return await axios.get(`${CUSTOMER_URL}/invoice-summary/${tableId}`);
};
  
// API 7: Các yêu cầu dịch vụ khác (gọi nhân viên...)
// status: vd "CALL_STAFF" 
export const callService = async (tableId, status = "CALL_STAFF") => {
  return await axios.post(`${CUSTOMER_URL}/call-service`, null, {
    params: { tableId, status },
  });
};

// API 8: Thay đổi số lượng món 
export const updateItemQuantity = async (tableId, itemId, note, newQuantity) => {
  return await axios.put(`${CUSTOMER_URL}/cart/items/${itemId}`, null, {
    params: { tableId, quantity: newQuantity },
  });
};

// API 9: Xóa món khỏi giỏ hàng
export const removeItem = async (tableId, itemId) => {
  return await axios.delete(`${CUSTOMER_URL}/cart/remove`, {
    params: { tableId, itemId },
  });
};

// API 10: Lấy QR code thanh toán 
export const getPaymentQRCode = async (tableId) => {
  return await axios.post(`${CUSTOMER_URL}/payment/paypal`, null, {
    params: { tableId },
  });
};

// API 11: thanh toán tiền mặt 
export const payWithCash = async (tableId) => {
  return await axios.post(`${CUSTOMER_URL}/payment/cash`, null, {
    params: { tableId },
  });
};

// API 11: thanh toán tiền mặt 
export const sentFeedback = async (content, rating, orderId, senderName, email, imageUrl, itemId) => {
  return await axios.post(`${CUSTOMER_URL}/feedbacks`, {
    content,
    rating,
    orderId,
    senderName,
    email,
    imageUrl,
    itemId
  }, 
  { headers: { "Content-Type": "application/json", }, });
};
// API cho nhân viên

// API 12: lấy thông tin tất cả các bàn 
export const getAllTableInfo = async () => {
  return await axios.get(`${API_BASE_URL}/staff/tables`);
};

// API 13: xác nhận thanh toán
export const approvePayment = async (tableId) => {
  return await axios.post(`${API_BASE_URL}/staff/tables/${tableId}/approve-payment`);
};

// API 14: lấy thông tin chi tiết hóa đơn của bàn
export const getTablesInvoice = async (tableId) => {
  return await axios.get(`${API_BASE_URL}/staff/tables/${tableId}/order-details`);
};

// API 15: lấy thông tin chi tiết các đơn còn active
export const getActiveOrder = async (tableId) => {
  return await axios.get(`${API_BASE_URL}/staff/tables/${tableId}/active-order`);
};

// API 16: lấy toan bộ feedbacks của khách hàng
export const getAllFeedbacks = async () => {
  return await axios.get(`${API_BASE_URL}/staff/feedbacks`);
};

// API 17: lấy feedbacks của một món cụ thể
export const getItemFeedbacks = async (itemId) => {
  return await axios.get(`${API_BASE_URL}/feedbacks/item/${itemId}`);
};

// API 18: xác nhận đơn hàng của khách (chuyển trạng thái từ PENDING -> CONFIRMED)
export const staffConfirmOrder = async (tableOrderId) => {
  return await axios.put(`${API_BASE_URL}/staff/orders/${tableOrderId}/confirm`);
};

/* Helper: rút thông báo lỗi từ axios error để hiển thị lên UI */
const ERROR_MESSAGE_MAP = {
  "Old password is incorrect!": "Mật khẩu hiện tại không đúng.",
  "New password cannot be the same as the old password!": "Mật khẩu mới không được trùng mật khẩu cũ.",
  "Account does not exist!": "Tài khoản không tồn tại.",
  "Invalid input data, please check again.": "Dữ liệu không hợp lệ, vui lòng kiểm tra lại.",
  "Mã OTP không hợp lệ.": "Mã OTP không hợp lệ.",
  "Mã OTP không hợp lệ": "Mã OTP không hợp lệ.",
  "Mã khôi phục đã hết hạn (quá 5 phút)!": "Mã OTP đã hết hạn (quá 5 phút). Vui lòng gửi lại mã mới.",
  "Mã khôi phục đã hết hạn(quá 5 phút)": "Mã OTP đã hết hạn (quá 5 phút). Vui lòng gửi lại mã mới.",
  "Phiên đổi mật khẩu không hợp lệ hoặc đã bị hủy.": "Phiên đổi mật khẩu không hợp lệ. Vui lòng xác thực OTP lại.",
  "Phiên đổi mật khẩu đã hết hạn (quá 5 phút)!": "Phiên đổi mật khẩu đã hết hạn. Vui lòng xác thực OTP lại.",
};

export const getApiErrorMessage = (err, fallback = "Đã có lỗi xảy ra.") => {
  const data = err?.response?.data;
  if (data?.validationErrors && typeof data.validationErrors === "object") {
    const fieldMessages = Object.values(data.validationErrors).filter(Boolean);
    if (fieldMessages.length > 0) {
      return fieldMessages.join(". ");
    }
  }
  const raw =
    data?.message ||
    data?.error ||
    (typeof data === "string" ? data : null) ||
    err?.message ||
    fallback;

  if (typeof raw === "string") {
    const technical =
      /JSON parse error|Cannot construct instance|HttpMessageNotReadable|nested exception|class com\./i.test(
        raw
      );
    if (technical) {
      return fallback;
    }
  }

  return ERROR_MESSAGE_MAP[raw] || raw;
};