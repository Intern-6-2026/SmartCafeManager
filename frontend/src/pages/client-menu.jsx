import React, { useCallback, useEffect, useMemo, useState } from "react";
import "../styles/client-menu.css";
import AddDrinkModal from "../components/add-drink";
import FeedbackModal from "../components/feedback";
import { useParams } from "react-router-dom";
import {
  addItemToCart,
  getCart,
  confirmOrder,
  requestCheckout,
  callService,
  getApiErrorMessage,
} from "../services/apiService";

/* hard coded data (hiển thị menu — backend chưa có API lấy menu theo danh mục) */
const CATEGORIES = ["Coffee", "Trà", "Nước Ép", "Đá xay", "Bánh"];

const MENU_ITEMS = [
  { id: 1, name: "Cà phê đen", price: 25000, wait: "5:00", category: "Coffee", img: "/images/iced-black-coffee.png" },
  { id: 2, name: "Cà phê sữa", price: 30000, wait: "5:00", category: "Coffee", img: "/images/cafe-sua-da.png" },
  { id: 3, name: "Cà phê hạt dẻ", price: 35000, wait: "6:00", category: "Coffee", img: "/images/cà phê hạt dẻ.png" },
  { id: 4, name: "Cà phê muối", price: 35000, wait: "6:00", category: "Coffee", img: "/images/cà_phê_muối.png" },
  { id: 5, name: "Bạc xỉu", price: 30000, wait: "5:00", category: "Coffee", img: "/images/Bạc_xỉu.png" },
  { id: 6, name: "Cappuccino", price: 35000, wait: "7:00", category: "Coffee", img: "/images/cappuccino.png" },
  { id: 7, name: "Trà đào cam sả", price: 30000, wait: "5:00", category: "Trà", img: "/images/tra_dao_cam_sa.png" },
  { id: 8, name: "Trà vải", price: 30000, wait: "5:00", category: "Trà", img: "/images/tra_vai.png" },
  { id: 9, name: "Trà sữa trân châu", price: 35000, wait: "6:00", category: "Trà", img: "/images/tra_sua_tran_chau.png" },
  { id: 10, name: "Trà sữa matcha", price: 35000, wait: "6:00", category: "Trà", img: "/images/tra_sua_matcha.png" },
  { id: 11, name: "Nước ép cam", price: 30000, wait: "5:00", category: "Nước Ép", img: "/images/nuoc_ep_cam.png" },
  { id: 12, name: "Nước ép dứa", price: 30000, wait: "5:00", category: "Nước Ép", img: "/images/nuoc_ep_dua.png"}
];

const fmt = (n) => new Intl.NumberFormat("vi-VN").format(n) + "đ";

function ClientMenu() {
  /* Route: /menu/table/:tableId — tableId chính là tableName gửi lên API (vd: Ban01) */
  const { tableId } = useParams();
  const tableName = tableId;

  const [menuOpen, setMenuOpen] = useState(false);
  const [category, setCategory] = useState("Coffee");
  const [cart, setCart] = useState([]); // giỏ tạm PENDING lấy từ server
  const [selectedItem, setSelectedItem] = useState(null); // món đang mở modal Thêm món
  const [feedbackOpen, setFeedbackOpen] = useState(false); // modal Phản hồi
  const [paymentMethod, setPaymentMethod] = useState("CASH"); // enum backend
  const [message, setMessage] = useState(""); // thông báo kết quả API
  const [loading, setLoading] = useState(false);

  const notify = (msg) => {
    setMessage(String(msg));
    setTimeout(() => setMessage(""), 4000);
  };

  /* Lọc món theo Service Type */
  const filtered = useMemo(
    () => MENU_ITEMS.filter((m) => m.category === category),
    [category]
  );

  /* ===== API 2: Xem giỏ hàng tạm thời (PENDING) ===== */
  const loadCart = useCallback(async () => {
    try {
      const res = await getCart(tableName);
      setCart(Array.isArray(res.data) ? res.data : []);
    } catch (err) {
      // 500 "Bàn hiện tại không có hóa đơn nào đang mở!" => giỏ trống, không phải lỗi thật
      if (err?.response?.status === 500) {
        setCart([]);
      } else {
        setCart([]);
        notify(getApiErrorMessage(err, "Không kết nối được máy chủ."));
      }
    }
  }, [tableName]);

  useEffect(() => {
    loadCart();
  }, [loadCart]);

  /* Giỏ hàng: map dữ liệu server sang hàng hiển thị */
  const cartRows = cart.map((c) => {
    const local = MENU_ITEMS.find((m) => m.id === c.item?.itemId);
    return {
      orderDetailId: c.orderDetailId,
      name: c.item?.itemName ?? "—",
      price: c.unitPrice,
      qty: c.quantity,
      note: c.note,
      wait: local?.wait ?? "—",
    };
  });
  const total = cartRows.reduce((s, r) => s + r.price * r.qty, 0);

  /* ===== API 1: Thêm món vào giỏ tạm — bấm "Thêm" trong modal ===== */
  const confirmAddItem = async (item, qty, note) => {
    setLoading(true);
    try {
      const res = await addItemToCart(tableName, item.id, qty, note);
      notify(res.data);
      await loadCart();
    } catch (err) {
      notify(getApiErrorMessage(err, "Thêm món thất bại."));
    } finally {
      setLoading(false);
      setSelectedItem(null);
    }
  };

  /* ===== API 3: [GỌI MÓN] chốt đơn gửi bếp ===== */
  const handleGoiMon = async () => {
    setLoading(true);
    try {
      const res = await confirmOrder(tableName);
      notify(res.data);
      await loadCart(); // giỏ tạm sẽ trống sau khi chốt
    } catch (err) {
      notify(getApiErrorMessage(err, "Gọi món thất bại."));
    } finally {
      setLoading(false);
    }
  };

  /* ===== API 6: Yêu cầu thanh toán ===== */
  const handleThanhToan = async () => {
    setLoading(true);
    try {
      const res = await requestCheckout(tableName, paymentMethod);
      notify(res.data);
    } catch (err) {
      notify(getApiErrorMessage(err, "Gửi yêu cầu thanh toán thất bại."));
    } finally {
      setLoading(false);
    }
  };

  /* ===== API 7: Gọi nhân viên ===== */
  const handleGoiNhanVien = async () => {
    setLoading(true);
    try {
      const res = await callService(tableName, "CALLING_WAITER");
      notify(res.data);
    } catch (err) {
      notify(getApiErrorMessage(err, "Gọi nhân viên thất bại."));
    } finally {
      setLoading(false);
    }
  };

  /* Backend hiện KHÔNG có API sửa/xoá món trong giỏ tạm,
     nên các nút này chỉ hiển thị thông báo. */
  const changeQty = () =>
    notify("Backend chưa hỗ trợ sửa số lượng. Hãy thêm món để cộng dồn.");
  const removeItem = () =>
    notify("Backend chưa hỗ trợ xoá món khỏi giỏ tạm.");

  /* Bấm "Gửi" trong modal Phản hồi (chưa có API phản hồi trong tài liệu) */
  const submitFeedback = (data) => {
    console.log("Phản hồi:", data);
    notify("Cảm ơn bạn đã gửi phản hồi!");
    setFeedbackOpen(false);
  };

  const pickCategory = (c) => {
    setCategory(c);
    setMenuOpen(false);
  };

  return (
    <div className="client-menu">
      <header>
        <div className="header-row">
          <div className="brand">
            <img src="/images/logo.jpg" alt="Logo" className="brand-logo" />
            <h1 className="brand-name-bold">NEO</h1>
            <h1 className="brand-name-not-bold">CAFÉ</h1>
          </div>
          <div className="header-title">THÔNG TIN BÀN</div>
          {/* Nút ≡ mở panel Service Type */}
          <button
            className="menu-btn"
            aria-label="Mở danh sách loại dịch vụ"
            aria-expanded={menuOpen}
            onClick={() => setMenuOpen((v) => !v)}
          >
            <span className="menu-bar" />
            <span className="menu-bar" />
            <span className="menu-bar" />
          </button>
        </div>
      </header>

      {/* Types of Drinks*/}
      <div
        className={`drawer-overlay ${menuOpen ? "show" : ""}`}
        onClick={() => setMenuOpen(false)}
      />
      <nav className={`category-nav ${menuOpen ? "open" : ""}`} aria-label="Loại dịch vụ">
        {CATEGORIES.map((c) => (
          <button
            key={c}
            className={`category-btn ${category === c ? "active" : ""}`}
            onClick={() => pickCategory(c)}
          >
            {c}
          </button>
        ))}
      </nav>

      {/* Thông báo kết quả API */}
      {message && <div className="api-message" role="status">{message}</div>}

      <main>
        <div className="main-content">
          <div className="menu">
            <div className="grid-menu">
              {filtered.map((m) => (
                <button
                  className="card"
                  key={m.id}
                  onClick={() => setSelectedItem(m)}
                  aria-label={`Chọn ${m.name}`}
                >
                  <div className="card-img">
                    <img src={m.img} alt={m.name} className="item-img" />
                  </div>
                  <div className="card-name">{m.name}</div>
                  <div className="card-price">{fmt(m.price)}</div>
                </button>
              ))}
              {filtered.length === 0 && (
                <div className="menu-empty">Chưa có món nào trong mục {category}.</div>
              )}
            </div>
          </div>

          {/* Chi tiết đơn hàng (giỏ tạm PENDING từ server) */}
          <section className="order-detail">
            <h3>Bàn số: {tableId}</h3>
            <div className="order-header">
              <span className="order-header-title">Tên món</span>
              <span className="order-header-title">Thời gian chờ</span>
              <span className="order-header-title">Giá</span>
            </div>

            <div className="order-list">
              {cartRows.length === 0 && (
                <div className="order-empty">
                  Chưa có món nào.
                </div>
              )}
              {cartRows.map((r) => (
                <div className="order-row" key={r.orderDetailId}>
                  <button
                    className="remove-btn"
                    aria-label={`Xoá ${r.name}`}
                    onClick={removeItem}
                  >
                    <img src="/images/Icon Remove.png" alt="" className="remove-icon" />
                  </button>
                  <div className="order-info">
                    <div className="order-top">
                      <span className="order-name">{r.name}</span>
                      <span className="order-wait">{r.wait}</span>
                      <span className="order-price">{fmt(r.price)}</span>
                    </div>
                    {r.note && <div className="order-item-note">Ghi chú: {r.note}</div>}
                    <div className="order-bottom">
                      <div className="order-qty">
                        <button
                          className="qty-btn"
                          aria-label={`Giảm số lượng ${r.name}`}
                          onClick={changeQty}
                        >
                          −
                        </button>
                        <span className="qty-value">{r.qty}</span>
                        <button
                          className="qty-btn"
                          aria-label={`Tăng số lượng ${r.name}`}
                          onClick={changeQty}
                        >
                          +
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>

            <div className="order-total">
              <span className="total-label">Tổng tiền</span>
              <span className="total-value">{fmt(total)}</span>
            </div>

            {/* Phương thức thanh toán — enum backend yêu cầu viết hoa */}
            <div className="payment-method">
              <label htmlFor="payment-select" className="payment-label">
                Phương thức thanh toán
              </label>
              <select
                id="payment-select"
                className="payment-select"
                value={paymentMethod}
                onChange={(e) => setPaymentMethod(e.target.value)}
              >
                <option value="CASH">Tiền mặt</option>
                <option value="BANK_TRANSFER">Chuyển khoản ngân hàng</option>
                <option value="MOMO">Ví MoMo</option>
                <option value="VNPAY">VNPay</option>
              </select>
            </div>

            <div className="order-actions">
              <button className="btn-goimon" onClick={handleGoiMon} disabled={loading}>
                Gọi món
              </button>
              <button className="btn-thanhtoan" onClick={handleThanhToan} disabled={loading}>
                Thanh toán
              </button>
              <button className="btn-goinhanvien" onClick={handleGoiNhanVien} disabled={loading}>
                Gọi nhân viên
              </button>
              <button className="btn-feedback" onClick={() => setFeedbackOpen(true)}>
                Phản hồi
              </button>
            </div>
          </section>
        </div>

        <footer>
          <img src="/images/logo.jpg" alt="Logo" className="brand-logo" />
          <p className="contact-infor">
            Chấp nhận : Visa, MasterCard, Vouchers <br />
            Phí giao dịch áp dụng cho thẻ tín dụng <br />
            Hotline/Số điện thoại: 19001900 <br />
            Địa chỉ quán: Số 1 đường Võ Văn Ngân, phường Thủ Đức, thành phố Hồ Chí Minh
          </p>
        </footer>
      </main>

      {/* 2 MODAL (component riêng) */}
      <AddDrinkModal
        item={selectedItem}
        onConfirm={confirmAddItem}
        onClose={() => setSelectedItem(null)}
      />
      <FeedbackModal
        open={feedbackOpen}
        onSubmit={submitFeedback}
        onClose={() => setFeedbackOpen(false)}
      />
    </div>
  );
}

export default ClientMenu;