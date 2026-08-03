import React, { useMemo, useState } from "react";
import "../../styles/sale-manager.css";
import PaymentModal from "../../components/PaymentModal";
import PaymentDoneModal from "../../components/PaymentDoneModal";
import {
  LABEL, CLS, BADGE,
  INITIAL_TABLES, INITIAL_BILLS, NEW_DRINKS, fmt,
} from "../../data/saleData";

function SaleManager() {
  const [tables, setTables] = useState(INITIAL_TABLES);
  const [bills, setBills] = useState(INITIAL_BILLS);
  const [selected, setSelected] = useState("03");
  const [payOpen, setPayOpen] = useState(false);
  const [doneOpen, setDoneOpen] = useState(false);
  const [lastChange, setLastChange] = useState(0);

  const table = tables.find((t) => t.n === selected);
  const items = bills[selected] ?? null;
  const total = useMemo(
    () => (items ? items.reduce((s, it) => s + it.price, 0) : 0),
    [items]
  );

  const patchTable = (n, patch) =>
    setTables((prev) => prev.map((t) => (t.n === n ? { ...t, ...patch } : t)));

  /* Nhận đơn: Đơn mới -> Đang phục vụ, mọi món thành đã nhận */
  const acceptOrder = () => {
    patchTable(selected, { s: "serve" });
    setBills((prev) => ({
      ...prev,
      [selected]: (prev[selected] ?? []).map((it) => ({ ...it, isNew: false })),
    }));
  };

  /* Demo: khách gọi thêm món -> bàn về Đơn mới, món cũ giữ nguyên (sẽ mờ), món mới isNew=true */
  const demoAddItem = () => {
    const pick = NEW_DRINKS[Math.floor(Math.random() * NEW_DRINKS.length)];
    setBills((prev) => ({
      ...prev,
      [selected]: [...(prev[selected] ?? []), { ...pick }],
    }));
    if (table && table.s !== "bill") patchTable(selected, { s: "neworder" });
  };

  const openPay = () => setPayOpen(true);

  const confirmPay = (change) => {
    setPayOpen(false);
    setLastChange(change);
    setDoneOpen(true);
  };

  /* Đóng modal "đã thu tiền": bàn về trống, xoá hóa đơn */
  const closeDone = () => {
    setDoneOpen(false);
    patchTable(selected, { s: "empty", meta: undefined });
    setBills((prev) => {
      const next = { ...prev };
      delete next[selected];
      return next;
    });
  };

  const statusKey = table ? table.s : "empty";
  const showAccept = statusKey === "neworder";
  const showPay = statusKey === "bill";

  return (
    <div className="sale-manager">
      <div className="topbar">
        <div className="brand">
          <div className="brand-mark">N</div>
          <div className="brand-name">NEOCAFÉ</div>
        </div>
        <div className="topbar-right">
          <span>Ca sáng · 07:00–15:00</span>
          <div className="staff"><div className="staff-avatar">TL</div>Thu Lan</div>
        </div>
      </div>

      <div className="layout">
        <div className="floor">
          <div className="section-head">
            <div className="section-title">Sơ đồ bàn</div>
            <div className="legend">
              <span><i className="dot empty" />Trống</span>
              <span><i className="dot neworder" />Đơn mới</span>
              <span><i className="dot serve" />Đang phục vụ</span>
              <span><i className="dot call" />Gọi nhân viên</span>
              <span><i className="dot bill" />Chờ tính tiền</span>
            </div>
          </div>
          <div className="grid">
            {tables.map((t) => (
              <button
                key={t.n}
                className={`table-card ${CLS[t.s]} ${t.n === selected ? "selected" : ""}`}
                onClick={() => setSelected(t.n)}
              >
                <div className="tnum">Bàn {t.n}</div>
                <div className="tstatus">{LABEL[t.s]}</div>
                {t.meta && <div className="tmeta">{t.meta}</div>}
              </button>
            ))}
          </div>
        </div>

        <div className="bill">
          <div className="bill-panel">
            {!items ? (
              <div className="bill-empty">
                Bàn {selected} đang trống.<br />Chưa có hóa đơn nào được mở.
              </div>
            ) : (
              <>
                <div className="bill-head">
                  <div className="bill-title">Bàn {selected}</div>
                  <div
                    className="bill-badge"
                    style={{ background: BADGE[statusKey][0], color: BADGE[statusKey][1] }}
                  >
                    {LABEL[statusKey]}
                  </div>
                </div>
                <div className="bill-meta">{table?.meta || ""}</div>

                <div className="bill-list">
                  {items.map((it, i) => {
                    const dim = statusKey === "neworder" && !it.isNew;
                    return (
                      <div className={`bill-row ${dim ? "dimmed" : ""}`} key={i}>
                        <div>
                          <div className="bn">
                            {it.name}
                            {it.isNew && <span className="item-new">Mới</span>}
                          </div>
                          {it.note && <div className="bill-note">{it.note}</div>}
                        </div>
                        <div className="bq">x{it.qty}</div>
                        <div className="bp">{fmt(it.price)}</div>
                      </div>
                    );
                  })}
                </div>

                <div className="bill-total">
                  <span className="lbl">Tổng tiền</span>
                  <span className="val">{fmt(total)}</span>
                </div>

                {showAccept ? (
                  <div className="bill-actions">
                    <button className="btn btn-primary" onClick={acceptOrder}>Nhận đơn</button>
                  </div>
                ) : showPay ? (
                  <div className="bill-actions">
                    <button className="btn btn-primary" onClick={openPay}>Xác nhận thanh toán</button>
                  </div>
                ) : (
                  <div className="bill-hint">Bàn chưa yêu cầu thanh toán.</div>
                )}

                <button className="demo-add" onClick={demoAddItem}>
                  ＋ Giả lập khách gọi thêm món (demo)
                </button>
              </>
            )}
          </div>
        </div>
      </div>

      <PaymentModal
        open={payOpen}
        tableName={`Bàn ${selected}`}
        total={total}
        onConfirm={confirmPay}
        onClose={() => setPayOpen(false)}
      />
      <PaymentDoneModal
        open={doneOpen}
        tableName={`Bàn ${selected}`}
        change={lastChange}
        onClose={closeDone}
      />
    </div>
  );
}

export default SaleManager;