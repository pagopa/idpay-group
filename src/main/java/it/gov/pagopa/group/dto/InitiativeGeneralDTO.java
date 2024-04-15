package it.gov.pagopa.group.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * InitiativeGeneralDTO
 */
//@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InitiativeGeneralDTO   {

  @JsonProperty("budgetCents")
  private Long budgetCents;

  @JsonProperty("beneficiaryBudgetCents")
  private Long beneficiaryBudgetCents;

}
