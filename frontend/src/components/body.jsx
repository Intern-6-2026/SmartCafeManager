import React, { useEffect, useState } from "react";
import coffeeBeans from "../assets/coffee-beans.jpg";
import { getLatestItems, getBestSellerItems } from "../services/apiService";
import { useNavigate } from "react-router-dom";
function Body() {
  const [latestItems, setLatestItems] = useState([]);
  const [bestSellerItems, setBestSellerItems] = useState([]);
  const navigate = useNavigate();

  const [showFloatingButton, setShowFloatingButton] = useState(false);
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

  // Thêm logic theo dõi cuộn trang
  useEffect(() => {
    const handleScroll = () => {
      // Nếu cuộn xuống quá 300px thì hiện nút, ngược lại thì ẩn
      if (window.scrollY > 300) {
        setShowFloatingButton(true);
      } else {
        setShowFloatingButton(false);
      }
    };

    window.addEventListener("scroll", handleScroll);
    return () => window.removeEventListener("scroll", handleScroll);
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
          <button
            onClick={() => navigate("/menu/table/TB001")}
            className="bg-white text-black px-6 py-2 rounded-full font-medium"
          >
            Đặt món
          </button>
        </div>
      </section>

      {/* 2. Phần Món mới nhất */}
      <section className="container mx-auto my-8 overflow-hidden rounded-lg">
        <div className="bg-[#EBE2CB] p-6 flex flex-col items-center">
          <h2 className="text-2xl font-bold mb-4">Top 4 món mới nhất</h2>
          <button className="bg-[#5C4D3F] text-white px-6 py-2 rounded-full font-medium">
            Khám phá
          </button>
        </div>

        <div className="bg-white p-6 grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          {latestItems.map((item) => (
            <div
              key={item.itemId || item.id}
              onClick={() => navigate("/menu/table/TB001")} // Thêm sự kiện này
              className="flex flex-col items-center text-center cursor-pointer hover:shadow-lg transition-all" // Thêm style để biết là có thể nhấn
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

      {/* 3. Phần Món bán chạy nhất */}
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
              key={item.itemId || item.id}
              onClick={() => navigate("/menu/table/TB001")} // Thêm sự kiện này
              className="flex flex-col items-center text-center cursor-pointer hover:shadow-lg transition-all" // Thêm style để biết là có thể nhấn
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
        <p className="text-[#70665b] mb-6">
          Đăng kí thành viên để nhận voucher khuyến mãi 15% cho lần mua sắm tiếp
          theo
        </p>
        <div className="flex flex-col gap-4">
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
          <button className="w-full bg-[#3E2723] text-white py-3 rounded-lg font-bold text-lg hover:bg-[#5D4037] transition-all duration-300 shadow-md transform hover:scale-[1.02]">
            Đăng kí
          </button>
        </div>
      </section>
      {showFloatingButton && (
        <button
          onClick={() => navigate("/menu/table/TB001")}
          className="fixed bottom-6 right-6 z-50 bg-[#5C4D3F] text-white p-4 rounded-full shadow-2xl hover:scale-105 transition-all duration-300 animate-bounce"
        >
          {/* Bạn có thể để chữ hoặc icon */}
          <span className="font-bold">Đặt món</span>
        </button>
      )}
    </main>
  );
}

export default Body;
