package it.gov.pagopa.group.connector.notification_manager;

import it.gov.pagopa.group.dto.event.NotificationCitizenOnQueueDTO;
import it.gov.pagopa.group.dto.event.NotificationQueueDTO;
import it.gov.pagopa.group.utils.ParallelStreamUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.Callable;

import static it.gov.pagopa.group.constants.GroupConstants.Producer.NotifyCitizen.OPERATION_TYPE;

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
        log.debug("[NOTIFY_TO_NOTIFICATION_MANAGER] - Get list of beneficiaries from Group");
        if (!beneficiaryTokenizedList.isEmpty()) {
//            Callable<Object> runnable = () -> {
//                beneficiaryTokenizedList.stream().parallel().forEach(beneficiaryTokenized ->
//                        notificationManagerService.sendToNotificationManager(initiativeId, initiativeName, serviceId, beneficiaryTokenized));
//                return null;
//            };
//            ParallelStreamUtils.goForParallelExecution(runnable, parallelPool);
            beneficiaryTokenizedList.forEach(beneficiaryTokenized ->
                    notificationManagerService.sendToNotificationManager(initiativeId, initiativeName, serviceId, beneficiaryTokenized));
        }
        else{
            log.debug("[NOTIFY_TO_NOTIFICATION_MANAGER] - No beneficiaries found");
        }
    }





}
