package it.gov.pagopa.group.connector.initiative;

import it.gov.pagopa.group.dto.InitiativeDTO;

public interface InitiativeRestConnector {

    InitiativeDTO getInitiative(String initiativeId);

}
