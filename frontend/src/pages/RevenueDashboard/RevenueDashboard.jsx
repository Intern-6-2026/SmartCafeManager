import React from "react";
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  Legend,
} from "recharts";

const revenueByDay = [
  { day: "Thứ 2", revenue: 1200000 },
  { day: "Thứ 3", revenue: 1500000 },
  { day: "Thứ 4", revenue: 1800000 },
  { day: "Thứ 5", revenue: 1400000 },
  { day: "Thứ 6", revenue: 2200000 },
  { day: "Thứ 7", revenue: 3500000 },
  { day: "Chủ nhật", revenue: 4000000 },
];

const categoryData = [
  { name: "Cà phê", value: 45 },
  { name: "Trà sữa & Trà", value: 35 },
  { name: "Bánh ngọt", value: 20 },
];

const COLORS = ["#33261A", "#9C6B3A", "#D5A874"];

export default function RevenueDashboard() {
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
        <div
          style={{
            display: "flex",
            alignItems: "center",
            gap: "16px",
            fontSize: "13px",
            color: "#4A3627",
          }}
        >
          <span>Quản lý</span>
          <div
            style={{
              display: "flex",
              alignItems: "center",
              gap: "8px",
              fontWeight: 600,
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
        <div style={{ marginBottom: "20px" }}>
          <div
            style={{
              fontFamily: "'Fraunces', serif",
              fontSize: "22px",
              fontWeight: 600,
            }}
          >
            Thống kê thu nhập
          </div>
        </div>

        {/* Các thẻ tổng quan số liệu */}
        <div
          style={{
            display: "grid",
            gridTemplateColumns: "repeat(auto-fit, minmax(280px, 1fr))",
            gap: "20px",
            marginBottom: "24px",
          }}
        >
          <div
            style={{
              background: "#FFFDF9",
              border: "1px solid #E4D2B8",
              borderRadius: "16px",
              padding: "20px",
            }}
          >
            <p
              style={{
                fontSize: "13px",
                color: "#6E5C4A",
                marginBottom: "6px",
              }}
            >
              Tổng doanh thu hôm nay
            </p>
            <h3
              style={{
                fontSize: "24px",
                fontWeight: 700,
                color: "#9C6B3A",
                margin: 0,
              }}
            >
              2.450.000 đ
            </h3>
          </div>
          <div
            style={{
              background: "#FFFDF9",
              border: "1px solid #E4D2B8",
              borderRadius: "16px",
              padding: "20px",
            }}
          >
            <p
              style={{
                fontSize: "13px",
                color: "#6E5C4A",
                marginBottom: "6px",
              }}
            >
              Tổng hóa đơn trong ngày
            </p>
            <h3
              style={{
                fontSize: "24px",
                fontWeight: 700,
                color: "#33261A",
                margin: 0,
              }}
            >
              32 hóa đơn
            </h3>
          </div>
          <div
            style={{
              background: "#FFFDF9",
              border: "1px solid #E4D2B8",
              borderRadius: "16px",
              padding: "20px",
            }}
          >
            <p
              style={{
                fontSize: "13px",
                color: "#6E5C4A",
                marginBottom: "6px",
              }}
            >
              Doanh thu tháng này
            </p>
            <h3
              style={{
                fontSize: "24px",
                fontWeight: 700,
                color: "#33261A",
                margin: 0,
              }}
            >
              45.800.000 đ
            </h3>
          </div>
        </div>

        {/* Khu vực hiển thị biểu đồ */}
        <div
          style={{
            display: "grid",
            gridTemplateColumns: "repeat(auto-fit, minmax(500px, 1fr))",
            gap: "24px",
          }}
        >
          {/* Biểu đồ đường (Line Chart) */}
          <div
            style={{
              background: "#FFFDF9",
              border: "1px solid #E4D2B8",
              borderRadius: "16px",
              padding: "20px",
            }}
          >
            <h3
              style={{
                fontFamily: "'Fraunces', serif",
                fontSize: "16px",
                fontWeight: 600,
                marginBottom: "16px",
                color: "#33261A",
              }}
            >
              Xu hướng doanh thu theo tuần
            </h3>
            <div style={{ height: "280px" }}>
              <ResponsiveContainer width="100%" height="100%">
                <LineChart data={revenueByDay}>
                  <XAxis dataKey="day" stroke="#6E5C4A" fontSize={12} />
                  <YAxis stroke="#6E5C4A" fontSize={12} />
                  <Tooltip
                    formatter={(value) => `${value.toLocaleString()} đ`}
                  />
                  <Line
                    type="monotone"
                    dataKey="revenue"
                    stroke="#9C6B3A"
                    strokeWidth={3}
                    dot={{ r: 5, fill: "#33261A" }}
                  />
                </LineChart>
              </ResponsiveContainer>
            </div>
          </div>

          {/* Biểu đồ tròn (Pie Chart) */}
          <div
            style={{
              background: "#FFFDF9",
              border: "1px solid #E4D2B8",
              borderRadius: "16px",
              padding: "20px",
            }}
          >
            <h3
              style={{
                fontFamily: "'Fraunces', serif",
                fontSize: "16px",
                fontWeight: 600,
                marginBottom: "16px",
                color: "#33261A",
              }}
            >
              Tỷ lệ bán hàng theo danh mục
            </h3>
            <div style={{ height: "280px" }}>
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={categoryData}
                    cx="50%"
                    cy="50%"
                    outerRadius={85}
                    dataKey="value"
                    label
                  >
                    {categoryData.map((entry, index) => (
                      <Cell
                        key={`cell-${index}`}
                        fill={COLORS[index % COLORS.length]}
                      />
                    ))}
                  </Pie>
                  <Tooltip />
                  <Legend />
                </PieChart>
              </ResponsiveContainer>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
