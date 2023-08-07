package it.gov.pagopa.group.event;

import it.gov.pagopa.group.dto.event.GenericNotificationQueueDTO;

public interface NotificationManagerProducer {

    public <T extends GenericNotificationQueueDTO> boolean sendAllowedCitizen(T notificationQueueDTO);

}
