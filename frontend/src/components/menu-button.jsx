import React, { useEffect, useRef, useState } from "react";
import { Menu, X } from "lucide-react";
import { Link, useNavigate } from "react-router-dom";
import "../styles/menu-button.css"
/* Danh sách mục điều hướng + điều kiện hiển thị theo quyền */
const MENU_ITEMS = [
  { name: "Tin tức", path: "/news" },
  { name: "Thống kê thu nhập", path: "/admin/revenue", requireAuth: true, requireAdmin: true },
  { name: "Quản lý tin tức", path: "/admin/news", requireAuth: true, requireAdmin: true },
  { name: "Quản lý hóa đơn", path: "/admin/invoices", requireAuth: true, requireStaffOrAdmin: true },
  { name: "Quản lý món", path: "/admin/items", requireAuth: true, requireStaffOrAdmin: true },
  { name: "Quản lý bàn", path: "/sale-manager", requireAuth: true, requireStaffOrAdmin: true },
  { name: "Quản lý phản hồi", path: "/feedback-manager", requireAuth: true, requireStaffOrAdmin: true },
  { name: "Tin tức nhân viên", path: "/staff-news", requireAuth: true, requireStaffOrAdmin: true},
  { name: "Hồ sơ", path: "/profile", requireAuth: true },
];

/* Nút menu điều hướng (☰) + dropdown, dùng lại được ở mọi trang.
   Tự đọc trạng thái đăng nhập / quyền từ localStorage và tự lo đăng xuất.

   Props (đều tùy chọn):
   - className    : class thêm cho nút ☰ (đặt màu chữ cho khớp nền trang)
   - menuClassName: class thêm cho khối dropdown (vd đổi màu nền cho trang tối)
   - onNavigate   : callback gọi khi chọn 1 mục (vd đóng sidebar cha) */
function MenuButton({ className = "", menuClassName = "", onNavigate }) {
  const [isOpen, setIsOpen] = useState(false);
  const [isLoggedIn, setIsLoggedIn] = useState(() => Boolean(localStorage.getItem("token")));
  const [roleName, setRoleName] = useState(() =>
    (localStorage.getItem("roleName") || "").toUpperCase()
  );
  const wrapRef = useRef(null);
  const navigate = useNavigate();

  // Đồng bộ trạng thái đăng nhập khi localStorage đổi (đăng nhập/xuất ở tab khác)
  useEffect(() => {
    const syncAuth = () => {
      setIsLoggedIn(Boolean(localStorage.getItem("token")));
      setRoleName((localStorage.getItem("roleName") || "").toUpperCase());
    };
    syncAuth();
    window.addEventListener("storage", syncAuth);
    return () => window.removeEventListener("storage", syncAuth);
  }, []);

  // Bấm ra ngoài hoặc nhấn Esc thì đóng dropdown
  useEffect(() => {
    if (!isOpen) return;
    const onClickOutside = (e) => {
      if (wrapRef.current && !wrapRef.current.contains(e.target)) setIsOpen(false);
    };
    const onKey = (e) => e.key === "Escape" && setIsOpen(false);
    document.addEventListener("mousedown", onClickOutside);
    document.addEventListener("keydown", onKey);
    return () => {
      document.removeEventListener("mousedown", onClickOutside);
      document.removeEventListener("keydown", onKey);
    };
  }, [isOpen]);

  const isAdmin = roleName === "ADMIN";
  const isStaff = roleName === "STAFF";

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("roleName");
    localStorage.removeItem("userName");
    setIsLoggedIn(false);
    setRoleName("");
    setIsOpen(false);
    navigate("/");
  };

  const visibleItems = MENU_ITEMS.filter((item) => {
    if (item.requireAuth && !isLoggedIn) return false;
    if (item.requireAdmin && !isAdmin) return false;
    if (item.requireStaffOrAdmin && !isAdmin && !isStaff) return false;
    return true;
  });

  const handlePick = () => {
    setIsOpen(false);
    onNavigate?.();
  };

  return (
    <div className="menu-button-wrap" ref={wrapRef} style={{ position: "relative" }}>
      <button
        type="button"
        onClick={() => setIsOpen((v) => !v)}
        className={`menu-button-toggle ${className}`}
        aria-label="Mở menu điều hướng"
        aria-expanded={isOpen}
      >
        {isOpen ? <X size={28} /> : <Menu size={28} />}
      </button>

      {isOpen && (
        <div className={`menu-button-dropdown ${menuClassName}`}>
          {visibleItems.map((item) => (
            <Link
              key={item.name}
              to={item.path}
              className="menu-button-item"
              onClick={handlePick}
            >
              {item.name}
            </Link>
          ))}

          {isLoggedIn && (
            <button
              type="button"
              className="menu-button-item menu-button-logout"
              onClick={handleLogout}
            >
              Đăng xuất
            </button>
          )}
        </div>
      )}
    </div>
  );
}

export default MenuButton;