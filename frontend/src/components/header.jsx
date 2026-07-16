import React, { useState } from "react";
import { Menu, X } from "lucide-react";
import logo from "../assets/logo.svg";

function Header() {
  const [isOpen, setIsOpen] = useState(false);

  // Danh sách tính năng Khách hàng theo SRS[cite: 1]
  const menuItems = [
    { name: "Xem Menu" },
    { name: "Đặt món" },
    { name: "Món đã đặt" },
    { name: "Yêu cầu thanh toán" },
    { name: "Gọi phục vụ" },
    { name: "Đánh giá" },
    { name: "Chơi game" },
  ];

  return (
    <nav className="relative w-full bg-[#D2A97B] p-4 flex items-center justify-between shadow-md z-50">
      {/* Phần Logo bên trái */}
      <div className="flex items-center gap-2">
        <img src={logo} alt="Logo" className="w-10 h-10 object-contain" />
        <div className="text-[20px] font-['Inter']">
          <span className="font-bold text-[#000]">NEO</span>
          <span className="font-normal text-[#000]">CAFÉ</span>
        </div>
      </div>

      {/* Phần bên phải: Nút Đăng nhập + Hamburger */}
      <div className="flex items-center gap-4">
        {/* Nút Đăng nhập - Theo SRS thì Admin/Staff cần đăng nhập[cite: 1] */}
        <a
          href="/login"
          className="text-sm font-bold text-[#000] hover:underline whitespace-nowrap"
        >
          Đăng nhập
        </a>

        {/* Nút Hamburger */}
        <button onClick={() => setIsOpen(!isOpen)} className="text-[#000]">
          {isOpen ? <X size={28} /> : <Menu size={28} />}
        </button>
      </div>

      {/* Menu thả xuống */}
      {isOpen && (
        <div className="absolute top-full left-0 w-full bg-[#D2A97B] shadow-lg border-t border-[#c69c6d] flex flex-col items-center">
          {menuItems.map((item) => (
            <button
              key={item.name}
              className="w-full text-center py-4 text-[#000] font-medium border-b border-[#c69c6d] hover:bg-[#c69c6d] transition-all"
              onClick={() => setIsOpen(false)}
            >
              {item.name}
            </button>
          ))}
        </div>
      )}
    </nav>
  );
}

export default Header;
