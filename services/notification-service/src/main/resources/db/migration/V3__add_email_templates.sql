-- V3__add_email_templates.sql
-- Add email template seeds for MVP email system
-- Date: 2026-07-08

INSERT INTO notification_templates (name, channel, subject, body, version, active)
VALUES
(
    'email-verification',
    'EMAIL',
    'Verify your email address',
    'Hi {{userName}},

Thank you for registering. Please click the link below to verify your email address:
{{verificationLink}}

If you did not create an account, no further action is required.

Best regards,
The Team',
    1,
    TRUE
),
(
    'reset-password',
    'EMAIL',
    'Password Reset Request',
    'Hi {{userName}},

We received a request to reset your password. Click the link below to set a new password:
{{resetLink}}

This link expires in 24 hours.

If you did not request a password reset, please ignore this email.

Best regards,
The Team',
    1,
    TRUE
),
(
    'purchase-confirmation',
    'EMAIL',
    'Order Confirmation - {{orderId}}',
    'Hi {{userName}},

Thank you for your purchase! Your order has been confirmed.

Order Details:
- Order ID: {{orderId}}
- Product: {{productName}}
- Amount: {{totalAmount}}
- Date: {{orderDate}}

We will send you a separate email with your license key shortly.

Best regards,
The Team',
    1,
    TRUE
);
-- Continue V3 with remaining templates
-- License delivery, App approved, App rejected

INSERT INTO notification_templates (name, channel, subject, body, version, active)
VALUES
(
    'license-delivery',
    'EMAIL',
    'Your License Key for {{productName}}',
    'Hi {{userName}},

Here is your license key for {{productName}}:

License Key: {{licenseKey}}

Please keep this key safe and do not share it with others.

Activation Instructions:
1. Open the application
2. Go to Settings → License
3. Enter the license key above

If you have any questions, contact support@example.com

Best regards,
The Team',
    1,
    TRUE
),
(
    'app-approved',
    'EMAIL',
    'Great news! Your app {{appName}} has been approved',
    'Hi {{userName}},

Congratulations! Your application "{{appName}}" has been approved by our administrators and is now live.

You can view it here: {{appLink}}

Next steps:
- Monitor your app analytics in the dashboard
- Update app details anytime in App Settings
- Contact support if you have any questions

Best regards,
The Review Team',
    1,
    TRUE
),
(
    'app-rejected',
    'EMAIL',
    'Update on your app {{appName}}',
    'Hi {{userName}},

Thank you for submitting "{{appName}}". Unfortunately, we cannot approve it at this time.

Reason for rejection:
{{reason}}

What to do next:
1. Review the rejection reason above
2. Make the necessary changes to your application
3. Resubmit for review

If you have questions about this decision, contact support@example.com

Best regards,
The Review Team',
    1,
    TRUE
);
