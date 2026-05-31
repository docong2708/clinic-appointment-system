# Tổng quan cấu trúc dự án Clinic Appointment System

Tài liệu này mô tả cấu trúc hiện tại của dự án `clinic-appointment-system` sau khi đã bắt đầu dựng skeleton theo hướng **Java Spring Boot Microservices + Maven multi-module + Clean Architecture/DDD**.

Dự án được tổ chức thành ba nhóm chính:

- `infra`: các service hạ tầng phục vụ toàn hệ thống microservices.
- `services`: các service nghiệp vụ độc lập.
- `shared`: các thư viện dùng chung giữa nhiều service.

---

## 1. Thông tin chung

| Thành phần | Giá trị |
| --- | --- |
| Tên dự án | Clinic Appointment |
| Group ID | `com.group01` |
| Build tool | Maven |
| Java version | 21 |
| Spring Boot | 3.5.14 |
| Spring Cloud | 2025.0.0 |
| Packaging gốc | `pom` |
| Kiến trúc tổng thể | Microservices |
| Kiến trúc bên trong service | Clean Architecture / DDD định hướng use case |

File `pom.xml` ở thư mục gốc đóng vai trò **parent/aggregator**, dùng để khai báo các module con và quản lý version chung cho Spring Boot, Spring Cloud, Lombok, plugin build và các dependency dùng chung.

---

## 2. Cấu trúc thư mục cấp cao

```text
clinic-appointment-system/
|-- .ai-content/
|   `-- project-structure.md
|-- infra/
|   |-- api-gateway/
|   |-- config-server/
|   `-- eureka-server/
|-- services/
|   |-- appointment-service/
|   |-- doctor-service/
|   |-- notification-service/
|   `-- patient-service/
|-- shared/
|   |-- common-events/
|   |-- common-security/
|   `-- common-web/
|-- .gitignore
`-- pom.xml
```

### Ý nghĩa các nhóm chính

| Nhóm | Ý nghĩa |
| --- | --- |
| `infra` | Chứa các service hạ tầng như Gateway, Config Server, Eureka Server. Đây không phải nghiệp vụ chính, mà là phần hỗ trợ hệ microservices vận hành. |
| `services` | Chứa các service nghiệp vụ thật của hệ thống phòng khám. Mỗi service nên là một Spring Boot application độc lập. |
| `shared` | Chứa code dùng chung như response format, exception chung, security helper, event model. Không nên nhét business logic nặng vào đây. |

---

## 3. Danh sách module

| Nhóm | Module | Vai trò |
| --- | --- | --- |
| `infra` | `api-gateway` | Cổng vào hệ thống. Client nên gọi vào Gateway thay vì gọi thẳng từng service. |
| `infra` | `config-server` | Quản lý cấu hình tập trung cho các service. |
| `infra` | `eureka-server` | Service registry/discovery để các service đăng ký và tìm nhau. |
| `services` | `appointment-service` | Quản lý nghiệp vụ lịch hẹn khám. |
| `services` | `doctor-service` | Quản lý thông tin bác sĩ, lịch làm việc, trạng thái bác sĩ. |
| `services` | `notification-service` | Quản lý thông báo, ví dụ nhắc lịch, gửi trạng thái lịch hẹn. |
| `services` | `patient-service` | Quản lý thông tin bệnh nhân và hồ sơ liên quan. |
| `shared` | `common-events` | Chứa event dùng chung giữa các service nếu cần. |
| `shared` | `common-security` | Chứa thành phần dùng chung về xác thực, phân quyền, JWT, security context nếu cần. |
| `shared` | `common-web` | Chứa response chung, exception chung, filter/helper web nếu cần. |

---

## 4. Module được khai báo trong Maven gốc

```xml
<modules>
    <module>services/patient-service</module>
    <module>services/doctor-service</module>
    <module>services/notification-service</module>
    <module>services/appointment-service</module>

    <module>infra/api-gateway</module>
    <module>infra/config-server</module>
    <module>infra/eureka-server</module>

    <module>shared/common-events</module>
    <module>shared/common-security</module>
    <module>shared/common-web</module>
</modules>
```

---

## 5. Port và application name

| Module | Application name | Port |
| --- | --- | ---: |
| `infra/api-gateway` | `api-gateway` | 8080 |
| `infra/config-server` | `config-server` | 8888 |
| `infra/eureka-server` | `eureka-server` | 8761 |
| `services/appointment-service` | `appointment-service` | 8081 |
| `services/doctor-service` | `doctor-service` | 8082 |
| `services/notification-service` | `notification-service` | 8083 |
| `services/patient-service` | `patient-service` | 8084 |
| `shared/common-events` | `common-events` | Chưa cần port |
| `shared/common-security` | `common-security` | Chưa cần port |
| `shared/common-web` | `common-web` | Chưa cần port |

Các module trong `shared` là thư viện dùng chung nên thông thường **không chạy như một service độc lập**, vì vậy không cần port.

---

## 6. Luồng kiến trúc tổng thể

```text
Client
  |
  v
api-gateway
  |
  |-- appointment-service
  |-- doctor-service
  |-- notification-service
  `-- patient-service

config-server  -> cung cấp cấu hình tập trung
eureka-server  -> registry/discovery cho các service
shared/*       -> thư viện dùng chung được các service import khi cần
```

### Luồng ví dụ: tạo lịch hẹn

```text
Client
  |
  v
api-gateway
  |
  v
appointment-service
  |
  |-- gọi doctor-service để kiểm tra bác sĩ nếu cần
  |-- gọi patient-service để kiểm tra bệnh nhân nếu cần
  `-- gửi event/thông báo sang notification-service nếu cần
```

Quy tắc quan trọng: `appointment-service` **không được query trực tiếp database của `doctor-service` hoặc `patient-service`**. Nếu cần dữ liệu, service phải gọi API hoặc dùng event/message.

---

## 7. Nguyên tắc database cho microservices

Dự án nên đi theo hướng:

```text
One Service - One Database
```

Có thể triển khai đơn giản bằng một PostgreSQL server nhưng nhiều database:

```text
PostgreSQL Server
|-- appointment_db
|-- doctor_db
|-- patient_db
`-- notification_db
```

Mapping:

| Service | Database |
| --- | --- |
| `appointment-service` | `appointment_db` |
| `doctor-service` | `doctor_db` |
| `patient-service` | `patient_db` |
| `notification-service` | `notification_db` |

Ví dụ cấu hình cho `appointment-service`:

```yaml
server:
  port: 8081

spring:
  application:
    name: appointment-service

  datasource:
    url: jdbc:postgresql://localhost:5432/appointment_db
    username: postgres
    password: 123456

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

Khi làm team, mỗi thành viên nên phụ trách service và database/schema của service đó. Không sửa trực tiếp database của service khác nếu không phải người phụ trách.

---

## 8. Kiến trúc bên trong mỗi service nghiệp vụ

Các service nghiệp vụ nên chia theo 4 layer chính:

```text
api
application
domain
infrastructure
```

Ý nghĩa:

| Layer | Vai trò | Không nên làm |
| --- | --- | --- |
| `api` | Nhận request HTTP, validate input cơ bản, chuyển DTO thành command, trả response. | Không chứa business logic chính, không gọi thẳng JPA Repository. |
| `application` | Điều phối use case, quản lý flow nghiệp vụ, gọi domain, gọi repository interface/port. | Không phụ thuộc trực tiếp database/JPA/RestTemplate/Kafka cụ thể. |
| `domain` | Chứa rule nghiệp vụ cốt lõi: Aggregate, Entity, Value Object, Domain Event, Repository Interface. | Không phụ thuộc Spring MVC, JPA, HTTP, database. |
| `infrastructure` | Chứa code kỹ thuật: JPA, PostgreSQL, RestClient/Feign, Kafka/RabbitMQ, config bean. | Không chứa business rule cốt lõi. |

Luồng phụ thuộc mong muốn:

```text
api
  -> application
       -> domain

infrastructure
  -> implements repository/port interface
```

Luồng chạy ví dụ:

```text
Controller
  -> UseCase
       -> Domain Aggregate
            -> Repository Interface
                 -> Repository Adapter
                      -> JpaRepository
                           -> Database
```

---

## 9. Cấu trúc hiện tại của `appointment-service`

Package gốc:

```text
com.group01.appointment
```

Cấu trúc đã bắt đầu dựng:

```text
appointment-service/
`-- src/main/java/com/group01/appointment/
    |-- api/
    |   |-- controller/
    |   |-- dto/
    |   `-- exception/
    |
    |-- application/
    |   |-- command/
    |   |-- port/
    |   |-- result/
    |   `-- usecase/
    |
    |-- domain/
    |   |-- aggregate/
    |   |-- entity/
    |   |-- event/
    |   |-- exception/
    |   |-- repository/
    |   `-- valueobject/
    |
    |-- infrastructure/
    |   |-- client/
    |   |-- config/
    |   |-- messaging/
    |   `-- persistence/
    |
    `-- AppointmentServiceApplication.java
```

---

## 10. Class hiện tại / dự kiến trong `appointment-service`

### 10.1. API layer

```text
api/
|-- controller/
|   `-- AppointmentController.java
|
|-- dto/
|   |-- CreateAppointmentRequest.java
|   |-- UpdateAppointmentRequest.java
|   |-- CancelAppointmentRequest.java
|   `-- AppointmentResponse.java
|
`-- exception/
    `-- GlobalExceptionHandler.java
```

Vai trò:

| Class | Vai trò |
| --- | --- |
| `AppointmentController` | Nhận HTTP request liên quan đến appointment, gọi các use case tương ứng. |
| `CreateAppointmentRequest` | DTO nhận request tạo lịch hẹn. |
| `UpdateAppointmentRequest` | DTO nhận request cập nhật lịch hẹn. |
| `CancelAppointmentRequest` | DTO nhận request hủy lịch hẹn. |
| `AppointmentResponse` | DTO trả dữ liệu lịch hẹn ra client. |
| `GlobalExceptionHandler` | Bắt exception và trả lỗi thống nhất cho API. |

---

### 10.2. Application layer

```text
application/
|-- command/
|   |-- CreateAppointmentCommand.java
|   |-- UpdateAppointmentCommand.java
|   `-- CancelAppointmentCommand.java
|
|-- port/
|   |-- DoctorClientPort.java
|   |-- PatientClientPort.java
|   `-- NotificationPort.java
|
|-- result/
|   `-- AppointmentResult.java
|
`-- usecase/
    |-- CreateAppointmentUseCase.java
    |-- GetAppointmentUseCase.java
    |-- GetAppointmentsUseCase.java
    |-- UpdateAppointmentUseCase.java
    |-- CancelAppointmentUseCase.java
    `-- ConfirmAppointmentUseCase.java
```

Vai trò:

| Class | Vai trò |
| --- | --- |
| `CreateAppointmentCommand` | Input nội bộ cho use case tạo lịch hẹn. Được convert từ `CreateAppointmentRequest`. |
| `UpdateAppointmentCommand` | Input nội bộ cho use case cập nhật lịch hẹn. |
| `CancelAppointmentCommand` | Input nội bộ cho use case hủy lịch hẹn. |
| `AppointmentResult` | Output nội bộ từ use case, sau đó có thể convert sang `AppointmentResponse`. |
| `CreateAppointmentUseCase` | Xử lý flow tạo lịch hẹn. |
| `GetAppointmentUseCase` | Lấy chi tiết một lịch hẹn. |
| `GetAppointmentsUseCase` | Lấy danh sách lịch hẹn. |
| `UpdateAppointmentUseCase` | Cập nhật thông tin lịch hẹn. |
| `CancelAppointmentUseCase` | Hủy lịch hẹn. |
| `ConfirmAppointmentUseCase` | Xác nhận lịch hẹn. |
| `DoctorClientPort` | Interface để `appointment-service` gọi sang `doctor-service` khi cần. |
| `PatientClientPort` | Interface để `appointment-service` gọi sang `patient-service` khi cần. |
| `NotificationPort` | Interface để gửi thông báo/event sau khi lịch hẹn thay đổi trạng thái. |

Lưu ý: `port/` có thể để trống nếu chưa làm giao tiếp liên service. Khi cần gọi service khác, tạo port trước rồi implement ở `infrastructure/client` hoặc `infrastructure/messaging`.

---

### 10.3. Domain layer

```text
domain/
|-- aggregate/
|   `-- Appointment.java
|
|-- entity/
|
|-- event/
|   |-- AppointmentCreatedEvent.java
|   |-- AppointmentConfirmedEvent.java
|   |-- AppointmentCanceledEvent.java
|   `-- AppointmentUpdatedEvent.java
|
|-- exception/
|   `-- DomainException.java
|
|-- repository/
|   `-- AppointmentRepository.java
|
`-- valueobject/
    |-- AppointmentId.java
    |-- AppointmentReason.java
    |-- AppointmentStatus.java
    |-- AppointmentTime.java
    |-- CancelReason.java
    |-- DoctorId.java
    `-- PatientId.java
```

Vai trò:

| Class | Vai trò |
| --- | --- |
| `Appointment` | Aggregate Root chính của `appointment-service`. Chứa rule nghiệp vụ như tạo, xác nhận, hủy, cập nhật lịch hẹn. |
| `AppointmentCreatedEvent` | Domain event phát sinh khi lịch hẹn được tạo. |
| `AppointmentConfirmedEvent` | Domain event phát sinh khi lịch hẹn được xác nhận. |
| `AppointmentCanceledEvent` | Domain event phát sinh khi lịch hẹn bị hủy. |
| `AppointmentUpdatedEvent` | Domain event phát sinh khi lịch hẹn được cập nhật. |
| `DomainException` | Exception dùng cho lỗi nghiệp vụ trong domain. |
| `AppointmentRepository` | Repository interface dùng để lưu/lấy `Appointment`. Implementation thật nằm ở infrastructure. |
| `AppointmentId` | Value Object bọc ID của lịch hẹn. |
| `AppointmentReason` | Value Object bọc lý do khám, có thể kiểm tra blank/độ dài. |
| `AppointmentStatus` | Trạng thái lịch hẹn, ví dụ `PENDING`, `CONFIRMED`, `CANCELED`, `COMPLETED`. |
| `AppointmentTime` | Value Object biểu diễn thời gian lịch hẹn, có thể chứa `startTime`, `endTime` và rule thời gian. |
| `CancelReason` | Value Object bọc lý do hủy lịch. |
| `DoctorId` | Value Object bọc ID bác sĩ. |
| `PatientId` | Value Object bọc ID bệnh nhân. |

### Ghi chú về `domain/entity`

Hiện tại `domain/entity` có thể để trống. Folder này chỉ dùng khi trong aggregate có object con có ID riêng nhưng không phải Aggregate Root chính.

Ví dụ sau này có thể thêm:

```text
domain/entity/
|-- AppointmentHistory.java
`-- AppointmentNote.java
```

`AppointmentHistory` hoặc `AppointmentNote` là entity con của `Appointment`, nhưng chưa bắt buộc trong giai đoạn skeleton.

---

### 10.4. Infrastructure layer

```text
infrastructure/
|-- client/
|   |-- DoctorClientAdapter.java
|   `-- PatientClientAdapter.java
|
|-- config/
|   `-- RestTemplateConfig.java
|
|-- messaging/
|   `-- AppointmentEventPublisherAdapter.java
|
`-- persistence/
    |-- AppointmentJpaEntity.java
    |-- AppointmentJpaRepository.java
    |-- AppointmentMapper.java
    `-- AppointmentRepositoryAdapter.java
```

Vai trò:

| Class | Vai trò |
| --- | --- |
| `AppointmentJpaEntity` | Entity JPA map với bảng `appointments` trong database. Đây là object phục vụ persistence, không phải domain aggregate. |
| `AppointmentJpaRepository` | Spring Data JPA repository, thao tác trực tiếp với database. |
| `AppointmentMapper` | Chuyển đổi giữa `Appointment` domain object và `AppointmentJpaEntity`. |
| `AppointmentRepositoryAdapter` | Implement `AppointmentRepository` bằng JPA thật. Đây là adapter nối domain/application với database. |
| `DoctorClientAdapter` | Implementation thật của `DoctorClientPort`, gọi HTTP sang `doctor-service`. |
| `PatientClientAdapter` | Implementation thật của `PatientClientPort`, gọi HTTP sang `patient-service`. |
| `AppointmentEventPublisherAdapter` | Implementation thật để publish event/message sau khi appointment thay đổi. |
| `RestTemplateConfig` | Cấu hình bean `RestTemplate` hoặc HTTP client dùng để gọi service khác. |

Trong giai đoạn đầu, nếu chưa gọi service khác và chưa dùng message broker, chỉ cần làm trước nhóm `persistence`:

```text
infrastructure/persistence/
|-- AppointmentJpaEntity.java
|-- AppointmentJpaRepository.java
|-- AppointmentMapper.java
`-- AppointmentRepositoryAdapter.java
```

---

## 11. Luồng xử lý trong `appointment-service`

### 11.1. Tạo lịch hẹn

```text
POST /api/appointments
  |
  v
AppointmentController
  |
  v
CreateAppointmentRequest
  |
  v
CreateAppointmentCommand
  |
  v
CreateAppointmentUseCase
  |
  |-- kiểm tra patient nếu cần qua PatientClientPort
  |-- kiểm tra doctor nếu cần qua DoctorClientPort
  |
  v
Appointment.create(...)
  |
  v
AppointmentRepository.save(...)
  |
  v
AppointmentRepositoryAdapter
  |
  v
AppointmentMapper.toJpaEntity(...)
  |
  v
AppointmentJpaRepository.save(...)
  |
  v
appointment_db
```

### 11.2. Xác nhận lịch hẹn

```text
ConfirmAppointmentUseCase
  |
  v
AppointmentRepository.findById(...)
  |
  v
Appointment.confirm()
  |
  v
AppointmentRepository.save(...)
  |
  v
publish AppointmentConfirmedEvent nếu cần
```

### 11.3. Hủy lịch hẹn

```text
CancelAppointmentUseCase
  |
  v
AppointmentRepository.findById(...)
  |
  v
Appointment.cancel(CancelReason)
  |
  v
AppointmentRepository.save(...)
  |
  v
publish AppointmentCanceledEvent nếu cần
```

---

## 12. Phân biệt các loại class trong `appointment-service`

| Nhóm | Ví dụ | Ý nghĩa |
| --- | --- | --- |
| Request DTO | `CreateAppointmentRequest` | Dữ liệu đi từ HTTP request vào controller. |
| Command | `CreateAppointmentCommand` | Input nội bộ cho use case. Không phụ thuộc HTTP. |
| Use Case | `CreateAppointmentUseCase` | Class xử lý một hành động nghiệp vụ cụ thể. |
| Result | `AppointmentResult` | Output nội bộ từ use case. |
| Response DTO | `AppointmentResponse` | Dữ liệu trả về client. |
| Aggregate | `Appointment` | Object nghiệp vụ chính, giữ rule quan trọng. |
| Value Object | `AppointmentTime`, `DoctorId`, `PatientId` | Object không có ID riêng, dùng để biểu diễn giá trị có ý nghĩa nghiệp vụ. |
| Repository Interface | `AppointmentRepository` | Interface để lưu/lấy aggregate. |
| JPA Entity | `AppointmentJpaEntity` | Object map với bảng database. |
| JpaRepository | `AppointmentJpaRepository` | Công cụ Spring Data JPA để thao tác database. |
| Mapper | `AppointmentMapper` | Chuyển đổi domain object và JPA entity. |
| Adapter | `AppointmentRepositoryAdapter` | Cầu nối giữa repository interface và JPA thật. |

---

## 13. So sánh với cách Spring Boot CRUD truyền thống

Cách truyền thống thường là:

```text
controller
  -> service
      -> repository
          -> entity
```

Cách hiện tại là:

```text
api/controller
  -> application/usecase
      -> domain/aggregate
          -> domain/repository interface
              -> infrastructure/persistence adapter
                  -> JpaRepository
                      -> database
```

Ưu điểm:

- Use case rõ ràng, dễ biết class nào xử lý nghiệp vụ nào.
- Domain không bị phụ thuộc trực tiếp vào JPA/Spring MVC.
- Dễ test business rule hơn.
- Dễ thay đổi cách lưu dữ liệu hơn vì application/domain chỉ phụ thuộc interface.
- Hợp với microservices vì mỗi service có domain riêng.

Nhược điểm:

- Nhiều class hơn.
- Ban đầu nhìn rườm rà hơn CRUD truyền thống.
- Với chức năng CRUD quá đơn giản, cách này có thể hơi nặng.

---

## 14. Các service khác nên làm tương tự

### `doctor-service`

Aggregate chính:

```text
domain/aggregate/Doctor.java
```

Có thể có:

```text
domain/entity/WorkingSchedule.java
domain/valueobject/DoctorId.java
domain/valueobject/DoctorName.java
domain/valueobject/DoctorStatus.java
domain/valueobject/Specialty.java
domain/valueobject/WorkingTimeRange.java
domain/repository/DoctorRepository.java
```

Use case có thể gồm:

```text
CreateDoctorUseCase.java
GetDoctorUseCase.java
GetDoctorsUseCase.java
UpdateDoctorUseCase.java
DeactivateDoctorUseCase.java
UpdateDoctorScheduleUseCase.java
```

### `patient-service`

Aggregate chính:

```text
domain/aggregate/Patient.java
```

Có thể có:

```text
domain/valueobject/PatientId.java
domain/valueobject/PatientName.java
domain/valueobject/Gender.java
domain/valueobject/DateOfBirth.java
domain/valueobject/PhoneNumber.java
domain/valueobject/EmailAddress.java
domain/valueobject/Address.java
domain/repository/PatientRepository.java
```

Use case có thể gồm:

```text
CreatePatientUseCase.java
GetPatientUseCase.java
GetPatientsUseCase.java
UpdatePatientUseCase.java
DeactivatePatientUseCase.java
```

### `notification-service`

Aggregate chính:

```text
domain/aggregate/Notification.java
```

Có thể có:

```text
domain/entity/NotificationAttempt.java
domain/valueobject/NotificationId.java
domain/valueobject/Recipient.java
domain/valueobject/NotificationContent.java
domain/valueobject/NotificationChannel.java
domain/valueobject/NotificationStatus.java
domain/repository/NotificationRepository.java
```

Use case có thể gồm:

```text
CreateNotificationUseCase.java
SendNotificationUseCase.java
GetNotificationUseCase.java
MarkNotificationSentUseCase.java
MarkNotificationFailedUseCase.java
```

---

## 15. Quy tắc làm việc nhóm

### 15.1. Chia người theo service

Ví dụ:

| Thành viên | Phụ trách |
| --- | --- |
| Member A | `appointment-service` |
| Member B | `doctor-service` |
| Member C | `patient-service` |
| Member D | `notification-service` |

Mỗi người chỉ sửa code và database của service mình phụ trách, trừ khi đã thống nhất contract/API với service khác.

### 15.2. Không dùng database chéo

Sai:

```text
appointment-service query trực tiếp bảng doctors trong doctor_db
```

Đúng:

```text
appointment-service gọi API sang doctor-service
doctor-service tự query doctor_db và trả response
```

### 15.3. Contract giữa service phải rõ

Ví dụ `doctor-service` cung cấp API:

```text
GET /api/doctors/{doctorId}
```

`appointment-service` chỉ phụ thuộc vào response contract, không phụ thuộc vào entity/database của `doctor-service`.

### 15.4. Không nhét business logic vào shared

`shared` chỉ nên chứa code dùng chung thật sự:

- `ApiResponse`
- exception base
- security helper
- event model chung nếu cần

Không nên đưa `Appointment`, `Doctor`, `Patient` vào `shared`, vì như vậy các service sẽ bị coupling mạnh.

---

## 16. Ghi chú hiện trạng

- Dự án đã có skeleton theo microservices với các nhóm `infra`, `services`, `shared`.
- `appointment-service` đã bắt đầu chia package theo Clean Architecture: `api`, `application`, `domain`, `infrastructure`.
- `domain` của `appointment-service` đã có `Appointment` aggregate, các value object, repository interface, domain event và domain exception.
- `application` của `appointment-service` đã có command, result, port và các use case chính.
- `infrastructure` của `appointment-service` nên ưu tiên hoàn thiện `persistence` trước khi làm `client` và `messaging`.
- Các service còn lại nên áp dụng cùng một style để team dễ đọc và dễ chia việc.

---

## 17. Thứ tự làm tiếp theo đề xuất

1. Hoàn thiện domain của `appointment-service`:
    - `Appointment`
    - `AppointmentStatus`
    - `AppointmentTime`
    - `AppointmentRepository`
    - `DomainException`

2. Hoàn thiện persistence:
    - `AppointmentJpaEntity`
    - `AppointmentJpaRepository`
    - `AppointmentMapper`
    - `AppointmentRepositoryAdapter`

3. Hoàn thiện use case cơ bản:
    - `CreateAppointmentUseCase`
    - `GetAppointmentUseCase`
    - `GetAppointmentsUseCase`
    - `UpdateAppointmentUseCase`
    - `CancelAppointmentUseCase`
    - `ConfirmAppointmentUseCase`

4. Hoàn thiện API:
    - `AppointmentController`
    - request/response DTO
    - `GlobalExceptionHandler`

5. Sau khi CRUD appointment chạy ổn, mới thêm:
    - `DoctorClientPort` + `DoctorClientAdapter`
    - `PatientClientPort` + `PatientClientAdapter`
    - `NotificationPort` + messaging adapter

---

## 18. Tóm tắt ngắn

```text
infra/       = service hạ tầng cho toàn hệ thống
services/    = service nghiệp vụ độc lập
shared/      = thư viện dùng chung

api/             = HTTP layer
application/     = use case layer
domain/          = business rule layer
infrastructure/  = technical implementation layer

Appointment = Aggregate Root
AppointmentTime = Value Object
AppointmentRepository = Repository Interface
AppointmentRepositoryAdapter = JPA implementation
AppointmentJpaEntity = database mapping object
```
