package it.gov.pagopa.group.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
@Document("group")
@FieldNameConstants
public class Group {
    @Id
    private String groupId;

    private String initiativeId;

    private String organizationId;

    private String fileName;

    private String status;

    private String exceptionMessage;

    private LocalDateTime elabDateTime;

    private int retry;

    private LocalDateTime creationDate;

    private LocalDateTime updateDate;

    private String creationUser;

    private String updateUser;

    private Integer beneficiariesReached;
}
