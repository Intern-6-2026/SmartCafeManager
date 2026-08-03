import React, { useState } from "react";

const mockInvoices = [
  { 
    id: "HD001", 
    table: "Bàn 01", 
    total: 125000, 
    time: "30/07/2026 10:30", 
    status: "Đã thanh toán",
    items: [
      { name: "Cà phê muối", quantity: 1, price: 35000 },
      { name: "Trà đào cam sả", quantity: 1, price: 45000 },
      { name: "Bánh croissant", quantity: 1, price: 45000 }
    ]
  },
  { 
    id: "HD002", 
    table: "Bàn 03", 
    total: 85000, 
    time: "30/07/2026 11:15", 
    status: "Đã thanh toán",
    items: [
      { name: "Cà phê hạt dẻ", quantity: 1, price: 40000 },
      { name: "Bạc xỉu", quantity: 1, price: 45000 }
    ]
  },
  { 
    id: "HD003", 
    table: "Bàn 02", 
    total: 74000, 
    time: "30/07/2026 12:00", 
    status: "Đã thanh toán",
    items: [
      { name: "Nước ép cam", quantity: 2, price: 37000 }
    ]
  },
];

export default function InvoiceManagement() {
  const [selectedInvoice, setSelectedInvoice] = useState(null);
  const [filterTable, setFilterTable] = useState("");

  const filteredData = mockInvoices.filter(inv => !filterTable || inv.table === filterTable);

  return (
    <div style={{ minHeight: "100vh", backgroundColor: "#F6EEE1", color: "#33261A", fontFamily: "'Be Vietnam Pro', sans-serif" }}>
      {/* Topbar chuẩn NEOCAFÉ */}
      <div style={{ background: "linear-gradient(160deg, #E7C9A1, #D5A874)", padding: "14px 22px", display: "flex", alignItems: "center", justifyContent: "space-between" }}>
        <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
          <div style={{ width: "38px", height: "38px", borderRadius: "50%", background: "#33261A", color: "#E7C9A1", display: "flex", alignItems: "center", justifyContent: "center", fontFamily: "'Fraunces', serif", fontWeight: 600, fontSize: "19px" }}>N</div>
          <div style={{ fontFamily: "'Fraunces', serif", fontWeight: 600, fontSize: "20px", letterSpacing: ".5px" }}>NEOCAFÉ</div>
        </div>
        <div style={{ display: "flex", alignItems: "center", gap: "16px", fontSize: "13px", color: "#4A3627" }}>
          <span>Quản lý</span>
          <div style={{ display: "flex", alignItems: "center", gap: "8px", fontWeight: 600 }}>
            <div style={{ width: "30px", height: "30px", borderRadius: "50%", background: "#33261A", color: "#E7C9A1", display: "flex", alignItems: "center", justifyContent: "center", fontSize: "12px", fontWeight: 700 }}>CT</div>
            Chí Thanh
          </div>
        </div>
      </div>

      {/* Nội dung chính */}
      <div style={{ maxWidth: "1240px", margin: "0 auto", padding: "22px" }}>
        <div style={{ display: "flex", alignItems: "baseline", justifyContent: "space-between", marginBottom: "16px" }}>
          <div style={{ fontFamily: "'Fraunces', serif", fontSize: "22px", fontWeight: 600 }}>Quản lý hóa đơn</div>
          <div style={{ fontSize: "13px", color: "#6E5C4A" }}>{filteredData.length} hóa đơn</div>
        </div>

        {/* Thanh lọc */}
        <div style={{ display: "flex", gap: "12px", alignItems: "flex-end", flexWrap: "wrap", marginBottom: "16px" }}>
          <div style={{ display: "flex", flexDirection: "column", gap: "5px" }}>
            <label style={{ fontSize: "11.5px", fontWeight: 600, color: "#6E5C4A" }}>Lọc theo bàn</label>
            <select 
              value={filterTable} 
              onChange={(e) => setFilterTable(e.target.value)}
              style={{ minWidth: "170px", fontFamily: "inherit", fontSize: "13px", color: "#33261A", border: "1px solid #E4D2B8", borderRadius: "8px", padding: "8px 10px", background: "#fff", outline: "none", cursor: "pointer" }}
            >
              <option value="">Tất cả bàn</option>
              <option value="Bàn 01">Bàn 01</option>
              <option value="Bàn 02">Bàn 02</option>
              <option value="Bàn 03">Bàn 03</option>
            </select>
          </div>
          {filterTable && (
            <button 
              onClick={() => setFilterTable("")}
              style={{ padding: "8px 14px", borderRadius: "8px", border: "1px solid #E4D2B8", background: "#fff", fontFamily: "inherit", fontSize: "12.5px", fontWeight: 600, color: "#6E5C4A", cursor: "pointer" }}
            >
              Xóa lọc
            </button>
          )}
        </div>

        {/* Bảng dữ liệu chuẩn phong cách */}
        <div style={{ background: "#FFFDF9", border: "1px solid #E4D2B8", borderRadius: "16px", overflowX: "auto" }}>
          <table style={{ width: "100%", borderCollapse: "collapse", minWidth: "900px" }}>
            <thead>
              <tr>
                <th style={{ textAlign: "left", fontSize: "11.5px", fontWeight: 700, textTransform: "uppercase", letterSpacing: ".4px", color: "#6E5C4A", padding: "13px 14px", background: "#F3E9D8", borderBottom: "1px solid #E4D2B8" }}>Mã hóa đơn</th>
                <th style={{ textAlign: "left", fontSize: "11.5px", fontWeight: 700, textTransform: "uppercase", letterSpacing: ".4px", color: "#6E5C4A", padding: "13px 14px", background: "#F3E9D8", borderBottom: "1px solid #E4D2B8" }}>Bàn</th>
                <th style={{ textAlign: "left", fontSize: "11.5px", fontWeight: 700, textTransform: "uppercase", letterSpacing: ".4px", color: "#6E5C4A", padding: "13px 14px", background: "#F3E9D8", borderBottom: "1px solid #E4D2B8" }}>Tổng tiền</th>
                <th style={{ textAlign: "left", fontSize: "11.5px", fontWeight: 700, textTransform: "uppercase", letterSpacing: ".4px", color: "#6E5C4A", padding: "13px 14px", background: "#F3E9D8", borderBottom: "1px solid #E4D2B8" }}>Thời gian</th>
                <th style={{ textAlign: "left", fontSize: "11.5px", fontWeight: 700, textTransform: "uppercase", letterSpacing: ".4px", color: "#6E5C4A", padding: "13px 14px", background: "#F3E9D8", borderBottom: "1px solid #E4D2B8" }}>Trạng thái</th>
                <th style={{ textAlign: "center", fontSize: "11.5px", fontWeight: 700, textTransform: "uppercase", letterSpacing: ".4px", color: "#6E5C4A", padding: "13px 14px", background: "#F3E9D8", borderBottom: "1px solid #E4D2B8" }}>Hành động</th>
              </tr>
            </thead>
            <tbody>
              {filteredData.map((inv) => (
                <tr key={inv.id} style={{ borderBottom: "1px solid #E4D2B8" }} onMouseEnter={(e)=>e.currentTarget.style.background="#FBF4E7"} onMouseLeave={(e)=>e.currentTarget.style.background="transparent"}>
                  <td style={{ padding: "13px 14px", fontSize: "13px", fontWeight: 600, color: "#6E5C4A" }}>#{inv.id}</td>
                  <td style={{ padding: "13px 14px", fontSize: "13px", fontWeight: 600 }}>{inv.table}</td>
                  <td style={{ padding: "13px 14px", fontSize: "13px", fontWeight: 700, color: "#9C6B3A" }}>{inv.total.toLocaleString()} đ</td>
                  <td style={{ padding: "13px 14px", fontSize: "12px", color: "#6E5C4A" }}>{inv.time}</td>
                  <td style={{ padding: "13px 14px", fontSize: "13px" }}>
                    <span style={{ padding: "4px 10px", background: "#EBF3E8", color: "#3B7A27", borderRadius: "20px", fontSize: "12px", fontWeight: 600 }}>
                      {inv.status}
                    </span>
                  </td>
                  <td style={{ padding: "13px 14px", textAlign: "center" }}>
                    <button 
                      onClick={() => setSelectedInvoice(inv)}
                      style={{ padding: "6px 14px", background: "#33261A", color: "#E7C9A1", borderRadius: "8px", border: "none", fontSize: "12.5px", fontWeight: 600, cursor: "pointer" }}
                    >
                      Xem hóa đơn
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Modal chi tiết hóa đơn */}
      {selectedInvoice && (
        <div style={{ position: "fixed", inset: 0, background: "rgba(25,18,12,0.82)", display: "flex", alignItems: "center", justifyContent: "center", padding: "20px", zIndex: 200 }}>
          <div style={{ background: "#FFFDF9", padding: "24px", borderRadius: "16px", width: "100%", maxWidth: "420px", border: "1px solid #E4D2B8", boxShadow: "0 20px 50px rgba(0,0,0,.4)" }}>
            <h3 style={{ fontFamily: "'Fraunces', serif", fontSize: "20px", fontWeight: 600, marginBottom: "16px", color: "#33261A" }}>Chi tiết hóa đơn: {selectedInvoice.id}</h3>
            <p style={{ fontSize: "13px", color: "#6E5C4A", marginBottom: "6px" }}><strong>Khu vực:</strong> {selectedInvoice.table}</p>
            <p style={{ fontSize: "13px", color: "#6E5C4A", marginBottom: "16px" }}><strong>Thời gian:</strong> {selectedInvoice.time}</p>
            
            <div style={{ borderTop: "1px solid #E4D2B8", borderBottom: "1px solid #E4D2B8", padding: "12px 0", marginBottom: "16px", maxHeight: "150px", overflowY: "auto" }}>
              {selectedInvoice.items.map((item, idx) => (
                <div key={idx} style={{ display: "flex", justifyContent: "between", fontSize: "13px", marginBottom: "8px" }}>
                  <span style={{ flex: 1 }}>{item.name} x{item.quantity}</span>
                  <span style={{ fontWeight: 600 }}>{(item.price * item.quantity).toLocaleString()} đ</span>
                </div>
              ))}
            </div>

            <div style={{ display: "flex", justifyContent: "space-between", fontSize: "16px", fontWeight: 700, marginBottom: "20px" }}>
              <span>Tổng cộng:</span> 
              <span style={{ color: "#9C6B3A" }}>{selectedInvoice.total.toLocaleString()} đ</span>
            </div>
            
            <button 
              onClick={() => setSelectedInvoice(null)}
              style={{ width: "100%", padding: "10px", background: "#E7C9A1", color: "#33261A", borderRadius: "8px", border: "none", fontWeight: 700, cursor: "pointer" }}
            >
              Đóng
            </button>
          </div>
        </div>
      )}
    </div>
  );
}