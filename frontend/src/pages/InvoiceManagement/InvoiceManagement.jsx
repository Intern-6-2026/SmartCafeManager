import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";

export default function InvoiceManagement() {
  const navigate = useNavigate();
  const [invoices, setInvoices] = useState([]);
  const [loading, setLoading] = useState(false);
  const [selectedInvoice, setSelectedInvoice] = useState(null);
  const [filterTable, setFilterTable] = useState("");

  // Thay thế filterDate đơn bằng startDate và endDate
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");

  // Gọi API lấy danh sách hóa đơn từ Backend
  useEffect(() => {
    const fetchInvoices = async () => {
      setLoading(true);
      try {
        const params = new URLSearchParams();
        if (filterTable) params.append("tableId", filterTable);

        // Truyền startDate và endDate nếu có
        if (startDate) params.append("startDate", startDate);
        if (endDate) params.append("endDate", endDate);

        const response = await fetch(
          `http://localhost:8080/api/v1/staff/statistics/invoices?${params.toString()}`,
          {
            method: "GET",
            headers: {
              "Content-Type": "application/json",
              Authorization: "Bearer " + localStorage.getItem("token"),
            },
          },
        );

        if (!response.ok) {
          throw new Error("Không thể tải dữ liệu từ server");
        }

        const data = await response.json();
        setInvoices(data);
      } catch (error) {
        console.error("Lỗi gọi API hóa đơn:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchInvoices();
  }, [filterTable, startDate, endDate]);

  // Gọi API lấy chi tiết 1 hóa đơn theo orderId
  const handleViewDetail = async (orderId) => {
    try {
      const response = await fetch(
        `http://localhost:8080/api/v1/staff/statistics/invoices/${orderId}`,
        {
          method: "GET",
          headers: {
            "Content-Type": "application/json",
            Authorization: "Bearer " + localStorage.getItem("token"),
          },
        },
      );
      if (response.ok) {
        const detailData = await response.json();
        setSelectedInvoice(detailData);
      }
    } catch (error) {
      console.error("Lỗi khi tải chi tiết hóa đơn:", error);
    }
  };

  const handleClearFilter = () => {
    setFilterTable("");
    setStartDate("");
    setEndDate("");
  };

  return (
    <div
      style={{
        minHeight: "100vh",
        backgroundColor: "#F6EEE1",
        color: "#33261A",
        fontFamily: "'Be Vietnam Pro', sans-serif",
      }}
    >
      {/* Topbar chuẩn NEOCAFÉ */}
      <div
        style={{
          background: "linear-gradient(160deg, #E7C9A1, #D5A874)",
          padding: "14px 22px",
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
        }}
      >
        <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
          <div
            style={{
              width: "38px",
              height: "38px",
              borderRadius: "50%",
              background: "#33261A",
              color: "#E7C9A1",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              fontFamily: "'Fraunces', serif",
              fontWeight: 600,
              fontSize: "19px",
            }}
          >
            N
          </div>
          <div
            style={{
              fontFamily: "'Fraunces', serif",
              fontWeight: 600,
              fontSize: "20px",
              letterSpacing: ".5px",
            }}
          >
            NEOCAFÉ
          </div>
        </div>

        <div style={{ display: "flex", alignItems: "center", gap: "16px" }}>
          <button
            onClick={() => navigate("/admin/revenue")}
            style={{
              padding: "7px 14px",
              background: "#33261A",
              color: "#E7C9A1",
              borderRadius: "8px",
              border: "none",
              fontSize: "13px",
              fontWeight: 600,
              cursor: "pointer",
            }}
          >
            ➔ Thống kê thu nhập
          </button>

          <div
            style={{
              display: "flex",
              alignItems: "center",
              gap: "8px",
              fontWeight: 600,
              fontSize: "13px",
              color: "#4A3627",
            }}
          >
            <div
              style={{
                width: "30px",
                height: "30px",
                borderRadius: "50%",
                background: "#33261A",
                color: "#E7C9A1",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                fontSize: "12px",
                fontWeight: 700,
              }}
            >
              CT
            </div>
            Chí Thanh
          </div>
        </div>
      </div>

      {/* Nội dung chính */}
      <div style={{ maxWidth: "1240px", margin: "0 auto", padding: "22px" }}>
        <div
          style={{
            display: "flex",
            alignItems: "baseline",
            justifyContent: "space-between",
            marginBottom: "16px",
          }}
        >
          <div
            style={{
              fontFamily: "'Fraunces', serif",
              fontSize: "22px",
              fontWeight: 600,
            }}
          >
            Quản lý hóa đơn
          </div>
          <div style={{ fontSize: "13px", color: "#6E5C4A" }}>
            {invoices.length} hóa đơn
          </div>
        </div>

        {/* Thanh lọc (Thêm Ngày bắt đầu & Ngày kết thúc) */}
        <div
          style={{
            display: "flex",
            gap: "12px",
            alignItems: "flex-end",
            flexWrap: "wrap",
            marginBottom: "16px",
          }}
        >
          <div style={{ display: "flex", flexDirection: "column", gap: "5px" }}>
            <label
              style={{ fontSize: "11.5px", fontWeight: 600, color: "#6E5C4A" }}
            >
              Lọc theo ID Bàn
            </label>
            <input
              type="number"
              placeholder="Nhập ID bàn..."
              value={filterTable}
              onChange={(e) => setFilterTable(e.target.value)}
              style={{
                minWidth: "150px",
                fontFamily: "inherit",
                fontSize: "13px",
                color: "#33261A",
                border: "1px solid #E4D2B8",
                borderRadius: "8px",
                padding: "7px 10px",
                background: "#fff",
                outline: "none",
              }}
            />
          </div>

          <div style={{ display: "flex", flexDirection: "column", gap: "5px" }}>
            <label
              style={{ fontSize: "11.5px", fontWeight: 600, color: "#6E5C4A" }}
            >
              Từ ngày
            </label>
            <input
              type="date"
              value={startDate}
              onChange={(e) => setStartDate(e.target.value)}
              style={{
                fontFamily: "inherit",
                fontSize: "13px",
                color: "#33261A",
                border: "1px solid #E4D2B8",
                borderRadius: "8px",
                padding: "7px 10px",
                background: "#fff",
                outline: "none",
                cursor: "pointer",
              }}
            />
          </div>

          <div style={{ display: "flex", flexDirection: "column", gap: "5px" }}>
            <label
              style={{ fontSize: "11.5px", fontWeight: 600, color: "#6E5C4A" }}
            >
              Đến ngày
            </label>
            <input
              type="date"
              value={endDate}
              onChange={(e) => setEndDate(e.target.value)}
              style={{
                fontFamily: "inherit",
                fontSize: "13px",
                color: "#33261A",
                border: "1px solid #E4D2B8",
                borderRadius: "8px",
                padding: "7px 10px",
                background: "#fff",
                outline: "none",
                cursor: "pointer",
              }}
            />
          </div>

          {(filterTable || startDate || endDate) && (
            <button
              onClick={handleClearFilter}
              style={{
                padding: "8px 14px",
                borderRadius: "8px",
                border: "1px solid #E4D2B8",
                background: "#fff",
                fontFamily: "inherit",
                fontSize: "12.5px",
                fontWeight: 600,
                color: "#6E5C4A",
                cursor: "pointer",
              }}
            >
              Xóa lọc
            </button>
          )}
        </div>

        {/* Bảng dữ liệu */}
        <div
          style={{
            background: "#FFFDF9",
            border: "1px solid #E4D2B8",
            borderRadius: "16px",
            overflowX: "auto",
          }}
        >
          <table
            style={{
              width: "100%",
              borderCollapse: "collapse",
              minWidth: "900px",
            }}
          >
            <thead>
              <tr>
                <th
                  style={{
                    textAlign: "left",
                    fontSize: "11.5px",
                    fontWeight: 700,
                    textTransform: "uppercase",
                    letterSpacing: ".4px",
                    color: "#6E5C4A",
                    padding: "13px 14px",
                    background: "#F3E9D8",
                    borderBottom: "1px solid #E4D2B8",
                  }}
                >
                  Mã hóa đơn
                </th>
                <th
                  style={{
                    textAlign: "left",
                    fontSize: "11.5px",
                    fontWeight: 700,
                    textTransform: "uppercase",
                    letterSpacing: ".4px",
                    color: "#6E5C4A",
                    padding: "13px 14px",
                    background: "#F3E9D8",
                    borderBottom: "1px solid #E4D2B8",
                  }}
                >
                  Bàn
                </th>
                <th
                  style={{
                    textAlign: "left",
                    fontSize: "11.5px",
                    fontWeight: 700,
                    textTransform: "uppercase",
                    letterSpacing: ".4px",
                    color: "#6E5C4A",
                    padding: "13px 14px",
                    background: "#F3E9D8",
                    borderBottom: "1px solid #E4D2B8",
                  }}
                >
                  Tổng tiền
                </th>
                <th
                  style={{
                    textAlign: "left",
                    fontSize: "11.5px",
                    fontWeight: 700,
                    textTransform: "uppercase",
                    letterSpacing: ".4px",
                    color: "#6E5C4A",
                    padding: "13px 14px",
                    background: "#F3E9D8",
                    borderBottom: "1px solid #E4D2B8",
                  }}
                >
                  Thời gian
                </th>
                <th
                  style={{
                    textAlign: "left",
                    fontSize: "11.5px",
                    fontWeight: 700,
                    textTransform: "uppercase",
                    letterSpacing: ".4px",
                    color: "#6E5C4A",
                    padding: "13px 14px",
                    background: "#F3E9D8",
                    borderBottom: "1px solid #E4D2B8",
                  }}
                >
                  Trạng thái
                </th>
                <th
                  style={{
                    textAlign: "center",
                    fontSize: "11.5px",
                    fontWeight: 700,
                    textTransform: "uppercase",
                    letterSpacing: ".4px",
                    color: "#6E5C4A",
                    padding: "13px 14px",
                    background: "#F3E9D8",
                    borderBottom: "1px solid #E4D2B8",
                  }}
                >
                  Hành động
                </th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td
                    colSpan="6"
                    style={{
                      textAlign: "center",
                      padding: "30px",
                      color: "#6E5C4A",
                      fontSize: "13px",
                    }}
                  >
                    Đang tải dữ liệu từ hệ thống...
                  </td>
                </tr>
              ) : invoices.length > 0 ? (
                invoices.map((inv) => (
                  <tr
                    key={inv.orderId}
                    style={{ borderBottom: "1px solid #E4D2B8" }}
                    onMouseEnter={(e) =>
                      (e.currentTarget.style.background = "#FBF4E7")
                    }
                    onMouseLeave={(e) =>
                      (e.currentTarget.style.background = "transparent")
                    }
                  >
                    <td
                      style={{
                        padding: "13px 14px",
                        fontSize: "13px",
                        fontWeight: 600,
                        color: "#6E5C4A",
                      }}
                    >
                      #{inv.orderId}
                    </td>
                    <td
                      style={{
                        padding: "13px 14px",
                        fontSize: "13px",
                        fontWeight: 600,
                      }}
                    >
                      {inv.tableName}
                    </td>
                    <td
                      style={{
                        padding: "13px 14px",
                        fontSize: "13px",
                        fontWeight: 700,
                        color: "#9C6B3A",
                      }}
                    >
                      {inv.totalAmount?.toLocaleString()} đ
                    </td>
                    <td
                      style={{
                        padding: "13px 14px",
                        fontSize: "12px",
                        color: "#6E5C4A",
                      }}
                    >
                      {inv.createdAt}
                    </td>
                    <td style={{ padding: "13px 14px", fontSize: "13px" }}>
                      <span
                        style={{
                          padding: "4px 10px",
                          background: "#EBF3E8",
                          color: "#3B7A27",
                          borderRadius: "20px",
                          fontSize: "12px",
                          fontWeight: 600,
                        }}
                      >
                        {inv.status}
                      </span>
                    </td>
                    <td style={{ padding: "13px 14px", textAlign: "center" }}>
                      <button
                        onClick={() => handleViewDetail(inv.orderId)}
                        style={{
                          padding: "6px 14px",
                          background: "#33261A",
                          color: "#E7C9A1",
                          borderRadius: "8px",
                          border: "none",
                          fontSize: "12.5px",
                          fontWeight: 600,
                          cursor: "pointer",
                        }}
                      >
                        Xem hóa đơn
                      </button>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td
                    colSpan="6"
                    style={{
                      textAlign: "center",
                      padding: "30px",
                      color: "#6E5C4A",
                      fontSize: "13px",
                    }}
                  >
                    Không có hóa đơn nào.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Modal chi tiết hóa đơn */}
      {selectedInvoice && (
        <div
          style={{
            position: "fixed",
            inset: 0,
            background: "rgba(25,18,12,0.82)",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            padding: "20px",
            zIndex: 200,
          }}
        >
          <div
            style={{
              background: "#FFFDF9",
              padding: "24px",
              borderRadius: "16px",
              width: "100%",
              maxWidth: "420px",
              border: "1px solid #E4D2B8",
              boxShadow: "0 20px 50px rgba(0,0,0,.4)",
            }}
          >
            <h3
              style={{
                fontFamily: "'Fraunces', serif",
                fontSize: "20px",
                fontWeight: 600,
                marginBottom: "16px",
                color: "#33261A",
              }}
            >
              Chi tiết hóa đơn: #{selectedInvoice.orderId}
            </h3>
            <p
              style={{
                fontSize: "13px",
                color: "#6E5C4A",
                marginBottom: "6px",
              }}
            >
              <strong>Bàn:</strong> {selectedInvoice.tableName}
            </p>
            <p
              style={{
                fontSize: "13px",
                color: "#6E5C4A",
                marginBottom: "16px",
              }}
            >
              <strong>Thời gian:</strong> {selectedInvoice.createdAt}
            </p>

            <div
              style={{
                borderTop: "1px solid #E4D2B8",
                borderBottom: "1px solid #E4D2B8",
                padding: "12px 0",
                marginBottom: "16px",
                maxHeight: "150px",
                overflowY: "auto",
              }}
            >
              {(selectedInvoice.items || []).map((item, idx) => (
                <div
                  key={idx}
                  style={{
                    display: "flex",
                    justifyContent: "space-between",
                    fontSize: "13px",
                    marginBottom: "8px",
                  }}
                >
                  <span style={{ flex: 1 }}>
                    {item.itemName} x{item.quantity}
                  </span>
                  <span style={{ fontWeight: 600 }}>
                    {(item.price * item.quantity).toLocaleString()} đ
                  </span>
                </div>
              ))}
            </div>

            <div
              style={{
                display: "flex",
                justifyContent: "space-between",
                fontSize: "16px",
                fontWeight: 700,
                marginBottom: "20px",
              }}
            >
              <span>Tổng cộng:</span>
              <span style={{ color: "#9C6B3A" }}>
                {selectedInvoice.totalAmount?.toLocaleString()} đ
              </span>
            </div>

            <button
              onClick={() => setSelectedInvoice(null)}
              style={{
                width: "100%",
                padding: "10px",
                background: "#E7C9A1",
                color: "#33261A",
                borderRadius: "8px",
                border: "none",
                fontWeight: 700,
                cursor: "pointer",
              }}
            >
              Đóng
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
