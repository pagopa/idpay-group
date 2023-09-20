package it.gov.pagopa.group.repository;

import it.gov.pagopa.group.model.GroupUserWhitelist;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@ContextConfiguration(classes = GroupUserWhitelistRepositoryExtendedImpl.class)
class GroupUserWhitelistRepositoryExtendedImplTest {
    @MockBean
    MongoTemplate mongoTemplate;
    @Autowired
    GroupUserWhitelistRepositoryExtended groupUserWhitelistRepositoryExtended;

    @Test
    void deletePaged() {
        String initiativeId = "initiativeId";
        int pageSize = 2;

        GroupUserWhitelist group1 = GroupUserWhitelist.builder().initiativeId("initiativeId1").build();
        GroupUserWhitelist group2 = GroupUserWhitelist.builder().initiativeId("initiativeId2").build();
        GroupUserWhitelist group3 = GroupUserWhitelist.builder().initiativeId("initiativeId3").build();

        List<GroupUserWhitelist> groupUserList = new ArrayList<>();
        groupUserList.add(group1);
        groupUserList.add(group2);
        groupUserList.add(group3);

        Mockito.when(mongoTemplate.findAllAndRemove(Mockito.any(Query.class), Mockito.eq(GroupUserWhitelist.class)))
                .thenReturn(groupUserList);

        List<GroupUserWhitelist> deletedUserGroups = groupUserWhitelistRepositoryExtended.deletePaged(initiativeId, pageSize);

        Mockito.verify(mongoTemplate, Mockito.times(1)).findAllAndRemove(Mockito.any(Query.class),Mockito.eq(GroupUserWhitelist.class));

        Assertions.assertEquals(groupUserList, deletedUserGroups);
    }

}