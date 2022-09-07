package it.gov.pagopa.group.connector.pdv;

import it.gov.pagopa.group.dto.FiscalCodeTokenizedDTO;
import it.gov.pagopa.group.dto.PiiDTO;

public interface PdvEncryptRestConnector {
    FiscalCodeTokenizedDTO putPii(PiiDTO piiDTO);
}
