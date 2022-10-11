package it.gov.pagopa.group.dto.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class NotificationCitizenOnQueueDTO extends NotificationQueueDTO{
    private String initiativeName;
}
