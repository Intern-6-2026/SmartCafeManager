import axios from "axios";

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

export const resetPassword = async (token, newPassword) => {
  return await axios.post(`${API_BASE_URL}/auth/reset-password`, { token, newPassword });
};

export const getCurrentUserProfile = async () => {
  return await axios.get(`${API_BASE_URL}/users/profile`);
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
  return await axios.post(`${API_BASE_URL}/users/profile/avatar`, formData, {
    headers: { "Content-Type": "multipart/form-data" },
  });
};

export const getAllItems = async () => {
  return await axios.get(`${API_BASE_URL}/items`);
};

export const getLatestItems = async () => {
  return await axios.get(`${API_BASE_URL}/items/latest`);
};

export const getBestSellerItems = async () => {
  return await axios.get(`${API_BASE_URL}/items/best-sellers`);
};

const CUSTOMER_URL = `${API_BASE_URL}/items`;

export const addItemToCart = async (tableName, itemId, quantity, note) => {
  const params = { tableName, itemId, quantity };
  if (note) params.note = note;
  return await axios.post(`${CUSTOMER_URL}/add-item`, null, { params });
};

export const getCart = async (tableName) => {
  return await axios.get(`${CUSTOMER_URL}/cart`, { params: { tableName } });
};

export const confirmOrder = async (tableName) => {
  return await axios.post(`${CUSTOMER_URL}/confirm-order`, null, {
    params: { tableName },
  });
};

export const getOrderHistory = async (tableName) => {
  return await axios.get(`${CUSTOMER_URL}/order-history`, {
    params: { tableName },
  });
};

export const getInvoice = async (tableName) => {
  return await axios.get(`${CUSTOMER_URL}/invoice`, { params: { tableName } });
};

export const requestCheckout = async (tableName, paymentMethod) => {
  return await axios.post(`${CUSTOMER_URL}/request-checkout`, null, {
    params: { tableName, paymentMethod },
  });
};

export const callService = async (tableName, status = "CALLING_WAITER") => {
  return await axios.post(`${CUSTOMER_URL}/call-service`, null, {
    params: { tableName, status },
  });
};

export const getApiErrorMessage = (err, fallback = "Đã có lỗi xảy ra.") => {
  return (
    err?.response?.data?.message ||
    err?.response?.data?.error ||
    (typeof err?.response?.data === "string" ? err.response.data : null) ||
    err?.message ||
    fallback
  );
};