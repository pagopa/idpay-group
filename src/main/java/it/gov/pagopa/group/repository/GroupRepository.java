package it.gov.pagopa.group.repository;

import it.gov.pagopa.group.model.Group;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;
import java.util.stream.Stream;

public interface GroupRepository extends MongoRepository<Group, String> {

    Optional<Group> findFirstByStatusAndRetryLessThan(String status, int retry);

    @Query(value = "{initiativeId : ?0, organizationId : ?1}", fields = "{status : 1, exceptionMessage : 1, initiativeId : 1, organizationId : 1, fileName : 1, creationDate : 1, beneficiaryList : 1}")
    Optional<Group> getStatus(String initiativeId, String organizationId);

    @Query(value = "{initiativeId : ?0}", fields = "{beneficiaryList : 1}")
    Optional<Group> findBeneficiaryList(String initiativeId);

    @Query(value = "{initiativeId : ?0}", fields = "{beneficiaryList : 1}")
    Stream<String> findGroupByInitiativeId(String initiativeId);
}
