package it.gov.pagopa.group.exception;

import it.gov.pagopa.common.web.exception.ServiceException;

public class GroupNotFoundOrNotValidStatusException extends ServiceException {
    public GroupNotFoundOrNotValidStatusException(String code, String message) { this(code, message, false, null); }
    public GroupNotFoundOrNotValidStatusException(String code, String message, boolean printStackTrace, Throwable ex) {
        super(code, message, printStackTrace, ex);
    }
}
