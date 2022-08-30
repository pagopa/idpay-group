package it.gov.pagopa.group.connector;

import it.gov.pagopa.group.dto.InitiativeDTO;

public interface InitiativeService {
    InitiativeDTO getInitiative(String initiativeId);
}
