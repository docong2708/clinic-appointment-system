CREATE TABLE appointments (
                              id UUID PRIMARY KEY,

                              patient_id UUID NOT NULL,
                              doctor_id UUID NOT NULL,

                              start_time TIMESTAMP NOT NULL,
                              end_time TIMESTAMP NOT NULL,

                              reason VARCHAR(500),
                              cancel_reason VARCHAR(500),

                              status VARCHAR(50) NOT NULL,

                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);