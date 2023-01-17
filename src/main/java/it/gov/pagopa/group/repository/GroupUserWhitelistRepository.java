package it.gov.pagopa.group.repository;

import it.gov.pagopa.group.model.GroupUserWhitelist;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface GroupUserWhitelistRepository extends MongoRepository<GroupUserWhitelist, String> {

    List<String> findByInitiativeId(String initiativeId);
    @Query(value="{groupId : $0}", delete = true)
    Long deleteByGroupId(String groupId);
}
