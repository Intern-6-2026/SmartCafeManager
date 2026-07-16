# 🛒 TÀI LIỆU TÍCH HỢP HỆ THỐNG SMART CAFE

Tài liệu này mô tả cách Front-end giao tiếp với Backend của hệ thống Smart Cafe.

---

# 🛠️ Thông số kỹ thuật

| Thuộc tính | Giá trị |
|------------|----------|
| Base URL | `http://localhost:8080/api/v1` |
| Authentication | JWT Bearer Token (đối với API yêu cầu đăng nhập) |
| Content-Type | `application/json` |
| Response | JSON |

---

# I. API AUTHENTICATION

## 1. Đăng nhập

**Method**

```
POST
```

**Endpoint**

```
/auth/login
```

**Request**

```json
{
  "username": "admin",
  "password": "your_password_here"
}
```

**Response**

```json
{
    "token": "...",
    "message": "Login successful",
    "requirePasswordChange": false
}
```

---

## 2. Quên mật khẩu

**Method**

```
POST
```

**Endpoint**

```
/auth/forgot-password
```

**Request**

```json
{
    "email":"admin@gmail.com"
}
```

---

## 3. Đặt lại mật khẩu

**Method**

```
POST
```

**Endpoint**

```
/auth/reset-password
```

**Request**

```json
{
    "token":"123456",
    "newPassword":"NewPassword123!"
}
```

---

# II. API USER

## 1. Xem thông tin cá nhân

**Method**

```
GET
```

**Endpoint**

```
/users/profile
```

> Header

```
Authorization: Bearer {token}
```

---

## 2. Cập nhật thông tin

**Method**

```
PUT
```

**Endpoint**

```
/users/profile
```

---

## 3. Đổi mật khẩu

**Method**

```
PUT
```

**Endpoint**

```
/users/change-password
```

---

# III. QUY TRÌNH GỌI MÓN TẠI BÀN

## 1. Xem Menu

- Món mới

```
GET /items/latest
```

- Món bán chạy

```
GET /items/best-sellers
```

↓

## 2. Chọn món

Khách chọn món.

Backend tạo Order Detail với trạng thái

```
PENDING
```

↓

## 3. Xem giỏ hàng

Hiển thị toàn bộ món có trạng thái

```
PENDING
```

↓

## 4. Gọi món

Khách bấm

```
[GỌI MÓN]
```

Backend chuyển trạng thái

```
PENDING
↓

CONFIRMED
```

↓

## 5. Theo dõi món

Hiển thị

```
CONFIRMED
SERVED
```

↓

## 6. Thanh toán

Khách xem hóa đơn

↓

Gửi yêu cầu thanh toán.

---

# IV. API MENU

## 1. Lấy toàn bộ món ăn

```
GET /api/v1/items
```

---

## 2. Lấy món mới

```
GET /api/v1/items/latest
```

---

## 3. Lấy món bán chạy

```
GET /api/v1/items/best-sellers
```

---

# V. API GIỎ HÀNG

## 1. Thêm món vào giỏ

```
POST /api/v1/items/add-item
```

Query Parameters

- tableName
- itemId
- quantity
- note

---

## 2. Xem giỏ hàng

```
GET /api/v1/items/cart
```

---

## 3. Gọi món

```
POST /api/v1/items/confirm-order
```

---

## 4. Lịch sử gọi món

```
GET /api/v1/items/order-history
```

---

# VI. API HÓA ĐƠN

## 1. Xem hóa đơn

```
GET /api/v1/items/invoice
```

---

## 2. Yêu cầu thanh toán

```
POST /api/v1/items/request-checkout
```

---

## 3. Gọi nhân viên

```
POST /api/v1/items/call-service
```

---

# VII. ENUM

## PaymentMethod

- CASH
- BANK_TRANSFER
- MOMO
- VNPAY

---

## ServiceStatus

- NORMAL
- WAITING_FOOD
- CALLING_WAITER
- REQUESTING_BILL

---

## OrderDetailStatus

- PENDING
- CONFIRMED
- SERVED
- CANCELLED