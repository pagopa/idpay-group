package it.gov.pagopa.group.controller;

import it.gov.pagopa.group.connector.initiative.InitiativeRestConnector;
import it.gov.pagopa.group.constants.GroupConstants;
import it.gov.pagopa.group.dto.*;
import it.gov.pagopa.group.model.Group;
import it.gov.pagopa.group.service.BeneficiaryGroupService;
import it.gov.pagopa.group.service.FileValidationService;
import it.gov.pagopa.group.utils.AuditUtilities;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;

@Slf4j
@RestController
public class BeneficiaryGroupController implements BeneficiaryGroup {

    private final BeneficiaryGroupService beneficiaryGroupService;

    private final InitiativeRestConnector initiativeRestConnector;

    private final FileValidationService fileValidationService;

    private final Clock clock;

    private final AuditUtilities auditUtilities;

    public BeneficiaryGroupController(BeneficiaryGroupService beneficiaryGroupService, InitiativeRestConnector initiativeRestConnector, FileValidationService fileValidationService, Clock clock, AuditUtilities auditUtilities) {
        this.beneficiaryGroupService = beneficiaryGroupService;
        this.initiativeRestConnector = initiativeRestConnector;
        this.fileValidationService = fileValidationService;
        this.clock = clock;
        this.auditUtilities = auditUtilities;
    }

    @Override
    public ResponseEntity<GroupUpdateDTO> uploadBeneficiaryGroupFile(@RequestParam("file") MultipartFile file, @PathVariable("organizationId") String organizationId, @PathVariable("initiativeId") String initiativeId){
        if (file.isEmpty()){
            log.info("[UPLOAD_FILE_GROUP] - Initiative: {}. File is empty", initiativeId);
            auditUtilities.logUploadCFKO(initiativeId, organizationId, file.getName(), "File is empty");
            return ResponseEntity.ok(GroupUpdateDTO.builder().status(GroupConstants.Status.KO).errorKey(GroupConstants.Status.KOkeyMessage.INVALID_FILE_EMPTY).elabTimeStamp(LocalDateTime.now(clock)).build());
        }
        if (!(GroupConstants.CONTENT_TYPE.equals(file.getContentType()))){
            log.info("[UPLOAD_FILE_GROUP] - Initiative: {}. ContentType not accepted: {}", initiativeId, file.getContentType());
            auditUtilities.logUploadCFKO(initiativeId, organizationId, file.getName(), "ContentType not accepted");
            return ResponseEntity.ok(GroupUpdateDTO.builder().status(GroupConstants.Status.KO).errorKey(GroupConstants.Status.KOkeyMessage.INVALID_FILE_FORMAT).elabTimeStamp(LocalDateTime.now(clock)).build());
        }
        InitiativeDTO initiativeDTO = initiativeRestConnector.getInitiative(initiativeId);
        BigDecimal budget = initiativeDTO.getGeneral().getBudget();
        BigDecimal beneficiaryBudget = initiativeDTO.getGeneral().getBeneficiaryBudget();
        try {
            int counterCheckFile = fileValidationService.rowFileCounterCheck(file);
            if (counterCheckFile > 0){
                if (BigDecimal.valueOf(counterCheckFile).multiply(beneficiaryBudget).compareTo(budget) <= 0){
                    beneficiaryGroupService.save(file, initiativeId, organizationId, GroupConstants.Status.DRAFT);
                    auditUtilities.logUploadCFOK(initiativeId, organizationId, file.getName());
                    return ResponseEntity.ok(GroupUpdateDTO.builder().status(GroupConstants.Status.DRAFT).elabTimeStamp(LocalDateTime.now(clock)).build());
                }else {
                    log.info("[UPLOAD_FILE_GROUP] - Initiative: {}. Invalid beneficiary number", initiativeId);
                    auditUtilities.logUploadCFKO(initiativeId, organizationId, file.getName(), "Invalid beneficiary number");
                    return ResponseEntity.ok(GroupUpdateDTO.builder().status(GroupConstants.Status.KO).errorKey(GroupConstants.Status.KOkeyMessage.INVALID_FILE_BENEFICIARY_NUMBER_HIGH_FOR_BUDGET).elabTimeStamp(LocalDateTime.now(clock)).build());
                }
            }else {
                log.info("[UPLOAD_FILE_GROUP] - Initiative: {}. Wrong CF at row {}", initiativeId, Math.abs(counterCheckFile));
                auditUtilities.logUploadCFKO(initiativeId, organizationId, file.getName(), String.format("Wrong CF at row %s", Math.abs(counterCheckFile)));
                return ResponseEntity.ok(GroupUpdateDTO.builder().status(GroupConstants.Status.KO).errorRow(Math.abs(counterCheckFile)).errorKey(GroupConstants.Status.KOkeyMessage.INVALID_FILE_CF_WRONG).elabTimeStamp(LocalDateTime.now(clock)).build());
            }
        } catch (Exception e) {
            log.error("[UPLOAD_FILE_GROUP] - Generic Error: {}", e.getMessage());
            auditUtilities.logUploadCFKO(initiativeId, organizationId, file.getName(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @Override
    public ResponseEntity<Void> notifyInitiativeToCitizen(String initiativeId, InitiativeNotificationDTO initiativeNotificationDTO){
        log.info("[NOTIFY_ALLOWED_CITIZEN] - Start processing...");
        beneficiaryGroupService.sendInitiativeNotificationForCitizen(initiativeId, initiativeNotificationDTO.getInitiativeName(), initiativeNotificationDTO.getServiceId());
        log.info("[NOTIFY_ALLOWED_CITIZEN] - Request Sent");
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @Override
    public ResponseEntity<StatusGroupDTO> getGroupStatus(@PathVariable("organizationId") String organizationId, @PathVariable("initiativeId") String initiativeId) {
        Group group = beneficiaryGroupService.getStatusByInitiativeId(initiativeId, organizationId);
        log.info("[GET_FILE_GROUP] - Initiative {}. Status {}. ErrorMessage: {}. CreationDate: {}. File Name {}", initiativeId, group.getStatus(), group.getExceptionMessage(), group.getCreationDate(), group.getFileName());
        return ResponseEntity.ok(StatusGroupDTO.builder()
                        .status(group.getStatus())
                        .errorMessage(group.getExceptionMessage())
                        .fileUploadingDateTime(group.getCreationDate())
                        .fileName(group.getFileName())
                        .beneficiariesReached(group.getBeneficiariesReached())
                        .build());
    }

    @Override
    public ResponseEntity<CitizenStatusDTO> getCitizenStatus(String initiativeId, String citizenToken) {
        CitizenStatusDTO citizenStatusDTO = new CitizenStatusDTO();
        if(beneficiaryGroupService.getCitizenStatusByCitizenToken(initiativeId, citizenToken)){
            citizenStatusDTO.setStatus(true);
        }
        log.info("[GET_CITIZEN_STATUS] [WhiteList-AllowedList] - Initiative {}. Citizen status: {}", initiativeId, citizenStatusDTO.isStatus());
        return ResponseEntity.ok(citizenStatusDTO);
    }

    @Override
    public ResponseEntity<Void> setGroupStatusToValidated(String initiativeId) {
        log.info("[SET_GROUP_STATUS_TO_VALIDATED] - Start processing initiative {}", initiativeId);
        beneficiaryGroupService.setStatusToValidated(initiativeId);
        log.info("[SET_GROUP_STATUS_TO_VALIDATED] - Status updated");
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }
}


