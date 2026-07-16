<<<<<<< HEAD
# 🛒 TÀI LIỆU TÍCH HỢP HỆ THỐNG SMART CAFE

Tài liệu này mô tả cách Front-end giao tiếp với Backend của hệ thống Smart Cafe.
=======
# ☕ Smart Cafe Management - API Testing Guide (Postman)

Tài liệu này cung cấp hướng dẫn chi tiết về cách thiết lập môi trường và thực hiện kiểm thử các API của hệ thống **Smart Cafe Management** bằng công cụ Postman. Dự án hiện tại hỗ trợ các tính năng về **Xác thực người dùng (Authentication)** và **Quản lý hồ sơ (User Profile)** dành cho 3 phân quyền: `ADMIN`, `STAFF`, và `CUSTOMER`.

---

## 📑 Mục lục
1. [Yêu cầu chuẩn bị](#1-yêu-cầu-chuẩn-bị)
2. [Thiết lập môi trường Postman](#2-thiết-lập-môi-trường-postman)
3. [Danh sách tài khoản Test](#3-danh-sách-tài-khoản-test)
4. [Danh sách API & Test Cases](#4-danh-sách-api--test-cases)
   - [Module Authentication](#module-1-authentication)
   - [Module User Profile](#module-2-user-profile)
5. [Quy trình Test thực tế (Workflow)](#5-quy-trình-test-thực-tế-khuyến-nghị)

---

## 1. Yêu cầu chuẩn bị

Để bắt đầu kiểm thử, vui lòng đảm bảo hệ thống của bạn đã đáp ứng các điều kiện sau:
* **Postman:** Đã cài đặt phiên bản mới nhất.
* **Server Backend:** Đang chạy ở môi trường local tại port `8080`.
* **Cơ sở dữ liệu:** Đã import thành công file `smart_cafe_management.sql` vào MySQL.

---

## 2. Thiết lập môi trường Postman

Để quá trình kiểm thử diễn ra trơn tru (đặc biệt là việc tự động xử lý JWT Token), hãy tạo một **Environment** mới trong Postman (ví dụ: `Smart Cafe Local`) và cấu hình các biến sau:

| Tên biến (VARIABLE) | Value mặc định (INITIAL VALUE) | Mô tả |
| :--- | :--- | :--- |
| `baseUrl` | `http://localhost:8080/api/v1` | URL gốc của toàn bộ API |
| `token` | *(Để trống)* | Token JWT sẽ được tự động lưu vào đây |

### ⚡ Tự động lưu JWT Token
Tại request **`POST /auth/login`**, hãy chuyển sang tab **Tests** trong Postman và dán đoạn mã script sau. Đoạn mã này sẽ tự động bắt token từ Response và lưu vào môi trường:

```javascript
var jsonData = pm.response.json();
if (jsonData.token) {
    pm.environment.set("token", jsonData.token);
    console.log("Đã lưu JWT Token vào môi trường!");
}

```
>>>>>>> 6f367d9e48f4cbd30c14a9e766cc61f749f977ed

---

## 3. Danh sách tài khoản Test

<<<<<<< HEAD
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
=======
Dữ liệu gốc trong DB đã cung cấp sẵn các tài khoản dưới đây (trạng thái `ACTIVE`). Sử dụng các tài khoản này để kiểm tra tính phân quyền của hệ thống:

| Username | Email | Vai trò (Role) | Mô tả |
| --- | --- | --- | --- |
| `admin` | codegymintern@gmail.com | **ADMIN** | Quản trị viên hệ thống |
| `thungan01` | thungan1@smartcafe.vn | **STAFF** | Thu ngân (Nhân viên) |
| `phabep01` | phabep1@smartcafe.vn | **STAFF** | Pha chế / Bếp (Nhân viên) |
| `khach_vip01` | khachvip1@gmail.com | **CUSTOMER** | Khách hàng VIP |
| `khach_thuong01` | khachthuong1@gmail.com | **CUSTOMER** | Khách hàng thường |

> **💡 Lưu ý:** Mật khẩu trong DB được mã hóa bằng Bcrypt. Khi test thực tế, hãy sử dụng mật khẩu mặc định được quy định lúc tạo dữ liệu mẫu (thường là `123456`).

---

## 4. Danh sách API & Test Cases
>>>>>>> 6f367d9e48f4cbd30c14a9e766cc61f749f977ed

### MODULE 1: AUTHENTICATION

<<<<<<< HEAD
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
=======
> **Base Path:** `/api/v1/auth` *(Công khai, không yêu cầu Token)*

#### 1.1. Đăng nhập hệ thống (Login)
* **Mô tả chuyên sâu về chức năng:**
* Đây là cổng an ninh đầu tiên của hệ thống. API thực hiện đối chiếu thông tin `username` và `password` với cơ sở dữ liệu (mật khẩu được kiểm tra qua thuật toán mã hóa Bcrypt).
* Kiểm tra cờ trạng thái tài khoản: Nếu tài khoản bị khóa (`INACTIVE`) hoặc đã bị xóa mềm (`deleted_at is not null`), hệ thống sẽ từ chối truy cập ngay lập tức.
* Nếu hợp lệ, Backend sẽ sinh ra một chuỗi **JWT (JSON Web Token)** có thời hạn để Frontend sử dụng cho các request bảo mật sau này.
* **Đặc biệt:** API có tính toán thời gian `password_changed_at`. Nếu mật khẩu đã quá hạn 30 ngày chưa đổi, cờ `requirePasswordChange: true` sẽ được trả về.
* Nếu là **`true`**: Frontend **chặn không cho vào Trang chủ**, buộc chuyển hướng (Redirect) ngay sang màn hình *Đổi mật khẩu bắt buộc*, kèm thông báo: *"Mật khẩu của bạn đã hết hạn 30 ngày, vui lòng đổi mật khẩu mới để tiếp tục"*.
  
* **Phương thức:** `POST`
* **URL:** `{{baseUrl}}/auth/login`
* **Headers:** `Content-Type: application/json`
* **Body:**
```json
{
  "username": "admin",
  "password": "your_password_here"
}
>>>>>>> 6f367d9e48f4cbd30c14a9e766cc61f749f977ed

```

* **Kỳ vọng (Expected Responses):**
* **`200 OK`**:
```json
{
  "token": "eyJhbGciOiJlUzI1NiJ9...",
  "message": "Login successful!",
  "requirePasswordChange": false
}

```

* **`400 Bad Request`**: `"Incorrect password!"` hoặc `"Account does not exist or has been deleted!"`.

<<<<<<< HEAD
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
=======
#### 1.2. Yêu cầu khôi phục mật khẩu (Forgot Password)
* **Mô tả chuyên sâu về chức năng:**
* Khởi tạo quy trình lấy lại mật khẩu cho người dùng khi họ quên.
* Kiểm tra `email` có tồn tại trong hệ thống và tài khoản gắn liền có đang `ACTIVE` hay không.
* Nếu hợp lệ, hệ thống tự động sinh ra một **mã OTP 6 chữ số ngẫu nhiên**, lưu vào database kèm thời hạn sử dụng (thuộc tính `reset_token_expiry`, quy định là **5 phút** kể từ lúc tạo).
* Gọi service gửi Email chứa mã OTP này tới hòm thư của người dùng.

* **Phương thức:** `POST`
* **URL:** `{{baseUrl}}/auth/forgot-password`
* **Headers:** `Content-Type: application/json`
* **Body:**
```json
{
  "email": "thungan1@smartcafe.vn"
}

```

* **Kỳ vọng:**
* **`200 OK`**: `"Password recovery OTP has been sent to your email."`
* **`400 Bad Request`**: `"No valid account found for this email!"`

#### 1.3. Đặt lại mật khẩu mới (Reset Password)
* **Mô tả chuyên sâu về chức năng:**
* Bước cuối cùng của quy trình khôi phục mật khẩu.
* Đối chiếu `token` (mã OTP 6 số) người dùng gửi lên với cột `reset_token` trong database.
* Kiểm tra tính hợp lệ của thời gian: Nếu thời điểm hiện tại đã vượt quá `reset_token_expiry` (quá 5 phút), yêu cầu sẽ bị từ chối.
* Nếu hợp lệ, mã hóa Bcrypt mật khẩu mới (`newPassword`), cập nhật vào database, reset cột `password_changed_at` về thời điểm hiện tại và xóa bỏ chuỗi OTP (set null) để không ai dùng lại được mã này nữa.
  
* **Phương thức:** `POST`
* **URL:** `{{baseUrl}}/auth/reset-password`
* **Headers:** `Content-Type: application/json`
* **Body:**
```json
{
  "token": "123456",
  "newPassword": "NewPassword123!"
}

```

* **Kỳ vọng:**
* **`200 OK`**: `"New password updated successfully!"`
* **`400 Bad Request`**: `"Invalid recovery token or account does not exist!"`

---

### MODULE 2: USER PROFILE

> **Base Path:** `/api/v1/users` *(Bắt buộc truyền Token)* > **Header chung cho toàn module:** `Authorization: Bearer {{token}}`

#### 2.1. Xem thông tin cá nhân (Get Profile)
* **Mô tả chuyên sâu về chức năng:**
* API lấy thông tin định danh và chi tiết của tài khoản đang đăng nhập. Không cần truyền ID trên URL vì Backend sẽ tự động trích xuất `username` từ **JWT Token** nằm trong Header `Authorization`.
* Dựa vào Role của tài khoản, Backend sẽ query vào bảng `employee` (nếu là ADMIN/STAFF) hoặc bảng `customer` (nếu là CUSTOMER) để trả về DTO phù hợp nhất.
* Hệ thống tự động lọc và xử lý dữ liệu: Nhân viên thì trả về `salary` (lương) và ẩn điểm tích lũy; Khách hàng thì trả về `loyaltyPoints` (điểm thưởng) và set lương bằng `null`.
* Hiện tại đã thực hiện đưa salary và loyaltyPoints vào phần output, admin mới có thể xem qua thông tin về hai nội dung này (sẽ điều chỉnh sau)

* **Phương thức:** `GET`
* **URL:** `{{baseUrl}}/users/profile`
* **Kỳ vọng (Với tài khoản `STAFF`):**
```json
{
  "username": "thungan01",
  "email": "thungan1@smartcafe.vn",
  "fullName": "Trần Thu Ngân",
  "dateOfBirth": "1998-10-20T00:00:00.000+00:00",
  "gender": "FEMALE",
  "phone": "0905333444",
  "address": "45 Hùng Vương, Đà Nẵng",
  "salary": 8500000.00,
  "loyaltyPoints": null,
  "roleName": "STAFF",
  "imageUrl": "[https://cdn-icons-png.flaticon.com/512/3135/3135789.png](https://cdn-icons-png.flaticon.com/512/3135/3135789.png)"
}

```

*(**Lưu ý:** Với tài khoản `CUSTOMER`: Thuộc tính `salary` sẽ là `null`, trong khi `loyaltyPoints` sẽ có giá trị).*

#### 2.2. Cập nhật thông tin cá nhân (Update Profile)
* **Mô tả chuyên sâu về chức năng:**
* Cho phép người dùng tự chỉnh sửa thông tin cá nhân của mình.
* API hỗ trợ cơ chế **Partial Update (Cập nhật từng phần)**: Người dùng muốn sửa trường nào thì gửi trường đó, các trường không gửi hoặc gửi `null` sẽ được Backend giữ nguyên dữ liệu cũ trong DB.
* Có xử lý logic kiểm tra ràng buộc (Validation): Nếu người dùng đổi Email sang một chuỗi Email mới, Backend sẽ query kiểm tra xem Email mới này đã bị tài khoản khác chiếm dụng hay chưa.
  
* **Phương thức:** `PUT`
* **URL:** `{{baseUrl}}/users/profile`
* **Body:**
```json
{
  "fullName": "Trần Thu Ngân (Đã sửa)",
  "dateOfBirth": "1998-10-20",
  "gender": "FEMALE",
  "phoneNumber": "0905999888",
  "address": "123 Bạch Đằng, Đà Nẵng",
  "email": "thungan_new@smartcafe.vn",
  "imageUrl": "[https://cdn-icons-png.flaticon.com/512/3135/new-avatar.png](https://cdn-icons-png.flaticon.com/512/3135/new-avatar.png)"
}

```

*(**Lưu ý:** Chỉ chấp nhận `gender` là `MALE` hoặc `FEMALE`. Trường nào không truyền hoặc truyền `null` sẽ được giữ nguyên).*
* **Kỳ vọng:** Trả về HTTP **`200 OK`** kèm đối tượng Profile đã được cập nhật.

#### 2.3. Đổi mật khẩu (Change Password)
* **Mô tả chuyên sâu về chức năng:**
* Dành cho người dùng **đã đăng nhập vào hệ thống** muốn đổi mật khẩu mới (khác với luồng Quên mật khẩu ở Module 1).
* Xác thực kép: Yêu cầu người dùng phải nhập đúng Mật khẩu hiện tại (`oldPassword`). Backend sẽ Bcrypt match mật khẩu cũ này trong DB, nếu sai sẽ từ chối ngay.
* Kiểm tra ràng buộc logic: Mật khẩu mới (`newPassword`) không được phép trùng với mật khẩu cũ.
* Cập nhật mật khẩu mới đã mã hóa và tự động gia hạn thời gian `password_changed_at`.
  
* **Phương thức:** `PUT`
* **URL:** `{{baseUrl}}/users/change-password`
* **Body:**
```json
{
  "oldPassword": "your_current_password",
  "newPassword": "NewStrongPassword456!"
}

```

* **Kỳ vọng:**
* **`200 OK`**: `"Password changed successfully!"`
* **`400 Bad Request`**: `"Old password is incorrect!"`

---
>>>>>>> 6f367d9e48f4cbd30c14a9e766cc61f749f977ed

## 5. Quy trình Test thực tế khuyến nghị

<<<<<<< HEAD
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
=======
Tester nên thực hiện bài test theo luồng (**End-to-End Flow**) dưới đây để đảm bảo logic nghiệp vụ chặt chẽ:

1. **Test Xác thực:** Gọi `POST /auth/login` với tài khoản `thungan01`. Kiểm tra xem biến `token` đã được tự động lưu vào *Environment Variable* hay chưa.
2. **Test Xem Profile:** Gọi `GET /users/profile`. Đảm bảo dữ liệu trả về đúng với role `STAFF` (có hiển thị `salary`, `loyaltyPoints` là null).
3. **Test Cập nhật Profile:** Gọi `PUT /users/profile`, thay đổi số điện thoại hoặc địa chỉ thành số mới. Sau đó gọi lại `GET /users/profile` để kiểm tra dữ liệu đã thực sự được lưu xuống DB chưa.
4. **Test Luồng Quên Mật Khẩu (Đặc biệt):** - Gọi `POST /auth/forgot-password` với email `thungan1@smartcafe.vn`.
* *Mẹo test nhanh:* Nếu không tiện check Email, hãy query trực tiếp xuống DB bằng lệnh SQL sau để lấy mã OTP:
```sql
SELECT reset_token FROM account WHERE username = 'thungan01';

```

* Gọi `POST /auth/reset-password` bằng mã OTP vừa lấy được.
>>>>>>> 6f367d9e48f4cbd30c14a9e766cc61f749f977ed

5. **Xác nhận đổi mật khẩu:** Gọi lại `POST /auth/login` với mật khẩu cũ *(phải thất bại với status 400)* và sau đó login bằng mật khẩu mới *(phải thành công với status 200)*.

<<<<<<< HEAD
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
=======
```
>>>>>>> 6f367d9e48f4cbd30c14a9e766cc61f749f977ed
