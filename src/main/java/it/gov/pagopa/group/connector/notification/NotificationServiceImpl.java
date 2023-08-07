package it.gov.pagopa.group.connector.notification;

import it.gov.pagopa.group.dto.event.CitizenNotificationOnQueueDTO;
import it.gov.pagopa.group.event.NotificationManagerProducer;
import it.gov.pagopa.group.event.OnboardingNotificationProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

import static it.gov.pagopa.group.constants.GroupConstants.Producer.NotifyCitizen.OPERATION_TYPE;

@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationManagerProducer notificationManagerProducer;
    private final OnboardingNotificationProducer onboardingNotificationProducer;
    public AtomicLong count = new AtomicLong(0);

    public NotificationServiceImpl(
            NotificationManagerProducer notificationManagerProducer,
            OnboardingNotificationProducer onboardingNotificationProducer
            ) {
        this.notificationManagerProducer = notificationManagerProducer;
        this.onboardingNotificationProducer = onboardingNotificationProducer;
    }

    @Override
    public void sendNotification(String initiativeId, String initiativeName, String serviceId, String beneficiaryTokenized) {
        CitizenNotificationOnQueueDTO citizenNotificationOnQueueDTO = CitizenNotificationOnQueueDTO.builder()
                .userId(beneficiaryTokenized)
                .initiativeId(initiativeId)
                .operationType(OPERATION_TYPE)
                .initiativeName(initiativeName)
                .serviceId(serviceId)
                .build();
        if(!notificationManagerProducer.sendAllowedCitizen(citizenNotificationOnQueueDTO)){
            log.error("[NOTIFY_TO_NOTIFICATION_MANAGER] - Something gone wrong while notify Initiative [{}] to NotificationManager MS for userId: {}", citizenNotificationOnQueueDTO.getInitiativeId(), citizenNotificationOnQueueDTO.getUserId());
        }
        if(!onboardingNotificationProducer.sendAllowedCitizen(citizenNotificationOnQueueDTO)){
            log.error("[NOTIFY_TO_ONBOARDING] - Something gone wrong while notify Initiative [{}] to Onboarding MS for userId: {}", citizenNotificationOnQueueDTO.getInitiativeId(), citizenNotificationOnQueueDTO.getUserId());
        }
    }
}
