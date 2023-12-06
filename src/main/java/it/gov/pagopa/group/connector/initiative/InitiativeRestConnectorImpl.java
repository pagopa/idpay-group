package it.gov.pagopa.group.connector.initiative;

import it.gov.pagopa.group.constants.InitiativeConstants;
import it.gov.pagopa.group.dto.InitiativeDTO;
import it.gov.pagopa.group.exception.BeneficiaryGroupException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class InitiativeRestConnectorImpl implements InitiativeRestConnector {

    private final InitiativeService initiativeService;

    public InitiativeRestConnectorImpl(InitiativeService initiativeService) {
        this.initiativeService = initiativeService;
    }

    @Override
    public InitiativeDTO getInitiative(String initiativeId) {
        InitiativeDTO initiativeDTO = initiativeService.getInitiative(initiativeId);
        if(Arrays.asList(InitiativeConstants.Validation.ALLOWED_INITIATIVE_STATUS_ARRAY).contains(initiativeDTO.getStatus()))
            return initiativeDTO;
        else
            throw new BeneficiaryGroupException(
                    InitiativeConstants.Exception.UnprocessableEntity.CODE,
                    String.format(InitiativeConstants.Exception.UnprocessableEntity.INITIATIVE_STATUS_NOT_PROCESSABLE_FOR_GROUP, initiativeId),
                    HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
