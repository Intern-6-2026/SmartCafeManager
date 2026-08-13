import React, { useCallback, useEffect, useMemo, useState, useRef } from "react";
import "../../styles/sale-manager.css";
import { Client } from '@stomp/stompjs';
import { ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import { ToastService } from "../../services/toastService";
import { Link } from "react-router-dom";
import Logo from "../../components/Logo";
import {
  getAllTableInfo,
  getTablesInvoice,
  approvePayment,
  getApiErrorMessage,
  getActiveOrder,
  staffConfirmOrder,
  staffServeTable
} from "../../services/apiService";

const fmt = (n) => new Intl.NumberFormat("vi-VN").format(n || 0) + "đ";

/* Tự làm mới lưới bàn (ms) — đặt 0 để tắt */
const REFRESH_MS = 15000;

/* Ánh xạ serviceStatus của backend sang trạng thái hiển thị.
   Mới xác nhận được "EMPTY" từ tài liệu API, các giá trị còn lại là suy đoán
   -> nếu màu thẻ bàn sai thì sửa bảng này. */
const STATUS_MAP = {
  NORMAL: "empty",
  EMPTY: "empty",
  WAITING_FOOD: "neworder",
  SERVING: "serve",
  CALL_STAFF: "call",
  WAITING_PAYMENT: "bill",
  REQUESTING_BILL: "bill",
};

const STATUS_LABEL = {
  empty: "Trống",
  neworder: "Đơn mới",
  serve: "Đang phục vụ",
  call: "Gọi nhân viên",
  bill: "Chờ tính tiền",
};

/* Mệnh giá gợi ý trong modal thanh toán */
const QUICK_CASH = [50000, 100000, 200000, 500000];

/* Chuẩn hoá bàn từ API (tableId/tableName/serviceStatus/isOccupied)
   về shape nội bộ (id/name/status) */
const normalizeTable = (t) => ({
  id: t.tableId,
  name: String(t.tableName ?? t.tableId ?? "").replace(/^Bàn\s*/i, ""),
  status: STATUS_MAP[t.serviceStatus] || (t.isOccupied ? "serve" : "empty"),
  rawStatus: t.serviceStatus, // giữ lại để đối chiếu khi map sai
  physicalState: t.physicalState,
});

/* Chuẩn hoá 1 dòng chi tiết đơn từ API (orderDetailId/item.itemName/unitPrice)
   về shape nội bộ (id/name/qty/price/total)
   status: PENDING -> CONFIRMED -> SERVED | CANCELLED */
const normalizeDetail = (d) => ({
  id: d.orderDetailId,
  name: d.itemName ?? "—",
  qty: d.quantity ?? 0,
  price: d.price ?? d.unitPrice ?? d.item?.price ?? 0,
  total: (d.price ?? d.unitPrice ?? d.item?.price ?? 0) * (d.quantity ?? 0),
  note: d.note ?? "",
  status: d.status,
});

function SaleManager() {
  const [tables, setTables] = useState([]); // danh sách bàn từ server
  const [selectedId, setSelectedId] = useState(1); // bàn đang chọn
  const [details, setDetails] = useState([]); // chi tiết đơn của bàn đang chọn
  const [openAt, setOpenAt] = useState(""); // giờ mở bàn (order.openAt)
  const [customerName, setCustomerName] = useState(""); // tên khách nếu có
  const [payOpen, setPayOpen] = useState(false); // modal thanh toán
  const [doneOpen, setDoneOpen] = useState(false); // modal đã thu tiền
  const [cash, setCash] = useState(""); // tiền khách đưa
  const [lastChange, setLastChange] = useState(0); // tiền thối vừa trả
  const [showNhanDon, setShowNhanDon] = useState(false); // hiển thị nút nhận đơn
  const [showServed, setShowServed] = useState(false); // hiển thị nút xác nhận lên món
  const [notifications, setNotifications] = useState([]); // danh sách thông báo
  const [bellOpen, setBellOpen] = useState(false); // mở/đóng bảng thông báo
  const [loading, setLoading] = useState(false);

  /* Thêm 1 thông báo vào danh sách (hiện chuông + lắc) */
  const pushNotification = useCallback((text, type = "info") => {
    setNotifications((prev) => [
      {
        id: Date.now() + Math.random(),
        text: String(text),
        type,
        time: new Date().toLocaleTimeString("vi-VN", { hour: "2-digit", minute: "2-digit" }),
      },
      ...prev,
    ].slice(0, 30)); // giữ tối đa 30 thông báo gần nhất
  }, []);

  const removeNotification = useCallback((id) => {
    setNotifications((prev) => prev.filter((n) => n.id !== id));
  }, []);

  const notify = ( msg, type = "info", msgType = null, tableId = null) => {
    /* Thông báo dạng toast.
         notify(msg)            -> toast thường (info)
         notify(msg, "success") -> toast xanh
         notify(msg, "error")   -> toast đỏ */
    const text = String(msg);
    if (type === "success") ToastService.success(text);
    else if (type === "error") ToastService.error(text);
    else {

      switch (msgType) {
        case "NEW_ORDER":
          pushNotification(text, type);
          if (tableId === selectedId) {
            loadDetails(selectedId); // tự động tải lại chi tiết bàn đang xem
          }
          loadTables();
          break;
        case "CALL_STAFF":
          pushNotification(text, type);
          loadTables();
          break;
        case "CASH_PAYMENT_REQUEST":
          pushNotification(text, type);
          if (tableId === selectedId) {
            loadDetails(selectedId); // tự động tải lại chi tiết bàn đang xem
          }
          loadTables();
          break;
        case "ALL_ITEMS_SERVED":
          if (tableId === selectedId) {
            loadDetails(selectedId); // tự động tải lại chi tiết bàn đang xem
          }
          loadTables();
          break;
        case "CHECKOUT_COMPLETE":
          pushNotification(text, type);
          if (tableId === selectedId) {
            loadDetails(selectedId); // tự động tải lại chi tiết bàn đang xem
          }
          loadTables();
          break;
        case "ORDER_CONFIRMED":
          if (tableId === selectedId) {
            loadDetails(selectedId); // tự động tải lại chi tiết bàn đang xem
          }
          break;
        default:
          break;
      }
    }
    
  };

  const clearNotifications = () => {
    setNotifications([]);
    setBellOpen(false);
  };

  /* ===== API 12: Lấy thông tin tất cả các bàn ===== */
  const loadTables = useCallback(async (silent = false) => {
    if (!silent) setLoading(true);
    try {
      const res = await getAllTableInfo();
      const list = Array.isArray(res.data)
        ? res.data.filter((t) => !t.deleted).map(normalizeTable)
        : [];
      setTables(list);
      /* Lần đầu vào trang: tự chọn bàn đang có khách, không có thì bàn đầu tiên */
      setSelectedId((prev) => {
        if (prev !== null && list.some((t) => t.id === prev)) return prev;
        const busy = list.find((t) => t.status !== "empty");
        return (busy || list[0])?.id ?? null;
      });
    } catch (err) {
      notify(getApiErrorMessage(err, "Không tải được danh sách bàn."), "error");
    } finally {
      if (!silent) setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadTables();
  }, [loadTables]);

  /* ===== API 14: Lấy chi tiết hóa đơn của bàn ===== */
  const loadDetails = useCallback(async (tableId) => {
    if (tableId === null || tableId === undefined) {
      setDetails([]);
      setOpenAt("");
      setCustomerName("");
      return;
    }
    try {
      const res = await getTablesInvoice(tableId);
      const raw = Array.isArray(res.data) ? res.data : [];
      /* Bỏ món đã huỷ và đã xoá mềm khỏi hóa đơn */
      const visible = raw.filter((d) => !d.deleted && d.status !== "CANCELLED");
      setDetails(visible.map(normalizeDetail));
      //Hiện nút nhận đơn nếu có món mới gọi (ORDERED) chưa xác nhận
      if (visible.filter((d) => d.status === "ORDERED").length > 0) {
        setShowNhanDon(true);
      } else {
        setShowNhanDon(false);
      }
      //Hiện nut xác nhận phục vụ món nếu có món đã xác nhận nhưng chưa phục vụ
      if (visible.filter((d) => d.status === "CONFIRMED").length > 0) {
        setShowServed(true);
      } else {
        setShowServed(false);
      }

      const order = raw[0]?.order;
      if (order?.openAt) {
        const d = new Date(order.openAt);
        setOpenAt(
          `${String(d.getHours()).padStart(2, "0")}:${String(d.getMinutes()).padStart(2, "0")}`
        );
      } else {
        setOpenAt("");
      }
      setCustomerName(order?.customer?.fullName ?? "");
    } catch (err) {
      /* Bàn trống thường trả 404/500 "không có hóa đơn nào đang mở" — không phải lỗi thật */
      setDetails([]);
      setOpenAt("");
      setCustomerName("");
      const code = err?.response?.status;
      if (code !== 404 && code !== 500) {
        notify(getApiErrorMessage(err, "Không tải được hóa đơn của bàn."), "error");
      }
    }
  }, []);

  useEffect(() => {
    loadDetails(selectedId);
  }, [selectedId, loadDetails]);

  const selectedTable = useMemo(
    () => tables.find((t) => t.id === selectedId) ?? null,
    [tables, selectedId]
  );

  /* Tổng tiền tự tính từ các món đang hiển thị.
     OrderDetail không có trường thành tiền nên phải nhân đơn giá × số lượng. */
  const total = useMemo(
    () => details.reduce((sum, d) => sum + d.total, 0),
    [details]
  );

  const change = (Number(cash) || 0) - total;
  const canFinish = Number(cash) > 0 && change >= 0 && !loading;
  
  /* ===== API nhận đơn =====*/
  const handleNhanDon = async () => {
    setLoading(true);
    try {
      const res = await getActiveOrder(selectedTable.id);
      const order = res.data; // axios bọc dữ liệu trong .data
      if (!order?.tableOrderId) {
        notify("Không tìm thấy đơn đang mở của bàn này.", "error");
        return;
      }
      const confirmRes = await staffConfirmOrder(order.tableOrderId);
      notify("Đã xác nhận món.", "success");
      await loadDetails(selectedTable.id); // tải lại để cập nhật trạng thái món
    } catch (err) {
      notify(getApiErrorMessage(err, "Xác nhận món thất bại."), "error");
    } finally {
      setLoading(false);
    }
  };

  const openPayModal = () => {
    setCash("");
    setPayOpen(true);
  };

  /* ===== API 13: Xác nhận thanh toán ===== */
  const handleXacNhanThanhToan = async () => {
    if (!canFinish || !selectedTable) return;
    setLoading(true);
    try {
      // const order = await getActiveOrder(selectedTable.id);
      // if (!order?.tableOrderId) {
      //   notify("Không tìm thấy đơn đang mở của bàn này.");
      //   return;
      // }
      const res = await approvePayment(selectedTable.id);
      notify("Đã xác nhận thanh toán.", "success");
      setLastChange(change);
      setPayOpen(false);
      setDoneOpen(true);
    } catch (err) {
      notify(getApiErrorMessage(err, "Xác nhận thanh toán thất bại."), "error");
    } finally {
      setLoading(false);
    }
  };

  const handleServeAll = async () => {
    setLoading(true);
    try {
      const res = await staffServeTable(selectedTable.id);
      notify("Đã xác nhận phục vụ tất cả món.", "success");
      await loadDetails(selectedTable.id);
    } catch (err) {
      notify(getApiErrorMessage(err, "Xác nhận phục vụ thất bại."), "error");
    } finally {
      setLoading(false);
    }
  };

  const closeDone = async () => {
    setDoneOpen(false);
    /* Nạp lại từ server để lấy trạng thái bàn thật sau khi đóng hóa đơn */
    await loadTables(true);
    await loadDetails(selectedId);
  };

  /* Đóng modal bằng phím Esc */
  useEffect(() => {
    const onKey = (e) => {
      if (e.key !== "Escape") return;
      if (payOpen) setPayOpen(false);
      else if (doneOpen) closeDone();
    };
    document.addEventListener("keydown", onKey);
    return () => document.removeEventListener("keydown", onKey);
  });

  const showAccept = selectedTable?.status === "neworder";
  const showPay = selectedTable?.status === "bill";
  
  //Thực hiện kết nối WebSocket để nhận thông báo từ server khi có sự kiện mới liên quan đến bàn
  useEffect(() => {
    const client = new Client({
      brokerURL: 'ws://localhost:8080/ws', // Đổi thành IP backend thực tế
      reconnectDelay: 5000,
      onConnect: () => {
        console.log(`[WebSocket] Đã kết nối.`);
        
        // Đăng ký nhận tin nhắn của staff-requests và table-events
        client.subscribe(`/topic/staff-requests`, (message) => {
          if (message.body) {
            const data = JSON.parse(message.body);
            notify(data?.message, "info", data?.type, data?.tableId);
          }
        });

      },
      onStompError: (frame) => {
        console.error('[WebSocket] Lỗi STOMP: ', frame.headers['message']);
      },
    });

    client.activate();

    return () => {
      client.deactivate();
      console.log(`[WebSocket] Đã ngắt kết nối theo dõi bàn.`);
    };
  }, []);

  return (
    <div className="sale-manager">
      <header>
        <div className="header-row">
          <div className="brand">
            <Link to="/home">
              <Logo className="brand-logo" />
            </Link>
            <h1 className="brand-name-bold">NEO</h1>
            <h1 className="brand-name-not-bold">CAFÉ</h1>
          </div>
          <div className="header-title">MÀN HÌNH BÁN HÀNG</div>
        </div>
      </header>

      <ToastContainer position="top-center" />
      
      <main>
        <div className="main-content">
          {/* ----------------------------- Lưới bàn ----------------------------- */}
          <div className="floor">
            <div className="section-head">
              <div className="section-title-row">
                <h3 className="section-title">Danh sách bàn</h3>
              </div>
              
              <div className="legend">
                {["empty", "neworder", "serve", "call", "bill"].map((s) => (
                  <span className="legend-item" key={s}>
                    <i className={`dot dot-${s}`} />
                    {STATUS_LABEL[s]}
                  </span>
                ))}
              </div>
            </div>

            {tables.length === 0 ? (
              <div className="floor-empty">
                {loading ? "Đang tải danh sách bàn..." : "Chưa có bàn nào."}
              </div>
            ) : (
              <div className="grid-table">
                {tables.map((t) => (
                  <button
                    key={t.id}
                    className={`table-card tc-${t.status} ${
                      t.id === selectedId ? "selected" : ""
                    }`}
                    onClick={() => setSelectedId(t.id)}
                    title={t.rawStatus ? `serviceStatus: ${t.rawStatus}` : ""}
                    aria-label={`Chọn bàn ${t.name}`}
                  >
                    <div className="table-num">Bàn {t.name}</div>
                    <div className="table-status">{STATUS_LABEL[t.status]}</div>
                    {t.physicalState && t.physicalState !== "GOOD" && (
                      <div className="table-meta">{t.physicalState}</div>
                    )}
                  </button>
                ))}
              </div>
            )}
          </div>

          {/* ------------------------------ Hóa đơn ----------------------------- */}
          <section className="order-detail">
            {!selectedTable ? (
              <div className="order-empty">Chọn một bàn để xem hóa đơn.</div>
            ) : (
              <>
                <div className="order-head">
                  <h3 className="order-title">Bàn {selectedTable.name}</h3>
                  <span className={`order-badge badge-${selectedTable.status}`}>
                    {STATUS_LABEL[selectedTable.status]}
                  </span>
                </div>

                <div className="order-meta">
                  {[openAt && `Mở lúc ${openAt}`, customerName].filter(Boolean).join(" · ")}
                </div>

                <div className="order-header">
                  <span className="order-header-title">Tên món</span>
                  <span className="order-header-title">Thành tiền</span>
                </div>

                <div className="order-scroll">
                  {details.length === 0 ? (
                    <div className="order-empty">Bàn chưa có món nào.</div>
                  ) : (
                    <div className="order-list">
                      {details.map((d) => (
                        <div
                          className={`order-row`}
                          key={d.id}
                        >
                          <div className="order-info">
                            <div className="order-top">
                              <span className="order-name">
                                {d.name}
                                {d.status === "ORDERED" && (
                                  <span className="tag tag-new">Mới</span>
                                )}
                                {d.status === "CONFIRMED" && (
                                  <span className="tag tag-confirmed">Đã gửi bếp</span>
                                )}
                                {d.status === "SERVED" && (
                                  <span className="tag tag-served">Đã phục vụ</span>
                                )}
                                {d.status === "PENDING" && (
                                  <span className="tag tag-pending">Chưa gọi</span>
                                )}
                              </span>
                              <span className="order-price">{fmt(d.total)}</span>
                            </div>
                            {d.note && <div className="order-item-note">{d.note}</div>}
                            <div className="order-bottom">
                              <span className="order-qty">x {d.qty}</span>
                              <span className="order-unit">{fmt(d.price)}/món</span>
                            </div>
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>

                <div className="order-total">
                  <span className="total-label">Tổng tiền</span>
                  <span className="total-value">{fmt(total)}</span>
                </div>

                <div className="order-actions">
                  {showNhanDon && (
                    <>
                      <button className="btn-nhandon" onClick={handleNhanDon}>
                        Nhận đơn
                      </button>
                    </>
                  )}
                  {showServed && (
                    <>
                      <button className="btn-served" onClick={handleServeAll}>
                        Đã lên món
                      </button>
                    </>
                  )}
                  {showPay && (
                    <button
                      className="btn-thanhtoan"
                      onClick={openPayModal}
                      disabled={loading || details.length === 0}
                    >
                      Xác nhận thanh toán
                    </button>
                  )}

                  {!showAccept && !showPay && (
                    <div className="action-hint">Bàn chưa yêu cầu thanh toán.</div>
                  )}
                </div>
              </>
            )}
          </section>
        </div>

      </main>

      <footer>
        <Logo className="brand-logo" />
        <p className="contact-infor">
          Chấp nhận : Visa, MasterCard, Vouchers 
          <br />
          Phí giao dịch áp dụng cho thẻ tín dụng 
          <br />
          Hotline/Số điện thoại: 19001900 <br />
          Địa chỉ quán: Số 1 đường Võ Văn Ngân, phường Thủ Đức, thành phố Hồ Chí Minh
        </p>
      </footer>

      {/* ------------------------- Modal thanh toán ------------------------- */}
      {payOpen && selectedTable && (
        <div className="modal-overlay" onClick={() => setPayOpen(false)}>
          <div
            className="modal-box"
            role="dialog"
            aria-modal="true"
            onClick={(e) => e.stopPropagation()}
          >
            <div className="modal-title">Thanh toán · Bàn {selectedTable.name}</div>

            <div className="pay-table">
              <div className="pay-row">
                <span className="pay-label">Tổng tiền</span>
                <span className="pay-value">{fmt(total)}</span>
              </div>
              <div className="pay-row">
                <span className="pay-label">Tiền khách đưa</span>
                <input
                  type="number"
                  className="pay-input"
                  min="0"
                  step="1000"
                  placeholder="0"
                  value={cash}
                  autoFocus
                  onChange={(e) => setCash(e.target.value)}
                  onKeyDown={(e) => e.key === "Enter" && handleXacNhanThanhToan()}
                />
              </div>
              <div className="pay-row pay-change">
                <span className="pay-label">Tiền thối</span>
                <span className="pay-value">{change >= 0 ? fmt(change) : "—"}</span>
              </div>
            </div>

            <div className="pay-quick">
              <button className="quick-btn" onClick={() => setCash(String(total))}>
                Đúng tiền
              </button>
              {QUICK_CASH.filter((v) => v >= total).map((v) => (
                <button key={v} className="quick-btn" onClick={() => setCash(String(v))}>
                  {fmt(v)}
                </button>
              ))}
            </div>

            {Number(cash) > 0 && change < 0 && (
              <div className="pay-warn">Tiền khách đưa chưa đủ so với tổng hóa đơn.</div>
            )}

            <div className="modal-actions">
              <button
                className="btn-primary"
                onClick={handleXacNhanThanhToan}
                disabled={!canFinish}
              >
                {loading ? "Đang xử lý..." : "Hoàn tất"}
              </button>
              <button className="btn-ghost" onClick={() => setPayOpen(false)}>
                Quay lại
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ------------------------ Modal đã thu tiền ------------------------- */}
      {doneOpen && selectedTable && (
        <div className="modal-overlay" onClick={closeDone}>
          <div
            className="modal-box"
            role="dialog"
            aria-modal="true"
            onClick={(e) => e.stopPropagation()}
          >
            <div className="done-icon">✓</div>
            <div className="modal-title done-title">Đã thu tiền</div>
            <p className="done-text">
              Hóa đơn <strong>Bàn {selectedTable.name}</strong> đã được đóng.
            </p>
            <p className="done-text done-muted">Tiền thối cho khách</p>
            <p className="done-change">{fmt(lastChange)}</p>
            <div className="modal-actions">
              <button className="btn-primary" onClick={closeDone}>
                Đóng
              </button>
            </div>
          </div>
        </div>
      )}
      {/* -------------------- Chuông thông báo nổi -------------------- */}
      {notifications.length > 0 && (
        <div className="notif-bell-wrap">
          {bellOpen && (
            <div className="notif-panel" role="dialog" aria-label="Danh sách thông báo">
              <div className="notif-panel-head">
                <span>Thông báo ({notifications.length})</span>
                <button className="notif-clear" onClick={clearNotifications}>
                  Xoá hết
                </button>
              </div>
              <div className="notif-list">
                {notifications.map((n) => (
                  <div className={`notif-item ${n.type}`} key={n.id}>
                    <span className="notif-dot" />
                    <div className="notif-body">
                      <div className="notif-text">{n.text}</div>
                      <div className="notif-time">{n.time}</div>
                    </div>
                    <button className="notif-close" onClick={() => removeNotification(n.id)}>
                        <svg viewBox="0 0 24 24" width="16" height="16" fill="none" aria-hidden="true">
                          <path
                            d="M18 6 6 18M6 6l12 12"
                            stroke="currentColor"
                            strokeWidth="2"
                            strokeLinecap="round"
                            strokeLinejoin="round"
                          />
                        </svg>
                      </button>
                  </div>
                ))}
              </div>
            </div>
          )}

          <button
            className="notif-bell"
            onClick={() => setBellOpen((v) => !v)}
            aria-label={`${notifications.length} thông báo`}
          >
            <svg viewBox="0 0 24 24" width="24" height="24" fill="none" aria-hidden="true">
              <path
                d="M12 3a6 6 0 0 0-6 6v3.5l-1.5 3h15L18 12.5V9a6 6 0 0 0-6-6Z"
                stroke="currentColor"
                strokeWidth="1.8"
                strokeLinejoin="round"
              />
              <path
                d="M9.5 18a2.5 2.5 0 0 0 5 0"
                stroke="currentColor"
                strokeWidth="1.8"
                strokeLinecap="round"
              />
            </svg>
            <span className="notif-badge">{notifications.length}</span>
          </button>
        </div>
      )}
    </div>
  );
}

export default SaleManager;