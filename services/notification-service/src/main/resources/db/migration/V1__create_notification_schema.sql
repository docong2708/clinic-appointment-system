CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE notification_inbox_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    source_service VARCHAR(100) NOT NULL,
    source_event_id UUID NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id UUID NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(255) NOT NULL DEFAULT 'RECEIVED',
    correlation_id VARCHAR(100),
    received_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMPTZ,
    error_message TEXT,
    CONSTRAINT uq_notification_inbox_events_source_event
        UNIQUE (source_service, source_event_id),
    CONSTRAINT chk_notification_inbox_events_status
        CHECK (status IN ('RECEIVED', 'PROCESSING', 'PROCESSED', 'FAILED'))
);

CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    batch_id UUID,
    source_inbox_event_id UUID
        REFERENCES notification_inbox_events(id)
        ON DELETE SET NULL,
    source_service VARCHAR(100) NOT NULL,
    source_event_id UUID NOT NULL,
    dedupe_key VARCHAR(255) NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id UUID NOT NULL,
    recipient_user_id UUID NOT NULL,
    recipient_role VARCHAR(50),
    type VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'CREATED',
    priority SMALLINT NOT NULL DEFAULT 5,
    title VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    locale VARCHAR(20) NOT NULL DEFAULT 'vi-VN',
    template_key VARCHAR(100),
    template_version INTEGER,
    action_url VARCHAR(500),
    metadata JSONB,
    correlation_id VARCHAR(100),
    read_at TIMESTAMPTZ,
    archived_at TIMESTAMPTZ,
    expires_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_notifications_source_recipient_dedupe
        UNIQUE (source_service, source_event_id, recipient_user_id, dedupe_key),
    CONSTRAINT chk_notifications_priority_range
        CHECK (priority BETWEEN 1 AND 9),
    CONSTRAINT chk_notifications_status
        CHECK (status IN ('CREATED', 'READY', 'PARTIALLY_SENT', 'SENT', 'FAILED', 'CANCELED'))
);

CREATE TABLE notification_deliveries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    notification_id UUID NOT NULL
        REFERENCES notifications(id)
        ON DELETE CASCADE,
    channel VARCHAR(255) NOT NULL,
    destination_type VARCHAR(30),
    destination VARCHAR(512) NOT NULL,
    status VARCHAR(255) NOT NULL DEFAULT 'PENDING',
    retry_count INTEGER NOT NULL DEFAULT 0,
    scheduled_at TIMESTAMPTZ,
    next_retry_at TIMESTAMPTZ,
    provider_name VARCHAR(100),
    provider_message_id VARCHAR(255),
    provider_response JSONB,
    last_error TEXT,
    correlation_id VARCHAR(100),
    sent_at TIMESTAMPTZ,
    failed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_notification_deliveries_retry_count
        CHECK (retry_count >= 0),
    CONSTRAINT chk_notification_deliveries_channel
        CHECK (channel IN ('IN_APP', 'EMAIL', 'SMS', 'PUSH')),
    CONSTRAINT chk_notification_deliveries_status
        CHECK (status IN ('PENDING', 'SENDING', 'SENT', 'FAILED', 'CANCELED'))
);

CREATE TABLE notification_delivery_attempts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    delivery_id UUID NOT NULL
        REFERENCES notification_deliveries(id)
        ON DELETE CASCADE,
    attempt_number INTEGER NOT NULL,
    status VARCHAR(255) NOT NULL,
    provider_name VARCHAR(100),
    provider_message_id VARCHAR(255),
    error_code VARCHAR(100),
    error_message TEXT,
    attempted_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_notification_delivery_attempts_delivery_attempt
        UNIQUE (delivery_id, attempt_number),
    CONSTRAINT chk_notification_delivery_attempts_attempt_number
        CHECK (attempt_number > 0),
    CONSTRAINT chk_notification_delivery_attempts_status
        CHECK (status IN ('PENDING', 'SENDING', 'SENT', 'FAILED', 'CANCELED'))
);

CREATE TABLE notification_preferences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    notification_type VARCHAR(100) NOT NULL,
    channel VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_notification_preferences_user_type_channel
        UNIQUE (user_id, notification_type, channel),
    CONSTRAINT chk_notification_preferences_channel
        CHECK (channel IN ('IN_APP', 'EMAIL', 'SMS', 'PUSH'))
);

CREATE INDEX idx_notifications_recipient_created
    ON notifications (recipient_user_id, created_at DESC);

CREATE INDEX idx_notifications_recipient_unread
    ON notifications (recipient_user_id, read_at)
    WHERE read_at IS NULL AND archived_at IS NULL;

CREATE INDEX idx_notifications_aggregate
    ON notifications (aggregate_type, aggregate_id);

CREATE INDEX idx_notifications_source_inbox_event
    ON notifications (source_inbox_event_id);

CREATE INDEX idx_notifications_expires_at
    ON notifications (expires_at)
    WHERE expires_at IS NOT NULL;

CREATE INDEX idx_deliveries_due
    ON notification_deliveries (status, scheduled_at, next_retry_at);

CREATE INDEX idx_deliveries_notification
    ON notification_deliveries (notification_id);

CREATE INDEX idx_inbox_processed_at
    ON notification_inbox_events (processed_at)
    WHERE processed_at IS NOT NULL;

CREATE INDEX idx_attempts_attempted_at
    ON notification_delivery_attempts (attempted_at);

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_notifications_set_updated_at
    BEFORE UPDATE ON notifications
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_notification_deliveries_set_updated_at
    BEFORE UPDATE ON notification_deliveries
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_notification_preferences_set_updated_at
    BEFORE UPDATE ON notification_preferences
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();
