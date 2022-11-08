package it.gov.pagopa.group.dto.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class CitizenNotificationOnQueueDTO extends GenericNotificationQueueDTO {
    private String initiativeName;
}
