package it.gov.pagopa.group.connector.pdv;

import it.gov.pagopa.group.dto.FiscalCodeTokenizedDTO;
import it.gov.pagopa.group.dto.PiiDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PdvEncryptRestConnectorImpl implements PdvEncryptRestConnector {
    @Value("${rest-client.pdv-encrypt.api.key}")
    private String apikey;
    private final PdvEncryptFeignRestClient pdvEncryptFeignRestClient;

    public PdvEncryptRestConnectorImpl(PdvEncryptFeignRestClient pdvEncryptFeignRestClient) {
        this.pdvEncryptFeignRestClient = pdvEncryptFeignRestClient;
    }

    @Override
    public FiscalCodeTokenizedDTO putPii(PiiDTO piiDTO) {
        return pdvEncryptFeignRestClient.putPii(piiDTO, apikey);
    }
}
