package it.gov.pagopa.group.exception;

import it.gov.pagopa.common.web.exception.ServiceException;

public class InitiativeStatusNotValidException extends ServiceException {
    public InitiativeStatusNotValidException(String code, String message) {
        this(code, message, false, null);
    }

    public InitiativeStatusNotValidException(String code, String message, boolean printStackTrace, Throwable ex) {
        super(code, message, printStackTrace, ex);
    }
}
