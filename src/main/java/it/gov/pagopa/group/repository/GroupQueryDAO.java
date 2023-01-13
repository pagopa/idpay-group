package it.gov.pagopa.group.repository;

import it.gov.pagopa.group.model.Group;
import java.util.List;

public interface GroupQueryDAO {
    Group findFirstByStatusAndUpdate(String status);

    void pushBeneficiaryList(String initiativeId, List<String> beneficiaryList);
}
