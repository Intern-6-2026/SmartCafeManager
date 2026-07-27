import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import coffeeBeans from "../assets/coffee-beans.jpg";
// Thêm addItemToCart vào đây
import {
  getLatestItems,
  getBestSellerItems,
  addItemToCart,
} from "../services/apiService";

function Body() {
  const navigate = useNavigate();
  const [latestItems, setLatestItems] = useState([]);
  const [bestSellerItems, setBestSellerItems] = useState([]);

  useEffect(() => {
    getLatestItems().then((res) => {
      console.log("Dữ liệu món mới:", res.data);
      setLatestItems(res.data || []);
    });
    getBestSellerItems().then((res) => {
      console.log("Dữ liệu bán chạy:", res.data);
      setBestSellerItems(res.data || []);
    });
  }, []);

  // --- CẬP NHẬT HÀM XỬ LÝ KHI BẤM VÀO MÓN ĂN ---
  const handleItemClick = async (item) => {
    const itemId = item.itemId || item.id;
    const tableId = 1; // Mặc định bàn số 1 theo quy ước của team backend

    try {
      // 1. Gọi API thêm vào giỏ hàng thật trên server
      await addItemToCart(tableId, itemId, 1, "");
      console.log("Đã thêm món vào giỏ hàng thành công!");
    } catch (error) {
      console.error("Lỗi khi thêm vào giỏ hàng:", error);
    }
    window.scrollTo(0, 0); // Cuộn lên đầu trang
    // 2. Chuyển hướng sang trang menu của bàn số 1
    navigate(`/menu/table/${tableId}`);
  };

  return (
    <main className="w-full">
      {/* 1. Phần Hero */}
      <section className="relative w-full h-[300px] flex items-center">
        <img
          src={coffeeBeans}
          alt="Coffee Beans"
          className="absolute inset-0 w-full h-full object-cover"
        />
        <div className="absolute inset-0 bg-black/40"></div>
        <div className="relative z-10 px-6 text-white font-['Inter']">
          <p className="text-[16px] font-normal">Chào mừng bạn,</p>
          <h1 className="text-[24px] font-bold my-2">
            Trải nghiệm cà phê thông minh
          </h1>
          <button
            onClick={() => navigate("/menu/table/1")}
            className="bg-white text-black px-6 py-2 rounded-full font-medium cursor-pointer hover:bg-gray-100"
          >
            Đặt món
          </button>
        </div>
      </section>

      {/* 2. Phần Món mới nhất */}
      <section className="container mx-auto my-8 overflow-hidden rounded-lg">
        <div className="bg-[#EBE2CB] p-6 flex flex-col items-center">
          <h2 className="text-2xl font-bold mb-4">Top 4 món mới nhất</h2>
          <button
            onClick={() => navigate("/menu/table/1")}
            className="bg-[#5C4D3F] text-white px-6 py-2 rounded-full font-medium cursor-pointer"
          >
            Khám phá
          </button>
        </div>

        <div className="bg-white p-6 grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          {latestItems.map((item) => (
            <div
              key={item.itemId || item.id}
              className="flex flex-col items-center text-center"
            >
              <img
                src={
                  item.imageUrl && item.imageUrl.trim() !== ""
                    ? item.imageUrl
                    : "https://via.placeholder.com/150"
                }
                alt={item.itemName}
                onClick={() => handleItemClick(item)}
                className="w-40 h-40 object-cover rounded-2xl mb-4 bg-gray-200 cursor-pointer hover:opacity-80 transition-opacity"
                onError={(e) => {
                  e.target.src = "https://via.placeholder.com/150";
                }}
              />
              <h3
                onClick={() => handleItemClick(item)}
                className="font-bold text-lg cursor-pointer hover:text-[#A4B435] transition-colors"
              >
                {item.itemName}
              </h3>
              <p className="text-[#A4B435] font-bold">
                {item.price ? `${item.price.toLocaleString()} vnđ` : "Liên hệ"}
              </p>
            </div>
          ))}
        </div>
      </section>

      {/* 3. Phần Món bán chạy nhất */}
      <section className="container mx-auto my-8 overflow-hidden rounded-lg">
        <div className="bg-[#EBE2CB] p-6 flex flex-col items-center">
          <h2 className="text-2xl font-bold mb-4">
            Top những món bán chạy nhất
          </h2>
          <button
            onClick={() => navigate("/menu/table/1")}
            className="bg-[#5C4D3F] text-white px-6 py-2 rounded-full font-medium cursor-pointer"
          >
            Khám phá
          </button>
        </div>

        <div className="bg-white p-6 grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          {bestSellerItems.map((item) => (
            <div
              key={item.itemId || item.id}
              className="flex flex-col items-center text-center"
            >
              <img
                src={
                  item.imageUrl && item.imageUrl.trim() !== ""
                    ? item.imageUrl
                    : "https://via.placeholder.com/150"
                }
                alt={item.itemName}
                onClick={() => handleItemClick(item)}
                className="w-40 h-40 object-cover rounded-2xl mb-4 bg-gray-200 cursor-pointer hover:opacity-80 transition-opacity"
                onError={(e) => {
                  e.target.src = "https://via.placeholder.com/150";
                }}
              />
              <h3
                onClick={() => handleItemClick(item)}
                className="font-bold text-lg cursor-pointer hover:text-[#A4B435] transition-colors"
              >
                {item.itemName}
              </h3>
              <p className="text-[#A4B435] font-bold">
                {item.price ? `${item.price.toLocaleString()} vnđ` : "Liên hệ"}
              </p>
            </div>
          ))}
        </div>
      </section>

      {/* 4. Footer Email */}
      <section className="bg-[#EBE2CB] p-8 md:p-12 rounded-xl w-full max-w-lg mx-auto my-10 text-center">
        <h2 className="text-2xl font-bold text-[#4a3f33] mb-3">
          Đăng kí để nhận voucher khuyến mãi 15%!
        </h2>
        <p className="text-[#70665b] mb-6">
          Đăng kí thành viên để nhận voucher khuyến mãi 15% cho lần mua sắm tiếp
          theo
        </p>
        <div className="flex flex-col gap-4">
          <div className="flex items-center w-full bg-white rounded-lg px-4 py-3 border border-gray-300 shadow-sm focus-within:ring-2 focus-within:ring-[#4a3f33]">
            <input
              type="email"
              placeholder="Địa chỉ email"
              className="flex-grow bg-transparent outline-none text-gray-700 placeholder-gray-500 px-2"
            />
          </div>
          <button className="w-full bg-[#3E2723] text-white py-3 rounded-lg font-bold text-lg hover:bg-[#5D4037] transition-all duration-300 shadow-md">
            Đăng kí
          </button>
        </div>
      </section>
    </main>
  );
}

export default Body;
