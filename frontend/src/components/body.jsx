import React, { useEffect, useState } from "react";
import coffeeBeans from "../assets/coffee-beans.jpg";
import { getLatestItems, getBestSellerItems } from "../services/apiService";

function Body() {
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

      {/* 2. Phần Món mới nhất (Hiển thị tất cả) */}
      <section className="container mx-auto my-8 overflow-hidden rounded-lg">
        <div className="bg-[#EBE2CB] p-6 flex flex-col items-center">
          <h2 className="text-2xl font-bold mb-4">Các món mới nhất</h2>
          <button className="bg-[#5C4D3F] text-white px-6 py-2 rounded-full font-medium">
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
                className="w-40 h-40 object-cover rounded-2xl mb-4 bg-gray-200"
                onError={(e) => {
                  e.target.src = "https://via.placeholder.com/150";
                }}
              />
              <h3 className="font-bold text-lg">{item.itemName}</h3>
              <p className="text-[#A4B435] font-bold">
                {item.price ? `${item.price.toLocaleString()} vnđ` : "Liên hệ"}
              </p>
            </div>
          ))}
        </div>
      </section>

      {/* 3. Phần Món bán chạy nhất (Hiển thị tất cả) */}
      <section className="container mx-auto my-8 overflow-hidden rounded-lg">
        <div className="bg-[#EBE2CB] p-6 flex flex-col items-center">
          <h2 className="text-2xl font-bold mb-4">Các món bán chạy nhất</h2>
          <button className="bg-[#5C4D3F] text-white px-6 py-2 rounded-full font-medium">
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
                className="w-40 h-40 object-cover rounded-2xl mb-4 bg-gray-200"
                onError={(e) => {
                  e.target.src = "https://via.placeholder.com/150";
                }}
              />
              <h3 className="font-bold text-lg">{item.itemName}</h3>
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
        <div className="flex flex-col gap-4">
          <input
            type="email"
            placeholder="Địa chỉ email"
            className="w-full px-4 py-3 rounded-lg border border-gray-300"
          />
          <button className="w-full bg-[#3E2723] text-white py-3 rounded-lg font-bold">
            Đăng kí
          </button>
        </div>
      </section>
    </main>
  );
}

export default Body;
