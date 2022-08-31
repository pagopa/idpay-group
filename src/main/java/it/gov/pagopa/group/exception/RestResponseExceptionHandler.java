package it.gov.pagopa.group.exception;


import it.gov.pagopa.group.constants.GroupConstants;
import it.gov.pagopa.group.dto.ErrorDTO;
import it.gov.pagopa.group.dto.GroupUpdateDTO;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
//@Slf4j
public class RestResponseExceptionHandler {

    // API
    @ExceptionHandler({FileSizeLimitExceededException.class})
    public ResponseEntity<GroupUpdateDTO> handleGroupFileException(FileSizeLimitExceededException ex) {
        return ResponseEntity.ok(GroupUpdateDTO.builder().status("KO").errorKey("group.groups.invalid.file.size").elabTimeStamp(LocalDateTime.now()).build());
    }

    @ExceptionHandler({BeneficiaryGroupException.class})
    public ResponseEntity<ErrorDTO> handlerGroupNotFoundException(BeneficiaryGroupException ex){
            return new ResponseEntity<>(new ErrorDTO(ex.getCode(), ex.getMessage()),
                    ex.getHttpStatus());
    }
}
