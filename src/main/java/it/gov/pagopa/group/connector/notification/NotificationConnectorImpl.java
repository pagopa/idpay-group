package it.gov.pagopa.group.connector.notification;

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
    private final NotificationService notificationService;

    public NotificationConnectorImpl(@Value("${utils.task.execution.parallelPool}") int parallelPool,
                                     NotificationService notificationService){
        this.parallelPool = parallelPool;
        this.notificationService = notificationService;
    }

    @Override
    public void sendAllowedCitizen(List<String> beneficiaryTokenizedList, String initiativeId, String initiativeName, String serviceId) {
        if (!beneficiaryTokenizedList.isEmpty()) {
            log.info("[NOTIFY_ALLOWED_CITIZEN] - Sending citizen into Topics is about to begin...");
            Instant start = Instant.now();
            beneficiaryTokenizedList.stream().parallel().forEach(beneficiaryTokenized ->
                    notificationService.sendToNotificationManager(initiativeId, initiativeName, serviceId, beneficiaryTokenized));
            Instant end = Instant.now();
            log.debug("[NOTIFY_ALLOWED_CITIZEN] - Time to sent beneficiaries: {}", Duration.between(start, end).toString());
        }
        else{
            log.info("[NOTIFY_ALLOWED_CITIZEN] - No beneficiaries found");
        }
    }





}
