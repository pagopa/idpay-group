package it.gov.pagopa.group.constants;


public class GroupConstants {

    private GroupConstants(){}

    public static final String OPERATION_TYPE_DELETE_INITIATIVE = "DELETE_INITIATIVE";

    public static final String CONTENT_TYPE = "text/csv";

    public static final class Status {
        public static final String VALIDATED = "VALIDATED";
        public static final String PROCESSING = "PROCESSING";
        public static final String DRAFT = "DRAFT";
        public static final String PROC_KO = "PROC_KO";
        public static final String OK = "OK";
        public static final String KO = "KO";
        public static final class KOkeyMessage{
            public static final String INVALID_FILE_FORMAT          = "group.groups.invalid.file.format";
            public static final String INVALID_FILE_EMPTY           = "group.groups.invalid.file.empty";
            public static final String INVALID_FILE_SIZE            = "group.groups.invalid.file.size";
            public static final String INVALID_FILE_BENEFICIARY_NUMBER_HIGH_FOR_BUDGET   = "group.groups.invalid.file.beneficiary.number.budget";
            public static final String INVALID_FILE_CF_WRONG              = "group.groups.invalid.file.cf.wrong";
        }
    }

    public static final class Producer {

        public static final class NotifyCitizen {
            public static final String OPERATION_TYPE = "ALLOWED_CITIZEN_PUBLISH";
        }
    }

    public static final class ExceptionCode {
        public static final String NOT_FOUND = "GROUP_NOT_FOUND";
        public static final String GENERIC_ERROR = "GROUP_GENERIC_ERROR";
        public static final String TOO_MANY_REQUESTS = "GROUP_TOO_MANY_REQUESTS";
        public static final String INVALID_REQUEST = "GROUP_INVALID_REQUEST";
        public static final String GROUP_BENEFICIARY_LIST_NOT_PROVIDED = "GROUP_BENEFICIARY_LIST_NOT_PROVIDED";
        public static final String GROUP_NOT_FOUND_OR_STATUS_NOT_VALID = "GROUP_NOT_FOUND_OR_STATUS_NOT_VALID";
        public static final String GROUP_INITIATIVE_STATUS_NOT_VALID = "GROUP_INITIATIVE_STATUS_NOT_VALID";

        private ExceptionCode() {}
    }

    public static final class ExceptionMessage {
        public static final String NOT_BENEFICIARY_LIST_PROVIDED_FOR_INITIATIVE = "No beneficiary list provided for initiativeId [%s]";
        public static final String GROUP_NOT_FOUND_FOR_INITIATIVE = "There is no group for initiativeId [%s]";
        public static final String GROUP_NOT_FOUND_FOR_INITIATIVE_OR_STATUS_NOT_VALID = "There is no group for initiativeId [%s] or the status is invalid";
        public static final String INITIATIVE_UNPROCESSABLE_FOR_STATUS_NOT_VALID = "Initiative [%s] is unprocessable for status not valid";

        private ExceptionMessage() {}
    }

}
