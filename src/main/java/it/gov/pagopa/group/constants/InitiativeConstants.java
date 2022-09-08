package it.gov.pagopa.group.constants;

public class InitiativeConstants {

    private InitiativeConstants(){}

    public static final class Status {
        public static final String DRAFT = "DRAFT"; //In bozza
        public static final String TO_CHECK = "TO_CHECK"; //Da controllare/revisionare
        public static final String IN_REVISION = "IN_REVISION"; //In revisione
        public static final String APPROVED = "APPROVED"; //Approvata
        public static final String PUBLISHED = "PUBLISHED"; //In corso / Pubblicata ??
        public static final String CLOSED = "CLOSED"; //Terminata
        public static final String SUSPENDED = "SUSPENDED"; //Sospesa
    }

    public static final class Validation {
        public static final String [] allowedInitiativeStatusArray = {Status.DRAFT, Status.TO_CHECK};
    }

    public static final class Exception extends AbstractContant{

        public static final class UnprocessableEntity {
            public static final String CODE = BASE_CODE + "initiative.status.unprocessable.entity";
            public static final String INITIATIVE_STATUS_NOT_PROCESSABLE_FOR_GROUP = "initiativeId %s is not processable due to its Status";
        }
    }
}
