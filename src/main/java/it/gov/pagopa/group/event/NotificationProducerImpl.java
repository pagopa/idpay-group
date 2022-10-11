package it.gov.pagopa.group.event;

import it.gov.pagopa.group.dto.event.NotificationQueueDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NotificationProducerImpl implements NotificationProducer {

    private static final String ALLOWED_CITIZEN_OUT_0 = "allowedCitizen-out-0";
    @Autowired
    StreamBridge streamBridge;

    @Override
    public <T extends NotificationQueueDTO> boolean sendAllowedCitizen(T notificationQueueDTO) {
        log.debug("Sending anonymous allowed Citizen to {}", ALLOWED_CITIZEN_OUT_0);
        return streamBridge.send(ALLOWED_CITIZEN_OUT_0, notificationQueueDTO);
    }
}
