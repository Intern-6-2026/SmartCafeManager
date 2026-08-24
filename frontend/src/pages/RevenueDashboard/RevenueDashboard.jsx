import React, { useState, useEffect } from "react";
import { useNavigate, Link } from "react-router-dom";
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
import MenuButton from "../../components/menu-button";

const COLORS = ["#33261A", "#9C6B3A", "#D5A874", "#6E5C4A"];

export default function RevenueDashboard() {
  const navigate = useNavigate();
  const [stats, setStats] = useState({
    todayRevenue: 0,
    todayOrderCount: 0,
    monthRevenue: 0,
    weeklyRevenue: [],
    categorySales: [],
  });
  const [loading, setLoading] = useState(true);

  // Lấy thông tin user động từ localStorage
  const userName = localStorage.getItem("userName") || "Thành viên";
  const getInitials = (name) => {
    if (!name) return "U";
    const words = name.trim().split(" ");
    if (words.length >= 2) {
      return (words[0][0] + words[words.length - 1][0]).toUpperCase();
    }
    return name.substring(0, 2).toUpperCase();
  };
  const userInitial = getInitials(userName);

  useEffect(() => {
    const fetchDashboardStats = async () => {
      try {
        const response = await fetch("/api/v1/admin/statistics/dashboard", {
          method: "GET",
          headers: {
            "Content-Type": "application/json",
            Authorization: "Bearer " + localStorage.getItem("token"),
          },
        });

        if (response.ok) {
          const data = await response.json();
          setStats({
            todayRevenue: data.todayRevenue || 0,
            todayOrderCount: data.todayOrderCount || 0,
            monthRevenue: data.monthRevenue || 0,
            weeklyRevenue: data.weeklyRevenue || [],
            categorySales: data.categorySales || [],
          });
        }
      } catch (error) {
        console.error("Lỗi khi gọi API thống kê:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchDashboardStats();
  }, []);

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
        <Link
          to="/home"
          style={{
            display: "flex",
            alignItems: "center",
            gap: "10px",
            textDecoration: "none",
            color: "inherit",
          }}
        >
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
        </Link>

        <div className="header-title">THỐNG KÊ THU NHẬP</div>

        <MenuButton></MenuButton>
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
              {loading
                ? "Đang tải..."
                : `${stats.todayRevenue.toLocaleString()} đ`}
            </h3>
          </div>

          <div
            onClick={() => navigate("/admin/invoices")}
            title="Nhấn để xem chi tiết danh sách hóa đơn"
            style={{
              background: "#FFFDF9",
              border: "1px solid #E4D2B8",
              borderRadius: "16px",
              padding: "20px",
              cursor: "pointer",
              transition: "all 0.2s ease",
            }}
            onMouseEnter={(e) => {
              e.currentTarget.style.borderColor = "#9C6B3A";
              e.currentTarget.style.transform = "translateY(-2px)";
              e.currentTarget.style.boxShadow = "0 6px 15px rgba(0,0,0,0.05)";
            }}
            onMouseLeave={(e) => {
              e.currentTarget.style.borderColor = "#E4D2B8";
              e.currentTarget.style.transform = "translateY(0)";
              e.currentTarget.style.boxShadow = "none";
            }}
          >
            <div
              style={{
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
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
              <span
                style={{ fontSize: "12px", color: "#9C6B3A", fontWeight: 600 }}
              >
                Xem tất cả ➔
              </span>
            </div>
            <h3
              style={{
                fontSize: "24px",
                fontWeight: 700,
                color: "#33261A",
                margin: 0,
              }}
            >
              {loading ? "Đang tải..." : `${stats.todayOrderCount} hóa đơn`}
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
              {loading
                ? "Đang tải..."
                : `${stats.monthRevenue.toLocaleString()} đ`}
            </h3>
          </div>
        </div>

        {/* Biểu đồ */}
        <div
          style={{
            display: "grid",
            gridTemplateColumns: "repeat(auto-fit, minmax(500px, 1fr))",
            gap: "24px",
          }}
        >
          {/* Biểu đồ đường */}
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
                <LineChart data={stats.weeklyRevenue}>
                  <XAxis dataKey="dayOfWeek" stroke="#6E5C4A" fontSize={12} />
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

          {/* Biểu đồ tròn */}
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
                    data={stats.categorySales}
                    cx="50%"
                    cy="50%"
                    outerRadius={85}
                    dataKey="totalQuantity"
                    nameKey="categoryName"
                    label
                  >
                    {stats.categorySales.map((entry, index) => (
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
