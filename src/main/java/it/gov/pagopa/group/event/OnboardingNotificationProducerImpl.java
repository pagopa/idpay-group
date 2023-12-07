package it.gov.pagopa.group.event;

import it.gov.pagopa.group.dto.event.GenericNotificationQueueDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OnboardingNotificationProducerImpl implements OnboardingNotificationProducer {

    private static final String ALLOWED_CITIZEN_OUT_1 = "allowedCitizen-out-1";
    private final StreamBridge streamBridge;

    public OnboardingNotificationProducerImpl(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    @Override
    public <T extends GenericNotificationQueueDTO> boolean sendAllowedCitizen(T notificationQueueDTO) {
        log.debug("Sending anonymous allowed Citizen to {}", ALLOWED_CITIZEN_OUT_1);
        return streamBridge.send(ALLOWED_CITIZEN_OUT_1, notificationQueueDTO);
    }
}
