package it.gov.pagopa.group.connector.notification_manager;

public interface NotificationManagerService {

    void sendToNotificationManager(String initiativeId, String initiativeName, String serviceId, String beneficiaryTokenized);

}
