package it.gov.pagopa.group.repository;

import it.gov.pagopa.group.model.Group;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GroupUserWhitelistRepository extends MongoRepository<Group, String> {

    List<String> findByInitiativeId(String initiativeId);
    Long deleteByGroupId(String groupId);
}
