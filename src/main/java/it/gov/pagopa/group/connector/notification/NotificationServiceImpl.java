package it.gov.pagopa.group.connector.notification;

import it.gov.pagopa.group.dto.event.CitizenNotificationOnQueueDTO;
import it.gov.pagopa.group.event.NotificationManagerProducer;
import it.gov.pagopa.group.event.OnboardingNotificationProducer;
import org.springframework.stereotype.Service;

import static it.gov.pagopa.group.constants.GroupConstants.Producer.NotifyCitizen.OPERATION_TYPE;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationManagerProducer notificationManagerProducer;
    private final OnboardingNotificationProducer onboardingNotificationProducer;

    public NotificationServiceImpl(
            NotificationManagerProducer notificationManagerProducer,
            OnboardingNotificationProducer onboardingNotificationProducer
            ) {
        this.notificationManagerProducer = notificationManagerProducer;
        this.onboardingNotificationProducer = onboardingNotificationProducer;
    }

    @Override
    public void sendToNotificationManager(String initiativeId, String initiativeName, String serviceId, String beneficiaryTokenized) {
        CitizenNotificationOnQueueDTO citizenNotificationOnQueueDTO = CitizenNotificationOnQueueDTO.builder()
                .userId(beneficiaryTokenized)
                .initiativeId(initiativeId)
                .operationType(OPERATION_TYPE)
                .initiativeName(initiativeName)
                .serviceId(serviceId)
                .build();
        if(!notificationManagerProducer.sendAllowedCitizen(citizenNotificationOnQueueDTO)){
            throw new IllegalStateException("[NOTIFY_TO_NOTIFICATION_MANAGER] - Something gone wrong while notify Initiative to NotificationManager MS");
        }
        if(!onboardingNotificationProducer.sendAllowedCitizen(citizenNotificationOnQueueDTO)){
            throw new IllegalStateException("[NOTIFY_TO_ONBOARDING] - Something gone wrong while notify Initiative to Onboarding MS");
        }
    }
}
