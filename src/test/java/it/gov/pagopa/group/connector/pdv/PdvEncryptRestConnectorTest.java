package it.gov.pagopa.group.connector.pdv;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import feign.Request;
import feign.RequestTemplate;
import feign.RetryableException;
import it.gov.pagopa.group.config.RestConnectorConfig;
import it.gov.pagopa.group.dto.FiscalCodeTokenizedDTO;
import it.gov.pagopa.group.dto.PiiDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.TestPropertySourceUtils;

import java.nio.charset.Charset;
import java.util.Collections;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestPropertySource(
        locations = "classpath:application.yml",
        properties = {
                "rest-client.pdv-encrypt.cf=pdv-ms-tokenizer",
                "rest-client.pdv-encrypt.http.retry.period=100",
                "rest-client.pdv-encrypt.http.retry.maxPeriod=1",
                "rest-client.pdv-encrypt.http.retry.maxAttempts=3"
        })
@Slf4j
@SpringBootTest
@ContextConfiguration(
        initializers = PdvEncryptRestConnectorTest.WireMockInitializer.class,
        classes = {
                PdvEncryptRestConnectorImpl.class,
                RestConnectorConfig.class,
                FeignAutoConfiguration.class,
                HttpMessageConvertersAutoConfiguration.class
        })
class PdvEncryptRestConnectorTest {

    private static final String EXCEPTION_MESSAGE = "Exception Message";
    private static final String RANDOM_CF = "AOISFN73R54B745Z";

    public static class WireMockInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            //Instruct a Server to be the PDV Mock Server
            wireMockServer = new WireMockServer(new WireMockConfiguration().dynamicPort());
            wireMockServer.start();

            configurableApplicationContext.getBeanFactory().registerSingleton("wireMockServer", wireMockServer);

            configurableApplicationContext.addApplicationListener(
                    applicationEvent -> {
                        if (applicationEvent instanceof ContextClosedEvent) {
                            wireMockServer.stop();
                        }
                    });

            //Override test properties to instruct FeignClient Service to use the same address & port of WireMockServer
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                    configurableApplicationContext,
                    String.format(
                            "rest-client.pdv-encrypt.base-url=http://%s:%d",
                            wireMockServer.getOptions().bindAddress(), wireMockServer.port()));
        }
    }

    private static WireMockServer wireMockServer;

    private static final String FISCAL_CODE_TOKENIZED = "ec9f2fbe-ab67-4e93-a1d6-25ae6e685b74";

    @Autowired
    PdvEncryptRestConnector pdvEncryptRestConnector;

    @Test
    void verifyPdvCall() throws JsonProcessingException {
        PiiDTO piiDTO = PiiDTO.builder().pii(RANDOM_CF).build();

        //Connector will call the fake server and expecting to reply what we Stub on src\resources\mappings
        FiscalCodeTokenizedDTO fiscalCodeTokenizedDTO = pdvEncryptRestConnector.putPii(piiDTO);

        assertNotNull(fiscalCodeTokenizedDTO);
        assertEquals(FISCAL_CODE_TOKENIZED, fiscalCodeTokenizedDTO.getToken());

        // match a few of the encoded json values
        String json = new ObjectMapper().writeValueAsString(piiDTO);
        wireMockServer.verify(1,
                WireMock.putRequestedFor(WireMock.urlEqualTo("/tokens"))
                        .withRequestBody(equalToJson(json))
        );
    }

    @Test
    void whenPDVRetryerHasMaximum3Attempts_thenOnly3TriesAllowed(){
        Request request = Request.create(Request.HttpMethod.PUT, wireMockServer.baseUrl(), Collections.emptyMap(), new byte[0], Charset.defaultCharset(), new RequestTemplate());
        RetryableException e = new RetryableException(503, EXCEPTION_MESSAGE, null, 0L, request);
        PdvClientRetryer pdvClientRetryer = new PdvClientRetryer(100, 1, 3);

        //Retry #2
        pdvClientRetryer.continueOrPropagate(e);
        //Retry #3
        pdvClientRetryer.continueOrPropagate(e);

        //prepare Executable with invocation of the method on your system under test
        Executable executable = () -> pdvClientRetryer.continueOrPropagate(e); //4th Retry. This time will go in Exception after 3rd retry
        Exception exception = Assertions.assertThrows(RetryableException.class, executable);
        assertEquals(EXCEPTION_MESSAGE, exception.getMessage());
    }

    @Test
    void pdvTooManyRequestException() throws JsonProcessingException {
        PiiDTO pii = PiiDTO.builder().pii("PUT_TOO_MANY_REQUEST_PII").build();

        Executable executable = () -> pdvEncryptRestConnector.putPii(pii);

        Assertions.assertThrows(RetryableException.class, executable);

        String json = new ObjectMapper().writeValueAsString(pii);

        wireMockServer.verify(3,
                WireMock.putRequestedFor(WireMock.urlEqualTo("/tokens")).withRequestBody(equalToJson(json)));

    }
    @AfterEach
    void resetAll() {
        wireMockServer.resetAll();
    }

}
