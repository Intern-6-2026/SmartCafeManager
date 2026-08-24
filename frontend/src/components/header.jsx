import React, { useEffect, useState } from "react";
import { Menu, X } from "lucide-react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import Logo from "./Logo";

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
  const location = useLocation();

  useEffect(() => {
    const syncAuth = () => {
      setIsLoggedIn(Boolean(localStorage.getItem("token")));
      setUserName(localStorage.getItem("userName") || "");
      setRoleName((localStorage.getItem("roleName") || "").toUpperCase());
    };
    syncAuth();
    window.addEventListener("storage", syncAuth);
    window.addEventListener("focus", syncAuth);
    return () => {
      window.removeEventListener("storage", syncAuth);
      window.removeEventListener("focus", syncAuth);
    };
  }, [location.pathname]);

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
  const canManageItems = isAdmin || isStaff;

  const menuItems = [
    { name: "Trang chủ", path: "/home" },
    { name: "Xem Menu", path: "/menu/table/1" },
    { name: "Đặt món", path: "/menu/table/1" },
    { name: "Tin tức", path: "/news" },
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
    {
      name: "Quản lý hóa đơn",
      path: "/admin/invoices",
      requireAuth: true,
      requireStaffOrAdmin: true,
    },
    {
      name: "Quản lý bàn",
      path: "/sale-manager",
      requireAuth: true,
      requireStaffOrAdmin: true,
    },
    {
      name: "Quản lý phản hồi",
      path: "/feedback-manager",
      requireAuth: true,
      requireStaffOrAdmin: true,
    },
    {
      name: "Tin tức nhân viên",
      path: "/staff-news",
      requireAuth: true,
      requireStaffOrAdmin: true,
    },
    {
      name: "Quản lý món",
      path: "/admin/items",
      requireAuth: true,
      requireManageItems: true,
    },
    { name: "Hồ sơ", path: "/profile", requireAuth: true },
  ];

  return (
    <nav className="relative w-full bg-[#D2A97B] p-4 flex items-center justify-between shadow-md z-50">
      <Link to="/home" className="flex items-center gap-2 cursor-pointer">
        <Logo className="h-10 w-10" />
        <div className="text-[20px] font-['Inter']">
          <span className="font-bold text-[#000]">NEO</span>
          <span className="font-normal text-[#000]">CAFÉ</span>
        </div>
      </Link>

      <div className="flex items-center gap-4">
        {isLoggedIn ? (
          <>
            <Link
              to="/profile"
              className="text-sm font-bold text-[#000] hover:underline max-w-[140px] truncate"
              title={userName}
            >
              {userName || "Tài khoản"}
            </Link>
            <button
              type="button"
              onClick={handleLogout}
              className="text-sm font-bold text-[#5A3726] hover:underline"
            >
              Đăng xuất
            </button>
          </>
        ) : (
          <Link to="/" className="text-sm font-bold text-[#000] hover:underline">
            Đăng nhập
          </Link>
        )}
        <button type="button" onClick={() => setIsOpen(!isOpen)} className="text-[#000]" aria-label="Mở menu">
          {isOpen ? <X size={28} /> : <Menu size={28} />}
        </button>
      </div>

      {isOpen && (
        <div className="absolute top-full left-0 w-full bg-[#D2A97B] shadow-lg border-t border-[#c69c6d] flex flex-col items-center">
          {menuItems
            .filter((item) => {
              if (item.requireAuth && !isLoggedIn) return false;
              if (item.requireAdmin && !isAdmin) return false;
              if (item.requireStaffOrAdmin && !isAdmin && !isStaff) return false;
              if (item.requireManageItems && !canManageItems) return false;
              return true;
            })
            .map((item) => (
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
