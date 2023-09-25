package it.gov.pagopa.group.repository;

import it.gov.pagopa.group.model.Group;
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
@ContextConfiguration(classes = GroupRepositoryExtendedImpl.class)
class GroupRepositoryExtendedImplTest {
    @MockBean
    MongoTemplate mongoTemplate;
    @Autowired
    GroupRepositoryExtended groupRepositoryExtended;

    @Test
    void deletePaged() {
        String initiativeId = "initiativeId";
        int pageSize = 2;

        Group group1 = Group.builder().initiativeId("initiativeId1").build();
        Group group2 = Group.builder().initiativeId("initiativeId2").build();
        Group group3 = Group.builder().initiativeId("initiativeId3").build();

        List<Group> groupList = new ArrayList<>();
        groupList.add(group1);
        groupList.add(group2);
        groupList.add(group3);

        Mockito.when(mongoTemplate.findAllAndRemove(Mockito.any(Query.class), Mockito.eq(Group.class)))
                .thenReturn(groupList);

        List<Group> deletedGroups = groupRepositoryExtended.deletePaged(initiativeId, pageSize);

        Mockito.verify(mongoTemplate, Mockito.times(1)).findAllAndRemove(Mockito.any(Query.class),Mockito.eq(Group.class));

        Assertions.assertEquals(groupList, deletedGroups);
    }


}