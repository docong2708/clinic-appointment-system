package com.group01.commonevents.messaging;

public final class RabbitMQConstants {

    public static final String APPOINTMENT_EXCHANGE = "clinic.appointment.exchange";

    public static final String NOTIFICATION_APPOINTMENT_QUEUE = "notification.appointment.queue";

    public static final String APPOINTMENT_CREATED_ROUTING_KEY = "appointment.created";

    public static final String APPOINTMENT_CONFIRMED_ROUTING_KEY = "appointment.confirmed";

    public static final String APPOINTMENT_CANCELED_ROUTING_KEY = "appointment.canceled";

    public static final String APPOINTMENT_UPDATED_ROUTING_KEY = "appointment.updated";

    public static final String APPOINTMENT_ROUTING_PATTERN = "appointment.*";

    private RabbitMQConstants() {
    }
}