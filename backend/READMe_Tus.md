# Tài Liệu Luồng Nghiệp Vụ Cho Frontend

Tài liệu này mô tả luồng xử lý ở mức nghiệp vụ để frontend dễ tích hợp với backend. Các API bên dưới là các endpoint hiện có trong backend.

## Nhóm A - Đăng nhập

### Flow cho frontend
Khi người dùng nhập username và password -> ấn nút đăng nhập -> frontend gọi API đăng nhập -> backend tìm account theo username -> kiểm tra tài khoản có tồn tại và chưa bị xóa -> kiểm tra trạng thái tài khoản có đang active hay không -> xác thực username/password bằng Spring Security -> tạo JWT token -> trả về thông tin đăng nhập, role, username và cờ `requirePasswordChange` -> frontend lưu token và điều hướng sang màn hình phù hợp.

### API cần dùng
- `POST /api/v1/auth/login`
- Content-Type: `application/json`

### Request mẫu
```json
{
	"username": "admin",
	"password": "123456"
}
```

### Response mong đợi
```json
{
	"token": "jwt-token",
	"message": "Đăng nhập thành công!",
	"requirePasswordChange": false,
	"roleName": "ADMIN",
	"userName": "admin"
}
```

### Ghi chú cho frontend
- Nếu `requirePasswordChange = true` thì ưu tiên điều hướng người dùng đến màn đổi mật khẩu.
- Khi gọi các API sau đăng nhập, frontend phải gửi header: `Authorization: Bearer <token>`.

---

## Nhóm B - Quên mật khẩu

### Flow cho frontend
Khi người dùng quên mật khẩu -> ấn vào quên mật khẩu -> nhập email -> frontend gọi API gửi OTP -> backend kiểm tra email trong hệ thống -> nếu email hợp lệ và account đang active thì tạo OTP, lưu token khôi phục và gửi email -> backend luôn trả về cùng một thông báo trung tính để không lộ email có tồn tại hay không -> người dùng nhập OTP đã nhận -> frontend gọi API xác thực OTP -> backend kiểm tra OTP còn hạn và đổi sang token reset mới -> frontend dùng token đó để gọi API đặt lại mật khẩu mới.

### API cần dùng
- `POST /api/v1/auth/forgot-password`
- `POST /api/v1/auth/verify-otp`
- `POST /api/v1/auth/reset-password`
- Content-Type: `application/json`

### Ghi chú quyền truy cập
- Ba API này hiện đang được cấu hình `permitAll()` trong `SecurityConfig`, nên FE không cần token để gọi.

### 1. Gửi OTP khôi phục mật khẩu
#### Request mẫu
```json
{
	"email": "user@example.com"
}
```

#### Response mong đợi
```json
"Nếu email hợp lệ, hệ thống sẽ gửi mã OTP khôi phục mật khẩu đến email của bạn."
```

### 2. Xác thực OTP
#### Request mẫu
```json
{
	"token": "123456"
}
```

#### Response mong đợi
```json
"reset-token-uuid"
```

### 3. Đặt lại mật khẩu mới
#### Request mẫu
```json
{
	"token": "reset-token-uuid",
	"newPassword": "NewPassword@123"
}
```

#### Response mong đợi
```json
"Cập nhật mật khẩu mới thành công!"
```

### Ghi chú cho frontend
- OTP chỉ có hiệu lực trong thời gian ngắn, hiện tại là 5 phút.
- Sau khi OTP được xác thực, frontend phải giữ token reset để gọi bước đổi mật khẩu.
- Nếu sau này backend bổ sung cơ chế chống spam OTP, FE nên hiển thị trạng thái chờ hoặc cooldown tương ứng.

---

## Nhóm C - Quản lý tài khoản cá nhân

### Flow cho frontend
Khi người dùng vào trang profile -> frontend gọi API lấy profile hiện tại -> backend xác định người dùng đang đăng nhập từ token -> lấy dữ liệu profile tương ứng với Employee hoặc Customer -> trả về thông tin để hiển thị.

Khi người dùng chỉnh sửa profile -> ấn lưu thay đổi -> frontend gửi dữ liệu cập nhật -> backend tìm account hiện tại -> kiểm tra email nếu có thay đổi -> xác định profile là Employee hay Customer -> cập nhật các trường được phép thay đổi như họ tên, ngày sinh, giới tính, số điện thoại, địa chỉ, ảnh đại diện -> kiểm tra số điện thoại có bị trùng hay không -> lưu dữ liệu và trả lại profile mới.

Khi người dùng đổi mật khẩu -> nhập mật khẩu cũ và mật khẩu mới -> frontend gọi API đổi mật khẩu -> backend kiểm tra mật khẩu cũ đúng hay không -> kiểm tra mật khẩu mới không trùng mật khẩu cũ -> mã hóa mật khẩu mới -> cập nhật lại thời điểm đổi mật khẩu -> lưu dữ liệu.

Khi người dùng đổi avatar -> chọn file ảnh -> frontend gửi file lên backend dạng multipart/form-data -> backend upload ảnh lên Cloudinary -> cập nhật đường dẫn ảnh mới vào profile -> trả về profile mới.

### API cần dùng
- `GET /api/v1/users/profile`
- `PUT /api/v1/users/profile`
- `PUT /api/v1/users/change-password`
- `POST /api/v1/users/profile/avatar`

### 1. Lấy thông tin profile
#### Request
- Method: `GET`
- Header: `Authorization: Bearer <token>`

#### Response
- Thông tin profile hiện tại của user.

### 2. Cập nhật profile
#### Request mẫu
```json
{
	"fullName": "Nguyen Van A",
	"dateOfBirth": "1995-01-01",
	"gender": "MALE",
	"phoneNumber": "0909123456",
	"address": "Ho Chi Minh City",
	"email": "user@example.com",
	"imageUrl": "https://..."
}
```

#### Response
- Profile mới sau khi cập nhật.

### 3. Đổi mật khẩu
#### Request mẫu
```json
{
	"oldPassword": "OldPassword@123",
	"newPassword": "NewPassword@123"
}
```

#### Response
- Message thành công khi đổi mật khẩu xong.

### 4. Cập nhật avatar
#### Request
- Method: `POST`
- Content-Type: `multipart/form-data`
- Field file: `image`

#### Response
- Profile mới sau khi cập nhật avatar.

### Ghi chú cho frontend
- Các API trong nhóm này cần token hợp lệ.
- Nếu backend trả `requirePasswordChange = true` sau login, frontend nên ưu tiên cho người dùng đi vào luồng đổi mật khẩu trước khi dùng các chức năng khác.

---

## Tóm Tắt Nhanh API Theo Nhóm

### Nhóm A - Đăng nhập
- `POST /api/v1/auth/login`

### Nhóm B - Quên mật khẩu
- `POST /api/v1/auth/forgot-password`
- `POST /api/v1/auth/verify-otp`
- `POST /api/v1/auth/reset-password`

### Nhóm C - Quản lý tài khoản cá nhân
- `GET /api/v1/users/profile`
- `PUT /api/v1/users/profile`
- `PUT /api/v1/users/change-password`
- `POST /api/v1/users/profile/avatar`
