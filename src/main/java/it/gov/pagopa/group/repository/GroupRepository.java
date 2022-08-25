package it.gov.pagopa.group.repository;

import it.gov.pagopa.group.model.Group;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface GroupRepository extends MongoRepository<Group, String> {

    @Query(value="{organizationId : ?0}", fields="{initiativeId : 1, initiativeName : 1, status : 1, 'additionalInfo.serviceName' : 1, creationDate : 1, updateDate : 1}")
    List<Group> retrieveInitiativeSummary(String organizationId);

    Optional<Group> findByOrganizationIdAndInitiativeId(String organizationId, String initiativeId);

    @Query(value="{initiativeId : ?0}", fields="{initiativeId : 1, initiativeName : 1, status : 1, 'additionalInfo.serviceName' : 1, 'general' : 1, 'beneficiaryRule' : 1}")
    Optional<Group> retrieveInitiativeBeneficiaryView(String initiativeId);
}
