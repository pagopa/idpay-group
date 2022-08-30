package it.gov.pagopa.group.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PiiDTO {

    private String pii;
}
