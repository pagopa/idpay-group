package it.gov.pagopa.group.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import it.gov.pagopa.group.dto.CitizenStatusDTO;
import it.gov.pagopa.group.dto.ErrorDTO;
import it.gov.pagopa.group.dto.GroupUpdateDTO;
import it.gov.pagopa.group.dto.StatusGroupDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

public interface BeneficiaryGroup {

    @Operation(operationId = "uploadGroupOfBeneficiary", summary = "Upload CSV file containing a group of Beneficiary", description = "", tags = {"group"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ok",  content = @Content(mediaType = "application/json", schema = @Schema(implementation = GroupUpdateDTO.class))),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "401", description = "Authentication failed", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "404", description = "The requested ID was not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "409", description = "Conflict", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "429", description = "Too many Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "500", description = "Server ERROR", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class)))})
    @PutMapping(value = "/idpay/organization/{organizationId}/initiative/{initiativeId}/upload")
    ResponseEntity<GroupUpdateDTO> uploadBeneficiaryGroupFile(@RequestParam("file") MultipartFile file, @PathVariable("organizationId") String organizationId, @PathVariable("initiativeId") String initiativeId);

    @Operation(operationId = "getGroupOfBeneficiaryStatusAndDetails", summary = "Return Group of Beneficiary CSV file upload status with relative error detail if present", description = "", tags = {"group"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ok",  content = @Content(mediaType = "application/json", schema = @Schema(implementation = StatusGroupDTO.class))),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "401", description = "Authentication failed", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "404", description = "The requested ID was not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "500", description = "Server ERROR", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class)))})
    @GetMapping(value = "/idpay/organization/{organizationId}/initiative/{initiativeId}/status")
    ResponseEntity<StatusGroupDTO> getGroupStatus( @PathVariable("organizationId") String organizationId, @PathVariable("initiativeId") String initiativeId);

    @Operation(operationId = "getCitizenStatusForInitiative", summary = "Return a positive or negative boolean if the searched user is part of the preset group list uploaded for the specific initiative", description = "", tags = {"group"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ok",  content = @Content(mediaType = "application/json", schema = @Schema(implementation = CitizenStatusDTO.class))),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "401", description = "Authentication failed", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "404", description = "The requested ID was not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "500", description = "Server ERROR", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class)))})
    @GetMapping(value = "/idpay/organization/{organizationId}/initiative/{initiativeId}/citizen/{citizenToken}")
    ResponseEntity<CitizenStatusDTO> getCitizienStatus( @PathVariable("organizationId") String organizationId, @PathVariable("initiativeId") String initiativeId, @PathVariable("citizenToken") String citizenToken);
}
