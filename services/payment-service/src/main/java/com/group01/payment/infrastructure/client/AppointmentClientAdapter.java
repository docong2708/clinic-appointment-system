package com.group01.payment.infrastructure.client;

import com.group01.payment.application.exception.AppointmentNotFoundException;
import com.group01.payment.application.exception.AppointmentServiceUnavailableException;
import com.group01.payment.application.port.AppointmentClientPort;
import feign.FeignException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AppointmentClientAdapter implements AppointmentClientPort {

    private final AppointmentServiceClient appointmentServiceClient;

    public AppointmentClientAdapter(AppointmentServiceClient appointmentServiceClient) {
        this.appointmentServiceClient = appointmentServiceClient;
    }

    @Override
    public AppointmentInfo getAppointment(UUID appointmentId) {
        try {
            return toAppointmentInfo(appointmentServiceClient.getAppointment(appointmentId));
        } catch (FeignException.NotFound exception) {
            throw new AppointmentNotFoundException(appointmentId);
        } catch (FeignException exception) {
            throw new AppointmentServiceUnavailableException(exception);
        }
    }

    @Override
    public void markPaymentAwaiting(UUID appointmentId) {
        updateAppointmentPayment(appointmentId, () -> appointmentServiceClient.markPaymentAwaiting(appointmentId));
    }

    @Override
    public void markPaymentPaid(UUID appointmentId) {
        updateAppointmentPayment(appointmentId, () -> appointmentServiceClient.markPaymentPaid(appointmentId));
    }

    @Override
    public void markPaymentFailed(UUID appointmentId) {
        updateAppointmentPayment(appointmentId, () -> appointmentServiceClient.markPaymentFailed(appointmentId));
    }

    @Override
    public void markPaymentDeferred(UUID appointmentId) {
        updateAppointmentPayment(appointmentId, () -> appointmentServiceClient.markPaymentDeferred(appointmentId));
    }

    private void updateAppointmentPayment(UUID appointmentId, AppointmentPaymentUpdate update) {
        try {
            update.execute();
        } catch (FeignException.NotFound exception) {
            throw new AppointmentNotFoundException(appointmentId);
        } catch (FeignException.BadRequest | FeignException.Conflict exception) {
            throw new IllegalStateException("Không thể cập nhật trạng thái thanh toán của lịch hẹn", exception);
        } catch (FeignException exception) {
            throw new AppointmentServiceUnavailableException(exception);
        }
    }

    private AppointmentInfo toAppointmentInfo(AppointmentServiceClient.AppointmentResponse response) {
        return new AppointmentInfo(
                response.id(),
                response.patientId(),
                response.doctorId(),
                response.slotId(),
                response.startTime(),
                response.endTime(),
                response.status(),
                response.paymentStatus(),
                response.createdBy()
        );
    }

    @FunctionalInterface
    private interface AppointmentPaymentUpdate {
        void execute();
    }
}
