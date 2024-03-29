package it.gov.pagopa.group.utils;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
@AllArgsConstructor
@Slf4j(topic = "AUDIT")
public class AuditUtilities {
    public static final String SRCIP;

    static {
        String srcIp;
        try {
            srcIp = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.error("Cannot determine the ip of the current host", e);
            srcIp="UNKNOWN";
        }
        SRCIP = srcIp;
    }
    private static final String CEF = String.format("CEF:0|PagoPa|IDPAY|1.0|7|User interaction|2| event=Group dstip=%s", SRCIP);
    private static final String CEF_PATTERN = CEF + " msg={} cs1Label=initiativeId cs1={} cs2Label=organizationId cs2={} cs3Label=fileName cs3={}";
    private static final String CEF_PATTERN_GROUP = CEF + " msg={} cs1Label=initiativeId cs1={} cs2Label=fileName cs2={}";
    private static final String CEF_PATTERN_GROUP_WHITELIST = CEF + " msg={} suser={} cs1Label=initiativeId cs1={}";

    private void logAuditString(String pattern, String... parameters) {
        log.info(pattern, (Object[]) parameters);
    }

    public void logUploadCFOK(String initiativeId, String organizationId, String fileName) {
        logAuditString(
                CEF_PATTERN,
                "Upload CFs file completed.", initiativeId, organizationId, fileName
        );
    }

    public void logUploadCFKO(String initiativeId, String organizationId, String fileName, String msg) {
        logAuditString(
                CEF_PATTERN,
                "Upload CFs file failed: " + msg, initiativeId, organizationId, fileName
        );
    }

    public void logDeleteGroupWhitelistOperation(String userId, String initiativeId) {
        logAuditString(
                CEF_PATTERN_GROUP_WHITELIST,
                "User in whitelist deleted", userId, initiativeId
        );
    }

    public void logDeleteGroupOperation(String initiativeId, String fileName) {
        logAuditString(
                CEF_PATTERN_GROUP,
                "Whitelist file deleted", initiativeId, fileName
        );
    }
}