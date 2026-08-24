import React, { useEffect, useState } from "react";
import { Menu, X } from "lucide-react";
import { Link, useNavigate } from "react-router-dom";
import Logo from "./Logo";
import MenuButton from "./menu-button";

function Header() {
  const [isOpen, setIsOpen] = useState(false);
  const [userName, setUserName] = useState(
    () => localStorage.getItem("userName") || "",
  );
  const [isLoggedIn, setIsLoggedIn] = useState(() =>
    Boolean(localStorage.getItem("token")),
  );
  const [roleName, setRoleName] = useState(() =>
    (localStorage.getItem("roleName") || "").toUpperCase(),
  );
  const navigate = useNavigate();

  useEffect(() => {
    const syncAuth = () => {
      setIsLoggedIn(Boolean(localStorage.getItem("token")));
      setUserName(localStorage.getItem("userName") || "");
      setRoleName((localStorage.getItem("roleName") || "").toUpperCase());
    };
    syncAuth();
    window.addEventListener("storage", syncAuth);
    return () => window.removeEventListener("storage", syncAuth);
  }, []);

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("roleName");
    localStorage.removeItem("userName");
    setIsLoggedIn(false);
    setUserName("");
    setRoleName("");
    setIsOpen(false);
    navigate("/");
  };

  const isAdmin = roleName === "ADMIN";
  const isStaff = roleName === "STAFF";

  const menuItems = [
    { name: "Trang chủ", path: "/home" },
    { name: "Xem Menu", path: "/menu/table/1" },
    { name: "Đặt món", path: "/menu/table/1" },
    { name: "Tin tức", path: "/news" },
    // Dành riêng cho Admin: Quản lý thu nhập & Quản lý tin tức
    {
      name: "Thống kê thu nhập",
      path: "/admin/revenue",
      requireAuth: true,
      requireAdmin: true,
    },
    {
      name: "Quản lý tin tức",
      path: "/admin/news",
      requireAuth: true,
      requireAdmin: true,
    },
    // Dành riêng cho Staff & Admin: Quản lý hóa đơn
    {
      name: "Quản lý hóa đơn",
      path: "/admin/invoices",
      requireAuth: true,
      requireStaffOrAdmin: true,
    },
    { name: "Hồ sơ", path: "/profile", requireAuth: true },
  ];

  return (
    <nav className="relative w-full bg-[#D2A97B] p-4 flex items-center justify-between shadow-md z-50">
      {/* Bấm vào Logo luôn luôn về trang chủ /home */}
      <Link to="/home" className="flex items-center gap-2 cursor-pointer">
        <Logo className="h-10 w-10" />
        <div className="text-[20px] font-['Inter']">
          <span className="font-bold text-[#000]">NEO</span>
          <span className="font-normal text-[#000]">CAFÉ</span>
        </div>
      </Link>

      <div className="header-title">MÀN HÌNH QUẢN LÝ TIN TỨC</div>
      
      <MenuButton></MenuButton>
    </nav>
  );
}

export default Header;