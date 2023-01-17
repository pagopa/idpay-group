package it.gov.pagopa.group.repository;

import it.gov.pagopa.group.model.Group;
import it.gov.pagopa.group.model.GroupUserWhitelist;
import java.time.LocalDateTime;
import java.util.List;

public interface GroupQueryDAO {
    Group findFirstByStatusAndUpdate(String status);

    void pushBeneficiaryList(List<GroupUserWhitelist> beneficiaryList);
    void setStatusOk(String initiativeId, int beneficiariesReached);

    void setGroupForException(String initiativeId, String exceptionMessage, LocalDateTime elabDateTime, int retry);
}
