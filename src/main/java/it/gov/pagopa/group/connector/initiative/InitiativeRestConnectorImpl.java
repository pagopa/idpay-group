package it.gov.pagopa.group.connector.initiative;

import it.gov.pagopa.group.constants.InitiativeConstants;
import it.gov.pagopa.group.dto.InitiativeDTO;
import it.gov.pagopa.group.exception.InitiativeStatusNotValidException;
import org.springframework.stereotype.Service;

import java.util.Arrays;

import static it.gov.pagopa.group.constants.GroupConstants.ExceptionCode.GROUP_INITIATIVE_STATUS_NOT_VALID;
import static it.gov.pagopa.group.constants.GroupConstants.ExceptionMessage.INITIATIVE_UNPROCESSABLE_FOR_STATUS_NOT_VALID;

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
            throw new InitiativeStatusNotValidException(GROUP_INITIATIVE_STATUS_NOT_VALID,
                    String.format(INITIATIVE_UNPROCESSABLE_FOR_STATUS_NOT_VALID, initiativeId));
    }
}
