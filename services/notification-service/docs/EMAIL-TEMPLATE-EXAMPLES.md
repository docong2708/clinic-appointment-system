# 📧 Email Template Flow - Complete Examples

**Version:** 1.0  
**Date:** 2026-07-09  

---

## 6 Email Templates - Complete Examples

### 1. EMAIL_VERIFICATION Template

**Template Key:** email-verification

**Subject:**
`
Verify your email address
`

**Body:**
`
Hi {{userName}},

Thank you for registering. Please click the link below to verify your email address:
{{verificationLink}}

If you did not create an account, no further action is required.

Best regards,
The Team
`

**Required Placeholders:**
| Placeholder | Description | Example |
|-------------|-------------|---------|
| userName | User's display name | "John Doe" |
| verificationLink | Verification URL | "https://app.com/verify?token=abc123" |

---

### 2. RESET_PASSWORD Template

**Template Key:** eset-password

**Subject:**
`
Password Reset Request
`

**Body:**
`
Hi {{userName}},

We received a request to reset your password. Click the link below to set a new password:
{{resetLink}}

This link expires in 24 hours.

If you did not request a password reset, please ignore this email.

Best regards,
The Team
`

**Required Placeholders:**
| Placeholder | Description | Example |
|-------------|-------------|---------|
| userName | User's display name | "John Doe" |
| resetLink | Password reset URL | "https://app.com/reset?token=xyz789" |

---

### 3. PURCHASE_CONFIRMATION Template

**Template Key:** purchase-confirmation

**Subject:**
`
Order Confirmation - {{orderId}}
`

**Body:**
`
Hi {{userName}},

Thank you for your purchase! Your order has been confirmed.

Order Details:
- Order ID: {{orderId}}
- Product: {{productName}}
- Amount: {{totalAmount}}
- Date: {{orderDate}}

We will send you a separate email with your license key shortly.

Best regards,
The Team
`

**Required Placeholders:**
| Placeholder | Description | Example |
|-------------|-------------|---------|
| userName | Buyer's name | "Jane Smith" |
| orderId | Order ID | "ORD-20260709-001" |
| productName | Product name | "Premium License" |
| totalAmount | Total amount | ".00" |
| orderDate | Order date | "2026-07-09" |

---

### 4. LICENSE_DELIVERY Template

**Template Key:** license-delivery

**Subject:**
`
Your License Key for {{productName}}
`

**Body:**
`
Hi {{userName}},

Here is your license key for {{productName}}:

License Key: {{licenseKey}}

Please keep this key safe and do not share it with others.

Activation Instructions:
1. Open the application
2. Go to Settings → License
3. Enter the license key above

If you have any questions, contact support@example.com

Best regards,
The Team
`

**Required Placeholders:**
| Placeholder | Description | Example |
|-------------|-------------|---------|
| userName | License owner's name | "Jane Smith" |
| productName | Product name | "SaaS Premium" |
| licenseKey | License key/code | "LIC-XXXX-YYYY-ZZZZ" |

---

### 5. APP_APPROVED Template

**Template Key:** pp-approved

**Subject:**
`
Great news! Your app {{appName}} has been approved
`

**Body:**
`
Hi {{userName}},

Congratulations! Your application "{{appName}}" has been approved by our administrators and is now live.

You can view it here: {{appLink}}

Next steps:
- Monitor your app analytics in the dashboard
- Update app details anytime in App Settings
- Contact support if you have any questions

Best regards,
The Review Team
`

**Required Placeholders:**
| Placeholder | Description | Example |
|-------------|-------------|---------|
| userName | Developer's name | "John Developer" |
| appName | Application name | "My Cool App" |
| appLink | Public app URL | "https://app.com/apps/123" |

---

### 6. APP_REJECTED Template

**Template Key:** pp-rejected

**Subject:**
`
Update on your app {{appName}}
`

**Body:**
`
Hi {{userName}},

Thank you for submitting "{{appName}}". Unfortunately, we cannot approve it at this time.

Reason for rejection:
{{reason}}

What to do next:
1. Review the rejection reason above
2. Make the necessary changes to your application
3. Resubmit for review

If you have questions about this decision, contact support@example.com

Best regards,
The Review Team
`

**Required Placeholders:**
| Placeholder | Description | Example |
|-------------|-------------|---------|
| userName | Developer's name | "John Developer" |
| appName | Application name | "My Cool App" |
| reason | Rejection reason (multi-line) | "Application contains sensitive data without proper encryption." |

---

## Usage Examples

### Example 1: Send Email Verification

**Request:**
`json
{
  "eventType": "EMAIL_VERIFICATION",
  "recipientEmail": "user@example.com",
  "recipientName": "John Doe",
  "recipientUserId": "550e8400-e29b-41d4-a716-446655440001",
  "payload": {
    "userName": "John Doe",
    "verificationLink": "https://app.com/verify?token=abc123"
  }
}
`

**Rendered Email:**
`
Subject: Verify your email address
Body: Hi John Doe,

Thank you for registering. Please click the link below to verify your email address:
https://app.com/verify?token=abc123

If you did not create an account, no further action is required.

Best regards,
The Team
`

---

### Example 2: Send License Delivery

**Request:**
`json
{
  "eventType": "LICENSE_DELIVERY",
  "recipientEmail": "buyer@example.com",
  "recipientName": "Jane Smith",
  "recipientUserId": "550e8400-e29b-41d4-a716-446655440002",
  "payload": {
    "userName": "Jane Smith",
    "productName": "SaaS Premium",
    "licenseKey": "LIC-XXXX-YYYY-ZZZZ-1234"
  }
}
`

**Rendered Email:**
`
Subject: Your License Key for SaaS Premium
Body: Hi Jane Smith,

Here is your license key for SaaS Premium:

License Key: LIC-XXXX-YYYY-ZZZZ-1234

Please keep this key safe and do not share it with others.

Activation Instructions:
1. Open the application
2. Go to Settings → License
3. Enter the license key above

If you have any questions, contact support@example.com

Best regards,
The Team
`

---

### Example 3: Send App Approved

**Request:**
`json
{
  "eventType": "APP_APPROVED",
  "recipientEmail": "developer@example.com",
  "recipientName": "John Developer",
  "recipientUserId": "550e8400-e29b-41d4-a716-446655440003",
  "payload": {
    "userName": "John Developer",
    "appName": "My Cool App",
    "appLink": "https://app.com/apps/123"
  }
}
`

**Rendered Email:**
`
Subject: Great news! Your app My Cool App has been approved
Body: Hi John Developer,

Congratulations! Your application "My Cool App" has been approved by our administrators and is now live.

You can view it here: https://app.com/apps/123

Next steps:
- Monitor your app analytics in the dashboard
- Update app details anytime in App Settings
- Contact support if you have any questions

Best regards,
The Review Team
`

---

## API Testing

### Test All 6 Email Types

`ash
# 1. Email Verification
curl -X POST http://localhost:8083/api/v1/internal/emails/send \
  -H "Content-Type: application/json" \
  -d '{"eventType": "EMAIL_VERIFICATION", "recipientEmail": "test@example.com", "recipientName": "Test User", "payload": {"userName": "Test User", "verificationLink": "https://app.com/verify?token=test"}}'

# 2. Reset Password
curl -X POST http://localhost:8083/api/v1/internal/emails/send \
  -H "Content-Type: application/json" \
  -d '{"eventType": "RESET_PASSWORD", "recipientEmail": "test@example.com", "recipientName": "Test User", "payload": {"userName": "Test User", "resetLink": "https://app.com/reset?token=test"}}'

# 3. Purchase Confirmation
curl -X POST http://localhost:8083/api/v1/internal/emails/send \
  -H "Content-Type: application/json" \
  -d '{"eventType": "PURCHASE_CONFIRMATION", "recipientEmail": "test@example.com", "recipientName": "Test User", "payload": {"userName": "Test User", "orderId": "ORD-123", "productName": "Product", "totalAmount": ".00", "orderDate": "2026-07-09"}}'

# 4. License Delivery
curl -X POST http://localhost:8083/api/v1/internal/emails/send \
  -H "Content-Type: application/json" \
  -d '{"eventType": "LICENSE_DELIVERY", "recipientEmail": "test@example.com", "recipientName": "Test User", "payload": {"userName": "Test User", "productName": "Product", "licenseKey": "LIC-123"}}'

# 5. App Approved
curl -X POST http://localhost:8083/api/v1/internal/emails/send \
  -H "Content-Type: application/json" \
  -d '{"eventType": "APP_APPROVED", "recipientEmail": "test@example.com", "recipientName": "Test User", "payload": {"userName": "Test User", "appName": "Test App", "appLink": "https://app.com/123"}}'

# 6. App Rejected
curl -X POST http://localhost:8083/api/v1/internal/emails/send \
  -H "Content-Type: application/json" \
  -d '{"eventType": "APP_REJECTED", "recipientEmail": "test@example.com", "recipientName": "Test User", "payload": {"userName": "Test User", "appName": "Test App", "reason": "Missing required information."}}'
`

---

**Status:** ✅ Complete Documentation  
**Last Updated:** 2026-07-09
