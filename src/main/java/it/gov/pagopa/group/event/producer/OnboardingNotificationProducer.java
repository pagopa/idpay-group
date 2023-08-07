package it.gov.pagopa.group.event.producer;

import it.gov.pagopa.group.dto.event.GenericNotificationQueueDTO;

public interface OnboardingNotificationProducer {

    public <T extends GenericNotificationQueueDTO> boolean sendAllowedCitizen(T notificationQueueDTO);

}
