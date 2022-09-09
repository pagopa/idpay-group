package it.gov.pagopa.group.exception;

import it.gov.pagopa.group.ClockConfig;
import it.gov.pagopa.group.connector.pdv.PdvEncryptRestConnector;
import it.gov.pagopa.group.constants.GroupConstants;
import it.gov.pagopa.group.dto.ErrorDTO;
import it.gov.pagopa.group.dto.GroupUpdateDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.text.MessageFormat;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@WebMvcTest(value = {
        RestResponseExceptionHandler.class}, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@Slf4j
class ExceptionHandlerTest { //FIXME If possible, try to raise Exceptions (not handled from business logic) also from Controller and not only manually.
    RestResponseExceptionHandler restResponseExceptionHandler = new RestResponseExceptionHandler();

    @Test
    void raisedFileSizeLimitExceededException(){
        FileSizeLimitExceededException e = new FileSizeLimitExceededException("invalid file size", 0, 0);
        ResponseEntity<GroupUpdateDTO> responseEntity = restResponseExceptionHandler.handleFileSizeLimitExceededException(e);
        GroupUpdateDTO groupUpdateDTO = new GroupUpdateDTO(GroupConstants.Status.KO, null, GroupConstants.Status.KOkeyMessage.INVALID_FILE_SIZE, responseEntity.getBody().getElabTimeStamp());
        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isEqualTo(groupUpdateDTO);
    }

    @Test
    void raisedMaxUploadSizeExceededException(){
        MaxUploadSizeExceededException e = new MaxUploadSizeExceededException(2097152L);
        ResponseEntity<GroupUpdateDTO> responseEntity = restResponseExceptionHandler.handleMaxUploadSizeExceededException(e);
        GroupUpdateDTO groupUpdateDTO = new GroupUpdateDTO(GroupConstants.Status.KO, null, GroupConstants.Status.KOkeyMessage.INVALID_FILE_SIZE, responseEntity.getBody().getElabTimeStamp());
        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isEqualTo(groupUpdateDTO);
    }

    @Test
    void raisedBeneficiaryGroupException(){
        ErrorDTO errorDTO = new ErrorDTO(GroupConstants.Exception.NotFound.CODE, "There is no group for initiativeId A1");
        BeneficiaryGroupException e = new BeneficiaryGroupException(GroupConstants.Exception.NotFound.CODE,
                MessageFormat.format(GroupConstants.Exception.NotFound.NO_GROUP_FOR_INITIATIVE_ID, "A1"),
                HttpStatus.NOT_FOUND);
        ResponseEntity<ErrorDTO> responseEntity = restResponseExceptionHandler.handlerBeneficiaryGroupException(e);
        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(responseEntity.getBody()).isEqualTo(errorDTO);
    }
}
