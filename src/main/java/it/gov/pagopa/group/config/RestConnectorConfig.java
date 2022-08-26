package it.gov.pagopa.group.config;

import it.gov.pagopa.group.encrypt.EncryptRest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(clients = {EncryptRest.class})
public class RestConnectorConfig {

}
