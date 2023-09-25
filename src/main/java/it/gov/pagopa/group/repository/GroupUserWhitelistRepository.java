package it.gov.pagopa.group.repository;

import it.gov.pagopa.group.model.GroupUserWhitelist;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface GroupUserWhitelistRepository extends MongoRepository<GroupUserWhitelist, String>, GroupUserWhitelistRepositoryExtended {

    @Query(value = "{initiativeId : ?0}", fields = "{userId : 1}")
    List<GroupUserWhitelist> findByInitiativeId(String initiativeId);

    List<GroupUserWhitelist> deleteByInitiativeId (String initiativeId);
}
