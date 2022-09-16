package it.gov.pagopa.group.connector.initiative;

import it.gov.pagopa.group.dto.InitiativeDTO;
import it.gov.pagopa.group.dto.InitiativeGeneralDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;


@Slf4j
@ExtendWith(MockitoExtension.class) // Increased Test performance (evicting loading Spring Context)
class InitiativeServiceTest {

    private static final String INITIATIVE_ID = "Id1";
    private static final String ORGANIZATION_ID = "O1";
    private static final String ADDITIONAL_PATH_IDPAY_INITIATIVE_INITIATIVE_ID_BENEFICIARY_VIEW = "/idpay/initiative/{initiativeId}/beneficiary/view";
    private static final String INITIATIVE_NAME = "initiativeName";

//    @Autowired
    @InjectMocks
    private InitiativeServiceImpl initiativeServiceImpl = new InitiativeServiceImpl();

    @BeforeEach
    public void setUp() {
        //Mandatory because we are using only MockitoExtension (not needed Spring)
        ReflectionTestUtils.setField(initiativeServiceImpl, "initiativeBaseUrl", "http://localhost:8080");
    }

    @Mock
    private RestTemplate restTemplate;

    @Test
    void givenMockingIsDoneByMockito_whenGetIsCalled_shouldReturnMockedObject() {
        InitiativeDTO initiativeDTOexpected = createInitiativeDTO();

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<?> entity = new HttpEntity<>(null, headers);

        ResponseEntity<InitiativeDTO> entityExpected = new ResponseEntity<>(initiativeDTOexpected, HttpStatus.OK);
        Mockito.when(restTemplate.exchange(
                "http://localhost:8080" + ADDITIONAL_PATH_IDPAY_INITIATIVE_INITIATIVE_ID_BENEFICIARY_VIEW, //URL (ArgumentMatchers.anyString())
                HttpMethod.GET, // ArgumentMatchers.any(HttpMethod.class),
                entity, // ArgumentMatchers.any(HttpEntity.class),
                InitiativeDTO.class, // ArgumentMatchers.<Class<InitiativeDTO>>any())
                INITIATIVE_ID) //anyString()
        ).thenReturn(entityExpected);

        InitiativeDTO initiativeActual = initiativeServiceImpl.getInitiative(INITIATIVE_ID);
        Assertions.assertEquals(entityExpected.getBody(), initiativeActual);

        Mockito.verify(restTemplate,times(1)).exchange(
                "http://localhost:8080" + ADDITIONAL_PATH_IDPAY_INITIATIVE_INITIATIVE_ID_BENEFICIARY_VIEW, //URL (ArgumentMatchers.anyString())
                HttpMethod.GET, // ArgumentMatchers.any(HttpMethod.class),
                entity, // ArgumentMatchers.any(HttpEntity.class),
                InitiativeDTO.class, // ArgumentMatchers.<Class<InitiativeDTO>>any())
                INITIATIVE_ID) //anyString()
        ;

    }

    @Test
    void givenMockingIsDoneByMockito_whenGetIsCalled_shouldReturnMockedObject_____() {
        InitiativeDTO initiativeDTOexpected = createInitiativeDTO();

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<?> entity = new HttpEntity<>(null, headers);

        //doThrow InitiativeException for Void method
        doThrow(new ResourceAccessException("Exception Message"))
                .when(restTemplate).exchange(
                "http://localhost:8080" + ADDITIONAL_PATH_IDPAY_INITIATIVE_INITIATIVE_ID_BENEFICIARY_VIEW, //URL (ArgumentMatchers.anyString())
                HttpMethod.GET, // ArgumentMatchers.any(HttpMethod.class),
                entity, // ArgumentMatchers.any(HttpEntity.class),
                InitiativeDTO.class, // ArgumentMatchers.<Class<InitiativeDTO>>any())
                INITIATIVE_ID); //anyString()

        //prepare Executable with invocation of the method on your system under test
        Executable executable = () -> restTemplate.exchange(
                "http://localhost:8080" + ADDITIONAL_PATH_IDPAY_INITIATIVE_INITIATIVE_ID_BENEFICIARY_VIEW, //URL (ArgumentMatchers.anyString())
                HttpMethod.GET, // ArgumentMatchers.any(HttpMethod.class),
                entity, // ArgumentMatchers.any(HttpEntity.class),
                InitiativeDTO.class, // ArgumentMatchers.<Class<InitiativeDTO>>any())
                INITIATIVE_ID); //anyString()
        Exception exception = Assertions.assertThrows(ResourceAccessException.class, executable);
        assertEquals("Exception Message", exception.getMessage());

        Mockito.verify(restTemplate,times(1)).exchange(
                "http://localhost:8080" + ADDITIONAL_PATH_IDPAY_INITIATIVE_INITIATIVE_ID_BENEFICIARY_VIEW, //URL (ArgumentMatchers.anyString())
                HttpMethod.GET, // ArgumentMatchers.any(HttpMethod.class),
                entity, // ArgumentMatchers.any(HttpEntity.class),
                InitiativeDTO.class, // ArgumentMatchers.<Class<InitiativeDTO>>any())
                INITIATIVE_ID) //anyString()
        ;

    }

    private InitiativeDTO createInitiativeDTO(){
        InitiativeDTO initiativeDTO = new InitiativeDTO();
        InitiativeGeneralDTO initiativeGeneralDTO = new InitiativeGeneralDTO(BigDecimal.valueOf(90000), BigDecimal.valueOf(900));
        initiativeDTO.setInitiativeId(INITIATIVE_ID);
        initiativeDTO.setOrganizationId(ORGANIZATION_ID);
        initiativeDTO.setInitiativeName(INITIATIVE_NAME);
        initiativeDTO.setGeneral(initiativeGeneralDTO);
        return initiativeDTO;
    }

}
