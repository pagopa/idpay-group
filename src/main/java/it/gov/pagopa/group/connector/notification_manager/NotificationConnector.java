package it.gov.pagopa.group.connector.notification_manager;

import java.util.List;

public interface NotificationConnector {

    void sendAllowedCitizen(List<String> beneficiaryTokenizedList, String initiativeId, String initiativeName, String serviceId);

}
