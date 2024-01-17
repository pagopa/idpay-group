package it.gov.pagopa.group.exception;


import it.gov.pagopa.group.constants.GroupConstants;
import it.gov.pagopa.group.dto.GroupUpdateDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

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

}
