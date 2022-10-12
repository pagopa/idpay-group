package it.gov.pagopa.group.connector.notification_manager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
public class NotificationConnectorImpl implements NotificationConnector {

    private final int parallelPool;
    private final NotificationManagerService notificationManagerService;

    public NotificationConnectorImpl(@Value("${utils.task.execution.parallelPool}") int parallelPool,
                                     NotificationManagerService notificationManagerService){
        this.parallelPool = parallelPool;
        this.notificationManagerService = notificationManagerService;
    }

    @Override
    public void sendAllowedCitizen(List<String> beneficiaryTokenizedList, String initiativeId, String initiativeName, String serviceId) {
        if (!beneficiaryTokenizedList.isEmpty()) {
            log.info("[NOTIFY_TO_NOTIFICATION_MANAGER] - Sending citizen to Notification Manager is about to begin...");
            Instant start = Instant.now();
            beneficiaryTokenizedList.stream().parallel().forEach(beneficiaryTokenized ->
                    notificationManagerService.sendToNotificationManager(initiativeId, initiativeName, serviceId, beneficiaryTokenized));
            Instant end = Instant.now();
            log.debug("[NOTIFY_TO_NOTIFICATION_MANAGER] - Time to sent beneficiaries: {}", Duration.between(start, end).toString());
        }
        else{
            log.info("[NOTIFY_TO_NOTIFICATION_MANAGER] - No beneficiaries found");
        }
    }





}
