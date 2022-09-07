package it.gov.pagopa.group.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CitizenStatusDTO {

    private boolean status = false;
}
