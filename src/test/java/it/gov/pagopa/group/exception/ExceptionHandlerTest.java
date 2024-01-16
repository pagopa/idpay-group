package it.gov.pagopa.group.exception;

import it.gov.pagopa.group.constants.GroupConstants;
import it.gov.pagopa.group.dto.GroupUpdateDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import static org.assertj.core.api.Assertions.assertThat;

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
}
