import React, { useCallback, useEffect, useMemo, useState } from "react";
import "../../styles/client-menu.css";
import AddDrinkModal from "../../components/add-drink";
import FeedbackModal from "../../components/feedback";
import CheckoutModal from "../../components/checkout";
import { Link, useParams } from "react-router-dom";
import { logo } from "../../constants/assets";
import Logo from "../../components/Logo";
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
  img: it.imageUrl || logo,
  description: it.description,
  isAvailable: it.isAvailable !== false,
});

function ClientMenu() {
  /* Route: /menu/table/:tableId — tableId chính là tên bàn gửi lên API (vd: ban01) */
  const { tableId } = useParams();
  const [menuItems, setMenuItems] = useState([]); // menu lấy từ server
  const [category, setCategory] = useState("");
  const [cart, setCart] = useState([]); // giỏ tạm PENDING lấy từ server
  const [history, setHistory] = useState([]); // món đã gọi (CONFIRMED/SERVED) — chỉ xem
  const [billTotal, setBillTotal] = useState(0); // currentTotalAmount từ server
  const [selectedItem, setSelectedItem] = useState(null); // món đang mở modal Thêm món
  const [feedbackOpen, setFeedbackOpen] = useState(false); // modal Phản hồi
  const [tableOrderId, setTableOrderId] = useState(null); // id Order hiện tại
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

  /* ===== API 5: Xem giỏ hàng =====
     Response mới là 1 object gồm cả 2 danh sách:
     { tableOrderId, tableName, currentTotalAmount, orderedItems[], pendingItems[] } */
  const loadCart = useCallback(async () => {
    try {
      const res = await getCart(tableId);
      const data = res.data ?? {};
      setCart(Array.isArray(data.pendingItems) ? data.pendingItems : []);
      setHistory(Array.isArray(data.orderedItems) ? data.orderedItems : []);
      setBillTotal(data.currentTotalAmount ?? 0);
      setTableOrderId(data.tableOrderId ?? null);
    } catch (err) {
      // 500 "Bàn hiện tại không có hóa đơn nào đang mở!" => bàn trống, không phải lỗi thật
      setCart([]);
      setHistory([]);
      setBillTotal(0);
      setTableOrderId(null);
      if (err?.response?.status !== 500) {
        notify(getApiErrorMessage(err, "Không kết nối được máy chủ."));
      }
    }
  }, [tableId]);

  useEffect(() => {
    loadCart();
  }, [loadCart]);

  /* Giỏ tạm (PENDING): { orderDetailId, itemId, itemName, price, quantity, note, status } */
  const cartRows = cart.map((c) => ({
    orderDetailId: c.orderDetailId,
    name: c.itemName ?? "—",
    itemId: c.itemId,
    price: c.price,
    qty: c.quantity,
    note: c.note,
    status: c.status,
  }));
  /* Tổng tiền cả hóa đơn do server tính (gồm cả món đã gọi lẫn món trong giỏ) */
  const total = billTotal;

  /* Món đã gọi — chỉ hiển thị, không cho sửa/xoá */
  const historyRows = history.map((h) => ({
    orderDetailId: h.orderDetailId,
    name: h.itemName ?? "—",
    price: h.price,
    qty: h.quantity,
    note: h.note,
    status: h.status,
  }));

  const STATUS_LABEL = {
    CONFIRMED: "Đang pha chế",
    SERVED: "Đã phục vụ",
    CANCELLED: "Đã huỷ",
  };

  /* ===== API 4: Thêm món vào giỏ — bấm "Thêm" trong modal ===== */
  const confirmAddItem = async (item, qty, note) => {
    setLoading(true);
    try {
      const res = await addItemToCart(tableId, item.id, qty, note);
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
      const res = await confirmOrder(tableId);
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
      const res = await getInvoice(tableId, tableOrderId);
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
      const res = await requestCheckout(tableId, paymentMethod);
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
      const res = await callService(tableId, "CALLING_WAITER");
      notify(res.data);
    } catch (err) {
      notify(getApiErrorMessage(err, "Gọi nhân viên thất bại."));
    } finally {
      setLoading(false);
    }
  };

  /* ===== API update-quantity: cập nhật số lượng món ===== */
  const handleChangeQty = async (itemId, currentQty, note, delta) => {
    const newQty = currentQty + delta;
    if (newQty <= 0) {
      return handleRemoveItem(itemId);
    }
    setLoading(true);
    try {
      const res = await updateItemQuantity(tableId, itemId, note, newQty);
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
      const res = await removeItem(tableId, itemId);
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
            <Link to="/home">
              <Logo className="brand-logo" />
            </Link>
            <h1 className="brand-name-bold">NEO</h1>
            <h1 className="brand-name-not-bold">CAFÉ</h1>
          </div>
          <div className="header-title">THÔNG TIN BÀN</div>
        </div>
      </header>

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
            <h3>Bàn {tableId}</h3>
            <div className="order-header">
              <span className="order-header-title">Tên món</span>
              <span className="order-header-title">Giá</span>
            </div>

            {/* Một khung cuộn chung: món đã gọi ở trên, giỏ hàng ở dưới */}
            <div className="order-scroll">
              {/* Món đã gọi xuống bếp — chỉ xem, không sửa/xoá được */}
              {historyRows.length > 0 && (
              <div className="ordered-list" aria-label="Món đã gọi">
                <div className="ordered-label">Món đã gọi</div>
                {historyRows.map((r) => (
                  <div className="ordered-row" key={r.orderDetailId}>
                    <div className="ordered-info">
                      <div className="ordered-top">
                        <span className="ordered-name">{r.name}</span>
                        <span className="ordered-price">{fmt(r.price)}</span>
                      </div>
                      <div className="ordered-bottom">
                        <span className="ordered-qty">x {r.qty}</span>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}

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
                          onClick={() => handleChangeQty(r.itemId, r.qty, r.note, -1)}
                        >
                          −
                        </button>
                        <span className="qty-value">{r.qty}</span>
                        <button
                          className="qty-btn"
                          aria-label={`Tăng số lượng ${r.name}`}
                          onClick={() => handleChangeQty(r.itemId, r.qty, r.note, 1)}
                        >
                          +
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
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
          <Logo className="brand-logo" />
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