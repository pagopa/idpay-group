package it.gov.pagopa.group.connector.pdv;

import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import feign.codec.ErrorDecoder;
import it.gov.pagopa.group.constants.GroupConstants;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PdvClientErrorDecoderTest {

    @Test
    void throwsFeignException() {
        String reasonExceptionMessage = "[500 Internal server error] during [PUT] to [/test] [Service#test()]: []";

        // given
        ErrorDecoder decoder = new PdvClientErrorDecoder();
        Response response = responseStub(500, reasonExceptionMessage);

        FeignException exception = assertThrows(FeignException.class, () -> {
            decoder.decode("Service#test()", response);
        });

        assertEquals(reasonExceptionMessage, exception.getMessage());
    }

    @Test
    void throwsException() {
        String reasonExceptionMessage = "[300] during [PUT] to [url] [Service#test()]: []";

        // given
        ErrorDecoder decoder = new PdvClientErrorDecoder();
        Response response = responseStub(300, null);

        Exception e = decoder.decode("Service#test()", response);

        assertEquals(reasonExceptionMessage, e.getMessage());
    }

    private Response responseStub(int status, String reasonExceptionMessage) {
        return Response.builder()
                .request(
                        Request.create(Request.HttpMethod.PUT, "url", Collections.emptyMap(), new byte[0], Charset.defaultCharset(), new RequestTemplate()))
                .status(status)
                .reason(reasonExceptionMessage)
//                .headers(headers)
                .build();
    }
}
