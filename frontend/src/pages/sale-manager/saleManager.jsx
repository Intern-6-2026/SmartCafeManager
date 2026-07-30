import React, { useCallback, useEffect, useState } from "react";
import "../../styles/sale-manager.css";
import {
  getAllTables,
  getInvoice,
  payWithCash,
  getApiErrorMessage,
} from "../../services/apiService";

const LABEL = {
  empty: "Trống",
  wait: "Chờ món",
  serve: "Đang phục vụ",
  call: "Gọi nhân viên",
  bill: "Chờ tính tiền",
};
const CLS = {
  empty: "tc-empty",
  wait: "tc-wait",
  serve: "tc-serve",
  call: "tc-call",
  bill: "tc-bill",
};
const BADGE = {
  empty: ["#F1EFE8", "#6E5C4A"],
  wait: ["#FAEEDA", "#854F0B"],
  serve: ["#E1F5EE", "#0F6E56"],
  call: ["#E6F1FB", "#185FA5"],
  bill: ["#FBEAF0", "#993556"],
};

const fmt = (n) => new Intl.NumberFormat("vi-VN").format(n ?? 0) + "đ";

/* Quy đổi trạng thái backend -> khoá hiển thị.
   Bàn chưa có khách (isOccupied = false) luôn coi là "Trống",
   còn lại đọc theo serviceStatus. */
const toStatusKey = (t) => {
  if (t.isOccupied === false) return "empty";
  switch (t.serviceStatus) {
    case "WAITING_FOOD":
      return "wait";
    case "CALLING_WAITER":
      return "call";
    case "REQUESTING_BILL":
      return "bill";
    case "NORMAL":
    default:
      return "serve";
  }
};

function SaleManager() {
  const [tables, setTables] = useState([]);
  const [selected, setSelected] = useState(null); // tableId đang chọn
  const [invoice, setInvoice] = useState(null);
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [message, setMessage] = useState("");
  const [loadingTables, setLoadingTables] = useState(true);
  const [loadingBill, setLoadingBill] = useState(false);
  const [paying, setPaying] = useState(false);

  const notify = (msg) => {
    setMessage(String(msg));
    setTimeout(() => setMessage(""), 4000);
  };

  /* Nạp danh sách bàn */
  const loadTables = useCallback(async () => {
    setLoadingTables(true);
    try {
      const res = await getAllTables();
      const list = Array.isArray(res.data) ? res.data : [];
      setTables(list);
      // Tự chọn bàn đang có khách đầu tiên
      const firstBusy = list.find((t) => toStatusKey(t) !== "empty");
      if (firstBusy) setSelected(firstBusy.tableId);
    } catch (err) {
      setTables([]);
      notify(getApiErrorMessage(err, "Không tải được danh sách bàn."));
    } finally {
      setLoadingTables(false);
    }
  }, []);

  useEffect(() => {
    loadTables();
  }, [loadTables]);

  /* Nạp hóa đơn của bàn đang chọn */
  const loadInvoice = useCallback(async (tableId) => {
    if (!tableId) return;
    setLoadingBill(true);
    try {
      const res = await getInvoice(tableId);
      setInvoice(res.data ?? null);
    } catch (err) {
      // Bàn chưa có hóa đơn mở -> không phải lỗi thật
      setInvoice(null);
      if (err?.response?.status !== 500) {
        notify(getApiErrorMessage(err, "Không tải được hóa đơn."));
      }
    } finally {
      setLoadingBill(false);
    }
  }, []);

  useEffect(() => {
    loadInvoice(selected);
  }, [selected, loadInvoice]);

  /* Xác nhận thu tiền */
  const handleConfirmPayment = async () => {
    setPaying(true);
    try {
      const res = await payWithCash(selected);
      notify(res.data);
      setConfirmOpen(false);
      await loadTables();
      await loadInvoice(selected);
    } catch (err) {
      notify(getApiErrorMessage(err, "Xác nhận thanh toán thất bại."));
    } finally {
      setPaying(false);
    }
  };

  const table = tables.find((t) => t.tableId === selected);
  const statusKey = table ? toStatusKey(table) : "empty";

  /* Hóa đơn: gộp món đã gọi + món trong giỏ để nhân viên thấy toàn bộ */
  const rows = [
    ...(invoice?.orderedItems ?? []),
    ...(invoice?.pendingItems ?? []),
  ];
  const total = invoice?.currentTotalAmount ?? 0;

  return (
    <div className="sale-manager">
      <div className="topbar">
        <div className="brand">
          <div className="brand-mark">N</div>
          <div className="brand-name">NEOCAFÉ</div>
        </div>
        <div className="topbar-right">
          <span>Màn hình bán hàng</span>
        </div>
      </div>

      {message && <div className="sm-message" role="status">{message}</div>}

      <div className="layout">
        <div className="floor">
          <div className="section-head">
            <div className="section-title">Sơ đồ bàn</div>
            <div className="legend">
              <span><i className="dot empty" />Trống</span>
              <span><i className="dot wait" />Chờ món</span>
              <span><i className="dot serve" />Đang phục vụ</span>
              <span><i className="dot call" />Gọi nhân viên</span>
              <span><i className="dot bill" />Chờ tính tiền</span>
            </div>
          </div>

          {loadingTables ? (
            <div className="sm-empty">Đang tải danh sách bàn...</div>
          ) : tables.length === 0 ? (
            <div className="sm-empty">Chưa có bàn nào.</div>
          ) : (
            <div className="grid">
              {tables.map((t) => {
                const k = toStatusKey(t);
                return (
                  <button
                    key={t.tableId}
                    className={`table-card ${CLS[k]} ${t.tableId === selected ? "selected" : ""}`}
                    onClick={() => setSelected(t.tableId)}
                  >
                    <div className="tnum">{t.tableName}</div>
                    <div className="tstatus">{LABEL[k]}</div>
                  </button>
                );
              })}
            </div>
          )}
        </div>

        <div className="bill">
          <div className="bill-panel">
            {!selected ? (
              <div className="bill-empty">Chọn một bàn để xem hóa đơn.</div>
            ) : loadingBill ? (
              <div className="bill-empty">Đang tải hóa đơn...</div>
            ) : rows.length === 0 ? (
              <div className="bill-empty">
                {table?.tableName} chưa có hóa đơn nào đang mở.
              </div>
            ) : (
              <>
                <div className="bill-head">
                  <div className="bill-title">{table?.tableName}</div>
                  <div
                    className="bill-badge"
                    style={{ background: BADGE[statusKey][0], color: BADGE[statusKey][1] }}
                  >
                    {LABEL[statusKey]}
                  </div>
                </div>
                <div className="bill-meta">
                  {invoice?.tableOrderId ? `Hóa đơn #${invoice.tableOrderId}` : ""}
                </div>

                <div className="bill-list">
                  {rows.map((r) => (
                    <div className="bill-row" key={r.orderDetailId}>
                      <div>
                        <div className="bn">{r.itemName}</div>
                        {r.note && <div className="bill-note">{r.note}</div>}
                        {r.status === "PENDING" && (
                          <div className="bill-pending">Chưa gửi bếp</div>
                        )}
                      </div>
                      <div className="bq">x{r.quantity}</div>
                      <div className="bp">{fmt(r.price * r.quantity)}</div>
                    </div>
                  ))}
                </div>

                <div className="bill-total">
                  <span className="lbl">Tổng tiền</span>
                  <span className="val">{fmt(total)}</span>
                </div>

                <div className="bill-actions">
                  <button
                    className="btn btn-primary"
                    onClick={() => setConfirmOpen(true)}
                    disabled={paying}
                  >
                    Xác nhận thanh toán
                  </button>
                </div>
              </>
            )}
          </div>
        </div>
      </div>

      {confirmOpen && (
        <div
          className="confirm-overlay"
          onClick={(e) => e.target === e.currentTarget && setConfirmOpen(false)}
        >
          <div className="confirm-box" role="dialog" aria-modal="true" aria-labelledby="confirm-title">
            <div className="confirm-title" id="confirm-title">Xác nhận thanh toán</div>
            <p className="confirm-desc">
              Xác nhận thu tiền cho <strong>{table?.tableName}</strong> với tổng{" "}
              <strong>{fmt(total)}</strong>? Sau khi xác nhận, hóa đơn sẽ được đóng.
            </p>
            <div className="confirm-actions">
              <button className="btn btn-primary" onClick={handleConfirmPayment} disabled={paying}>
                {paying ? "Đang xử lý..." : "Xác nhận"}
              </button>
              <button className="btn btn-ghost" onClick={() => setConfirmOpen(false)} disabled={paying}>
                Quay lại
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default SaleManager;