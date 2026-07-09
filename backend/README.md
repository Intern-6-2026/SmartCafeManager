

```markdown
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

---

## 3. Danh sách tài khoản Test

Dữ liệu gốc trong DB đã cung cấp sẵn các tài khoản dưới đây (trạng thái `ACTIVE`). Sử dụng các tài khoản này để kiểm tra tính phân quyền của hệ thống:

| Username | Email | Vai trò (Role) | Mô tả |
| --- | --- | --- | --- |
| `admin` | codegymintern@gmail.com | **ADMIN** | Quản trị viên hệ thống |
| `thungan01` | thungan1@smartcafe.vn | **STAFF** | Thu ngân (Nhân viên) |
| `phabep01` | phabep1@smartcafe.vn | **STAFF** | Pha chế / Bếp (Nhân viên) |
| `khach_vip01` | khachvip1@gmail.com | **CUSTOMER** | Khách hàng VIP |
| `khach_thuong01` | khachthuong1@gmail.com | **CUSTOMER** | Khách hàng thường |

> **💡 Lưu ý:** Mật khẩu trong DB được mã hóa bằng Bcrypt. Khi test thực tế, hãy sử dụng mật khẩu mặc định được quy định lúc tạo dữ liệu mẫu (thường là `123456` hoặc `password`).

---

## 4. Danh sách API & Test Cases

### MODULE 1: AUTHENTICATION

> **Base Path:** `/api/v1/auth` *(Công khai, không yêu cầu Token)*

#### 1.1. Đăng nhập hệ thống (Login)

* **Phương thức:** `POST`
* **URL:** `{{baseUrl}}/auth/login`
* **Headers:** `Content-Type: application/json`
* **Body:**
```json
{
  "username": "admin",
  "password": "your_password_here"
}

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

#### 1.2. Yêu cầu khôi phục mật khẩu (Forgot Password)

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

## 5. Quy trình Test thực tế khuyến nghị

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

5. **Xác nhận đổi mật khẩu:** Gọi lại `POST /auth/login` với mật khẩu cũ *(phải thất bại với status 400)* và sau đó login bằng mật khẩu mới *(phải thành công với status 200)*.

```