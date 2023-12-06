package it.gov.pagopa.group.connector.initiative;

import it.gov.pagopa.group.dto.InitiativeDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class InitiativeServiceImpl implements InitiativeService {

    @Value("${rest-client.initiative.base-url}")
    private String initiativeBaseUrl;

    private final RestTemplate restTemplate;

    public InitiativeServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public InitiativeDTO getInitiative(String initiativeId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<?> entity = new HttpEntity<>(headers);
        ResponseEntity<InitiativeDTO> initiativeResponse = restTemplate.exchange(initiativeBaseUrl+"/idpay/initiative/{initiativeId}/beneficiary/view",
                HttpMethod.GET,
                entity,
                InitiativeDTO.class,
                initiativeId
        );
        return initiativeResponse.getBody();
    }
}
