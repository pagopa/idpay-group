package it.gov.pagopa.group.connector.initiative;

import it.gov.pagopa.group.constants.InitiativeConstants;
import it.gov.pagopa.group.dto.InitiativeDTO;
import it.gov.pagopa.group.exception.BeneficiaryGroupException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class InitiativeRestConnectorImpl implements InitiativeRestConnector {

    @Autowired
    private InitiativeService initiativeService;

    @Override
    public InitiativeDTO getInitiative(String initiativeId) {
        InitiativeDTO initiativeDTO = initiativeService.getInitiative(initiativeId);
        if(Arrays.asList(InitiativeConstants.Validation.allowedInitiativeStatusArray).contains(initiativeDTO.getStatus()))
            return initiativeDTO;
        else
            throw new BeneficiaryGroupException(
                    InitiativeConstants.Exception.UnprocessableEntity.CODE,
                    String.format(InitiativeConstants.Exception.UnprocessableEntity.INITIATIVE_STATUS_NOT_PROCESSABLE_FOR_GROUP, initiativeId),
                    HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
