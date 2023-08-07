package it.gov.pagopa.group.connector.notification;

import it.gov.pagopa.group.dto.event.GenericNotificationQueueDTO;
import it.gov.pagopa.group.event.producer.NotificationManagerProducer;
import it.gov.pagopa.group.event.producer.OnboardingNotificationProducer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.mockito.Mockito.*;

@WebMvcTest(value = {NotificationService.class})
class NotificationServiceTest {

    @Autowired
    NotificationService notificationService;

    @MockBean
    NotificationManagerProducer notificationManagerProducer;

    @MockBean
    OnboardingNotificationProducer onboardingNotificationProducer;

    @Test
    void sendNotificationTest() {
        when(notificationManagerProducer.sendAllowedCitizen(any(GenericNotificationQueueDTO.class))).thenReturn(true);
        when(onboardingNotificationProducer.sendAllowedCitizen(any(GenericNotificationQueueDTO.class))).thenReturn(true);

        notificationService.sendNotification("", "", "", "");

        verify(notificationManagerProducer, times(1)).sendAllowedCitizen(any(GenericNotificationQueueDTO.class));
        verify(onboardingNotificationProducer, times(1)).sendAllowedCitizen(any(GenericNotificationQueueDTO.class));
    }

    @Test
    void sendNotificationTest_fail() {
        when(notificationManagerProducer.sendAllowedCitizen(any(GenericNotificationQueueDTO.class))).thenReturn(false);
        when(onboardingNotificationProducer.sendAllowedCitizen(any(GenericNotificationQueueDTO.class))).thenReturn(false);

        notificationService.sendNotification("", "", "", "");

        verify(notificationManagerProducer, times(1)).sendAllowedCitizen(any(GenericNotificationQueueDTO.class));
        verify(onboardingNotificationProducer, times(1)).sendAllowedCitizen(any(GenericNotificationQueueDTO.class));
    }
}
