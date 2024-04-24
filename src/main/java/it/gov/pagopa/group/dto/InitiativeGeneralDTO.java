package it.gov.pagopa.group.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
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

  @JsonAlias("budget")
  private Long budgetCents;

  @JsonAlias("beneficiaryBudget")
  private Long beneficiaryBudgetCents;

}
