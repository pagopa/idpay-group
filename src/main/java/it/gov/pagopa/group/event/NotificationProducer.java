package it.gov.pagopa.group.event;

import it.gov.pagopa.group.dto.event.NotificationQueueDTO;

public interface NotificationProducer {

    public <T extends NotificationQueueDTO> boolean sendAllowedCitizen(T notificationQueueDTO);

}
