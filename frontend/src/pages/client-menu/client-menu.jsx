import React, { useCallback, useEffect, useMemo, useState } from "react";
import "../../styles/client-menu.css";
import AddDrinkModal from "../../components/add-drink";
import FeedbackModal from "../../components/feedback";
import CheckoutModal from "../../components/checkout";
import { Link, useParams } from "react-router-dom";
import logo from "../../assets/Logo.svg";
import {
  getAllItems,
  addItemToCart,
  getCart,
  confirmOrder,
  requestCheckout,
  getInvoice,
  callService,
  getApiErrorMessage,
  updateItemQuantity,
  removeItem
} from "../../services/apiService";

/* Menu dự phòng khi không kết nối được server (giữ đúng shape đã chuẩn hoá) */
const FALLBACK_MENU = [
  { id: 1, name: "Cà phê đen", price: 25000, category: "Coffee", img: "/images/iced-black-coffee.png", isAvailable: true },
  { id: 2, name: "Cà phê sữa", price: 30000, category: "Coffee", img: "/images/cafe-sua-da.png", isAvailable: true },
  { id: 3, name: "Cà phê hạt dẻ", price: 35000, category: "Coffee", img: "/images/cà phê hạt dẻ.png", isAvailable: true },
  { id: 4, name: "Cà phê muối", price: 35000, category: "Coffee", img: "/images/cà_phê_muối.png", isAvailable: true },
  { id: 5, name: "Bạc xỉu", price: 30000, category: "Coffee", img: "/images/Bạc_xỉu.png", isAvailable: true },
  { id: 6, name: "Cappuccino", price: 35000, category: "Coffee", img: "/images/cappuccino.png", isAvailable: true },
];

const fmt = (n) => new Intl.NumberFormat("vi-VN").format(n) + "đ";

/* Chuẩn hoá item từ API (itemId/itemName/imageUrl/categoryName)
   về shape nội bộ (id/name/img/category) dùng chung toàn trang + modal */
const normalizeItem = (it) => ({
  id: it.itemId,
  name: it.itemName,
  price: it.price,
  category: it.categoryName ?? it.categoryId ?? "Khác",
  img: it.imageUrl || {logo}, // ảnh dự phòng khi imageUrl null
  description: it.description,
  isAvailable: it.isAvailable !== false,
});

function ClientMenu() {
  /* Route: /menu/table/:tableName — tableName chính là tên bàn gửi lên API (vd: ban01) */
  const { tableName } = useParams();
  const [menuItems, setMenuItems] = useState([]); // menu lấy từ server
  const [category, setCategory] = useState("");
  const [cart, setCart] = useState([]); // giỏ tạm PENDING lấy từ server
  const [selectedItem, setSelectedItem] = useState(null); // món đang mở modal Thêm món
  const [feedbackOpen, setFeedbackOpen] = useState(false); // modal Phản hồi
  const [checkoutOpen, setCheckoutOpen] = useState(false); // modal Thanh toán
  const [invoice, setInvoice] = useState(null); // dữ liệu hóa đơn từ API /invoice
  const [paymentMethod, setPaymentMethod] = useState("CASH"); // enum backend
  const [message, setMessage] = useState(""); // thông báo kết quả API
  const [loading, setLoading] = useState(false);

  const notify = (msg) => {
    setMessage(String(msg));
    setTimeout(() => setMessage(""), 4000);
  };

  /* ===== API 1: Lấy toàn bộ menu ===== */
  useEffect(() => {
    (async () => {
      try {
        const res = await getAllItems();
        const items = Array.isArray(res.data) ? res.data.map(normalizeItem) : [];
        setMenuItems(items);
        if (items.length > 0) setCategory(items[0].category);
      } catch (err) {
        // Không lấy được menu từ server -> dùng menu dự phòng
        setMenuItems(FALLBACK_MENU);
        setCategory(FALLBACK_MENU[0].category);
        notify(getApiErrorMessage(err, "Không tải được menu từ máy chủ."));
      }
    })();
  }, []);

  /* Danh mục sinh tự động từ menu server */
  const categories = useMemo(
    () => [...new Set(menuItems.map((m) => m.category))],
    [menuItems]
  );

  /* Lọc món theo danh mục đang chọn */
  const filtered = useMemo(
    () => menuItems.filter((m) => m.category === category),
    [menuItems, category]
  );

  /* ===== API 5: Xem giỏ hàng tạm thời (PENDING) ===== */
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

  /* Giỏ hàng: response phẳng { orderDetailId, itemName, quantity, price, status } */
  const cartRows = cart.map((c) => ({
    orderDetailId: c.orderDetailId,
    name: c.item?.itemName ?? c.itemName ?? "—",
    itemId: c.item?.itemId ?? c.itemId,
    price: c.price,
    qty: c.quantity,
    note: c.note,
    status: c.status,
  }));
  const total = cartRows.reduce((s, r) => s + r.price * r.qty, 0);

  /* ===== API 4: Thêm món vào giỏ — bấm "Thêm" trong modal ===== */
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

  /* ===== API 6: [GỌI MÓN] chốt đơn gửi bếp ===== */
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

  /* ===== API 8: Bấm nút "Thanh toán" -> lấy hóa đơn, mở modal ===== */
  const handleThanhToan = async () => {
    setLoading(true);
    try {
      const res = await getInvoice(tableName);
      setInvoice(res.data);
      setCheckoutOpen(true);
    } catch (err) {
      notify(getApiErrorMessage(err, "Không lấy được hóa đơn."));
    } finally {
      setLoading(false);
    }
  };

  /* ===== API 9: Bấm "Xác nhận" trong modal -> gửi yêu cầu thanh toán ===== */
  const confirmCheckout = async () => {
    setLoading(true);
    try {
      const res = await requestCheckout(tableName, paymentMethod);
      notify(res.data);
      setCheckoutOpen(false);
    } catch (err) {
      notify(getApiErrorMessage(err, "Gửi yêu cầu thanh toán thất bại."));
    } finally {
      setLoading(false);
    }
  };

  /* ===== API 10: Gọi nhân viên ===== */
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

  /* ===== API update-quantity: cập nhật số lượng món ===== */
  const handleChangeQty = async (itemId, currentQty, delta) => {
    const newQty = currentQty + delta;
    if (newQty <= 0) {
      return handleRemoveItem(itemId);
    }
    setLoading(true);
    try {
      const res = await updateItemQuantity(tableName, itemId, newQty);
      notify(res.data);
      await loadCart();
    } catch (err) {
      notify(getApiErrorMessage(err, "Cập nhật số lượng thất bại."));
    } finally {
      setLoading(false);
    }
  };

  /* ===== API remove-item: xoá món khỏi giỏ ===== */
  const handleRemoveItem = async (itemId) => {
    setLoading(true);
    try {
      const res = await removeItem(tableName, itemId);
      notify(res.data);
      await loadCart();
    } catch (err) {
      notify(getApiErrorMessage(err, "Xoá món thất bại."));
    } finally {
      setLoading(false);
    }
  };

  /* Bấm "Gửi" trong modal Phản hồi (chưa có API phản hồi trong tài liệu) */
  const submitFeedback = (data) => {
    console.log("Phản hồi:", data);
    notify("Cảm ơn bạn đã gửi phản hồi!");
    setFeedbackOpen(false);
  };

  const pickCategory = (c) => {
    setCategory(c);
  };

  return (
    <div className="client-menu">
      <header>
        <div className="header-row">
          <div className="brand">
            <img src={logo} alt="Logo" className="brand-logo" />
            <h1 className="brand-name-bold">NEO</h1>
            <h1 className="brand-name-not-bold">CAFÉ</h1>
          </div>
          <div className="header-title">THÔNG TIN BÀN</div>
        </div>
      </header>

      {/* Danh mục sinh từ menu server */}
      <div
        className={`drawer-overlay ${menuOpen ? "show" : ""}`}
        onClick={() => setMenuOpen(false)}
      />
      <nav className={`category-nav ${menuOpen ? "open" : ""}`} aria-label="Loại dịch vụ">
        {categories.map((c) => (
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
            {/* Thanh loại món nằm ngang, dính phía trên vùng cuộn menu */}
            <nav className="category-nav" aria-label="Loại dịch vụ">
              {categories.map((c) => (
                <button
                  key={c}
                  className={`category-btn ${category === c ? "active" : ""}`}
                  onClick={() => pickCategory(c)}
                >
                  {c}
                </button>
              ))}
            </nav>
            <div className="grid-menu">
              {filtered.map((m) => (
                <button
                  className="card"
                  key={m.id}
                  onClick={() => m.isAvailable && setSelectedItem(m)}
                  disabled={!m.isAvailable}
                  aria-label={`Chọn ${m.name}`}
                >
                  <div className="card-img">
                    <img src={m.img} alt={m.name} className="item-img" />
                  </div>
                  <div className="card-name">{m.name}</div>
                  <div className="card-price">{fmt(m.price)}</div>
                  {!m.isAvailable && <div className="card-soldout">Hết món</div>}
                </button>
              ))}
              {filtered.length === 0 && (
                <div className="menu-empty">
                  {menuItems.length === 0
                    ? "Đang tải menu..."
                    : `Chưa có món nào trong mục ${category}.`}
                </div>
              )}
            </div>
          </div>

          {/* Chi tiết đơn hàng (giỏ tạm PENDING từ server) */}
          <section className="order-detail">
            <h3>{tableName}</h3>
            <div className="order-header">
              <span className="order-header-title">Tên món</span>
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
                    onClick={() => handleRemoveItem(r.itemId)}
                  >
                    <img src="/images/Icon Remove.png" alt="" className="remove-icon" />
                  </button>
                  <div className="order-info">
                    <div className="order-top">
                      <span className="order-name">{r.name}</span>
                      <span className="order-price">{fmt(r.price)}</span>
                    </div>
                    {/*{r.note && <div className="order-item-note">Ghi chú: {r.note}</div>}*/}
                    <div className="order-bottom">
                      <div className="order-qty">
                        <button
                          className="qty-btn"
                          aria-label={`Giảm số lượng ${r.name}`}
                          onClick={() => handleChangeQty(r.itemId, r.qty, -1)}
                        >
                          −
                        </button>
                        <span className="qty-value">{r.qty}</span>
                        <button
                          className="qty-btn"
                          aria-label={`Tăng số lượng ${r.name}`}
                          onClick={() => handleChangeQty(r.itemId, r.qty, 1)}
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
                <option value="E_WALLET">Ví điện tử</option>
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
          <img src="/images/Logo.png" alt="Logo" className="brand-logo" />
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
      <CheckoutModal
        open={checkoutOpen}
        invoice={invoice}
        paymentMethod={paymentMethod}
        onChangeMethod={setPaymentMethod}
        onConfirm={confirmCheckout}
        onClose={() => setCheckoutOpen(false)}
        loading={loading}
      />
    </div>
  );
}

export default ClientMenu;