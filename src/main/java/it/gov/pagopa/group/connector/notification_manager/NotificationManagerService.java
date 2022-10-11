package it.gov.pagopa.group.connector.notification_manager;

import java.util.List;

public interface NotificationManagerService {

    void sendToNotificationManager(String initiativeId, String initiativeName, String serviceId, String beneficiaryTokenized);

}
