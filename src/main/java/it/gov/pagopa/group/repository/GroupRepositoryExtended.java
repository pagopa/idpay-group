package it.gov.pagopa.group.repository;

import it.gov.pagopa.group.model.Group;

import java.util.List;

public interface GroupRepositoryExtended {
    List<Group> deletePaged(String initiativeId, int pageSize);
}
