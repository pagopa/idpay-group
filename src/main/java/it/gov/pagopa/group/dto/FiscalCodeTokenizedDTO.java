package it.gov.pagopa.group.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FiscalCodeTokenizedDTO {
    private String token;
}
