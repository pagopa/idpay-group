package it.gov.pagopa.group.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.validation.annotation.Validated;

/**
 * InitiativeDTO
 */
@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InitiativeDTO   {

  @JsonProperty("initiativeId")
  private String initiativeId;

  @JsonProperty("initiativeName")
  private String initiativeName;

  @JsonProperty("organizationId")
  private String organizationId;

  @JsonProperty("status")
  private String status;

  @JsonProperty("general")
  private InitiativeGeneralDTO general;

}
