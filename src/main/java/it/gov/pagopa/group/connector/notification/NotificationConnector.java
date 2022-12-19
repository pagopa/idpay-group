package it.gov.pagopa.group.connector.notification;

import java.util.List;

public interface NotificationConnector {

    void sendAllowedCitizen(List<String> beneficiaryTokenizedList, String initiativeId, String initiativeName, String serviceId);

}
