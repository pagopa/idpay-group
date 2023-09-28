package it.gov.pagopa.group.repository;

import it.gov.pagopa.group.model.GroupUserWhitelist;

import java.util.List;

public interface GroupUserWhitelistRepositoryExtended {
    List<GroupUserWhitelist> deletePaged(String initiativeId, int pageSize);
}
