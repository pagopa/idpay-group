package it.gov.pagopa.group.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
@Document("group")
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

    private List<String> beneficiaryList;
}
