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
