package it.gov.pagopa.group.encrypt;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "${rest-client.encrypt.cf}", url = "${rest-client.encrypt.base-url}")
public interface EncryptRest {
    @PutMapping(value = "/tokens/{token}/pii", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    String getPiiByToken(@PathVariable("token") String token,
                                 @RequestHeader("x-api-key") String apikey);
}
