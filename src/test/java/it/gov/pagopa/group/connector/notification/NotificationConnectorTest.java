package it.gov.pagopa.group.connector.notification;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

@RestClientTest(value = NotificationConnector.class)
class NotificationConnectorTest {

    @Autowired
    NotificationConnector notificationConnector;

    @MockBean
    NotificationService notificationService;

    @Test
    void sendAllowedCitizenTest_listNull() {
        doNothing().when(notificationService).sendNotification(anyString(), anyString(), anyString(), anyString());

        notificationConnector.sendAllowedCitizen(Collections.emptyList(), "", "", "");

        verify(notificationService, times(0)).sendNotification(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void sendAllowedCitizenTest() {
        List<String> beneficiaryTokenizedList = populateList();
        doNothing().when(notificationService).sendNotification(anyString(), anyString(), anyString(), anyString());

        notificationConnector.sendAllowedCitizen(beneficiaryTokenizedList, "", "", "");

        verify(notificationService, timeout(1000).times(beneficiaryTokenizedList.size())).sendNotification(anyString(), anyString(), anyString(), anyString());
    }

    private List<String> populateList() {
        return List.of("test", "test", "test");
    }

}
