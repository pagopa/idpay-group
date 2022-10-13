package it.gov.pagopa.group.dto.event;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class NotificationQueueDTO {

  private String operationType;
  private String userId;
  private String initiativeId;
  private String serviceId;

}
