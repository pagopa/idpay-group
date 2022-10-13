package it.gov.pagopa.group.dto.event;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class NotificationQueueDTO {

  private String operationType;
  private String userId;
  private String initiativeId;
  private String serviceId;

}
