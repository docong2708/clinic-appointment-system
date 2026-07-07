# ✅ Kiểm Tra Email Nhận Được

## Bước 1: Truy Cập Gmail
1. Đăng nhập: https://mail.google.com
2. Tài khoản: **mssclinicnotify@gmail.com**
3. Kiểm tra Inbox (có thể cần chờ 1-2 phút)

## Bước 2: Tìm Email
Email sẽ có:
- **From:** noreply@clinic.com
- **Subject:** System Notification: APPOINTMENT_CREATED
- **Body:** Event processed from appointment-service

## Bước 3: Kiểm Tra Content
Email sẽ chứa:
- HTML format đẹp mắt (Phase 4 feature)
- Event details từ payload
- Timestamp

---

## 🔍 Nếu Email KHÔNG Có

**Kiểm tra:**
1. **Logs của Notification Service** - Tìm dòng `Email sent successfully`
2. **Spam folder** - Email có thể trong Spam/Promotions
3. **Database** - Check `notification_deliveries`:
   ```sql
   SELECT status, sent_at, last_error FROM notification_deliveries 
   ORDER BY created_at DESC LIMIT 1;
   ```

**Nếu status = FAILED:**
- Check `last_error` message
- Kiểm tra Gmail App Password có đúng không
- Kiểm tra internet connection

---

## ✨ Nếu Email ĐÃ Có

**Chúc mừng!** ✅ Phase 3 & 4 hoạt động hoàn toàn!

Bạn có thể tiếp tục:
1. **Test Deduplication** - Gửi lại cùng eventId
2. **Test Retry** - Giả lập lỗi SMTP để xem scheduler retry
3. **Tiếp tục Phase 5** (nếu có)