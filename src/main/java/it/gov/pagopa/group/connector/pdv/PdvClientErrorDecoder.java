package it.gov.pagopa.group.connector.pdv;

import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PdvClientErrorDecoder implements ErrorDecoder {
    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String s, Response response) {
        log.info("Error Response!!!");

        if (response.status() == 503) {
            throw new RetryableException(
                    response.status(),
//                    String.format("Service unavailable (status code %s)", response.status()),
                    response.reason(),
                    response.request().httpMethod(),
                    null,
                    response.request());
        }

        return defaultErrorDecoder.decode(s, response);
    }
}