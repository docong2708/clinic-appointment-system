package com.group01.appointment.infrastructure.messaging;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service-client", url = "${clients.notification-service.base-url}")
public interface NotificationServiceClient {

    @PostMapping("${clients.notification-service.appointment-events-path}")
    void publishAppointmentEvent(@RequestBody Object event);
}
