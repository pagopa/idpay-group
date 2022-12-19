package it.gov.pagopa.group.connector.notification;

public interface NotificationService {

    void sendToNotificationManager(String initiativeId, String initiativeName, String serviceId, String beneficiaryTokenized);

}
