package it.gov.pagopa.group.encrypt;

public interface EncryptRestConnector {
    String getPiiByToken(String token);
}
