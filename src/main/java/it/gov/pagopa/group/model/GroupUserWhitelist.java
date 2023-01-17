package it.gov.pagopa.group.model;

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
@Document("group_user_whitelist")
@FieldNameConstants
public class GroupUserWhitelist {
    @Id
    private String id;

    private String groupId;

    private String initiativeId;

    private String userId;
}
