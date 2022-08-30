package it.gov.pagopa.group.connector.pdv;

import it.gov.pagopa.group.dto.FiscalCodeTokenizedDTO;
import it.gov.pagopa.group.dto.PiiDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EncryptRestConnectorImpl implements EncryptRestConnector{
    @Value("${rest-client.pdv-encrypt.api.key}")
    private String apikey;
    private final EncryptRestClient encryptRestClient;

    public EncryptRestConnectorImpl(EncryptRestClient encryptRestClient) {
        this.encryptRestClient = encryptRestClient;
    }

    @Override
    public FiscalCodeTokenizedDTO putPii(PiiDTO piiDTO) {
        return encryptRestClient.putPii(piiDTO, apikey);
    }
}
