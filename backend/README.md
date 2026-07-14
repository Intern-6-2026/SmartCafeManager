# 🛒 TÀI LIỆU TÍCH HỢP HỆ THỐNG GỌI MÓN TẠI BÀN (FRONT-END)

Tài liệu này mô tả cách Front-end giao tiếp với Backend của hệ thống gọi món tại bàn.

Tất cả API đều sử dụng chung Base URL:

```
http://localhost:8080/api/v1/items
```

---

# 🛠️ Thông số kỹ thuật

| Thuộc tính | Giá trị |
|------------|----------|
| Base URL | `http://localhost:8080/api/v1/items` |
| Authentication | Không yêu cầu Token |
| Content-Type | `application/x-www-form-urlencoded` (Query Parameters) |
| Response | JSON hoặc String |

---

# 🗺️ Quy trình nghiệp vụ

## 1. Xem Menu

Hiển thị toàn bộ món ăn hoặc danh sách:

- Món mới: http://localhost:8080/api/v1/items/best-sellers
- Bán chạy: http://localhost:8080/api/v1/items/latest

↓

## 2. Chọn món

Khách chọn món.

Backend tạo Order Detail có trạng thái

```
PENDING
```

↓

## 3. Xem giỏ hàng

Hiển thị toàn bộ món đang ở trạng thái

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

Hiển thị các món

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

# 📋 Danh sách API

## I. API MENU

---

## 1. Lấy toàn bộ món ăn

**Method**

```
GET
```

**Endpoint**

```
/
```

**Request**

```
GET /api/v1/items
```

**Response**

```json
[
  {
    "itemId": 5,
    "itemCode": "BN01",
    "categoryId": 3,
    "categoryName": "Bánh ngọt",
    "itemName": "Tiramisu",
    "price": 35000,
    "description": "Bánh ngọt thơm ngon chuẩn vị Ý",
    "imageUrl": "https://res.cloudinary.com/...",
    "isAvailable": true,
    "totalOrderCount": 40
  }
]
```

---

## 2. Lấy món mới nhất

**Method**

```
GET
```

**Endpoint**

```
/latest
```

**Request**

```
GET /api/v1/items/latest
```

---

## 3. Lấy món bán chạy

**Method**

```
GET
```

**Endpoint**

```
/best-sellers
```

**Request**

```
GET /api/v1/items/best-sellers
```

---

# II. API GIỎ HÀNG

---

## 4. Thêm món vào giỏ

**Method**

```
POST
```

**Endpoint**

```
/add-item
```

**Query Params**

|Tên|Kiểu|
|----|----|
|tableName|String|
|itemId|Long|
|quantity|Integer|
|note|String|

**Ví dụ**

```
POST /api/v1/items/add-item?tableName=Ban01&itemId=5&quantity=2&note=Ít đá
```

**Success**

```
Đã thêm món vào giỏ hàng tạm thời!
```

**Error**

```json
{
  "status": 500,
  "message": "Món ăn không tồn tại!"
}
```

---

## 5. Xem giỏ hàng

**Method**

```
GET
```

**Endpoint**

```
/cart
```

**Ví dụ**

```
GET /api/v1/items/cart?tableName=Ban01
```

**Response**

```json
[
  {
    "orderDetailId": 401,
    "itemName": "Tiramisu",
    "quantity": 2,
    "unitPrice": 35000,
    "status": "PENDING"
  }
]
```

---

## 6. Gọi món

**Method**

```
POST
```

**Endpoint**

```
/confirm-order
```

**Ví dụ**

```
POST /api/v1/items/confirm-order?tableName=Ban01
```

**Success**

```
Đã gửi đơn hàng thành công xuống bếp!
```

**Error**

```json
{
  "status": 500,
  "message": "Giỏ hàng của bạn đang trống, không thể gọi món!"
}
```

---

## 7. Lịch sử gọi món

**Method**

```
GET
```

**Endpoint**

```
/order-history
```

**Ví dụ**

```
GET /api/v1/items/order-history?tableName=Bàn 01
```

---

# III. API HÓA ĐƠN

---

## 8. Xem hóa đơn

**Method**

```
GET
```

**Endpoint**

```
/invoice
```

**Ví dụ**

```
GET /api/v1/items/invoice?tableName= Bàn 01
```

**Response**

```json
{
    "tableOrderId": 3,
    "tableName": "Ban01",
    "totalAmount": 120000,
    "orderStatus": "OPEN",
    "serviceStatus": "NORMAL",
    "openAt": "2026-07-13T20:57:54.392032",
    "orderDetails": [
        {
            "orderDetailId": 4,
            "quantity": 2,
            "unitPrice": 25000,
            "note": "",
            "status": "PENDING",
            "itemId": 1,
            "itemName": "Cà phê Đen Đá",
            "imageUrl": null
        },
        {
            "orderDetailId": 5,
            "quantity": 2,
            "unitPrice": 35000,
            "note": "Ít đá",
            "status": "PENDING",
            "itemId": 5,
            "itemName": "Tiramisu",
            "imageUrl": null
        }
    ]
}
```

---

## 9. Yêu cầu thanh toán

**Method**

```
POST
```

**Endpoint**

```
/request-checkout
```

**Ví dụ**

```
POST /api/v1/items/request-checkout?tableName=Ban01&paymentMethod=BANK_TRANSFER
```

**Success**

```
Yêu cầu thanh toán bằng BANK_TRANSFER đã được gửi!
```

---

## 10. Gọi nhân viên

**Method**

```
POST
```

**Endpoint**

```
/call-service
```

**Ví dụ**

```
POST /api/v1/items/call-service?tableName=Ban01&status=CALLING_WAITER
```

**Success**

```
Hệ thống đã ghi nhận yêu cầu: CALLING_WAITER
```

---

# 📊 ENUM

## PaymentMethod

|Giá trị|Ý nghĩa|
|--------|--------|
|CASH|Tiền mặt|
|BANK_TRANSFER|Chuyển khoản|
|MOMO|Ví MoMo|
|VNPAY|VNPay|

---

## ServiceStatus

|Giá trị|Ý nghĩa|
|--------|--------|
|NORMAL|Bình thường|
|WAITING_FOOD|Đang chờ món|
|CALLING_WAITER|Đang gọi nhân viên|
|REQUESTING_BILL|Yêu cầu thanh toán|

---

## OrderDetail Status

|Giá trị|Ý nghĩa|
|--------|--------|
|PENDING|Đã thêm vào giỏ|
|CONFIRMED|Đã gửi xuống bếp|
|SERVED|Đã phục vụ|
|CANCELLED|Đã hủy|

---