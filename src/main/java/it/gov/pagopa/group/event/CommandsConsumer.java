package it.gov.pagopa.group.event;

import it.gov.pagopa.group.dto.event.QueueCommandOperationDTO;
import it.gov.pagopa.group.service.BeneficiaryGroupService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class CommandsConsumer {
    @Bean
    public Consumer<QueueCommandOperationDTO> consumerCommands(BeneficiaryGroupService beneficiaryGroupService) {
        return beneficiaryGroupService::processCommand;
    }
}
