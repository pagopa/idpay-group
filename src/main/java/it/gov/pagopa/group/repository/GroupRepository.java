package it.gov.pagopa.group.repository;

import it.gov.pagopa.group.model.Group;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface GroupRepository extends MongoRepository<Group, String> {

    Optional<Group> findFirstByStatus(String status);

    @Query(value = "{initiativeId : ?0}")
    Group getStatus(String initiativeId);
}
