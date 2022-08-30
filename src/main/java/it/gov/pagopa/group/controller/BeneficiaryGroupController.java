package it.gov.pagopa.group.controller;

import it.gov.pagopa.group.dto.GroupUpdateDTO;
import it.gov.pagopa.group.dto.InitiativeDTO;
import it.gov.pagopa.group.dto.StatusGroupDTO;
import it.gov.pagopa.group.model.Group;
import it.gov.pagopa.group.service.BeneficiaryGroupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@Slf4j
public class BeneficiaryGroupController implements BeneficiaryGroup {
    public static final String FISCAL_CODE_REGEX = "^([A-Za-z]{6}[0-9lmnpqrstuvLMNPQRSTUV]{2}[abcdehlmprstABCDEHLMPRST]{1}[0-9lmnpqrstuvLMNPQRSTUV]{2}[A-Za-z]{1}[0-9lmnpqrstuvLMNPQRSTUV]{3}[A-Za-z]{1})$";

    RestTemplate restTemplate = new RestTemplate();

    public static final String url = "http://localhost:8080/idpay/initiative/{initiativeId}/beneficiary/view";
    @Autowired
    private BeneficiaryGroupService beneficiaryGroupService;


    @Override
    public ResponseEntity<GroupUpdateDTO> uploadBeneficiaryGroupFile(@RequestParam("file") MultipartFile file, @PathVariable("organizationId") String organizationId, @PathVariable("initiativeId") String initiativeId){
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<?> entity = new HttpEntity<>(headers);
        HttpEntity<InitiativeDTO> initiativeResponse = restTemplate.exchange("http://localhost:8080/idpay/initiative/"+initiativeId+"/beneficiary/view",
                HttpMethod.GET,
                entity,
                InitiativeDTO.class,
                initiativeId
        );
        InitiativeDTO initiativeDTO = initiativeResponse.getBody();
        BigDecimal budget = initiativeDTO.getGeneral().getBudget();
        BigDecimal beneficiaryBudget = initiativeDTO.getGeneral().getBeneficiaryBudget();
        try {
            if (file.isEmpty()){
                beneficiaryGroupService.save(file, initiativeId, organizationId, "KO");
                return ResponseEntity.ok(GroupUpdateDTO.builder().status("KO").errorKey("group.groups.empty.file").elabTimeStamp(LocalDateTime.now()).build());
            }
            int counterCheckFile = rowFileCounterCheck(file);
            if (counterCheckFile > 0){
                if (BigDecimal.valueOf(counterCheckFile).multiply(beneficiaryBudget).compareTo(budget) < 0){
                    beneficiaryGroupService.save(file, initiativeId, organizationId, "VALIDATED");
                    return ResponseEntity.ok(GroupUpdateDTO.builder().status("VALIDATED").elabTimeStamp(LocalDateTime.now()).build());
                }else {
                    beneficiaryGroupService.save(file, initiativeId, organizationId, "KO");
                    return ResponseEntity.ok(GroupUpdateDTO.builder().status("KO").errorKey("group.groups.invalid.beneficiary.number").elabTimeStamp(LocalDateTime.now()).build());
                }
            }else {
                beneficiaryGroupService.save(file, initiativeId, organizationId, "KO");
                return ResponseEntity.ok(GroupUpdateDTO.builder().status("KO").errorRow(Math.abs(counterCheckFile)).errorKey("group.groups.invalid.cf").elabTimeStamp(LocalDateTime.now()).build());
            }
        }
        catch (Exception e){
            log.error("error", e);
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).build();
        }
    }

    private int rowFileCounterCheck(MultipartFile file) throws IOException {
        BufferedReader br;
        int counter = 0;
        String line;
        InputStream is = file.getInputStream();
        br = new BufferedReader(new InputStreamReader(is));
        while ((line = br.readLine()) != null) {
            counter++;
            if (checkCf(line) == false) {
                return -counter;
            }
        }
        return counter;
    }

    private boolean checkCf(String cf){
        if (cf.matches(FISCAL_CODE_REGEX)){
            return true;
        }
        return false;
    }

    @Override
    public ResponseEntity<StatusGroupDTO> getGroupStatus(@PathVariable("initiativeId") String initiativeId){
        Group group = beneficiaryGroupService.getStatusByInitiativeId(initiativeId);
        StatusGroupDTO statusGroupDTO = new StatusGroupDTO();
        statusGroupDTO.setStatus(group.getStatus());
        statusGroupDTO.setErrorMessage("");
        return ResponseEntity.ok(statusGroupDTO);
    }
}


