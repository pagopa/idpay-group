package it.gov.pagopa.group.connector.pdv;

import feign.RetryableException;
import feign.Retryer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PdvClientRetryer extends Retryer.Default {

    public PdvClientRetryer(long period, long maxPeriod, int maxAttempts) {
        super(period, maxPeriod, maxAttempts);
    }

    @Override
    public void continueOrPropagate(RetryableException ex) {
        super.continueOrPropagate(ex);
        log.warn("Retrying HTTP request...");
    }

}
