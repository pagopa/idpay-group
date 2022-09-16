package it.gov.pagopa.group.exception;


import it.gov.pagopa.group.constants.GroupConstants;
import it.gov.pagopa.group.dto.ErrorDTO;
import it.gov.pagopa.group.dto.GroupUpdateDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.springframework.core.NestedRuntimeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import java.time.LocalDateTime;

@ControllerAdvice
@Slf4j
public class RestResponseExceptionHandler {

    // HttpStatus 200 with KO Status message
    @ExceptionHandler({FileSizeLimitExceededException.class})
    public ResponseEntity<GroupUpdateDTO> handleFileSizeLimitExceededException(FileSizeLimitExceededException ex) {
        log.info("[HANDLER] [UPLOAD_FILE_GROUP] - Size Limit Error: {}", ex.getMessage());
        return ResponseEntity.ok(GroupUpdateDTO.builder().status(GroupConstants.Status.KO).errorKey(GroupConstants.Status.KOkeyMessage.INVALID_FILE_SIZE).elabTimeStamp(LocalDateTime.now()).build());
    }
    @ExceptionHandler({MaxUploadSizeExceededException.class})
    public ResponseEntity<GroupUpdateDTO> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        log.info("[HANDLER] [UPLOAD_FILE_GROUP] - Size Limit Error: {}", ex.getMessage());
        return ResponseEntity.ok(GroupUpdateDTO.builder().status(GroupConstants.Status.KO).errorKey(GroupConstants.Status.KOkeyMessage.INVALID_FILE_SIZE).elabTimeStamp(LocalDateTime.now()).build());
    }

    // HttpStatus dynamic on Exception
    @ExceptionHandler({BeneficiaryGroupException.class})
    public ResponseEntity<ErrorDTO> handlerBeneficiaryGroupException(BeneficiaryGroupException ex){
        log.error("[HANDLER] [GROUP_EXCEPTION] - Error: {}", ex.getMessage());
            return new ResponseEntity<>(new ErrorDTO(ex.getCode(), ex.getMessage()),
                    ex.getHttpStatus());
    }

    // HttpStatus 500
    @ExceptionHandler({MultipartException.class, ResourceAccessException.class})
    public ResponseEntity<ErrorDTO> handlerNestedRuntimeException(NestedRuntimeException ex){
        log.error("[HANDLER] [RUNTIME_EXCEPTION] - Error: {}", ex.getMessage());
        return new ResponseEntity<>(new ErrorDTO("500", ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
