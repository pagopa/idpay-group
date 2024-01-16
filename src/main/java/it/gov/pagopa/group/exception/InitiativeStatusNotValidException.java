package it.gov.pagopa.group.exception;

import it.gov.pagopa.common.web.exception.ServiceException;
import it.gov.pagopa.common.web.exception.ServiceExceptionPayload;

public class InitiativeStatusNotValidException extends ServiceException {
    public InitiativeStatusNotValidException(String code, String message) {
        this(code, message, null, false, null);
    }

    public InitiativeStatusNotValidException(String code, String message, ServiceExceptionPayload payload, boolean printStackTrace, Throwable ex) {
        super(code, message, payload, printStackTrace, ex);
    }
}
