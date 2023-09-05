package it.gov.pagopa.group.exception;

import it.gov.pagopa.common.web.exception.ClientExceptionWithBody;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@SuppressWarnings("squid:S110")
public class BeneficiaryGroupException extends ClientExceptionWithBody {
    public BeneficiaryGroupException(String code, String message, HttpStatus httpStatus) {
        super(httpStatus, code, message);
    }
}
