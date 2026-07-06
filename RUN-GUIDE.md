# 🚀 Hướng Dẫn Chạy Dự Án Thủ Công (Manual Run Guide)

Dưới đây là các lệnh đơn giản, ngắn gọn và trọng tâm để khởi chạy toàn bộ hệ thống microservices.

---

## 📋 Điều Kiện Cần (Prerequisites)
- **Java 21**
- **Maven** (hoặc dùng `./mvnw` đi kèm)
- **PostgreSQL** running on `localhost:5432` (database: `notification_db`)
- **Gmail App Password** (đã cấu hình sẵn trong `application.yml`)

---

## 🛠️ Bước 1: Build / Biên Dịch Dự Án
Chạy lệnh này tại thư mục gốc `clinic-appointment-system` để biên dịch trước:
```bash
mvn clean compile -DskipTests
```

---

## 🚦 Bước 2: Khởi Chạy Theo Thứ Tự

Mở các cửa sổ Terminal riêng biệt tại thư mục gốc dự án và chạy các lệnh sau (chờ service trước khởi động hoàn tất mới chạy service tiếp theo):

### 1. Config Server (Port 8888)
Cung cấp cấu hình tập trung cho toàn bộ hệ thống.
```bash
mvn spring-boot:run -pl infra/config-server
```

### 2. Eureka Server (Port 8761)
Service Registry để các microservice đăng ký vị trí với nhau.
```bash
mvn spring-boot:run -pl infra/eureka-server
```

### 3. API Gateway (Port 8060)
Cổng điều phối API định tuyến request.
```bash
mvn spring-boot:run -pl infra/api-gateway
```

### 4. Notification Service (Port 8083)
Service nhận và xử lý gửi thông báo (vừa hoàn thành Phase 3 & 4).
```bash
mvn spring-boot:run -pl services/notification-service
```

---

## 📦 Bước 3: Khởi Chạy Các Business Services Khác (Tùy chọn)

Nếu bạn muốn chạy thêm các chức năng khác của phòng khám, mở Terminal mới và chạy:

- **Doctor Service (Port 8082):**
  ```bash
  mvn spring-boot:run -pl services/doctor-service
  ```
- **Appointment Service (Port 8081):**
  ```bash
  mvn spring-boot:run -pl services/appointment-service
  ```
- **Patient Service (Port 8080):**
  ```bash
  mvn spring-boot:run -pl services/patient-service
  ```
- **User Service (Port 8085):**
  ```bash
  mvn spring-boot:run -pl services/user-service
  ```

---

## 💡 Tip/Lưu ý
- Nếu máy của bạn báo lỗi lệnh `mvn`, hãy đổi sang dùng `./mvnw` (hoặc `.\mvnw.cmd` trên Windows Cmd/PowerShell).
- Ví dụ: `./mvnw spring-boot:run -pl services/notification-service`