package it.gov.pagopa.group.connector.notification_manager;

import it.gov.pagopa.group.dto.event.NotificationCitizenOnQueueDTO;
import it.gov.pagopa.group.event.NotificationProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static it.gov.pagopa.group.constants.GroupConstants.Producer.NotifyCitizen.OPERATION_TYPE;

@Service
public class NotificationManagerServiceImpl implements NotificationManagerService {

    @Autowired
    NotificationProducer notificationProducer;

    @Override
    public void sendToNotificationManager(String initiativeId, String initiativeName, String serviceId, String beneficiaryTokenized) {
        NotificationCitizenOnQueueDTO notificationCitizenOnQueueDTO = NotificationCitizenOnQueueDTO.builder()
                .userId(beneficiaryTokenized)
                .initiativeId(initiativeId)
                .operationType(OPERATION_TYPE)
                .initiativeName(initiativeName)
                .serviceId(serviceId)
                .build();
        if(!notificationProducer.sendAllowedCitizen(notificationCitizenOnQueueDTO)){
            throw new IllegalStateException("[NOTIFY_TO_NOTIFICATION_MANAGER] - Something gone wrong while notify Initiative to NotificationManager");
        }
    }
}
