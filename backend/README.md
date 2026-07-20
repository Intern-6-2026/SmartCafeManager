# 📢 Update - Liquibase Integration & API Update

> **Update by:** Lê Tấn Thống
>
> Từ phiên bản này dự án đã tích hợp **Liquibase** để quản lý Database Migration.
>
> **Lưu ý:** Tất cả thành viên trong nhóm vui lòng đọc kỹ hướng dẫn bên dưới trước khi chạy project.

---

# 1. Liquibase là gì?

Liquibase là công cụ giúp quản lý lịch sử thay đổi của Database.

Thay vì mỗi người tự sửa Database bằng MySQL Workbench rồi gửi file `.sql` cho nhau, từ bây giờ mọi thay đổi Database sẽ được quản lý bằng Migration.

Ưu điểm:

- Đồng bộ Database cho tất cả thành viên.
- Không cần gửi file SQL qua Zalo.
- Không bị thiếu bảng.
- Không bị khác cấu trúc Database.
- Theo dõi được lịch sử thay đổi.

---

# 2. Cài đặt

Trong `pom.xml` đã được thêm

```xml
<dependency>
    <groupId>org.liquibase</groupId>
    <artifactId>liquibase-core</artifactId>
</dependency>
```

Không cần cài thêm gì.

Chỉ cần Maven tải dependency.

---

# 3. Cấu trúc thư mục

Sau khi pull code sẽ có

```
src
└── main
    └── resources
        └── db
            └── changelog
                ├── db.changelog-master.yaml
                ├── 001-create-news-table.sql
                ├── 002-add-item-image.sql
                └── ...
```

Toàn bộ migration đều nằm trong

```
src/main/resources/db/changelog
```

---

# 4. Cấu hình

Trong

```
application.properties
```

đã cấu hình

```properties
spring.liquibase.enabled=true
spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.yaml
```

Không chỉnh sửa nếu không cần thiết.

---

# 5. Tạo Database

Nếu chưa có Database hãy tạo

```sql
CREATE DATABASE smart_cafe_manager
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

Sau đó sửa

```
application.properties
```

Ví dụ

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/smart_cafe_manager
spring.datasource.username=root
spring.datasource.password=
```

---

# 6. Import Database ban đầu

Import file SQL của dự án bằng MySQL Workbench.

Sau khi import sẽ có các bảng:

```
role
users
item
menu_category
table_order
order_detail
notification
news
...
```

Không cần tự tạo từng bảng.

Database hiện tại chính là **Baseline** của dự án.

---

# 7. Chạy Project

```
mvn spring-boot:run
```

Liquibase sẽ tự kiểm tra migration.

Nếu có migration mới sẽ tự chạy.

---

# 8. Sau này nếu cần sửa Database

Ví dụ muốn thêm bảng

```
news
```

Tạo file mới

```
src/main/resources/db/changelog/001-create-news-table.sql
```

Ví dụ

```sql
--liquibase formatted sql

--changeset thong:001

CREATE TABLE news
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255),
    content TEXT
);
```

Sau đó mở

```
db.changelog-master.yaml
```

thêm

```yaml
databaseChangeLog:

  - include:
      file: db/changelog/001-create-news-table.sql
```

Chạy lại project.

Liquibase sẽ tự tạo bảng.

---

# 9. Thêm cột mới

Ví dụ

```
002-add-item-image.sql
```

```sql
--liquibase formatted sql

--changeset thong:002

ALTER TABLE item
ADD COLUMN image_url VARCHAR(255);
```

Thêm vào

```
db.changelog-master.yaml
```

```yaml
databaseChangeLog:

  - include:
      file: db/changelog/001-create-news-table.sql

  - include:
      file: db/changelog/002-add-item-image.sql
```

Chạy project.

Liquibase sẽ tự cập nhật.

---

# 10. Quy tắc làm việc nhóm

## Không được sửa

```
001-create-news-table.sql
```

sau khi đã merge.

Nếu cần sửa

Tạo file mới

```
002-update-news-table.sql
```

Không được sửa migration cũ.

---

## Mỗi thay đổi Database = Một migration mới

Ví dụ

```
003-add-discount-column.sql
004-create-voucher-table.sql
005-create-payment-table.sql
```

---

## Đặt tên migration

| Chức năng | Ví dụ |
|-----------|----------------------------|
| Tạo bảng | 005-create-news-table.sql |
| Thêm cột | 006-add-image-column.sql |
| Xóa cột | 007-drop-phone-column.sql |
| Thêm FK | 008-add-user-role-fk.sql |

---

# 11. Sau khi Pull Code

```
git pull origin develop
```

Sau đó

```
mvn spring-boot:run
```

Liquibase sẽ tự cập nhật Database.

Không cần chạy file SQL bằng Workbench.

---

# 12. Không được làm

❌ Không sửa migration đã merge.

❌ Không đổi tên migration.

❌ Không xóa migration.

❌ Không chỉnh sửa DATABASECHANGELOG.

❌ Không chỉnh sửa DATABASECHANGELOGLOCK.

---

# 13. Hai bảng Liquibase

Liquibase sẽ tự tạo

```
DATABASECHANGELOG

DATABASECHANGELOGLOCK
```

Đây là hai bảng hệ thống.

Không được xóa.

Không được sửa dữ liệu.

---

# 14. API Update

## API Giỏ hàng tạm thời

### Endpoint

```
GET /api/v1/items/cart?tableName=Ban01
```

### Mục đích

API này chỉ trả về các món đang ở trạng thái **PENDING** (đã thêm vào giỏ nhưng chưa gửi xuống bếp).

### Response

```json
[
  {
    "orderDetailId": 7,
    "itemId": 1,
    "itemName": "Cà phê Đen Đá",
    "price": 25000,
    "quantity": 2,
    "tableName": "Ban01"
  }
]
```

### Hướng xử lý Frontend

- Hiển thị số lượng món đang nằm trong giỏ hàng.
- Cho phép chỉnh sửa số lượng hoặc xóa món.
- Khi người dùng nhấn **[Gọi món]**, gửi request cập nhật các `orderDetailId` này sang trạng thái `CONFIRMED`.

---

## API Hóa đơn / Tiến trình gọi món nhóm

### Endpoint

```
GET /api/v1/items/invoice?tableName=Ban01
```

### Mục đích

API trả về toàn bộ món của một bàn trong lượt ngồi hiện tại, bao gồm:

- Món đã gửi bếp (`CONFIRMED`)
- Món đã phục vụ (`SERVED`)
- Món đang nằm trong giỏ (`PENDING`)

Phù hợp với nghiệp vụ nhiều khách cùng quét QR và gọi món trên cùng một bàn.

### Response

```json
{
  "tableOrderId": 3,
  "tableName": "Ban01",
  "totalAmount": 170000,
  "orderStatus": "OPEN",
  "serviceStatus": "WAITING_FOOD",
  "openAt": "2026-07-13T13:57:54",
  "orderDetails": [
    {
      "orderDetailId": 4,
      "quantity": 2,
      "unitPrice": 25000,
      "note": "",
      "status": "CONFIRMED",
      "itemId": 1,
      "itemName": "Cà phê Đen Đá",
      "imageUrl": "https://..."
    },
    {
      "orderDetailId": 7,
      "quantity": 2,
      "unitPrice": 25000,
      "note": "Không đá",
      "status": "PENDING",
      "itemId": 1,
      "itemName": "Cà phê Đen Đá",
      "imageUrl": "https://..."
    }
  ]
}
```

### Hướng xử lý Frontend

- `PENDING`
  - Hiển thị nhãn **"Chờ gọi"** hoặc **"Trong giỏ hàng"**.
  - Cho phép chỉnh sửa số lượng.
  - Cho phép xóa món.
  - Hiển thị nút **"Gửi bếp các món đang chờ"**.

- `CONFIRMED`
  - Hiển thị nhãn **"Đã gửi bếp"**.
  - Không cho khách chỉnh sửa.

- `SERVED`
  - Hiển thị nhãn **"Đã phục vụ"**.
  - Không cho chỉnh sửa.

- `totalAmount`
  - Luôn hiển thị tổng tiền tạm tính theo thời gian thực.
  - Bao gồm cả món đã gửi bếp và món đang trong giỏ.

Swagger đã được cập nhật theo cấu trúc DTO mới. Có thể kiểm tra trực tiếp bằng Swagger UI hoặc Postman.
---

# 15. Kitchen Service Update

## 15.1 Order Item Lifecycle (`StatusOrderDetail`)

Hệ thống đã mở rộng vòng đời của từng món ăn trong `order_detail` nhằm hỗ trợ quy trình gọi món theo thời gian thực giữa Khách hàng, Bếp/Bar và Nhân viên phục vụ.

```text
Khách chọn món
      │
      ▼
   PENDING
      │
      │ Khách nhấn [Gọi món]
      ▼
  CONFIRMED
      │
      ├───────────────┐
      ▼               ▼
   SERVED        CANCELLED
 (Đã phục vụ)   (Hết món/Hủy món)
```

### Các trạng thái

| Status | Thao tác bởi | Ý nghĩa |
|---------|--------------|----------|
| `PENDING` | Khách hàng | Món đang nằm trong giỏ hàng. Có thể sửa số lượng hoặc xóa khỏi đơn. |
| `CONFIRMED` | Khách hàng | Đã gửi món xuống Bếp/Bar. Không thể chỉnh sửa. |
| `SERVED` | Kitchen / Staff | Món đã chế biến và phục vụ thành công. |
| `CANCELLED` | Kitchen / Manager | Món bị hủy hoặc hết hàng. Hệ thống tự động loại món khỏi tổng tiền hóa đơn. |

---

## 15.2 Kitchen APIs

### Phục vụ món

```http
PUT /api/v1/items/kitchen/serve-item
```

#### Query Parameters

| Parameter | Type | Required |
|-----------|------|----------|
| orderDetailId | Long | Yes |

**Chức năng**

```
CONFIRMED → SERVED
```

Khi bếp hoàn thành món và nhân viên đã phục vụ cho khách.

---

### Hủy món

```http
PUT /api/v1/items/kitchen/cancel-item
```

#### Query Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| orderDetailId | Long | Yes | ID của món cần hủy |
| reason | String | No | Mặc định: `"Hết món"` |

**Chức năng**

```
CONFIRMED → CANCELLED
```

Khi món bị hủy:

- Không tính vào `totalAmount`.
- Tổng tiền hóa đơn được tính lại tự động.
- Frontend hiển thị trạng thái **Đã hủy**.

---

## 15.3 Liquibase Migration

Thêm migration mới:

```
src/main/resources/db/changelog/003-update-order-detail-status.sql
```

```sql
--liquibase formatted sql

--changeset backend:003-update-order-detail-status

ALTER TABLE order_detail
MODIFY COLUMN status
ENUM(
    'PENDING',
    'CONFIRMED',
    'SERVED',
    'CANCELLED'
)
NOT NULL DEFAULT 'PENDING';
```

Sau đó khai báo trong:

```yaml
databaseChangeLog:

  - include:
      file: db/changelog/001-create-news-table.sql

  - include:
      file: db/changelog/002-add-item-image.sql

  - include:
      file: db/changelog/003-update-order-detail-status.sql
```

---

## 15.4 Frontend Integration

Khi gọi API

```http
GET /api/v1/items/invoice
```

Frontend nên hiển thị trạng thái như sau:

| Status | UI | Mô tả |
|---------|----|--------|
| `PENDING` | Trong giỏ hàng | Cho phép sửa và xóa món |
| `CONFIRMED` | Đã gửi bếp | Không cho chỉnh sửa |
| `SERVED` | Đã phục vụ | Hiển thị hoàn thành |
| `CANCELLED` | Đã hủy (Hết món) | Gạch ngang món và không cộng vào tổng tiền |

---

## 15.5 Business Rules

- Chỉ món ở trạng thái **PENDING** mới được chỉnh sửa hoặc xóa.
- Khi khách nhấn **Gọi món**, toàn bộ món sẽ chuyển sang **CONFIRMED**.
- Kitchen chỉ xử lý các món **CONFIRMED**.
- Kitchen có thể:
  - Chuyển sang **SERVED**.
  - Chuyển sang **CANCELLED**.
- Khi món bị hủy:
  - Không tính vào `totalAmount`.
  - Tổng tiền được cập nhật ngay.
  - Frontend hiển thị trạng thái **Đã hủy**.
- Món đã **SERVED** hoặc **CANCELLED** không được phép quay lại trạng thái trước.

---

## 15.6 State Transition

```text
             +-----------+
             | PENDING   |
             +-----------+
                    |
            Customer Order
                    |
                    ▼
             +-------------+
             | CONFIRMED   |
             +-------------+
               /         \
              /           \
             ▼             ▼
      +----------+   +-------------+
      | SERVED   |   | CANCELLED   |
      +----------+   +-------------+
```