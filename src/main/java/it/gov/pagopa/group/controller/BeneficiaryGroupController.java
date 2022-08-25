package it.gov.pagopa.group.controller;

import it.gov.pagopa.group.dto.GroupUpdateDTO;
import it.gov.pagopa.group.service.BeneficiaryGroupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@Slf4j
public class BeneficiaryGroupController implements BeneficiaryGroup {
    public static final String FISCAL_CODE_REGEX = "^([A-Za-z]{6}[0-9lmnpqrstuvLMNPQRSTUV]{2}[abcdehlmprstABCDEHLMPRST]{1}[0-9lmnpqrstuvLMNPQRSTUV]{2}[A-Za-z]{1}[0-9lmnpqrstuvLMNPQRSTUV]{3}[A-Za-z]{1})$";

    @Autowired
    private BeneficiaryGroupService beneficiaryGroupService;

    @Override
    public ResponseEntity<GroupUpdateDTO> uploadBeneficiaryGroupFile(@RequestParam("file") MultipartFile file, @PathVariable("organizationId") String organizationId, @PathVariable("initiativeId") String initiativeId){
        BigDecimal budget = BigDecimal.valueOf(1000000);
        BigDecimal beneficiaryBudget = BigDecimal.valueOf(1000);
        try {
            int checkFile = fileCheck(file);
            if (file.isEmpty()){
                return ResponseEntity.ok(GroupUpdateDTO.builder().status("KO").errorKey("group.groups.empty.file").errorRow(null).elabTimeStamp(LocalDateTime.now()).build());
            }
            if (checkFile > 0){
                if (BigDecimal.valueOf(checkFile).multiply(beneficiaryBudget).compareTo(budget) == -1){
                    beneficiaryGroupService.save(file, initiativeId, organizationId);
                    return ResponseEntity.ok(GroupUpdateDTO.builder().status("VALIDATED").elabTimeStamp(LocalDateTime.now()).build());
                }else {
                    return ResponseEntity.ok(GroupUpdateDTO.builder().status("KO").errorKey("group.groups.invalid.beneficiary.number").errorRow(null).elabTimeStamp(LocalDateTime.now()).build());
                }
            }else {
                return ResponseEntity.ok(GroupUpdateDTO.builder().status("KO").errorRow(-checkFile).errorKey("group.groups.invalid.cf").elabTimeStamp(LocalDateTime.now()).build());
            }
        }
        catch (Exception e){
            log.error("error", e);
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).build();
        }
    }

    private int fileCheck(MultipartFile file) throws IOException {
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
}


