package it.gov.pagopa.group.util;


import ch.qos.logback.classic.LoggerContext;
import it.gov.pagopa.group.utils.AuditUtilities;
import it.gov.pagopa.group.utils.MemoryAppender;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

class AuditUtilitiesTest {
    private static final String INITIATIVE_ID = "TEST_INITIATIVE_ID";
    private static final String ORGANIZATION_ID = "TEST_ORGANIZATION_ID";
    private static final String FILE_NAME = "TEST_FILE_NAME";
    private static final String REASON = "TEST_REASON";

    private final AuditUtilities auditUtilities = new AuditUtilities();
    private MemoryAppender memoryAppender;

    @BeforeEach
    public void setup() {
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("AUDIT");
        memoryAppender = new MemoryAppender();
        memoryAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        logger.setLevel(ch.qos.logback.classic.Level.INFO);
        logger.addAppender(memoryAppender);
        memoryAppender.start();
    }


    @Test
    void logUploadCFOK_ok(){
        auditUtilities.logUploadCFOK(INITIATIVE_ID, ORGANIZATION_ID, FILE_NAME);

        Assertions.assertEquals(
                ("CEF:0|PagoPa|IDPAY|1.0|7|User interaction|2| event=Group dstip=%s msg=Upload CFs file completed." +
                        " cs1Label=initiativeId cs1=%s cs2Label=organizationId cs2=%s cs3Label=fileName cs3=%s")
                        .formatted(
                                AuditUtilities.SRCIP,
                                INITIATIVE_ID,
                                ORGANIZATION_ID,
                                FILE_NAME
                        ),
                memoryAppender.getLoggedEvents().get(0).getFormattedMessage()
        );
    }

    @Test
    void logUnsubscribe_ok(){
        auditUtilities.logUploadCFKO(INITIATIVE_ID, ORGANIZATION_ID, FILE_NAME, REASON);

        Assertions.assertEquals(
                ("CEF:0|PagoPa|IDPAY|1.0|7|User interaction|2| event=Group dstip=%s msg=Upload CFs file failed:" +
                        " %s cs1Label=initiativeId cs1=%s cs2Label=organizationId cs2=%s cs3Label=fileName cs3=%s")
                        .formatted(
                                AuditUtilities.SRCIP,
                                REASON,
                                INITIATIVE_ID,
                                ORGANIZATION_ID,
                                FILE_NAME
                        ),
                memoryAppender.getLoggedEvents().get(0).getFormattedMessage()
        );
    }

}
