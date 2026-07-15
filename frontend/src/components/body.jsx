import React, { useEffect, useState } from "react";
import coffeeBeans from "../assets/coffee-beans.jpg";
import { getLatestItems, getBestSellerItems } from "../services/apiService";

function Body() {
  const [latestItems, setLatestItems] = useState([]);
  const [bestSellerItems, setBestSellerItems] = useState([]);

  useEffect(() => {
    getLatestItems().then((res) => {
      console.log("Dữ liệu món mới:", res.data); // <--- Dòng này là chìa khóa
      setLatestItems(res.data);
    });
    getBestSellerItems().then((res) => {
      console.log("Dữ liệu bán chạy:", res.data); // <--- Dòng này là chìa khóa
      setBestSellerItems(res.data);
    });
  }, []);

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
          <button className="bg-white text-black px-6 py-2 rounded-full font-medium">
            Đặt món
          </button>
        </div>
      </section>

      {/* 2. Phần Món mới nhất - Đã thêm bg-[#EBE2CB] */}
      <section className="container mx-auto my-8 overflow-hidden rounded-lg">
        {/* Header */}
        <div className="bg-[#EBE2CB] p-6 flex flex-col items-center">
          <h2 className="text-2xl font-bold mb-4">Top 4 món mới nhất</h2>
          <button className="bg-[#5C4D3F] text-white px-6 py-2 rounded-full font-medium">
            Khám phá
          </button>
        </div>

        {/* Grid món ăn (Nền trắng) */}
        <div className="bg-white p-6 grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          {latestItems.map((item) => (
            <div
              key={item.itemId}
              className="flex flex-col items-center text-center"
            >
              <div className="w-40 h-40 bg-[#FDF5E6] rounded-2xl mb-4"></div>
              <h3 className="font-bold text-lg">{item.itemName}</h3>
              <p className="text-[#A4B435] font-bold">Giá: {item.price} vnd</p>
            </div>
          ))}
        </div>
      </section>

      {/* Phần Món bán chạy nhất (Tương tự, không border) */}
      <section className="container mx-auto my-8 overflow-hidden rounded-lg">
        <div className="bg-[#EBE2CB] p-6 flex flex-col items-center">
          <h2 className="text-2xl font-bold mb-4">
            Top những món bán chạy nhất
          </h2>
          <button className="bg-[#5C4D3F] text-white px-6 py-2 rounded-full font-medium">
            Khám phá
          </button>
        </div>
        <div className="bg-white p-6 grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          {bestSellerItems.map((item) => (
            <div
              key={item.itemId}
              className="flex flex-col items-center text-center"
            >
              <div className="w-40 h-40 bg-[#FDF5E6] rounded-2xl mb-4"></div>
              <h3 className="font-bold text-lg">{item.itemName}</h3>
              <p className="text-[#A4B435] font-bold">Giá: {item.price} vnd</p>
            </div>
          ))}
        </div>
      </section>

      <section className="bg-[#EBE2CB] p-8 md:p-12 rounded-xl w-full max-w-lg mx-auto my-10 text-center">
        <h2 className="text-2xl font-bold text-[#4a3f33] mb-3">
          Đăng kí để nhận voucher khuyến mãi 15%!
        </h2>
        <p className="text-[#70665b] mb-6">
          Đăng kí thành viên để nhận voucher khuyến mãi 15% cho lần mua sắm tiếp
          theo
        </p>

        {/* Chỉ cần để 2 thẻ này nằm cạnh nhau, không cần thẻ div bao ngoài nữa */}
        <div className="flex flex-col gap-4">
          {/* Input Email - Không còn vòng tròn bao quanh */}
          <div className="flex items-center w-full bg-white rounded-lg px-4 py-3 border border-gray-300 shadow-sm focus-within:ring-2 focus-within:ring-[#4a3f33]">
            <svg
              className="w-5 h-5 text-gray-400 mr-3"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth="2"
                d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"
              />
            </svg>
            <input
              type="email"
              placeholder="Địa chỉ email"
              className="flex-grow bg-transparent outline-none text-gray-700 placeholder-gray-500"
            />
          </div>

          {/* Nút Đăng kí - Đậm hơn và sáng rõ hơn */}
          <button className="w-full bg-[#3E2723] text-white py-3 rounded-lg font-bold text-lg hover:bg-[#5D4037] transition-all duration-300 shadow-md transform hover:scale-[1.02]">
            Đăng kí
          </button>
        </div>
      </section>
    </main>
  );
}

export default Body;
