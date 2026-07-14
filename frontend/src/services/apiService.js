import axios from "axios";

// Đường dẫn cơ sở của Backend Spring Boot
const API_BASE_URL = "http://localhost:8080/api/v1";

// Gọi API lấy món mới nhất
export const getLatestItems = async () => {
  return await axios.get(`${API_BASE_URL}/items/latest`);
};

// Gọi API lấy món bán chạy nhất[cite: 2]
export const getBestSellerItems = async () => {
  return await axios.get(`${API_BASE_URL}/items/best-sellers`);
};

const CUSTOMER_URL = `${API_BASE_URL}/customer`;
 
// API 1: Thêm món vào giỏ tạm thời (note là tuỳ chọn)
export const addItemToCart = async (tableName, itemId, quantity, note) => {
  const params = { tableName, itemId, quantity };
  if (note) params.note = note;
  return await axios.post(`${CUSTOMER_URL}/add-item`, null, { params });
};
 
// API 2: Xem tất cả món trong giỏ hàng tạm thời (PENDING)
export const getCart = async (tableName) => {
  return await axios.get(`${CUSTOMER_URL}/cart`, { params: { tableName } });
};
 
// API 3: [GỌI MÓN] chốt đơn gửi xuống bếp (PENDING -> CONFIRMED)
export const confirmOrder = async (tableName) => {
  return await axios.post(`${CUSTOMER_URL}/confirm-order`, null, {
    params: { tableName },
  });
};
 
// API 4: Xem lịch sử các món đã gọi xuống bếp (CONFIRMED / SERVED / CANCELLED)
export const getOrderHistory = async (tableName) => {
  return await axios.get(`${CUSTOMER_URL}/order-history`, {
    params: { tableName },
  });
};
 
// API 5: Xem chi tiết tổng quan hóa đơn
export const getInvoice = async (tableName) => {
  return await axios.get(`${CUSTOMER_URL}/invoice`, { params: { tableName } });
};
 
// API 6: Yêu cầu thanh toán
// paymentMethod: "CASH" | "BANK_TRANSFER" | "MOMO" | "VNPAY" (bắt buộc VIẾT HOA)
export const requestCheckout = async (tableName, paymentMethod) => {
  return await axios.post(`${CUSTOMER_URL}/request-checkout`, null, {
    params: { tableName, paymentMethod },
  });
};
 
// API 7: Các yêu cầu dịch vụ khác (gọi nhân viên...)
// status: vd "CALLING_WAITER"
export const callService = async (tableName, status = "CALLING_WAITER") => {
  return await axios.post(`${CUSTOMER_URL}/call-service`, null, {
    params: { tableName, status },
  });
};
 
/* Helper: rút thông báo lỗi từ axios error để hiển thị lên UI */
export const getApiErrorMessage = (err, fallback = "Đã có lỗi xảy ra.") => {
  return (
    err?.response?.data?.message ||
    err?.response?.data?.error ||
    (typeof err?.response?.data === "string" ? err.response.data : null) ||
    err?.message ||
    fallback
  );
};