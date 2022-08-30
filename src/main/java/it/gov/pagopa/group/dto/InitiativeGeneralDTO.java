package it.gov.pagopa.group.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;

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

  @JsonProperty("budget")
  private BigDecimal budget;

  @JsonProperty("beneficiaryBudget")
  private BigDecimal beneficiaryBudget;

}
