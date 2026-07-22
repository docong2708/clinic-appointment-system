# Gmail Configuration Guide for Notification Service

## Vấn đề Hiện Tại

Cấu hình hiện tại:
\\\yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: noreply@clinic.com
    password: test-password  # ❌ KHÔNG HỢP LỆ
\\\

**Error khi gửi:** Authentication failed (Invalid credentials)

---

## Giải Pháp: Dùng Gmail App Password

### Bước 1: Enable 2-Factor Authentication (2FA)

1. Đăng nhập Gmail: https://myaccount.google.com
2. Vào **Security** (bên trái)
3. Scroll xuống **How you sign in to Google**
4. Click **2-Step Verification**
5. Làm theo hướng dẫn để enable 2FA

### Bước 2: Tạo App Password

1. Quay lại **Security** page
2. Scroll xuống **App passwords** (chỉ xuất hiện nếu đã enable 2FA)
3. Chọn:
   - **App:** Mail
   - **Device:** Windows Computer (hoặc device bạn dùng)
4. Click **Generate**
5. Google sẽ hiển thị **16-character password**
   - Ví dụ: \bcd efgh ijkl mnop\ (bỏ space → \bcdefghijklmnop\)
6. **Copy** password này

### Bước 3: Cập nhật application.yml

`yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: noreply@clinic.com          # Gmail address
    password: abcdefghijklmnop            # App Password (16 chars, no spaces)
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true
          connectiontimeout: 5000
          timeout: 5000
          writetimeout: 5000
`

---

## Cách 2: Dùng Environment Variables (Recommended)

Thay vì hardcode password, dùng environment variables:

### application.yml
\\\yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: \
    password: \
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
\\\

### Set Environment Variables

**Windows (Command Prompt):**
\\\atch
set MAIL_USERNAME=noreply@clinic.com
set MAIL_PASSWORD=abcdefghijklmnop
\\\

**Windows (PowerShell):**
\\\powershell
\ = "noreply@clinic.com"
\ = "abcdefghijklmnop"
\\\

**Linux/Mac (Bash):**
\\\ash
export MAIL_USERNAME=noreply@clinic.com
export MAIL_PASSWORD=abcdefghijklmnop
\\\

### Docker (.env file)
\\\
MAIL_USERNAME=noreply@clinic.com
MAIL_PASSWORD=abcdefghijklmnop
\\\

---

## Troubleshooting

### Error: "Invalid credentials"
- ✅ Đảm bảo 2FA đã enable
- ✅ Dùng App Password chứ không phải account password
- ✅ Copy đúng tất cả 16 ký tự (bỏ space)
- ✅ Restart service sau khi update password

### Error: "Connection timeout"
- ✅ Kiểm tra firewall cho port 587
- ✅ Kiểm tra internet connection
- ✅ Tăng timeout: \connectiontimeout: 10000\

### Error: "535 5.7.8 Username and password not accepted"
- ✅ Kiểm tra Gmail address chính xác
- ✅ Kiểm tra App Password không có space
- ✅ Chắc chắn là App Password, không phải account password

---

## Testing Email Configuration

### 1. Test via Spring Boot

Tạo test endpoint:
\\\java
@RestController
public class EmailTestController {
    private final JavaMailSender mailSender;
    
    public EmailTestController(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    
    @GetMapping("/test-email")
    public ResponseEntity<String> testEmail() {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo("your-email@gmail.com");
            message.setSubject("Test Email from Clinic");
            message.setText("If you see this, Gmail is working!");
            message.setFrom("noreply@clinic.com");
            mailSender.send(message);
            return ResponseEntity.ok("Email sent successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}
\\\

Gọi: \GET http://localhost:8083/test-email\

### 2. Check Logs

Run service và xem logs:
\\\ash
mvn spring-boot:run
\\\

Nếu thành công sẽ thấy:
\\\
[INFO] Email sent successfully to: your-email@gmail.com
\\\

---

## Security Best Practice

**KHÔNG LÀM:**
- ❌ Commit password vào Git
- ❌ Hardcode sensitive info trong code
- ❌ Share App Password công khai

**LÀM:**
- ✅ Dùng environment variables
- ✅ Dùng .env file (add vào .gitignore)
- ✅ Dùng secret management tool (AWS Secrets, HashiCorp Vault)
- ✅ Rotate App Password định kỳ

---

## Quick Setup Checklist

- [ ] Enable 2FA trên Gmail account
- [ ] Tạo App Password từ Google Account
- [ ] Copy App Password (16 ký tự)
- [ ] Update \pplication.yml\ hoặc env var
- [ ] Restart notification service
- [ ] Test email send
- [ ] Verify email received