package it.gov.pagopa.group.controller;

import it.gov.pagopa.group.connector.InitiativeService;
import it.gov.pagopa.group.dto.GroupUpdateDTO;
import it.gov.pagopa.group.dto.InitiativeDTO;
import it.gov.pagopa.group.dto.StatusGroupDTO;
import it.gov.pagopa.group.model.Group;
import it.gov.pagopa.group.service.BeneficiaryGroupService;
import it.gov.pagopa.group.service.FileValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@Slf4j
public class BeneficiaryGroupController implements BeneficiaryGroup {

    @Autowired
    private BeneficiaryGroupService beneficiaryGroupService;

    @Autowired
    private InitiativeService initiativeService;

    @Autowired
    private FileValidationService fileValidationService;


    @Override
    public ResponseEntity<GroupUpdateDTO> uploadBeneficiaryGroupFile(@RequestParam("file") MultipartFile file, @PathVariable("organizationId") String organizationId, @PathVariable("initiativeId") String initiativeId, HttpServletRequest request){
        InitiativeDTO initiativeDTO = initiativeService.getInitiative(initiativeId);
        BigDecimal budget = initiativeDTO.getGeneral().getBudget();
        BigDecimal beneficiaryBudget = initiativeDTO.getGeneral().getBeneficiaryBudget();
        try {
            if (file.isEmpty()){
                return ResponseEntity.ok(GroupUpdateDTO.builder().status("KO").errorKey("group.groups.empty.file").elabTimeStamp(LocalDateTime.now()).build());
            }
            int counterCheckFile = fileValidationService.rowFileCounterCheck(file);
            if (counterCheckFile > 0){
                if (BigDecimal.valueOf(counterCheckFile).multiply(beneficiaryBudget).compareTo(budget) < 0){
                    beneficiaryGroupService.save(file, initiativeId, organizationId, "VALIDATED");
                    return ResponseEntity.ok(GroupUpdateDTO.builder().status("VALIDATED").elabTimeStamp(LocalDateTime.now()).build());
                }else {
                    return ResponseEntity.ok(GroupUpdateDTO.builder().status("KO").errorKey("group.groups.invalid.beneficiary.number").elabTimeStamp(LocalDateTime.now()).build());
                }
            }else {
                return ResponseEntity.ok(GroupUpdateDTO.builder().status("KO").errorRow(Math.abs(counterCheckFile)).errorKey("group.groups.invalid.cf").elabTimeStamp(LocalDateTime.now()).build());
            }
        }
        catch (Exception e){
            log.error("error", e);
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).build();
        }
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


