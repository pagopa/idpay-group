package it.gov.pagopa.group.exception;

import it.gov.pagopa.common.web.exception.ServiceException;
import it.gov.pagopa.common.web.exception.ServiceExceptionPayload;

public class FolderInitializeException extends ServiceException {
    public FolderInitializeException(String code, String message) {
        this(code, message, null, false, null);
    }

    public FolderInitializeException(String code, String message, ServiceExceptionPayload payload, boolean printStackTrace, Throwable ex) {
        super(code, message, payload, printStackTrace, ex);
    }
}
