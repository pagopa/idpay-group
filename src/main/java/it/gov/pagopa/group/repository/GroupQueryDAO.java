package it.gov.pagopa.group.repository;

import it.gov.pagopa.group.model.Group;

public interface GroupQueryDAO {
    Group findFirstByStatusAndUpdate(String status);
}
