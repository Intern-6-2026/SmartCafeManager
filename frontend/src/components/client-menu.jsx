import React, { useMemo, useState } from "react";
import "../styles/ClientMenu.css";

/* Hard code data, sau này có thể lấy từ API */
const CATEGORIES = ["Coffee", "Trà", "Nước Ép", "Đá xay", "Bánh"];

const MENU_ITEMS = [
  { id: 1, name: "Cà phê đen", price: 25000, wait: "5:00", category: "Coffee", img: "images/iced-black-coffee.png" },
  { id: 2, name: "Cà phê sữa", price: 30000, wait: "5:00", category: "Coffee", img: "images/cafe-sua-da.png" },
  { id: 3, name: "Cà phê hạt dẻ", price: 35000, wait: "6:00", category: "Coffee", img: "images/cà phê hạt dẻ.png" },
  { id: 4, name: "Cà phê muối", price: 35000, wait: "6:00", category: "Coffee", img: "images/cà_phê_muối.png" },
  { id: 5, name: "Bạc xỉu", price: 30000, wait: "5:00", category: "Coffee", img: "images/Bạc_xỉu.png" },
  { id: 6, name: "Cappuccino", price: 35000, wait: "7:00", category: "Coffee", img: "images/cappuccino.png" },
  { id: 7, name: "Trà đào cam sả", price: 30000, wait: "5:00", category: "Trà", img: "images/tra_dao_cam_sa.png" },
  { id: 8, name: "Trà sữa trân châu", price: 35000, wait: "6:00", category: "Trà", img: "images/tra_sua_tran_chau.png" },
  { id: 9, name: "Nước ép cam", price: 30000, wait: "5:00", category: "Nước Ép", img: "images/nuoc_ep_cam.png" },
  { id: 10, name: "Nước ép dứa", price: 30000, wait: "5:00", category: "Nước Ép", img: "images/nuoc_ep_dua.png" },
  { id: 11, name: "Đá xay socola", price: 35000, wait: "6:00", category: "Đá xay", img: "images/da_xay_socola.png" },
  { id: 12, name: "Đá xay matcha", price: 35000, wait: "6:00", category: "Đá xay", img: "images/da_xay_matcha.png"}
];

const fmt = (n) => new Intl.NumberFormat("vi-VN").format(n) + "đ";

function ClientMenu() {
  const [menuOpen, setMenuOpen] = useState(false);
  const [category, setCategory] = useState("Coffee");
  const [cart, setCart] = useState([
    { id: 1, qty: 1 },
    { id: 2, qty: 1 },
  ]);

  const filtered = useMemo(
    () => MENU_ITEMS.filter((m) => m.category === category),
    [category]
  );

  /*  Giỏ hàng  */
  const cartRows = cart
    .map((c) => ({ ...MENU_ITEMS.find((m) => m.id === c.id), qty: c.qty }))
    .filter((r) => r.name);
  const total = cartRows.reduce((s, r) => s + r.price * r.qty, 0);

  const addToCart = (id) =>
    setCart((prev) => {
      const found = prev.find((c) => c.id === id);
      return found
        ? prev.map((c) => (c.id === id ? { ...c, qty: c.qty + 1 } : c))
        : [...prev, { id, qty: 1 }];
    });

  const changeQty = (id, delta) =>
    setCart((prev) =>
      prev
        .map((c) => (c.id === id ? { ...c, qty: c.qty + delta } : c))
        .filter((c) => c.qty > 0)
    );

  const removeItem = (id) => setCart((prev) => prev.filter((c) => c.id !== id));

  const pickCategory = (c) => {
    setCategory(c);
    setMenuOpen(false);
  };

  return (
    <div className="client-menu">
      <header>
        <div className="header-row">
          <div className="brand">
            <img src="/images/Logo.png" alt="Logo" className="brand-logo" />
            <h1 className="brand-name-bold">NEO</h1>
            <h1 className="brand-name-not-bold">CAFÉ</h1>
          </div>
          <div className="header-title">THÔNG TIN BÀN</div>
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

      {/* ----- Drawer Service Type (hiện khi ấn nút ≡) ----- */}
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

      <main>
        <div className="main-content">
          
          <div className="menu">
            <div className="grid-menu">
              {filtered.map((m) => (
                <button
                  className="card"
                  key={m.id}
                  onClick={() => addToCart(m.id)}
                  aria-label={`Thêm ${m.name} vào đơn`}
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

          {/*Chi tiết đơn hàng*/}
          <section className="order-detail">
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
                <div className="order-row" key={r.id}>
                  <button
                    className="remove-btn"
                    aria-label={`Xoá ${r.name}`}
                    onClick={() => removeItem(r.id)}
                  >
                    <img src="images/Icon Remove.png" alt="" className="remove-icon" />
                  </button>
                  <div className="order-info">
                    <div className="order-top">
                      <span className="order-name">{r.name}</span>
                      <span className="order-wait">{r.wait}</span>
                      <span className="order-price">{fmt(r.price)}</span>
                    </div>
                    <div className="order-bottom">
                      <div className="order-qty">
                        <button
                          className="qty-btn"
                          aria-label={`Giảm số lượng ${r.name}`}
                          onClick={() => changeQty(r.id, -1)}
                        >
                          −
                        </button>
                        <span className="qty-value">{r.qty}</span>
                        <button
                          className="qty-btn"
                          aria-label={`Tăng số lượng ${r.name}`}
                          onClick={() => changeQty(r.id, 1)}
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

            <div className="order-actions">
              <button className="btn-goimon">Gọi món</button>
              <button className="btn-thanhtoan">Thanh toán</button>
              <button className="btn-goinhanvien">Gọi nhân viên</button>
              <button className="btn-phanhoi">Phản hồi</button>
            </div>
          </section>
        </div>

        <footer>
          <img src="images/Logo.png" alt="Logo" className="brand-logo" />
          <p className="contact-infor">
            Chấp nhận : Visa, MasterCard, Vouchers <br />
            Phí giao dịch áp dụng cho thẻ tín dụng <br />
            Hotline/Số điện thoại: 19001900 <br />
            Địa chỉ quán: Số 1 đường Võ Văn Ngân, phường Thủ Đức, thành phố Hồ Chí Minh
          </p>
        </footer>
      </main>
    </div>
  );
}

export default ClientMenu;