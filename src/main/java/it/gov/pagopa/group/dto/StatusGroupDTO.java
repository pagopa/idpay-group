package it.gov.pagopa.group.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StatusGroupDTO {

    private String status;

    private LocalDateTime fileUploadingDateTime;

    private String fileName;

    private String errorMessage;
}
