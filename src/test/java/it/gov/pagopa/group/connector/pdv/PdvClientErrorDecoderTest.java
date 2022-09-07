package it.gov.pagopa.group.connector.pdv;

import feign.*;
import feign.codec.ErrorDecoder;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class PdvClientErrorDecoderTest {

    @Test
    public void throwsFeignException() {
        String reasonExceptionMessage = "[500 Internal server error] during [PUT] to [/test] [Service#test()]: []";

        // given
        ErrorDecoder decoder = new PdvClientErrorDecoder();
        Response response = responseStub(500, reasonExceptionMessage);

        FeignException exception = assertThrows(FeignException.class, () -> {
            decoder.decode("Service#test()", response);
        });

        assertEquals(reasonExceptionMessage, exception.getMessage());
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
