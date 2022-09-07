package it.gov.pagopa.group.config;

import feign.Retryer;
import it.gov.pagopa.group.connector.pdv.PdvClientRetryer;
import it.gov.pagopa.group.connector.pdv.PdvEncryptFeignRestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(clients = {PdvEncryptFeignRestClient.class})
public class RestConnectorConfig {
    @Bean
    public Retryer retryer(@Value("${rest-client.pdv-encrypt.http.retry.period}") long period,
                           @Value("${rest-client.pdv-encrypt.http.retry.maxPeriod}") long maxPeriod,
                           @Value("${rest-client.pdv-encrypt.http.retry.maxAttempts}") int maxAttempts) {
        return new PdvClientRetryer(period, maxPeriod, maxAttempts);
    }
}
