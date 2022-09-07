package it.gov.pagopa.group.exception;


import it.gov.pagopa.group.constants.GroupConstants;
import it.gov.pagopa.group.dto.ErrorDTO;
import it.gov.pagopa.group.dto.GroupUpdateDTO;
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import java.time.LocalDateTime;

@ControllerAdvice
//@Slf4j
public class RestResponseExceptionHandler {

    // API
    @ExceptionHandler({FileSizeLimitExceededException.class, MaxUploadSizeExceededException.class})
    public ResponseEntity<GroupUpdateDTO> handleGroupFileException(FileSizeLimitExceededException ex) {
        return ResponseEntity.ok(GroupUpdateDTO.builder().status(GroupConstants.Status.KO).errorKey(GroupConstants.Status.KOkeyMessage.INVALID_FILE_SIZE).elabTimeStamp(LocalDateTime.now()).build());
    }

    @ExceptionHandler({BeneficiaryGroupException.class})
    public ResponseEntity<ErrorDTO> handlerGroupNotFoundException(BeneficiaryGroupException ex){
            return new ResponseEntity<>(new ErrorDTO(ex.getCode(), ex.getMessage()),
                    ex.getHttpStatus());
    }

    @ExceptionHandler({MultipartException.class})
    public ResponseEntity<ErrorDTO> handlerFileGroupNullException(MultipartException ex){
        return new ResponseEntity<>(new ErrorDTO("500", ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
