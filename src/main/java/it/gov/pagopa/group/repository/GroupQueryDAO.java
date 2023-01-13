package it.gov.pagopa.group.repository;

import it.gov.pagopa.group.model.Group;
import java.time.LocalDateTime;
import java.util.List;

public interface GroupQueryDAO {
    Group findFirstByStatusAndUpdate(String status);

    void pushBeneficiaryList(String initiativeId, List<String> beneficiaryList);
    void setStatusOk(String initiativeId);

    void setGroupForException(String initiativeId, String exceptionMessage, LocalDateTime elabDateTime, int retry);
}
