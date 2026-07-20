import React, { useState } from "react";
import { Menu, X } from "lucide-react";
import { Link } from "react-router-dom"; // Nhớ import Link
import Logo from "./Logo";

function Header() {
  const [isOpen, setIsOpen] = useState(false);

  // Định nghĩa menu khớp với các route bạn đã có
  const menuItems = [
    { name: "Xem Menu", path: "/menu/table/ban01" }, // Tạm để TB001 làm ví dụ
    { name: "Đặt món", path: "/menu/table/ban01" },
    { name: "Món đã đặt", path: "/" }, // Bạn cần bổ sung route này sau
    { name: "Yêu cầu thanh toán", path: "/" },
    { name: "Gọi phục vụ", path: "/" },
    { name: "Đánh giá", path: "/" },
    { name: "Chơi game", path: "/" },
  ];

  return (
    <nav className="relative w-full bg-[#D2A97B] p-4 flex items-center justify-between shadow-md z-50">
      <div className="flex items-center gap-2">
        <Logo className="h-10 w-10" />
        <div className="text-[20px] font-['Inter']">
          <span className="font-bold text-[#000]">NEO</span>
          <span className="font-normal text-[#000]">CAFÉ</span>
        </div>
      </div>

      <div className="flex items-center gap-4">
        {/* Link tới Login (vì AppRoutes để path="/" là Login) */}
        <Link to="/" className="text-sm font-bold text-[#000] hover:underline">
          Đăng nhập
        </Link>
        <button onClick={() => setIsOpen(!isOpen)} className="text-[#000]">
          {isOpen ? <X size={28} /> : <Menu size={28} />}
        </button>
      </div>

      {isOpen && (
        <div className="absolute top-full left-0 w-full bg-[#D2A97B] shadow-lg border-t border-[#c69c6d] flex flex-col items-center">
          {menuItems.map((item) => (
            <Link
              key={item.name}
              to={item.path}
              className="w-full text-center py-4 text-[#000] font-medium border-b border-[#c69c6d] hover:bg-[#c69c6d] transition-all"
              onClick={() => setIsOpen(false)}
            >
              {item.name}
            </Link>
          ))}
        </div>
      )}
    </nav>
  );
}

export default Header;
