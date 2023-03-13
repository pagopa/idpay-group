package it.gov.pagopa.group.utils;


import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;

import it.gov.pagopa.group.exception.BeneficiaryGroupException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class AuditUtilities {
    private static final String SRCIP;

    static {
        try {
            SRCIP = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new BeneficiaryGroupException(String.valueOf(HttpStatus.BAD_REQUEST.value()), e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    private static final String CEF = String.format("CEF:0|PagoPa|IDPAY|1.0|7|User interaction|2| event=Group dstip=%s", SRCIP);
    private static final String MSG = " msg=";
    private static final String INITIATIVE_ID = "cs1Label=initiativeId cs1=";
    private static final String ORGANIZATION_ID = "cs2Label=organizationId cs2=";
    private static final String FILE_NAME = "cs3Label=fileName cs3=";

    final Logger logger = Logger.getLogger("AUDIT");


    private String buildLog(String eventLog, String initiativeId, String organizationId, String fileName) {
        return CEF + MSG + eventLog + " " + INITIATIVE_ID + initiativeId + " " + ORGANIZATION_ID + organizationId + " " + FILE_NAME + fileName;
    }

    public void logUploadCFOK(String initiativeId, String organizationId, String fileName) {
        String testLog = this.buildLog("Upload CFs file completed ", initiativeId, organizationId, fileName);
        logger.info(testLog);
    }

    public void logUploadCFKO(String initiativeId, String organizationId, String fileName, String msg) {
        String testLog = this.buildLog("Upload CFs file failed: " + msg, initiativeId, organizationId, fileName);
        logger.info(testLog);
    }

}