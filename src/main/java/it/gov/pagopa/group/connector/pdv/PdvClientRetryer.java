package it.gov.pagopa.group.connector.pdv;

import feign.RetryableException;
import feign.Retryer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PdvClientRetryer extends Retryer.Default {
    private final int maxAttempts;
    private final long period;
    private final long maxPeriod;

    public PdvClientRetryer(long period, long maxPeriod, int maxAttempts) {
        super(period, maxPeriod, maxAttempts);
        this.period = period;
        this.maxPeriod = maxPeriod;
        this.maxAttempts = maxAttempts;
    }

    private PdvClientRetryer(PdvClientRetryer retryer) {
        super(retryer.period, retryer.maxPeriod, retryer.maxAttempts);
        this.period = retryer.period;
        this.maxPeriod = retryer.maxPeriod;
        this.maxAttempts = retryer.maxAttempts;
    }

    @Override
    public void continueOrPropagate(RetryableException ex) {
        super.continueOrPropagate(ex);
        log.warn("Retrying HTTP request...");
    }

}
