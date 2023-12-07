package it.gov.pagopa.group.event;

import it.gov.pagopa.group.dto.event.GenericNotificationQueueDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NotificationManagerProducerImpl implements NotificationManagerProducer {

    private static final String ALLOWED_CITIZEN_OUT_0 = "allowedCitizen-out-0";
    private final StreamBridge streamBridge;

    public NotificationManagerProducerImpl(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    @Override
    public <T extends GenericNotificationQueueDTO> boolean sendAllowedCitizen(T notificationQueueDTO) {
        log.debug("Sending anonymous allowed Citizen to {}", ALLOWED_CITIZEN_OUT_0);
        return streamBridge.send(ALLOWED_CITIZEN_OUT_0, notificationQueueDTO);
    }
}
