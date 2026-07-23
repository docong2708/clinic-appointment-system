-- Localize seeded email templates to Vietnamese.

UPDATE notification_templates
SET subject = 'Xác minh địa chỉ email của bạn',
    body = $$Xin chào {{userName}},

Cảm ơn bạn đã đăng ký. Vui lòng nhấn vào liên kết bên dưới để xác minh địa chỉ email:
{{verificationLink}}

Nếu bạn không tạo tài khoản, bạn không cần thực hiện thêm thao tác nào.

Trân trọng,
MSS Clinic$$,
    version = version + 1
WHERE name = 'email-verification'
  AND channel = 'EMAIL';

UPDATE notification_templates
SET subject = 'Yêu cầu đặt lại mật khẩu',
    body = $$Xin chào {{userName}},

Chúng tôi đã nhận được yêu cầu đặt lại mật khẩu của bạn. Vui lòng nhấn vào liên kết bên dưới để tạo mật khẩu mới:
{{resetLink}}

Liên kết này sẽ hết hạn sau 24 giờ.

Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.

Trân trọng,
MSS Clinic$$,
    version = version + 1
WHERE name = 'reset-password'
  AND channel = 'EMAIL';

UPDATE notification_templates
SET subject = 'Xác nhận đơn hàng - {{orderId}}',
    body = $$Xin chào {{userName}},

Cảm ơn bạn đã mua hàng. Đơn hàng của bạn đã được xác nhận.

Thông tin đơn hàng:
- Mã đơn hàng: {{orderId}}
- Sản phẩm: {{productName}}
- Số tiền: {{totalAmount}}
- Ngày đặt hàng: {{orderDate}}

Chúng tôi sẽ gửi khóa bản quyền cho bạn trong một email riêng.

Trân trọng,
MSS Clinic$$,
    version = version + 1
WHERE name = 'purchase-confirmation'
  AND channel = 'EMAIL';

UPDATE notification_templates
SET subject = 'Khóa bản quyền cho {{productName}}',
    body = $$Xin chào {{userName}},

Đây là khóa bản quyền của bạn cho {{productName}}:

Khóa bản quyền: {{licenseKey}}

Vui lòng lưu khóa này cẩn thận và không chia sẻ với người khác.

Hướng dẫn kích hoạt:
1. Mở ứng dụng
2. Vào Cài đặt -> Bản quyền
3. Nhập khóa bản quyền ở trên

Nếu bạn có thắc mắc, vui lòng liên hệ support@example.com.

Trân trọng,
MSS Clinic$$,
    version = version + 1
WHERE name = 'license-delivery'
  AND channel = 'EMAIL';

UPDATE notification_templates
SET subject = 'Tin vui! Ứng dụng {{appName}} của bạn đã được duyệt',
    body = $$Xin chào {{userName}},

Chúc mừng bạn! Ứng dụng "{{appName}}" đã được quản trị viên phê duyệt và hiện đã hoạt động.

Bạn có thể xem ứng dụng tại: {{appLink}}

Các bước tiếp theo:
- Theo dõi số liệu ứng dụng trong bảng điều khiển
- Cập nhật thông tin ứng dụng bất cứ lúc nào trong phần Cài đặt ứng dụng
- Liên hệ bộ phận hỗ trợ nếu bạn có câu hỏi

Trân trọng,
Đội ngũ duyệt ứng dụng$$,
    version = version + 1
WHERE name = 'app-approved'
  AND channel = 'EMAIL';

UPDATE notification_templates
SET subject = 'Cập nhật về ứng dụng {{appName}}',
    body = $$Xin chào {{userName}},

Cảm ơn bạn đã gửi ứng dụng "{{appName}}". Rất tiếc, chúng tôi chưa thể phê duyệt ứng dụng vào thời điểm này.

Lý do từ chối:
{{reason}}

Bạn cần làm gì tiếp theo:
1. Xem lại lý do từ chối ở trên
2. Thực hiện các thay đổi cần thiết cho ứng dụng
3. Gửi lại để được xét duyệt

Nếu bạn có câu hỏi về quyết định này, vui lòng liên hệ support@example.com.

Trân trọng,
Đội ngũ duyệt ứng dụng$$,
    version = version + 1
WHERE name = 'app-rejected'
  AND channel = 'EMAIL';
