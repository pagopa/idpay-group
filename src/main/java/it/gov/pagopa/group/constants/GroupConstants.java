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
    }

    public static final class Exception {

        public static final class NotFound {
            public static final String CODE = "it.gov.pagopa.group.not.found";
            public static final String INITIATIVE_LIST_BY_ORGANIZATION_MESSAGE = "List of Initiatives with organizationId {0} not found.";
            public static final String INITIATIVE_BY_INITIATIVE_ID_MESSAGE = "Initiative with initiativeId {0} not found.";
            public static final String INITIATIVE_BY_INITIATIVE_ID_ORGANIZATION_ID_MESSAGE = "Initiative with organizationId {0} and initiativeId {1} not found.";
        }
        public static final class BadRequest {
            public static final String CODE = "it.gov.pagopa.initiative.bad.request";
            public static final String NO_GROUP_FOR_INITIATIVE_ID = "There is no group for initiativeId {0}";
        }
    }
}
