package it.gov.pagopa.group.connector.pdv;

import it.gov.pagopa.group.dto.FiscalCodeTokenizedDTO;
import it.gov.pagopa.group.dto.PiiDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "${rest-client.pdv-encrypt.cf}", url = "${rest-client.pdv-encrypt.base-url}")
public interface EncryptRestClient {
    @PutMapping(value = "/tokens", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    FiscalCodeTokenizedDTO putPii(@RequestBody PiiDTO piiDTO, @RequestHeader("x-api-key") String apikey);
}
