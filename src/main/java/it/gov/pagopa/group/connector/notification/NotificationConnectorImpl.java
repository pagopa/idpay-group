package it.gov.pagopa.group.connector.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
public class NotificationConnectorImpl implements NotificationConnector {

    private final NotificationService notificationService;

    public NotificationConnectorImpl(NotificationService notificationService){
        this.notificationService = notificationService;
    }

    @Override
    @Async
    public void sendAllowedCitizen(List<String> beneficiaryTokenizedList, String initiativeId, String initiativeName, String serviceId) {
        if (!beneficiaryTokenizedList.isEmpty()) {
            log.info("[NOTIFY_ALLOWED_CITIZEN] - Sending No. of {} citizen into Topics is about to begin...", beneficiaryTokenizedList.size());
            Instant start = Instant.now();
            beneficiaryTokenizedList.stream().parallel().forEach(beneficiaryTokenized ->
                    notificationService.sendNotification(initiativeId, initiativeName, serviceId, beneficiaryTokenized));
            Instant end = Instant.now();
            log.debug("[NOTIFY_ALLOWED_CITIZEN] - Time to sent notification: {}", Duration.between(start, end).toString());
        }
        else{
            log.info("[NOTIFY_ALLOWED_CITIZEN] - No beneficiaries found");
        }
    }





}
