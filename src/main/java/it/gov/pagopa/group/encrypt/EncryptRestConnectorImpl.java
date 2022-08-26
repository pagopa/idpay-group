package it.gov.pagopa.group.encrypt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EncryptRestConnectorImpl implements EncryptRestConnector{
    private final String apikey;
    private final EncryptRest encryptRest;

    public EncryptRestConnectorImpl(@Value("${api.key.decrypt}")String apikey,
                                    EncryptRest encryptRest) {
        this.apikey = apikey;
        this.encryptRest = encryptRest;
    }

    @Override
    public String getPiiByToken(String token) {
        return encryptRest.getPiiByToken(token, apikey);
    }
}
