package it.gov.pagopa.group.exception;

import it.gov.pagopa.common.web.exception.ServiceException;

public class FileLoadException extends ServiceException {
    public FileLoadException(String code, String message) {
        this(code, message, false, null);
    }

    public FileLoadException(String code, String message, boolean printStackTrace, Throwable ex) {
        super(code, message, printStackTrace, ex);
    }
}
