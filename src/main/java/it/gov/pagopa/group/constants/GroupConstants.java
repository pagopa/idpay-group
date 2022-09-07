package it.gov.pagopa.group.constants;


public class GroupConstants {

    private GroupConstants(){}

    public static final String CONTENT_TYPE = "text/csv";

    public static final class Status {
        public static final String VALIDATED = "VALIDATED";
        public static final String PROCESSING = "PROCESSING";
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

    public static final class Exception {

        public static final class NotFound {
            public static final String CODE = "it.gov.pagopa.group.not.found";
            public static final String NO_BENEFICIARY_LIST_PROVIDED_FOR_INITIATIVE_ID = "No beneficiary list provided for initiativeId {0}";
        }
        public static final class BadRequest {
            public static final String CODE = "it.gov.pagopa.initiative.bad.request";
            public static final String NO_GROUP_FOR_INITIATIVE_ID = "There is no group for initiativeId {0}";
        }
    }
}
