package com.group01.doctor.config;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.exception.FlywayValidateException;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(FlywayRepairConfig.FlywayRepairProperties.class)
@Slf4j
public class FlywayRepairConfig {

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy(FlywayRepairProperties properties) {
        return flyway -> migrateWithOptionalRepair(flyway, properties);
    }

    private void migrateWithOptionalRepair(Flyway flyway, FlywayRepairProperties properties) {
        try {
            flyway.migrate();
        } catch (FlywayValidateException ex) {
            if (!properties.isRepairOnValidationError()) {
                throw ex;
            }

            log.warn("Flyway validation failed for doctor-service. Running repair and retrying migrate. message={}",
                    ex.getMessage());
            flyway.repair();
            flyway.migrate();
        }
    }

    @ConfigurationProperties(prefix = "app.flyway")
    public static class FlywayRepairProperties {
        private boolean repairOnValidationError = true;

        public boolean isRepairOnValidationError() {
            return repairOnValidationError;
        }

        public void setRepairOnValidationError(boolean repairOnValidationError) {
            this.repairOnValidationError = repairOnValidationError;
        }
    }
}
