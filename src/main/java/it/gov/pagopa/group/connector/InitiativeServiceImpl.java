package it.gov.pagopa.group.connector;

import it.gov.pagopa.group.dto.InitiativeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class InitiativeServiceImpl implements InitiativeService {

    @Value("${rest-client.initiative.base-url}")
    private String initiativeBaseUrl;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public InitiativeDTO getInitiative(String initiativeId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<?> entity = new HttpEntity<>(headers);
        HttpEntity<InitiativeDTO> initiativeResponse = restTemplate.exchange(initiativeBaseUrl+"/idpay/initiative/{initiativeId}/beneficiary/view",
                HttpMethod.GET,
                entity,
                InitiativeDTO.class,
                initiativeId
        );
        return initiativeResponse.getBody();
    }
}
