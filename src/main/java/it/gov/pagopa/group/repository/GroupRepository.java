package it.gov.pagopa.group.repository;

import it.gov.pagopa.group.model.Group;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface GroupRepository extends MongoRepository<Group, String> {

    Optional<Group> findFirstByStatusAndRetryLessThan(String status, int retry);

    @Query(value = "{initiativeId : ?0, organizationId : ?1}", fields = "{status : 1, exceptionMessage : 1, initiativeId : 1, organizationId : 1, fileName : 1, creationDate : 1}")
    Optional<Group> getStatus(String initiativeId, String organizationId);

    @Query(value = "{initiativeId : ?0, organizationId : ?1}", fields = "{beneficiaryList : 1}")
    Optional<Group> getBeneficiaryList(String initiativeId, String organizationId);
}
