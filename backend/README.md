# 🛒 TÀI LIỆU TÍCH HỢP HỆ THỐNG GỌI MÓN TẠI BÀN (DÀNH RIÊNG CHO FRONT-END)

Tài liệu này định nghĩa cách thức giao tiếp giữa ứng dụng Client (Giao diện Khách hàng) và Hệ thống Máy chủ (Backend). Front-end chỉ cần quan tâm đến cách xây dựng giao diện dựa trên các quy trình nghiệp vụ và cấu trúc dữ liệu JSON được mô tả bên dưới.

---

## 🛠️ THÔNG SỐ KỸ THUẬT CHUNG
* **Cấu hình Base URL:** `http://localhost:8080/api/v1/customer`
* **Cơ chế Bảo mật (Authentication):** **Không yêu cầu Token** (Hệ thống mở công khai cho khách quét mã QR tại bàn để order trực tiếp).
* **Kiểu truyền dữ liệu (Content-Type):** Toàn bộ 7 API sử dụng phương thức truyền tham số trực tiếp trên đường dẫn **URL Query Parameters** (`application/x-www-form-urlencoded`). Tham số được gắn sau dấu `?` và nối nhau bằng dấu `&`.
* **Định dạng dữ liệu nhận về:** `Chuỗi văn bản (String)` hoặc `Đối tượng JSON / Mảng JSON`.

---

## 🗺️ PHẦN 1: TOÀN BỘ QUY TRÌNH NGHIỆP VỤ (UI WORKFLOW)

Quy trình trải nghiệm của khách hàng trên ứng dụng tương ứng với vòng đời từ lúc vào quán đến lúc thanh toán ra về bao gồm 4 giai đoạn chính:

### Giai đoạn 1: Quét QR & Chọn món vào giỏ tạm
* Khách hàng quét mã QR dán tại bàn để truy cập vào ứng dụng (Hệ thống nhận diện qua tham số `tableName`, ví dụ: `Ban01`).
* Khách xem menu, chọn một món ăn, nhập số lượng và ghi chú (nếu có) rồi ấn "Thêm vào giỏ". Lúc này món ăn mới chỉ nằm trong **Giỏ hàng tạm thời** của riêng bàn đó và ở trạng thái chờ chốt (`PENDING`). Nhà bếp chưa hề biết đến sự tồn tại của các món này.

### Giai đoạn 2: Xác nhận gửi đơn xuống bếp (Chốt đơn)
* Khách mở Giỏ hàng tạm thời ra để kiểm tra lại. Sau khi chốt, khách bấm nút **[GỌI MÓN]** hoặc **[ĐẶT ĐỒ]**.
* Hệ thống chuyển trạng thái toàn bộ món trong giỏ tạm từ chờ chốt (`PENDING`) sang đã xác nhận (`CONFIRMED`). Lúc này, thông báo mới chính thức hiển thị ở màn hình của nhà bếp để đầu bếp chế biến, đồng thời hệ thống tự động khóa bàn, đổi trạng thái bàn sang "Đang chờ món" (`WAITING_FOOD`).

### Giai đoạn 3: Theo dõi trạng thái làm món & Gọi thêm
* Khách hàng có thể truy cập vào màn hình "Lịch sử gọi món" để theo dõi xem món nào bếp đang làm (`CONFIRMED`) và món nào nhân viên đã bốc bê lên bàn phục vụ xong (`SERVED`).
* Trong quá trình ngồi ăn, khách hoàn toàn có thể lặp lại Giai đoạn 1 và 2 để gọi thêm đồ uống hoặc món ăn mới mà không làm ảnh hưởng đến các món cũ đã gọi. Hệ thống sẽ tự động tính lũy tổng số tiền.

### Giai đoạn 4: Xem hóa đơn & Yêu cầu tính tiền ra về
* Ăn xong, khách bấm vào nút "Xem hóa đơn" để kiểm tra tổng số tiền tích lũy tích hợp (`totalAmount`) và danh sách các món ăn đã dùng.
* Khách bấm chọn phương thức thanh toán mong muốn (ví dụ: Chuyển khoản ngân hàng) và ấn **[XÁC NHẬN THANH TOÁN]**. Trạng thái của bàn lập tức đổi sang "Chờ tính tiền" (`REQUESTING_BILL`) để thông báo cho quầy thu ngân cầm hóa đơn giấy hoặc mang mã QR ngân hàng ra tận bàn cho khách.

---

## 📑 PHẦN 2 & 3: CHI TIẾT 7 API VÀ TẤT CẢ CÁC KỊCH BẢN PHÁT SINH

### 1. API THÊM MÓN VÀO GIỎ TẠM THỜI
* **Method:** `POST`
* **Endpoint:** `/add-item`
* **Mục đích:** Lưu món ăn khách vừa chọn vào giỏ hàng tạm thời. Nếu món đó đã có sẵn trong giỏ tạm, hệ thống sẽ tự động cộng dồn số lượng.

#### 🟢 Kịch bản 1: Thành công
* **Mô tả:** Khách chọn 2 ly Cà phê sữa, ghi chú "Ít đường nhiều đá" tại Bàn 01.
* **URL Request:** `http://localhost:8080/api/v1/customer/add-item?tableName=Ban01&itemId=1&quantity=2&note=It duong nhieu da`
* **Trạng thái mạng (Status Code):** `200 OK`
* **Dữ liệu nhận về (Response):**
  ```text
  Đã thêm món vào giỏ hàng tạm thời!
  🔴 Kịch bản 2: Thất bại do lỗi nhập liệu từ giao diệnMô tả: Front-end thiết kế lỗi, khi gửi Request đi bị khuyết mất tham số số lượng quantity.URL Request: http://localhost:8080/api/v1/customer/add-item?tableName=Ban01&itemId=1&note=It duongTrạng thái mạng (Status Code): 400 Bad RequestDữ liệu nhận về (Response):JSON{
  "status": 400,
  "error": "Bad Request",
  "message": "Required request parameter 'quantity' is not present"
}
🔴 Kịch bản 3: Thất bại do vi phạm logic nghiệp vụ hệ thốngMô tả: Khách sử dụng ứng dụng can thiệp chỉnh sửa mã QR hoặc ID món ăn không có trong thực đơn của nhà hàng.URL Request: http://localhost:8080/api/v1/customer/add-item?tableName=Ban01&itemId=9999&quantity=1Trạng thái mạng (Status Code): 500 Internal Server ErrorDữ liệu nhận về (Response):JSON{
  "status": 500,
  "message": "Món ăn không tồn tại!"
}
2. API XEM TẤT CẢ MÓN TRONG GIỎ HÀNG TẠM THỜIMethod: GETEndpoint: /cartMục đích: Lấy ra danh sách các món ăn đang nằm chờ trong giỏ (Chỉ lấy các món có trạng thái là PENDING).🟢 Kịch bản 1: Thành công (Giỏ hàng đang có đồ)URL Request: http://localhost:8080/api/v1/customer/cart?tableName=Ban01Trạng thái mạng (Status Code): 200 OKDữ liệu nhận về (Mảng JSON):JSON[
  {
    "orderDetailId": 401,
    "quantity": 2,
    "unitPrice": 35000.00,
    "note": "It duong nhieu da",
    "status": "PENDING",
    "item": {
      "itemId": 1,
      "itemName": "Cà phê sữa đá",
      "price": 35000.00
    }
  }
]
🔴 Kịch bản 2: Thất bại do logic nghiệp vụ (Bàn trống hoàn toàn)Mô tả: Khách vừa ngồi vào bàn, chưa bấm thêm bất cứ món nào vào giỏ nhưng đã ấn nút "Xem giỏ hàng". Lúc này hệ thống chưa kích hoạt mở hóa đơn cho bàn này.URL Request: http://localhost:8080/api/v1/customer/cart?tableName=Ban01Trạng thái mạng (Status Code): 500 Internal Server ErrorDữ liệu nhận về (Response):JSON{
  "status": 500,
  "message": "Bàn hiện tại không có hóa đơn nào đang mở!"
}
3. API BẤM NÚT [GỌI MÓN] ĐỂ XÁC NHẬN GỬI XUỐNG BẾPMethod: POSTEndpoint: /confirm-orderMục đích: Chốt toàn bộ danh sách trong giỏ tạm gửi đi. Chuyển trạng thái các món từ PENDING $\rightarrow$ CONFIRMED.🟢 Kịch bản 1: Thành côngURL Request: http://localhost:8080/api/v1/customer/confirm-order?tableName=Ban01Trạng thái mạng (Status Code): 200 OKDữ liệu nhận về (Response):PlaintextĐã gửi đơn hàng thành công xuống bếp!
🔴 Kịch bản 2: Thất bại do logic nghiệp vụ (Cố tình gửi giỏ trống)Mô tả: Khách đã bấm nút gửi bếp một lần rồi (giỏ tạm đã sạch trống), sau đó lại cố tình ấn tiếp nút "Gửi bếp" thêm lần nữa khi chưa chọn thêm món mới nào.URL Request: http://localhost:8080/api/v1/customer/confirm-order?tableName=Ban01Trạng thái mạng (Status Code): 500 Internal Server ErrorDữ liệu nhận về (Response):JSON{
  "status": 500,
  "message": "Giỏ hàng của bạn đang trống, không thể gọi món!"
}
4. API XEM LỊCH SỬ CÁC MÓN ĐÃ GỌI XUỐNG BẾPMethod: GETEndpoint: /order-historyMục đích: Lấy ra danh sách các món ăn đã gửi xuống bếp thành công để khách theo dõi tiến độ làm đồ (Lọc các món có trạng thái khác PENDING).🟢 Kịch bản 1: Thành côngURL Request: http://localhost:8080/api/v1/customer/order-history?tableName=Ban01Trạng thái mạng (Status Code): 200 OKDữ liệu nhận về (Mảng JSON):JSON[
  {
    "orderDetailId": 401,
    "quantity": 2,
    "unitPrice": 35000.00,
    "status": "CONFIRMED",
    "item": { "itemName": "Cà phê sữa đá" }
  },
  {
    "orderDetailId": 398,
    "quantity": 1,
    "unitPrice": 45000.00,
    "status": "SERVED",
    "item": { "itemName": "Bánh mì hướng dương" }
  }
]
5. API XEM CHI TIẾT TỔNG QUAN HÓA ĐƠNMethod: GETEndpoint: /invoiceMục đích: Trả về cấu trúc DTO độc lập gồm tổng tiền và toàn bộ danh sách món ăn phục vụ cho màn hình tổng quan hóa đơn trước khi khách nhấn nút thanh toán tiền ra về.🟢 Kịch bản 1: Thành côngURL Request: http://localhost:8080/api/v1/customer/invoice?tableName=Ban01Trạng thái mạng (Status Code): 200 OKDữ liệu nhận về (Đối tượng DTO JSON phẳng):JSON{
  "tableOrderId": 1001,
  "tableName": "Ban01",
  "totalAmount": 115000.00,
  "orderStatus": "OPEN",
  "serviceStatus": "WAITING_FOOD",
  "openAt": "2026-07-13T20:15:00",
  "orderDetails": [
    { "orderDetailId": 401, "quantity": 2, "unitPrice": 35000.00, "status": "CONFIRMED" },
    { "orderDetailId": 398, "quantity": 1, "unitPrice": 45000.00, "status": "SERVED" }
  ]
}
6. API KHÁCH ẤN NÚT YÊU CẦU THANH TOÁNMethod: POSTEndpoint: /request-checkoutMục đích: Gửi hình thức thanh toán mong muốn lên quầy thu ngân để báo nhân viên cầm hóa đơn ra bàn kiểm tra chốt bàn.🟢 Kịch bản 1: Thành côngMô tả: Khách chọn hình thức chuyển khoản ngân hàng (BANK_TRANSFER).URL Request: http://localhost:8080/api/v1/customer/request-checkout?tableName=Ban01&paymentMethod=BANK_TRANSFERTrạng thái mạng (Status Code): 200 OKDữ liệu nhận về (Response):PlaintextYêu cầu thanh toán bằng BANK_TRANSFER đã được gửi! Nhân viên sẽ đến ngay.
🔴 Kịch bản 2: Thất bại do truyền sai ký tự quy ước (Enum)Mô tả: Front-end gửi chuỗi chữ thường hoặc sai tên ký tự quy định định dạng lên máy chủ (tien_mat, momo_pay).URL Request: http://localhost:8080/api/v1/customer/request-checkout?tableName=Ban01&paymentMethod=momoTrạng thái mạng (Status Code): 400 Bad RequestDữ liệu nhận về (Response):JSON{
  "status": 400,
  "message": "Failed to convert value of type 'java.lang.String' to required type..."
}
7. API CÁC YÊU CẦU DỊCH VỤ KHÁC (NÚT TRỢ GIÚP NHANH)Method: POSTEndpoint: /call-serviceMục đích: Khách cần hỗ trợ các dịch vụ phụ (ví dụ: xin thêm khăn lau, gọi phục vụ trực tiếp giải quyết sự cố bàn ghế...).🟢 Kịch bản 1: Thành côngMô tả: Khách nhấn nút "Gọi phục vụ" trên màn hình hỗ trợ nhanh.URL Request: http://localhost:8080/api/v1/customer/call-service?tableName=Ban01&status=CALLING_WAITERTrạng thái mạng (Status Code): 200 OKDữ liệu nhận về (Response):PlaintextHệ thống đã ghi nhận yêu cầu: CALLING_WAITER
📊 PHẦN 4: BẢNG TRA CỨU CÁC TẬP HỢP DỮ LIỆU QUY ƯỚC (ENUMS)Để tránh lỗi đồng bộ dữ liệu mạng (400 Bad Request), Front-end bắt buộc phải truyền chính xác các chuỗi văn bản VIẾT HOA TOÀN BỘ dưới đây khi thực hiện tạo Request:1. Phương thức thanh toán (PaymentMethod)Giá trị chuỗi gửi lênÝ nghĩa hiển thị trên giao diệnCASHThanh toán bằng Tiền mặtBANK_TRANSFERThanh toán bằng Chuyển khoản Ngân hàng (Quét QR mã định danh)MOMOThanh toán qua Ví điện tử MoMoVNPAYThanh toán qua Cổng VNPay2. Trạng thái dịch vụ của Bàn (ServiceStatus)Giá trị chuỗi quy ướcÝ nghĩa trạng thái hiển thị của bànNORMALBàn hoạt động bình thườngWAITING_FOODKhách đã chốt đơn thành công, đang chờ bếp lên đồCALLING_WAITERKhách đang bấm chuông gọi nhân viên hỗ trợ trực tiếpREQUESTING_BILLKhách đã chọn phương thức thanh toán, đang chờ thu ngân tính tiền3. Trạng thái chi tiết món ăn (StatusOrderDetail)Giá trị nhận về từ APIÝ nghĩa vòng đời món ănPENDINGMón ăn mới chỉ nằm trong giỏ hàng tạm thời (Chưa gửi bếp)CONFIRMEDĐã gửi xuống bếp thành công, đầu bếp đang chế biếnSERVEDĐồ ăn/thức uống đã được nhân viên phục vụ bưng ra bànCANCELLEDMón ăn bị hủy bỏ (Do nhà hàng hết nguyên liệu hoặc khách đổi món sớm)