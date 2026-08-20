import React, { useState, useEffect } from "react";
import { Link } from "react-router-dom";

export default function UserManagement() {
  // Tab hiện tại: "employees" hoặc "customers"
  const [activeTab, setActiveTab] = useState("employees");

  // Dữ liệu và trạng thái chung
  const [employees, setEmployees] = useState([]);
  const [customers, setCustomers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [currentId, setCurrentId] = useState(null);

  // Toast Notification
  const [toast, setToast] = useState({
    show: false,
    message: "",
    type: "success",
  });

  const showToast = (message, type = "success") => {
    setToast({ show: true, message, type });
    setTimeout(() => {
      setToast({ show: false, message: "", type: "success" });
    }, 3000);
  };

  // Form State tổng hợp cho cả 2 đối tượng
  const [formData, setFormData] = useState({
    username: "",
    password: "",
    email: "",
    fullName: "",
    dateOfBirth: "",
    gender: "MALE",
    phoneNumber: "",
    address: "",
    salary: "", // Dành riêng cho nhân viên
    loyaltyPoints: 0, // Dành riêng cho khách hàng
    image: null,
  });

  const userName = localStorage.getItem("userName") || "Quản trị viên";
  const getInitials = (name) => {
    if (!name) return "A";
    const words = name.trim().split(" ");
    if (words.length >= 2) {
      return (words[0][0] + words[words.length - 1][0]).toUpperCase();
    }
    return name.substring(0, 2).toUpperCase();
  };
  const userInitial = getInitials(userName);

  // 1. Gọi API lấy danh sách Nhân viên[cite: 1]
  const fetchEmployees = async () => {
    setLoading(true);
    try {
      const response = await fetch("/api/v1/admin/employees", {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          Authorization: "Bearer " + localStorage.getItem("token"),
        },
      });
      if (response.ok) {
        const data = await response.json();
        setEmployees(data);
      } else {
        showToast("Không thể tải danh sách nhân viên!", "error");
      }
    } catch (error) {
      console.error("Lỗi API nhân viên:", error);
    } finally {
      setLoading(false);
    }
  };

  // 2. Gọi API lấy danh sách Khách hàng[cite: 2]
  const fetchCustomers = async () => {
    setLoading(true);
    try {
      const response = await fetch("/api/v1/admin/customers", {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          Authorization: "Bearer " + localStorage.getItem("token"),
        },
      });
      if (response.ok) {
        const data = await response.json();
        setCustomers(data);
      } else {
        showToast("Không thể tải danh sách khách hàng!", "error");
      }
    } catch (error) {
      console.error("Lỗi API khách hàng:", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (activeTab === "employees") {
      fetchEmployees();
    } else {
      fetchCustomers();
    }
  }, [activeTab]);

  const handleChange = (e) => {
    const { name, value, files } = e.target;
    if (name === "image") {
      setFormData({ ...formData, image: files[0] });
    } else {
      setFormData({ ...formData, [name]: value });
    }
  };

  const handleOpenAdd = () => {
    setIsEditing(false);
    setFormData({
      username: "",
      password: "",
      email: "",
      fullName: "",
      dateOfBirth: "",
      gender: "MALE",
      phoneNumber: "",
      address: "",
      salary: "",
      loyaltyPoints: 0,
      image: null,
    });
    setShowModal(true);
  };

  const handleOpenEdit = (item) => {
    setIsEditing(true);
    const idField =
      activeTab === "employees" ? item.employeeId : item.customerId;
    setCurrentId(idField);
    setFormData({
      username: item.username || "",
      password: "",
      email: item.email || "",
      fullName: item.fullName || "",
      dateOfBirth: item.dateOfBirth ? item.dateOfBirth.split("T")[0] : "",
      gender: item.gender || "MALE",
      phoneNumber: item.phoneNumber || "",
      address: item.address || "",
      salary: item.salary || "",
      loyaltyPoints: item.loyaltyPoints || 0,
      image: null,
    });
    setShowModal(true);
  };

  // 3. Submit Form Thêm / Sửa chung cho cả 2 phân hệ[cite: 1, 2]
  const handleSubmit = async (e) => {
    e.preventDefault();
    const data = new FormData();
    for (const key in formData) {
      // Nếu ở tab khách hàng thì không cần gửi trường salary và ngược lại
      if (activeTab === "employees" && key === "loyaltyPoints") continue;
      if (activeTab === "customers" && key === "salary") continue;

      if (formData[key] !== null && formData[key] !== "") {
        data.append(key, formData[key]);
      }
    }

    const baseUrl =
      activeTab === "employees"
        ? "/api/v1/admin/employees"
        : "/api/v1/admin/customers";
    const url = isEditing ? `${baseUrl}/${currentId}` : baseUrl;

    try {
      const response = await fetch(url, {
        method: "POST", // Hỗ trợ multipart/form-data
        headers: {
          Authorization: "Bearer " + localStorage.getItem("token"),
        },
        body: data,
      });

      if (response.ok) {
        showToast(
          isEditing ? "Cập nhật thông tin thành công!" : "Thêm mới thành công!",
          "success",
        );
        setShowModal(false);
        if (activeTab === "employees") fetchEmployees();
        else fetchCustomers();
      } else {
        const errText = await response.text();
        showToast(
          "Lỗi: " + (errText || "Không thể thực hiện thao tác"),
          "error",
        );
      }
    } catch (error) {
      console.error("Lỗi hệ thống:", error);
      showToast("Đã xảy ra lỗi kết nối!", "error");
    }
  };

  // 4. Xóa / Khóa đối tượng[cite: 1, 2]
  const handleDelete = async (id) => {
    const confirmMsg =
      activeTab === "employees"
        ? "Bạn có chắc chắn muốn xóa nhân viên này không?"
        : "Bạn có chắc chắn muốn khóa/xóa tài khoản khách hàng này không?";

    if (window.confirm(confirmMsg)) {
      const baseUrl =
        activeTab === "employees"
          ? "/api/v1/admin/employees"
          : "/api/v1/admin/customers";
      try {
        const response = await fetch(`${baseUrl}/${id}`, {
          method: "DELETE",
          headers: {
            Authorization: "Bearer " + localStorage.getItem("token"),
          },
        });

        if (response.ok) {
          showToast("Thực hiện thành công!", "success");
          if (activeTab === "employees") fetchEmployees();
          else fetchCustomers();
        } else {
          showToast("Thực hiện thất bại!", "error");
        }
      } catch (error) {
        console.error("Lỗi khi xóa:", error);
      }
    }
  };

  return (
    <div
      style={{
        minHeight: "100vh",
        backgroundColor: "#F6EEE1",
        color: "#33261A",
        fontFamily: "'Be Vietnam Pro', sans-serif",
        position: "relative",
      }}
    >
      {/* Toast Notification */}
      {toast.show && (
        <div
          style={{
            position: "fixed",
            top: "20px",
            right: "20px",
            background: toast.type === "success" ? "#27AE60" : "#C56854",
            color: "#FFF",
            padding: "12px 20px",
            borderRadius: "10px",
            boxShadow: "0 6px 20px rgba(0,0,0,0.15)",
            zIndex: 9999,
            fontWeight: 600,
            fontSize: "14px",
          }}
        >
          {toast.message}
        </div>
      )}

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

        <div style={{ display: "flex", alignItems: "center", gap: "16px" }}>
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
              {userInitial}
            </div>
            {userName}
          </div>
        </div>
      </div>

      {/* Nội dung chính */}
      <div style={{ maxWidth: "1240px", margin: "0 auto", padding: "22px" }}>
        {/* Header & Tabs chuyển đổi */}
        <div
          style={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            marginBottom: "20px",
            flexWrap: "wrap",
            gap: "12px",
          }}
        >
          <div style={{ display: "flex", gap: "10px" }}>
            <button
              onClick={() => setActiveTab("employees")}
              style={{
                background: activeTab === "employees" ? "#33261A" : "#E4D2B8",
                color: activeTab === "employees" ? "#E7C9A1" : "#33261A",
                border: "none",
                padding: "10px 20px",
                borderRadius: "10px",
                fontWeight: 700,
                cursor: "pointer",
                fontFamily: "'Fraunces', serif",
                fontSize: "16px",
                transition: "all 0.2s",
              }}
            >
              Quản lý Nhân viên
            </button>
            <button
              onClick={() => setActiveTab("customers")}
              style={{
                background: activeTab === "customers" ? "#33261A" : "#E4D2B8",
                color: activeTab === "customers" ? "#E7C9A1" : "#33261A",
                border: "none",
                padding: "10px 20px",
                borderRadius: "10px",
                fontWeight: 700,
                cursor: "pointer",
                fontFamily: "'Fraunces', serif",
                fontSize: "16px",
                transition: "all 0.2s",
              }}
            >
              Quản lý Khách hàng
            </button>
          </div>

          <button
            onClick={handleOpenAdd}
            style={{
              background: "#9C6B3A",
              color: "#FFFDF9",
              border: "none",
              padding: "10px 18px",
              borderRadius: "10px",
              fontWeight: 600,
              cursor: "pointer",
              transition: "background 0.2s",
            }}
            onMouseEnter={(e) => (e.currentTarget.style.background = "#7A522C")}
            onMouseLeave={(e) => (e.currentTarget.style.background = "#9C6B3A")}
          >
            {activeTab === "employees"
              ? "+ Thêm nhân viên mới"
              : "+ Thêm khách hàng mới"}
          </button>
        </div>

        {/* Bảng dữ liệu hiển thị động theo Tab */}
        <div
          style={{
            background: "#FFFDF9",
            border: "1px solid #E4D2B8",
            borderRadius: "16px",
            overflow: "hidden",
            boxShadow: "0 4px 12px rgba(0,0,0,0.03)",
          }}
        >
          <table
            style={{
              width: "100%",
              borderCollapse: "collapse",
              textAlign: "left",
            }}
          >
            <thead>
              <tr
                style={{
                  background: "#F4EAD9",
                  borderBottom: "1px solid #E4D2B8",
                  fontSize: "14px",
                }}
              >
                <th style={{ padding: "14px 16px" }}>Ảnh</th>
                <th style={{ padding: "14px 16px" }}>Họ và tên</th>
                <th style={{ padding: "14px 16px" }}>Username</th>
                <th style={{ padding: "14px 16px" }}>Email</th>
                <th style={{ padding: "14px 16px" }}>Số điện thoại</th>
                <th style={{ padding: "14px 16px" }}>
                  {activeTab === "employees" ? "Lương cơ bản" : "Điểm tích lũy"}
                </th>
                <th style={{ padding: "14px 16px", textAlign: "center" }}>
                  Hành động
                </th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td
                    colSpan="7"
                    style={{
                      textAlign: "center",
                      padding: "30px",
                      color: "#6E5C4A",
                    }}
                  >
                    Đang tải dữ liệu...
                  </td>
                </tr>
              ) : activeTab === "employees" && employees.length === 0 ? (
                <tr>
                  <td
                    colSpan="7"
                    style={{
                      textAlign: "center",
                      padding: "30px",
                      color: "#6E5C4A",
                    }}
                  >
                    Chưa có nhân viên nào trong hệ thống.
                  </td>
                </tr>
              ) : activeTab === "customers" && customers.length === 0 ? (
                <tr>
                  <td
                    colSpan="7"
                    style={{
                      textAlign: "center",
                      padding: "30px",
                      color: "#6E5C4A",
                    }}
                  >
                    Chưa có khách hàng nào trong hệ thống.
                  </td>
                </tr>
              ) : (
                (activeTab === "employees" ? employees : customers).map(
                  (item) => {
                    const itemId =
                      activeTab === "employees"
                        ? item.employeeId
                        : item.customerId;
                    return (
                      <tr
                        key={itemId}
                        style={{
                          borderBottom: "1px solid #EFCFA8",
                          fontSize: "14px",
                        }}
                      >
                        <td style={{ padding: "12px 16px" }}>
                          <img
                            src={
                              item.imageUrl || "https://via.placeholder.com/40"
                            }
                            alt="avatar"
                            style={{
                              width: "38px",
                              height: "38px",
                              borderRadius: "50%",
                              objectFit: "cover",
                            }}
                          />
                        </td>
                        <td style={{ padding: "12px 16px", fontWeight: 600 }}>
                          {item.fullName}
                        </td>
                        <td style={{ padding: "12px 16px" }}>
                          {item.username}
                        </td>
                        <td style={{ padding: "12px 16px" }}>{item.email}</td>
                        <td style={{ padding: "12px 16px" }}>
                          {item.phoneNumber || "---"}
                        </td>
                        <td
                          style={{
                            padding: "12px 16px",
                            fontWeight: activeTab === "customers" ? 600 : 400,
                            color:
                              activeTab === "customers" ? "#9C6B3A" : "inherit",
                          }}
                        >
                          {activeTab === "employees"
                            ? item.salary
                              ? `${item.salary.toLocaleString()} đ`
                              : "---"
                            : `${item.loyaltyPoints || 0} điểm`}
                        </td>
                        <td
                          style={{ padding: "12px 16px", textAlign: "center" }}
                        >
                          <button
                            onClick={() => handleOpenEdit(item)}
                            style={{
                              background: "#D5A874",
                              color: "#33261A",
                              border: "none",
                              padding: "6px 12px",
                              borderRadius: "6px",
                              marginRight: "8px",
                              cursor: "pointer",
                              fontWeight: 600,
                            }}
                          >
                            Sửa
                          </button>
                          <button
                            onClick={() => handleDelete(itemId)}
                            style={{
                              background: "#C56854",
                              color: "#FFF",
                              border: "none",
                              padding: "6px 12px",
                              borderRadius: "6px",
                              cursor: "pointer",
                              fontWeight: 600,
                            }}
                          >
                            {activeTab === "employees" ? "Xóa" : "Khóa"}
                          </button>
                        </td>
                      </tr>
                    );
                  },
                )
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Modal Thêm / Sửa */}
      {showModal && (
        <div
          style={{
            position: "fixed",
            inset: 0,
            background: "rgba(51, 38, 26, 0.5)",
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
            zIndex: 1000,
          }}
        >
          <div
            style={{
              background: "#FFFDF9",
              border: "1px solid #E4D2B8",
              borderRadius: "16px",
              padding: "24px",
              width: "100%",
              maxWidth: "520px",
              maxHeight: "90vh",
              overflowY: "auto",
              boxShadow: "0 10px 25px rgba(0,0,0,0.1)",
            }}
          >
            <h3
              style={{
                fontFamily: "'Fraunces', serif",
                fontSize: "18px",
                fontWeight: 600,
                marginBottom: "16px",
                color: "#33261A",
              }}
            >
              {isEditing
                ? activeTab === "employees"
                  ? "Sửa thông tin nhân viên"
                  : "Sửa thông tin khách hàng"
                : activeTab === "employees"
                  ? "Thêm nhân viên mới"
                  : "Thêm khách hàng mới"}
            </h3>

            <form
              onSubmit={handleSubmit}
              style={{ display: "flex", flexDirection: "column", gap: "12px" }}
            >
              <div>
                <label
                  style={{
                    fontSize: "13px",
                    fontWeight: 600,
                    color: "#6E5C4A",
                  }}
                >
                  Tên đăng nhập (Username)
                </label>
                <input
                  type="text"
                  name="username"
                  value={formData.username}
                  onChange={handleChange}
                  required
                  disabled={isEditing}
                  style={{
                    width: "100%",
                    padding: "10px",
                    borderRadius: "8px",
                    border: "1px solid #E4D2B8",
                    marginTop: "4px",
                  }}
                />
              </div>

              {!isEditing && (
                <div>
                  <label
                    style={{
                      fontSize: "13px",
                      fontWeight: 600,
                      color: "#6E5C4A",
                    }}
                  >
                    Mật khẩu
                  </label>
                  <input
                    type="password"
                    name="password"
                    value={formData.password}
                    onChange={handleChange}
                    required
                    style={{
                      width: "100%",
                      padding: "10px",
                      borderRadius: "8px",
                      border: "1px solid #E4D2B8",
                      marginTop: "4px",
                    }}
                  />
                </div>
              )}

              <div>
                <label
                  style={{
                    fontSize: "13px",
                    fontWeight: 600,
                    color: "#6E5C4A",
                  }}
                >
                  Họ và tên
                </label>
                <input
                  type="text"
                  name="fullName"
                  value={formData.fullName}
                  onChange={handleChange}
                  required
                  style={{
                    width: "100%",
                    padding: "10px",
                    borderRadius: "8px",
                    border: "1px solid #E4D2B8",
                    marginTop: "4px",
                  }}
                />
              </div>

              <div>
                <label
                  style={{
                    fontSize: "13px",
                    fontWeight: 600,
                    color: "#6E5C4A",
                  }}
                >
                  Email
                </label>
                <input
                  type="email"
                  name="email"
                  value={formData.email}
                  onChange={handleChange}
                  required
                  style={{
                    width: "100%",
                    padding: "10px",
                    borderRadius: "8px",
                    border: "1px solid #E4D2B8",
                    marginTop: "4px",
                  }}
                />
              </div>

              <div
                style={{
                  display: "grid",
                  gridTemplateColumns: "1fr 1fr",
                  gap: "12px",
                }}
              >
                <div>
                  <label
                    style={{
                      fontSize: "13px",
                      fontWeight: 600,
                      color: "#6E5C4A",
                    }}
                  >
                    Số điện thoại
                  </label>
                  <input
                    type="text"
                    name="phoneNumber"
                    value={formData.phoneNumber}
                    onChange={handleChange}
                    style={{
                      width: "100%",
                      padding: "10px",
                      borderRadius: "8px",
                      border: "1px solid #E4D2B8",
                      marginTop: "4px",
                    }}
                  />
                </div>
                <div>
                  <label
                    style={{
                      fontSize: "13px",
                      fontWeight: 600,
                      color: "#6E5C4A",
                    }}
                  >
                    {activeTab === "employees"
                      ? "Lương cơ bản"
                      : "Điểm tích lũy"}
                  </label>
                  <input
                    type="number"
                    name={
                      activeTab === "employees" ? "salary" : "loyaltyPoints"
                    }
                    value={
                      activeTab === "employees"
                        ? formData.salary
                        : formData.loyaltyPoints
                    }
                    onChange={handleChange}
                    style={{
                      width: "100%",
                      padding: "10px",
                      borderRadius: "8px",
                      border: "1px solid #E4D2B8",
                      marginTop: "4px",
                    }}
                  />
                </div>
              </div>

              <div>
                <label
                  style={{
                    fontSize: "13px",
                    fontWeight: 600,
                    color: "#6E5C4A",
                  }}
                >
                  Ảnh đại diện (Cloudinary)
                </label>
                <input
                  type="file"
                  name="image"
                  onChange={handleChange}
                  style={{
                    width: "100%",
                    padding: "8px",
                    borderRadius: "8px",
                    border: "1px solid #E4D2B8",
                    marginTop: "4px",
                    background: "#FFF",
                  }}
                />
              </div>

              <div
                style={{
                  display: "flex",
                  justifyContent: "flex-end",
                  gap: "10px",
                  marginTop: "16px",
                }}
              >
                <button
                  type="button"
                  onClick={() => setShowModal(false)}
                  style={{
                    background: "#E4D2B8",
                    color: "#33261A",
                    border: "none",
                    padding: "10px 16px",
                    borderRadius: "8px",
                    fontWeight: 600,
                    cursor: "pointer",
                  }}
                >
                  Hủy
                </button>
                <button
                  type="submit"
                  style={{
                    background: "#9C6B3A",
                    color: "#FFFDF9",
                    border: "none",
                    padding: "10px 18px",
                    borderRadius: "8px",
                    fontWeight: 600,
                    cursor: "pointer",
                  }}
                >
                  Lưu lại
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
