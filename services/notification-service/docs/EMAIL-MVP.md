# 📧 EMAIL SYSTEM MVP - Comprehensive Design & Implementation Guide

**Version:** 1.0  
**Date:** 2026-07-08  
**Status:** Ready for Implementation  

---

## 📑 Table of Contents

1. [Overview](#overview)
2. [Email Use Cases](#email-use-cases)
3. [System Architecture](#system-architecture)
4. [REST API Specification](#rest-api-specification)
5. [Database Schema](#database-schema)
6. [Email Templates](#email-templates)
7. [Implementation Phases](#implementation-phases)

---

## Overview

The email system MVP provides a lightweight, event-driven email notification service integrated with the existing Notification Service. It supports 6 core email types:

- **Email Verification** - Account registration verification
- **Reset Password** - Password recovery
- **Purchase Confirmation** - Order confirmation
- **License Delivery** - License key distribution
- **App Approved** - Application approval notification
- **App Rejected** - Application rejection with reason

### Key Design Principles

- **Simplicity First:** Plain text + basic HTML templates, no multi-language or versioning
- **Event-Driven:** Integrates with existing REST API and message broker patterns
- **Async & Reliable:** Queue-based delivery with retry logic already in place
- **DDD-Aligned:** Follows existing domain-driven design patterns
- **Expandable:** Easy to add new email types and templates

---

## Email Use Cases

| # | Email Type | Trigger | Recipient | Channel |
|---|------------|---------|-----------|---------|
| 1 | Email Verification | User registers account | User email | EMAIL |
| 2 | Reset Password | User requests password reset | User email | EMAIL |
| 3 | Purchase Confirmation | Payment processed successfully | Buyer email | EMAIL |
| 4 | License Delivery | License issued after purchase | Buyer email | EMAIL |
| 5 | App Approved | Admin approves submitted app | Developer email | EMAIL |
| 6 | App Rejected | Admin rejects submitted app | Developer email | EMAIL |

---

## System Architecture

### Data Flow

\\\
[Business Service]
    ↓ (REST API call / Event publish)
[Notification Service - EventController]
    ↓ (ProcessInboxEventUseCase)
[Domain - NotificationAggregate]
    ↓ (Create notification with email delivery)
[EmailSenderService]
    ↓ (Render template + Send email)
[Mail Provider] (SMTP / SendGrid / AWS SES)
    ↓
[Email Notification History] (Track status)
\\\

### Components

**1. Domain Layer (Existing)**
- \NotificationAggregate\ - Main state machine
- \NotificationTemplate\ - Email template entity (new fields for email)
- \NotificationDelivery\ - Multi-channel delivery tracking

**2. Application Layer (Enhancements)**
- \SendEmailUseCase\ - Orchestrate email sending

**3. Infrastructure Layer (New/Enhanced)**
- \EmailSenderService\ - SMTP/API-based sender
- \EmailTemplateRenderer\ - Template placeholder replacement
- \NotificationTemplateService\ - Load and cache templates

**4. API Layer (Existing endpoints)**
- \EventController\ - Receives events from other services
- \NotificationController\ - Query notification history

---

## REST API Specification

### Endpoint 1: Send Email (via Event)

**URL:** \POST /api/v1/internal/events/email\

**Purpose:** Business services call this to trigger email sending

**Request Body:**
\\\json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440000",
  "eventType": "EMAIL_VERIFICATION",
  "recipientEmail": "user@example.com",
  "recipientName": "Nguyễn Văn A",
  "recipientUserId": "550e8400-e29b-41d4-a716-446655440001",
  "payload": {
    "verificationLink": "https://app.com/verify?token=abc123",
    "userName": "Nguyễn Văn A"
  }
}
\\\

**Response (Success 200):**
\\\json
{
  "notificationId": "550e8400-e29b-41d4-a716-446655440002",
  "status": "SENT",
  "message": "Email queued for delivery"
}
\\\

**Response (Error 400):**
\\\json
{
  "error": "INVALID_EMAIL_TYPE",
  "message": "Email type not supported: INVALID_TYPE"
}
\\\

### Endpoint 2: Query Email History

**URL:** \GET /api/v1/notifications?type=EMAIL&recipientEmail=user@example.com\

**Response (200):**
\\\json
{
  "content": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440002",
      "type": "EMAIL_VERIFICATION",
      "recipient": "user@example.com",
      "status": "SENT",
      "createdAt": "2026-07-08T10:00:00Z",
      "sentAt": "2026-07-08T10:00:05Z"
    }
  ],
  "totalElements": 1
}
\\\

---

## Database Schema

### New/Enhanced Table: \
otification_templates\

The existing \
otification_templates\ table needs these email-specific fields:

\\\sql
ALTER TABLE notification_templates ADD COLUMN IF NOT EXISTS email_subject VARCHAR(255);
ALTER TABLE notification_templates ADD COLUMN IF NOT EXISTS email_body_text TEXT;
ALTER TABLE notification_templates ADD COLUMN IF NOT EXISTS email_body_html TEXT;
ALTER TABLE notification_templates ADD COLUMN IF NOT EXISTS placeholders JSONB;
ALTER TABLE notification_templates ADD COLUMN IF NOT EXISTS channel VARCHAR(50) DEFAULT 'EMAIL';
\\\

### Email Notification Records Tracking

Email records are stored in existing tables:
- \
otifications\ - Main notification record (type='EMAIL_*', channel='EMAIL')
- \
otification_deliveries\ - Tracks email delivery status (channel='EMAIL')
- \
otification_delivery_attempts\ - Retry attempts for failed emails

---

## Email Templates

### Template 1: EMAIL_VERIFICATION

**Template Key:** \email-verification\

**Subject:** \Verify your email address\

**Text Body:**
\\\
Hi {{userName}},

Thank you for registering. Please click the link below to verify your email address:
{{verificationLink}}

If you did not create an account, no further action is required.

Best regards,
The Team
\\\

**Placeholders:**
- \{{userName}}\ - User's display name
- \{{verificationLink}}\ - Verification URL with token

---

### Template 2: RESET_PASSWORD

**Template Key:** \eset-password\

**Subject:** \Password Reset Request\

**Text Body:**
\\\
Hi {{userName}},

We received a request to reset your password. Click the link below to set a new password:
{{resetLink}}

This link expires in 24 hours.

If you did not request a password reset, please ignore this email.

Best regards,
The Team
\\\

**Placeholders:**
- \{{userName}}\ - User's display name
- \{{resetLink}}\ - Password reset URL with token

---

### Template 3: PURCHASE_CONFIRMATION

**Template Key:** \purchase-confirmation\

**Subject:** \Order Confirmation - {{orderId}}\

**Text Body:**
\\\
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
\\\

**Placeholders:**
- \{{userName}}\ - Buyer's name
- \{{orderId}}\ - Order ID
- \{{productName}}\ - Product name
- \{{totalAmount}}\ - Total amount paid
- \{{orderDate}}\ - Order date (formatted)

---

### Template 4: LICENSE_DELIVERY

**Template Key:** \license-delivery\

**Subject:** \Your License Key for {{productName}}\

**Text Body:**
\\\
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
\\\

**Placeholders:**
- \{{userName}}\ - License owner's name
- \{{productName}}\ - Product name
- \{{licenseKey}}\ - License key/code

---

### Template 5: APP_APPROVED

**Template Key:** \pp-approved\

**Subject:** \Great news! Your app {{appName}} has been approved\

**Text Body:**
\\\
Hi {{userName}},

Congratulations! Your application "{{appName}}" has been approved by our administrators and is now live.

You can view it here: {{appLink}}

Next steps:
- Monitor your app analytics in the dashboard
- Update app details anytime in App Settings
- Contact support if you have any questions

Best regards,
The Review Team
\\\

**Placeholders:**
- \{{userName}}\ - Developer/owner name
- \{{appName}}\ - Application name
- \{{appLink}}\ - Public app URL

---

### Template 6: APP_REJECTED

**Template Key:** \pp-rejected\

**Subject:** \Update on your app {{appName}}\

**Text Body:**
\\\
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
\\\

**Placeholders:**
- \{{userName}}\ - Developer/owner name
- \{{appName}}\ - Application name
- \{{reason}}\ - Rejection reason (multi-line text)

---

## Implementation Phases

### Phase 1: Database & Domain (Current)

**Deliverables:**
- Add email-specific columns to \
otification_templates\ table
- Create migration file for template setup
- Seed 6 email templates into database

**Duration:** 1-2 hours

---

### Phase 2: Email Rendering & Sending (Current)

**Deliverables:**
- \EmailTemplateRenderer\ - Render templates with placeholders
- \EmailSenderService\ - Send via SMTP/Mail API
- Handle email validation and error cases

**Duration:** 2-3 hours

---

### Phase 3: API & Use Cases (Current)

**Deliverables:**
- \SendEmailUseCase\ - Coordinate email sending
- Update \EventController\ to handle email events
- Add email type validation

**Duration:** 1-2 hours

---

### Phase 4: Testing & Documentation (Current)

**Deliverables:**
- Integration tests for email sending
- Test all 6 email templates
- Documentation & runbook

**Duration:** 1-2 hours

---

## Implementation Checklist

- [ ] Create database migration for email templates
- [ ] Seed 6 email templates
- [ ] Implement \EmailTemplateRenderer\
- [ ] Implement \EmailSenderService\
- [ ] Create \SendEmailUseCase\
- [ ] Update \EventController\ for email events
- [ ] Add email validation & error handling
- [ ] Write integration tests
- [ ] Test all 6 email types
- [ ] Document API usage

---

## Configuration

### Email Provider Setup

Add to \pplication.yaml\ or config server:

\\\yaml
mail:
  smtp:
    host: smtp.gmail.com
    port: 587
    username: \
    password: \
    from: noreply@example.com
    from-name: "MSS Clinic"
    auth: true
    tls: true
  
  provider: SMTP  # or SENDGRID, AWS_SES
\\\

### Environment Variables

\\\
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
\\\

---

## Quick Start Guide

### 1. Run the service
\\\ash
cd services/notification-service
mvn spring-boot:run
\\\

### 2. Send email via API
\\\ash
curl -X POST http://localhost:8083/api/v1/internal/events/email \
  -H "Content-Type: application/json" \
  -d '{
    "eventType": "EMAIL_VERIFICATION",
    "recipientEmail": "user@example.com",
    "recipientName": "John Doe",
    "recipientUserId": "550e8400-e29b-41d4-a716-446655440001",
    "payload": {
      "verificationLink": "https://app.com/verify?token=abc123",
      "userName": "John Doe"
    }
  }'
\\\

### 3. Check email history
\\\ash
curl http://localhost:8083/api/v1/notifications?type=EMAIL
\\\

---

## Supported Email Event Types

| Event Type | Code | Template Key |
|------------|------|--------------|
| Email Verification | EMAIL_VERIFICATION | email-verification |
| Reset Password | RESET_PASSWORD | reset-password |
| Purchase Confirmation | PURCHASE_CONFIRMATION | purchase-confirmation |
| License Delivery | LICENSE_DELIVERY | license-delivery |
| App Approved | APP_APPROVED | app-approved |
| App Rejected | APP_REJECTED | app-rejected |

---

## Error Handling

### Scenarios & Recovery

| Scenario | Behavior | Recovery |
|----------|----------|----------|
| Template not found | Fail with 404 | Seed templates |
| SMTP connection failed | Queue retry | Automatic retry (configurable) |
| Invalid email format | Reject with 400 | Validate input |
| Missing placeholder | Use empty string | Log warning |

---

## Next Steps

1. Review this document with the team
2. Proceed to Phase 1 implementation
3. Test with real email account
4. Deploy to staging for QA
5. Create runbook for ops team

---

**Document Status:** ✅ Ready for Implementation
**Last Updated:** 2026-07-08
**Author:** Engineering Team
