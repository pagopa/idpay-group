package it.gov.pagopa.group.exception;

import it.gov.pagopa.group.constants.GroupConstants;
import it.gov.pagopa.group.dto.ErrorDTO;
import it.gov.pagopa.group.dto.GroupUpdateDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.text.MessageFormat;

import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(value = {
        RestResponseExceptionHandler.class}, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@Slf4j
public class ExceptionHandlerTest {
    RestResponseExceptionHandler restResponseExceptionHandler = new RestResponseExceptionHandler();

    @Test
    void raisedGroupFileException(){
        FileSizeLimitExceededException e = new FileSizeLimitExceededException("invalid file size", 0, 0);
        ResponseEntity<GroupUpdateDTO> responseEntity = restResponseExceptionHandler.handleGroupFileException(e);
        GroupUpdateDTO groupUpdateDTO = new GroupUpdateDTO(GroupConstants.Status.KO, null, GroupConstants.Status.KOkeyMessage.INVALID_FILE_SIZE, responseEntity.getBody().getElabTimeStamp());
        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isEqualTo(groupUpdateDTO);
    }

    @Test
    void raisedGroupNotFoundException(){
        ErrorDTO errorDTO = new ErrorDTO(GroupConstants.Exception.NotFound.CODE, "There is no group for initiativeId A1");
        BeneficiaryGroupException e = new BeneficiaryGroupException(GroupConstants.Exception.NotFound.CODE,
                MessageFormat.format(GroupConstants.Exception.BadRequest.NO_GROUP_FOR_INITIATIVE_ID, "A1"),
                HttpStatus.BAD_REQUEST);
        ResponseEntity<ErrorDTO> responseEntity = restResponseExceptionHandler.handlerGroupNotFoundException(e);
        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(responseEntity.getBody()).isEqualTo(errorDTO);
    }
}
