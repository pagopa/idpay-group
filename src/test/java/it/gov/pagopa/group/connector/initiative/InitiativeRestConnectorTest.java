package it.gov.pagopa.group.connector.initiative;

import it.gov.pagopa.group.constants.InitiativeConstants;
import it.gov.pagopa.group.dto.InitiativeDTO;
import it.gov.pagopa.group.dto.InitiativeGeneralDTO;
import it.gov.pagopa.group.exception.BeneficiaryGroupException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;

@RestClientTest(value = InitiativeRestConnector.class)
class InitiativeRestConnectorTest {

    private static final String INITIATIVE_ID = "Id1";
    private static final String ORGANIZATION_ID = "O1";
    private static final String INITIATIVE_NAME = "initiativeName";

    @Autowired
    InitiativeRestConnector initiativeRestConnector;

    @MockBean
    InitiativeService initiativeService;

    @Test
    void whenRequestingAnInitiativeInStatusAllowed_thenReturnTheExpectingInitiativeAllowed(){
        InitiativeDTO initiativeDTOexpected = createInitiativeDTO(InitiativeConstants.Status.DRAFT);
        Mockito.when(initiativeService.getInitiative(anyString())).thenReturn(initiativeDTOexpected);

        InitiativeDTO initiativeDTOActual = initiativeRestConnector.getInitiative(INITIATIVE_ID);

        assertEquals(initiativeDTOexpected, initiativeDTOActual);
    }

    @Test
    void whenRequestingAnInitiativeInStatusNOTAllowed_thenReturnTheExpectingInitiativeAllowed(){
        InitiativeDTO initiativeDTOexpected = createInitiativeDTO(InitiativeConstants.Status.PUBLISHED);
        Mockito.when(initiativeService.getInitiative(anyString())).thenReturn(initiativeDTOexpected);

        BeneficiaryGroupException exception = assertThrows(BeneficiaryGroupException.class, () -> initiativeRestConnector.getInitiative(INITIATIVE_ID));

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, exception.getHttpStatus());
        assertEquals(InitiativeConstants.Exception.UnprocessableEntity.CODE, exception.getCode());
        assertEquals(String.format(InitiativeConstants.Exception.UnprocessableEntity.INITIATIVE_STATUS_NOT_PROCESSABLE_FOR_GROUP, INITIATIVE_ID), exception.getMessage());

    }

    private InitiativeDTO createInitiativeDTO(String status){
        InitiativeDTO initiativeDTO = new InitiativeDTO();
        InitiativeGeneralDTO initiativeGeneralDTO = new InitiativeGeneralDTO(BigDecimal.valueOf(90000), BigDecimal.valueOf(900));
        initiativeDTO.setInitiativeId(INITIATIVE_ID);
        initiativeDTO.setOrganizationId(ORGANIZATION_ID);
        initiativeDTO.setInitiativeName(INITIATIVE_NAME);
        initiativeDTO.setStatus(status);
        initiativeDTO.setGeneral(initiativeGeneralDTO);
        return initiativeDTO;
    }

}
