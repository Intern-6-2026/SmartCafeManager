# 🍽️ Smart Cafe Manager - Customer & Payment API

> Tài liệu tích hợp API dành cho Frontend (React / Vue / Angular)

---

# 1. Tổng quan

## Base URL

```text
http://localhost:8080
```

> Thay đổi theo từng môi trường (Development / Staging / Production).

---

## Content Type

```http
Content-Type: application/json
```

---

## CORS

Backend đã cấu hình:

```java
@CrossOrigin("*")
```

Frontend có thể gọi trực tiếp từ bất kỳ domain nào.

---

## Định danh bàn

Toàn bộ API sử dụng

```text
tableId (Long)
```

để xác định bàn.

Ví dụ:

```text
tableId = 1
```

Không truyền tên bàn như:

```text
Ban01
```

---

# 2. Axios Setup

Tạo một file:

```
src/services/api.js
```

```javascript
import axios from "axios";

const api = axios.create({
    baseURL: "http://localhost:8080",
    headers: {
        "Content-Type": "application/json",
    },
});

export default api;
```

Sau đó chỉ cần

```javascript
import api from "../services/api";
```

---

# 3. API Danh Sách

---

# 3.1 Quản lý bàn

Base Path

```
/api/v1/customer
```

---

## 1. Lấy thông tin bàn

### Endpoint

```http
GET /api/v1/customer/table-info
```

### Query Params

|Tên|Kiểu|Bắt buộc|
|----|--------|------|
|tableId|Long|✅|

### Axios

```javascript
api.get("/api/v1/customer/table-info", {
    params: {
        tableId: 1,
    },
});
```

---

## 2. Gọi phục vụ

### Endpoint

```http
POST /api/v1/customer/call-service
```

### Query Params

|Tên|Kiểu|
|------|------|
|tableId|Long|
|status|ServiceStatus|

### Enum

```
CALL_STAFF
REQUEST_PAYMENT
NEED_WATER
```

### Axios

```javascript
api.post("/api/v1/customer/call-service", null, {
    params: {
        tableId: 1,
        status: "CALL_STAFF",
    },
});
```

---

# 3.2 Giỏ hàng (PENDING)

---

## 3. Xem giỏ hàng

### Endpoint

```http
GET /api/v1/customer/cart/{tableId}
```

### Axios

```javascript
api.get(`/api/v1/customer/cart/${tableId}`);
```

### Response

```json
{
    "tableId":1,
    "tableName":"Bàn 01",
    "totalAmount":150000,
    "cartItems":[
        {
            "orderDetailId":10,
            "itemId":5,
            "itemName":"Cà phê sữa",
            "price":30000,
            "quantity":2,
            "note":"Ít đường",
            "status":"PENDING"
        }
    ]
}
```

---

## 4. Thêm món

### Endpoint

```http
POST /api/v1/customer/cart/add
```

### Query Params

|Tên|Kiểu|
|------|------|
|tableId|Long|
|itemId|Long|
|quantity|Integer|
|note|String|

### Axios

```javascript
api.post("/api/v1/customer/cart/add", null, {
    params: {
        tableId: 1,
        itemId: 5,
        quantity: 2,
        note: "Không đá",
    },
});
```

---

## 5. Cập nhật món

### Endpoint

```http
PUT /api/v1/customer/cart/items/{itemId}
```

### Axios

```javascript
api.put(`/api/v1/customer/cart/items/${itemId}`, null, {
    params: {
        tableId: 1,
        quantity: 3,
        note: "Thêm đá",
    },
});
```

> quantity <= 0 sẽ tự động xóa món.

---

## 6. Xóa món

### Endpoint

```http
DELETE /api/v1/customer/cart/remove
```

### Axios

```javascript
api.delete("/api/v1/customer/cart/remove", {
    params: {
        tableId: 1,
        itemId: 5,
    },
});
```

---

## 7. Xóa toàn bộ giỏ

### Endpoint

```http
DELETE /api/v1/customer/cart/clear
```

### Axios

```javascript
api.delete("/api/v1/customer/cart/clear", {
    params: {
        tableId: 1,
    },
});
```

---

# 3.3 Đặt món

---

## 8. Gửi món xuống bếp

### Endpoint

```http
POST /api/v1/customer/confirm-order
```

### Axios

```javascript
api.post("/api/v1/customer/confirm-order", null, {
    params: {
        tableId: 1,
    },
});
```

Sau khi gọi:

```
PENDING
↓

CONFIRMED
```

---

# 3.4 Bếp

---

## 9. Phục vụ món

```http
PUT /api/v1/customer/kitchen/serve-item
```

### Axios

```javascript
api.put("/api/v1/customer/kitchen/serve-item", null, {
    params: {
        orderDetailId: 10,
    },
});
```

---

## 10. Hủy món

```http
PUT /api/v1/customer/kitchen/cancel-item
```

### Axios

```javascript
api.put("/api/v1/customer/kitchen/cancel-item", null, {
    params: {
        orderDetailId: 10,
        reason: "Hết nguyên liệu",
    },
});
```

---

# 3.5 Thanh toán

---

## 11. Khách yêu cầu thanh toán

```http
POST /api/v1/customer/request-checkout
```

### PaymentMethod

```
CASH

BANK_TRANSFER

E_WALLET

PAYPAL
```

### Axios

```javascript
api.post("/api/v1/customer/request-checkout", null, {
    params: {
        tableId: 1,
        paymentMethod: "CASH",
    },
});
```

---

## 12. Xem hóa đơn

```http
GET /api/v1/customer/invoice-summary/{tableId}
```

### Axios

```javascript
api.get(`/api/v1/customer/invoice-summary/${tableId}`);
```

### Response

```json
{
    "tableOrderId":100,
    "tableName":"Bàn 01",
    "totalAmount":250000,
    "status":"UNPAID"
}
```

---

## 13. Chi tiết hóa đơn

```http
GET /api/v1/customer/invoice
```

### Query Params

```
tableId

tableOrderId
```

---

## 14. Thu ngân xác nhận thanh toán

```http
POST /api/v1/customer/complete-checkout
```

### Query Params

```
tableId

paymentMethod
```

Sau khi thành công

- Hóa đơn chuyển PAID

- Reset bàn

- Xóa giỏ hàng

---

# 3.6 Thanh toán PayPal

Base Path

```
/api/v1/items/payment
```

---

## 15. Tạo giao dịch

```http
POST /api/v1/items/payment/paypal
```

### Query Params

```
tableId
```

### Response

```json
{
    "approvalUrl":"https://www.sandbox.paypal.com/...",
    "qrCodeUrl":"https://api.qrserver.com/..."
}
```

Frontend có thể:

### Mobile

```text
window.location = approvalUrl;
```

hoặc

### QR

```jsx
<img src={qrCodeUrl} alt="PayPal QR"/>
```

---

## 16. Callback thành công

```http
GET /api/v1/items/payment/paypal/success
```

### Query Params

```
paymentId

PayerID

tableId
```

Sau khi PayPal redirect về Frontend

Ví dụ

```
http://localhost:3000/payment-success?tableId=1&paymentId=xxx&PayerID=yyy
```

Frontend cần lấy các Query Parameter rồi gọi API này.

Backend sẽ:

- Xác nhận thanh toán

- Hoàn tất hóa đơn

- Reset bàn

---

# 4. Luồng hoạt động

```text
Khách quét QR
      │
      ▼
Lấy thông tin bàn
GET /table-info
      │
      ▼
Chọn món
POST /cart/add
      │
      ▼
Xem giỏ hàng
GET /cart/{tableId}
      │
      ▼
Nhấn Gọi món
POST /confirm-order
      │
      ▼
Bếp nhận đơn
      │
      ├─────────────► Serve Item
      │
      └─────────────► Cancel Item
      │
      ▼
Thanh toán
      │
      ├────────── Tiền mặt
      │          POST /request-checkout
      │
      └────────── PayPal
                 │
                 ▼
          POST /payment/paypal
                 │
                 ▼
          approvalUrl / QRCode
                 │
                 ▼
      payment-success
                 │
                 ▼
GET /payment/paypal/success
                 │
                 ▼
Hoàn tất hóa đơn
```

---

# 5. Trạng thái món

|Status|Ý nghĩa|Frontend|
|--------|-----------|-------------|
|PENDING|Trong giỏ hàng|Cho phép sửa/xóa|
|CONFIRMED|Đã gửi bếp|Khóa chỉnh sửa|
|SERVED|Đã phục vụ|Hiển thị hoàn thành|
|CANCELLED|Đã hủy|Gạch ngang + giá 0|

---

# 6. Lưu ý quan trọng

## 1. tableId

Luôn truyền kiểu Number.

Ví dụ:

```javascript
tableId: 1
```

Không dùng

```javascript
tableId: "Ban01"
```

---

## 2. RequestParam

Backend đang dùng

```java
@RequestParam
```

Do đó với Axios cần viết:

```javascript
api.post(url, null, {
    params: {
        ...
    }
});
```

không gửi JSON Body.

---

## 3. Quy tắc UI

### PENDING

✅ Cho sửa

✅ Cho tăng giảm

✅ Cho ghi chú

---

### CONFIRMED

❌ Không cho sửa

---

### SERVED

Hiển thị đã phục vụ.

---

### CANCELLED

Hiển thị:

- gạch ngang tên món

- giá = 0

- lý do hủy nếu có

---

# 7. Gợi ý cấu trúc Frontend

```
src
│
├── services
│     api.js
│     customerApi.js
│     paymentApi.js
│
├── pages
│     MenuPage.jsx
│     CartPage.jsx
│     CheckoutPage.jsx
│     PaymentSuccess.jsx
│
├── components
│     CartItem.jsx
│     BillSummary.jsx
│     PaymentQRCode.jsx
│
└── hooks
      useCart.js
      useTable.js
```

---

# 8. Tổng kết

Tổng số API:

|Module|Số API|
|--------|------|
|Bàn & Phục vụ|2|
|Giỏ hàng|5|
|Đặt món|1|
|Bếp|2|
|Thanh toán|4|
|PayPal|2|

**Tổng cộng: 16 API**