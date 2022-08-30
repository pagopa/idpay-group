package it.gov.pagopa.group.repository;

import it.gov.pagopa.group.model.Group;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface GroupRepository extends MongoRepository<Group, String> {

    @Query(value = "{status : ?0}", fields = "{groupId : 1, initiativeId : 1, organizationId : 1, fileName : 1, status : 1, creationDate : 1, updateDate : 1, creationUser : 1, updateUser : 1, beneficiaryList : 1}")
    List<Group> findAllGroups(String status);

    @Query(value = "{initiativeId : ?0}", fields = "{status : 1, _id : 0}")
    Group getStatus(String initiativeId);
}
