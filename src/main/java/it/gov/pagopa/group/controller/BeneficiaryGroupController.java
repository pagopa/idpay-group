package it.gov.pagopa.group.controller;

import it.gov.pagopa.group.connector.initiative.InitiativeRestConnector;
import it.gov.pagopa.group.constants.GroupConstants;
import it.gov.pagopa.group.dto.CitizenStatusDTO;
import it.gov.pagopa.group.dto.GroupUpdateDTO;
import it.gov.pagopa.group.dto.InitiativeDTO;
import it.gov.pagopa.group.dto.StatusGroupDTO;
import it.gov.pagopa.group.model.Group;
import it.gov.pagopa.group.service.BeneficiaryGroupService;
import it.gov.pagopa.group.service.FileValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@Slf4j
public class BeneficiaryGroupController implements BeneficiaryGroup {

    @Autowired
    private BeneficiaryGroupService beneficiaryGroupService;

    @Autowired
    private InitiativeRestConnector initiativeRestConnector;

    @Autowired
    private FileValidationService fileValidationService;


    @Override
    public ResponseEntity<GroupUpdateDTO> uploadBeneficiaryGroupFile(@RequestParam("file") MultipartFile file, @PathVariable("organizationId") String organizationId, @PathVariable("initiativeId") String initiativeId){
        if (file.isEmpty()){
            return ResponseEntity.ok(GroupUpdateDTO.builder().status(GroupConstants.Status.KO).errorKey(GroupConstants.Status.KOkeyMessage.INVALID_FILE_EMPTY).elabTimeStamp(LocalDateTime.now()).build());
        }
        if (!(GroupConstants.CONTENT_TYPE.equals(file.getContentType()))){
            return ResponseEntity.ok(GroupUpdateDTO.builder().status(GroupConstants.Status.KO).errorKey(GroupConstants.Status.KOkeyMessage.INVALID_FILE_FORMAT).elabTimeStamp(LocalDateTime.now()).build());
        }
        InitiativeDTO initiativeDTO = initiativeRestConnector.getInitiative(initiativeId);
        BigDecimal budget = initiativeDTO.getGeneral().getBudget();
        BigDecimal beneficiaryBudget = initiativeDTO.getGeneral().getBeneficiaryBudget();
        try {
            int counterCheckFile = fileValidationService.rowFileCounterCheck(file);
            if (counterCheckFile > 0){
                if (BigDecimal.valueOf(counterCheckFile).multiply(beneficiaryBudget).compareTo(budget) <= 0){
                    beneficiaryGroupService.save(file, initiativeId, organizationId, GroupConstants.Status.VALIDATED);
                    return ResponseEntity.ok(GroupUpdateDTO.builder().status(GroupConstants.Status.VALIDATED).elabTimeStamp(LocalDateTime.now()).build());
                }else {
                    return ResponseEntity.ok(GroupUpdateDTO.builder().status(GroupConstants.Status.KO).errorKey(GroupConstants.Status.KOkeyMessage.INVALID_FILE_BENEFICIARY_NUMBER_HIGH_FOR_BUDGET).elabTimeStamp(LocalDateTime.now()).build());
                }
            }else {
                return ResponseEntity.ok(GroupUpdateDTO.builder().status(GroupConstants.Status.KO).errorRow(Math.abs(counterCheckFile)).errorKey(GroupConstants.Status.KOkeyMessage.INVALID_FILE_CF_WRONG).elabTimeStamp(LocalDateTime.now()).build());
            }
        } catch (Exception e) {
            log.error("[UPLOAD_FILE_GROUP] - Generic Error: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @Override
    public ResponseEntity<StatusGroupDTO> getGroupStatus(@PathVariable("organizationId") String organizationId, @PathVariable("initiativeId") String initiativeId) {
        Group group = beneficiaryGroupService.getStatusByInitiativeId(initiativeId, organizationId);
        return ResponseEntity.ok(StatusGroupDTO.builder()
                .status(group.getStatus())
                .errorMessage(group.getExceptionMessage())
                .fileUploadingDateTime(group.getCreationDate())
                .fileName(group.getFileName())
                .build());
    }

    @Override
    public ResponseEntity<CitizenStatusDTO> getCitizenStatus(String initiativeId, String citizenToken) {
        CitizenStatusDTO citizenStatusDTO = new CitizenStatusDTO();
        if(beneficiaryGroupService.getCitizenStatusByCitizenToken(initiativeId, citizenToken)){
            citizenStatusDTO.setStatus(true);
        }
        return ResponseEntity.ok(citizenStatusDTO);
    }
}


