package it.gov.pagopa.group.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.group.config.ServiceExceptionConfig;
import it.gov.pagopa.group.connector.initiative.InitiativeRestConnector;
import it.gov.pagopa.group.constants.GroupConstants;
import it.gov.pagopa.group.dto.*;
import it.gov.pagopa.group.exception.InitiativeStatusNotValidException;
import it.gov.pagopa.group.model.Group;
import it.gov.pagopa.group.service.BeneficiaryGroupService;
import it.gov.pagopa.group.service.FileValidationService;
import it.gov.pagopa.group.utils.CFGenerator;
import it.gov.pagopa.group.utils.AuditUtilities;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.client.ResourceAccessException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

import static it.gov.pagopa.group.constants.GroupConstants.ExceptionCode.GROUP_INITIATIVE_STATUS_NOT_VALID;
import static it.gov.pagopa.group.constants.GroupConstants.ExceptionMessage.INITIATIVE_UNPROCESSABLE_FOR_STATUS_NOT_VALID;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(
    value = {BeneficiaryGroup.class},
    excludeAutoConfiguration = SecurityAutoConfiguration.class)
@Slf4j
@Import(value = {ServiceExceptionConfig.class})
// @Import(ClockConfig.class)
class BeneficiaryGroupApiTest {

  @MockBean private BeneficiaryGroupService beneficiaryGroupService;

  @MockBean private InitiativeRestConnector initiativeRestConnector;

  @MockBean private FileValidationService fileValidationService;

  @Autowired protected MockMvc mvc;

  @Autowired private ObjectMapper objectMapper;

  // Mock your clock bean
  @MockBean private Clock clock;

  @MockBean AuditUtilities auditUtilities;

  @Value("${storage.file.path}")
  private String rootPath;

  private static final String INITIATIVE_ID = "Id1";
  private static final String ORGANIZATION_ID = "O1";
  private static final String ORGANIZATION_ID0_PLACEHOLDER = "{0}";
  private static final String INITIATIVE_ID0_PLACEHOLDER = "{0}";
  private static final String INITIATIVE_ID1_PLACEHOLDER = "{1}";
  private static final String CITIZEN_TOKEN_ID1_PLACEHOLDER = "{1}";
  private static final String BASE_URL = "http://localhost:8080/idpay/group";
  private static final String GET_GROUP_STATUS_URL =
      "/organization/"
          + ORGANIZATION_ID0_PLACEHOLDER
          + "/initiative/"
          + INITIATIVE_ID1_PLACEHOLDER
          + "/status";
  private static final String GET_CITIZEN_STATUS_URL =
      "/initiative/" + INITIATIVE_ID0_PLACEHOLDER + "/citizen/" + CITIZEN_TOKEN_ID1_PLACEHOLDER;
  private static final String PUT_GROUP_FILE =
      "/organization/"
          + ORGANIZATION_ID0_PLACEHOLDER
          + "/initiative/"
          + INITIATIVE_ID1_PLACEHOLDER
          + "/upload";

  private static final String CITIZEN_TOKEN = "CITIZEN_TOKEN";
  private static final String EXPECTED_JSON_CONTENT_BENEFICIARY_NUMBER_TOO_HIGH_FOR_BUDGET =
      "{\"status\":\"KO\",\"errorKey\":\"group.groups.invalid.file.beneficiary.number.budget\",\"elabTimeStamp\":\"2022-01-01T00:00:00\"}";

  private static final String EXPECTED_JSON_CONTENT_INVALID_CF_IN_FILE =
      "{\"status\":\"KO\",\"errorKey\":\"group.groups.invalid.file.cf.wrong\",\"elabTimeStamp\":\"2022-01-01T00:00:00\"}";

  // Some fixed date to make your tests
  private static final LocalDate LOCAL_DATE = LocalDate.of(2022, 1, 1);

  private static final String ORGANIZATION_ID_TEST = "O1";

  private static File cfListFile, cfListWrongFile;

  private final static String CF_LIST = "cfList";
  private final static String CF_LIST_WRONG = "cfListWrong";

  @BeforeAll
  static void initTempFile() {
    Map<String, File> fileMap = CFGenerator.generateTempFile();
    cfListFile = fileMap.get(CF_LIST);
    cfListWrongFile = fileMap.get(CF_LIST_WRONG);
  }

  @BeforeEach
  void initMocks() {
    // tell your tests to return the specified LOCAL_DATE when calling LocalDate.now(clock)
    // field that will contain the fixed clock
    Clock fixedClock =
        Clock.fixed(
            LOCAL_DATE.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
    doReturn(fixedClock.instant()).when(clock).instant();
    doReturn(fixedClock.getZone()).when(clock).getZone();
  }

  @Test
  void GET_GroupStatus_then200WithGroupInfo() throws Exception {
    Group group = createGroupValid_ok();

    when(beneficiaryGroupService.getStatusByInitiativeId(anyString(), anyString()))
        .thenReturn(group);

    mvc.perform(
            MockMvcRequestBuilders.get(
                    BASE_URL
                        + MessageFormat.format(
                            GET_GROUP_STATUS_URL,
                            group.getOrganizationId(),
                            group.getInitiativeId()))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andDo(print())
        .andReturn();
  }

  @Test
  void upload_PUT_when_FileIsValid_then200WithOkMessage() throws Exception {
    Group group = createGroupValid_ok();
    FileInputStream inputFile = new FileInputStream(cfListFile);
    MockMultipartFile file = new MockMultipartFile("file",
            cfListFile.getName().replaceAll("\\d", ""),
            "text/csv",
            inputFile);

    when(initiativeRestConnector.getInitiative(group.getInitiativeId()))
        .thenReturn(createInitiativeDTO(group.getOrganizationId(), group.getInitiativeId()));
    when(fileValidationService.rowFileCounterCheck(file)).thenReturn(20);
    MockMultipartHttpServletRequestBuilder builder =
        MockMvcRequestBuilders.multipart(
            (BASE_URL
                + MessageFormat.format(
                    PUT_GROUP_FILE, group.getOrganizationId(), group.getInitiativeId())));
    builder.with(
        new RequestPostProcessor() {
          @Override
          public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
            request.setMethod("PUT");
            return request;
          }
        });
    mvc.perform(builder.file(file))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andDo(print())
        .andReturn();
  }

  @Test
  void upload_PUT_when_invalidFormatFileProvided_then200withKOmessage() throws Exception {
    String initiativeId = "A1";
    GroupUpdateDTO groupUpdateDTO = createGroupUpdateDTONotValidFormatFile_ko();

    File file1 = new ClassPathResource("group" + File.separator + "invalid_format.xlsx").getFile();
    FileInputStream inputFile = new FileInputStream(file1);
    MockMultipartFile file =
        new MockMultipartFile(
            "file",
            "file.csv",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            inputFile);

    when(initiativeRestConnector.getInitiative(initiativeId))
        .thenReturn(createInitiativeDTO(ORGANIZATION_ID_TEST, initiativeId));

    MockMultipartHttpServletRequestBuilder builder =
        MockMvcRequestBuilders.multipart(
            (BASE_URL + MessageFormat.format(PUT_GROUP_FILE, ORGANIZATION_ID_TEST, initiativeId)));
    builder.with(
        new RequestPostProcessor() {
          @Override
          public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
            request.setMethod("PUT");
            return request;
          }
        });
    mvc.perform(builder.file(file))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andDo(print())
        .andReturn();
  }

  @Test
  void upload_PUT_when_emptyFileProvided_then200withKOmessage() throws Exception {
    String initiativeId = "A1";
    GroupUpdateDTO groupUpdateDTO = createGroupUpdateDTONotValidFormatFile_ko();

    File file1 = new ClassPathResource("group" + File.separator + "empty_file.csv").getFile();

    FileInputStream inputFile = new FileInputStream(file1);
    MockMultipartFile file = new MockMultipartFile("file", "file.csv", "text/csv", inputFile);

    when(initiativeRestConnector.getInitiative(initiativeId))
        .thenReturn(createInitiativeDTO(ORGANIZATION_ID_TEST, initiativeId));

    MockMultipartHttpServletRequestBuilder builder =
        MockMvcRequestBuilders.multipart(
            (BASE_URL + MessageFormat.format(PUT_GROUP_FILE, ORGANIZATION_ID_TEST, initiativeId)));
    builder.with(
        new RequestPostProcessor() {
          @Override
          public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
            request.setMethod("PUT");
            return request;
          }
        });
    mvc.perform(builder.file(file))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andDo(print())
        .andReturn();
  }

  @Test
  void upload_PUT_when_InitiativeIsNotProcessable_then_422() throws Exception {
    Group group = createGroupValid_ok();
    String organizationId = group.getOrganizationId();
    String initiativeId = group.getInitiativeId();

    InitiativeDTO initiativeDTO = createInitiativeDTOLowBudget(organizationId, initiativeId);

    FileInputStream inputFile = new FileInputStream(cfListFile);
    MockMultipartFile file = new MockMultipartFile("file",
            cfListFile.getName().replaceAll("\\d", ""),
            "text/csv",
            inputFile);

    when(initiativeRestConnector.getInitiative(initiativeId))
        .thenThrow(new InitiativeStatusNotValidException(GROUP_INITIATIVE_STATUS_NOT_VALID,
                String.format(INITIATIVE_UNPROCESSABLE_FOR_STATUS_NOT_VALID, initiativeId)));

    MockMultipartHttpServletRequestBuilder builder =
        MockMvcRequestBuilders.multipart(
            (BASE_URL + MessageFormat.format(PUT_GROUP_FILE, organizationId, initiativeId)));
    builder.with(
        new RequestPostProcessor() {
          @Override
          public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
            request.setMethod("PUT");
            return request;
          }
        });

    mvc.perform(builder.file(file))
        .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
        .andDo(print())
        .andReturn();
  }

  @Test
  void upload_PUT_when_InitiativeServiceIsNotReachable_then_500RaisedResourceAccessException()
      throws Exception {
    FileInputStream inputFile = new FileInputStream(cfListFile);
    MockMultipartFile file = new MockMultipartFile("file",
            cfListFile.getName().replaceAll("\\d", ""),
            "text/csv",
            inputFile);

    // doThrow InitiativeException for Void method
    doThrow(new ResourceAccessException("Exception Message"))
        .when(initiativeRestConnector)
        .getInitiative(INITIATIVE_ID);

    MockMultipartHttpServletRequestBuilder builder =
        MockMvcRequestBuilders.multipart(
            (BASE_URL + MessageFormat.format(PUT_GROUP_FILE, ORGANIZATION_ID, INITIATIVE_ID)));
    builder.with(
        new RequestPostProcessor() {
          @Override
          public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
            request.setMethod("PUT");
            return request;
          }
        });

    mvc.perform(builder.file(file))
        .andExpect(MockMvcResultMatchers.status().is5xxServerError())
        .andDo(print())
        .andReturn();
  }

  @Test
  void upload_PUT_when_FileCfTooLarge_then200withKOmessage() throws Exception {
    Group group = createGroupValid_ok();
    FileInputStream inputFile = new FileInputStream(cfListFile);
    MockMultipartFile file = new MockMultipartFile("file",
            cfListFile.getName().replaceAll("\\d", ""),
            "text/csv",
            inputFile);

    when(initiativeRestConnector.getInitiative(group.getInitiativeId()))
        .thenReturn(createInitiativeDTO(group.getOrganizationId(), group.getInitiativeId()));
    when(fileValidationService.rowFileCounterCheck(file)).thenReturn(20);
    MockMultipartHttpServletRequestBuilder builder =
        MockMvcRequestBuilders.multipart(
            (BASE_URL
                + MessageFormat.format(
                    PUT_GROUP_FILE, group.getOrganizationId(), group.getInitiativeId())));
    builder.with(
        new RequestPostProcessor() {
          @Override
          public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
            request.setMethod("PUT");
            return request;
          }
        });
    mvc.perform(builder.file(file))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andDo(print())
        .andReturn();
  }

  @Test
  void upload_PUT_when_InvalidBeneficiaryNumberCfOnFile_then200withKOmessage() throws Exception {
    Group group = createGroupValid_ok();

    InitiativeDTO initiativeDTO =
        createInitiativeDTOLowBudget(group.getOrganizationId(), group.getInitiativeId());

    FileInputStream inputFile = new FileInputStream(cfListFile);
    MockMultipartFile file = new MockMultipartFile("file",
            cfListFile.getName().replaceAll("\\d", ""),
            "text/csv",
            inputFile);

    when(initiativeRestConnector.getInitiative(initiativeDTO.getInitiativeId()))
        .thenReturn(initiativeDTO);
    when(fileValidationService.rowFileCounterCheck(file)).thenReturn(20);

    MockMultipartHttpServletRequestBuilder builder =
        MockMvcRequestBuilders.multipart(
            (BASE_URL + MessageFormat.format(PUT_GROUP_FILE, "O1", "A1")));
    builder.with(
        new RequestPostProcessor() {
          @Override
          public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
            request.setMethod("PUT");
            return request;
          }
        });

    mvc.perform(builder.file(file))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(content().json(EXPECTED_JSON_CONTENT_BENEFICIARY_NUMBER_TOO_HIGH_FOR_BUDGET))
        .andDo(print())
        .andReturn();
  }

  @Test
  void upload_PUT_when_InvalidCfFile_then200withKOmessage() throws Exception {
    Group group = createGroupValid_ok();

    InitiativeDTO initiativeDTO =
        createInitiativeDTO(group.getOrganizationId(), group.getInitiativeId());

    FileInputStream inputFile = new FileInputStream(cfListWrongFile);
    MockMultipartFile file = new MockMultipartFile("file",
            cfListWrongFile.getName().replaceAll("\\d", ""),
            "text/csv",
            inputFile);

    when(initiativeRestConnector.getInitiative(initiativeDTO.getInitiativeId()))
        .thenReturn(initiativeDTO);
    when(fileValidationService.rowFileCounterCheck(file)).thenReturn(-34);

    MockMultipartHttpServletRequestBuilder builder =
        MockMvcRequestBuilders.multipart(
            (BASE_URL + MessageFormat.format(PUT_GROUP_FILE, "O1", "A1")));
    builder.with(
        new RequestPostProcessor() {
          @Override
          public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
            request.setMethod("PUT");
            return request;
          }
        });

    mvc.perform(builder.file(file))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(content().json(EXPECTED_JSON_CONTENT_INVALID_CF_IN_FILE))
        .andDo(print())
        .andReturn();
  }

  @Test
  void upload_PUT_when_FileIOException_then500withGenericError() throws Exception {

    Group group = createGroupValid_ok();
    FileInputStream inputFile = new FileInputStream(cfListFile);
    MockMultipartFile file = new MockMultipartFile("file",
            cfListFile.getName().replaceAll("\\d", ""),
            "text/csv",
            inputFile);

    when(initiativeRestConnector.getInitiative(group.getInitiativeId()))
        .thenReturn(createInitiativeDTO(group.getOrganizationId(), group.getInitiativeId()));
    when(fileValidationService.rowFileCounterCheck(file)).thenThrow(new IOException());
    MockMultipartHttpServletRequestBuilder builder =
        MockMvcRequestBuilders.multipart(
            (BASE_URL
                + MessageFormat.format(
                    PUT_GROUP_FILE, group.getOrganizationId(), group.getInitiativeId())));
    builder.with(
        new RequestPostProcessor() {
          @Override
          public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
            request.setMethod("PUT");
            return request;
          }
        });
    mvc.perform(builder.file(file))
        .andExpect(MockMvcResultMatchers.status().isInternalServerError())
        .andDo(print())
        .andReturn();
  }

  @Test
  void whenCitizenTokenExist_then_getCitizenStatus_isTrue() throws Exception {
    Group group = createGroupValid_ok();

    CitizenStatusDTO citizenStatusDTO = new CitizenStatusDTO();
    citizenStatusDTO.setStatus(true);
    String expectedResponseJson = objectMapper.writeValueAsString(citizenStatusDTO);

    when(beneficiaryGroupService.getCitizenStatusByCitizenToken(anyString(), anyString()))
        .thenReturn(true);

    mvc.perform(
            MockMvcRequestBuilders.get(
                    BASE_URL
                        + MessageFormat.format(
                            GET_CITIZEN_STATUS_URL, group.getInitiativeId(), CITIZEN_TOKEN))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(expectedResponseJson))
        .andDo(print())
        .andReturn();
  }

  @Test
  void GET_whenCitizenTokenNotExist_then_getCitizenStatus_isFalse() throws Exception {
    Group group = createGroupValid_ok();

    CitizenStatusDTO citizenStatusDTO = new CitizenStatusDTO();
    citizenStatusDTO.setStatus(false);
    String expectedResponseJson = objectMapper.writeValueAsString(citizenStatusDTO);

    when(beneficiaryGroupService.getCitizenStatusByCitizenToken(anyString(), anyString()))
        .thenReturn(false);

    mvc.perform(
            MockMvcRequestBuilders.get(
                    BASE_URL
                        + MessageFormat.format(
                            GET_CITIZEN_STATUS_URL, group.getInitiativeId(), CITIZEN_TOKEN))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(expectedResponseJson))
        .andDo(print())
        .andReturn();
  }

  @Test
  void POST_setStatusToValidated() throws Exception {
    Group group = createGroupValid_ok();

    doNothing().when(beneficiaryGroupService).setStatusToValidated(anyString());

    mvc.perform(
            MockMvcRequestBuilders.post(
                BASE_URL
                    + MessageFormat.format("/initiative/{0}/validate", group.getInitiativeId())))
        .andExpect(MockMvcResultMatchers.status().isAccepted())
        .andDo(print())
        .andReturn();
  }

  @Test
  void POST_notifyInitiativeToCitizen() throws Exception {
    Group group = createGroupValid_ok();

    InitiativeNotificationDTO initiativeNotificationDTO = new InitiativeNotificationDTO("nome", "idServizio");


    doNothing().when(beneficiaryGroupService).sendInitiativeNotificationForCitizen(any(), anyString(), anyString());

    mvc.perform(
            MockMvcRequestBuilders.post(
                BASE_URL
                    + MessageFormat.format("/initiative/{0}/notify", group.getInitiativeId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(initiativeNotificationDTO)))
        .andExpect(MockMvcResultMatchers.status().isAccepted())
        .andDo(print())
        .andReturn();
  }

  private Group createGroupValid_ok() {
    Group group = new Group();
    group.setGroupId("A1_O1");
    group.setInitiativeId("A1");
    group.setOrganizationId("O1");
    group.setFileName("test.csv");
    group.setStatus("VALIDATED");
    group.setExceptionMessage(null);
    group.setElabDateTime(LocalDateTime.now(clock));
    group.setCreationDate(LocalDateTime.now(clock));
    group.setUpdateDate(LocalDateTime.now(clock));
    group.setCreationUser("admin");
    group.setUpdateUser("admin");
    return group;
  }

  private GroupUpdateDTO createGroupUpdateDTONotValidFormatFile_ko() {
    GroupUpdateDTO group = new GroupUpdateDTO();
    group.setStatus("KO");
    group.setErrorKey(GroupConstants.Status.KOkeyMessage.INVALID_FILE_FORMAT);
    group.setElabTimeStamp(LocalDateTime.now(clock));
    return group;
  }

  private GroupUpdateDTO createGroupUpdateDTONotValidEmptyFile_ko() {
    GroupUpdateDTO group = new GroupUpdateDTO();
    group.setStatus("KO");
    group.setErrorKey(GroupConstants.Status.KOkeyMessage.INVALID_FILE_EMPTY);
    group.setElabTimeStamp(LocalDateTime.now(clock));
    return group;
  }

  private InitiativeDTO createInitiativeDTO(String organizationId, String initiativeId) {
    InitiativeDTO initiativeDTO = new InitiativeDTO();
    InitiativeGeneralDTO initiativeGeneralDTO =
        new InitiativeGeneralDTO(BigDecimal.valueOf(90000), BigDecimal.valueOf(900));
    initiativeDTO.setInitiativeId(initiativeId);
    initiativeDTO.setOrganizationId(organizationId);
    initiativeDTO.setInitiativeName("initiativa1");
    initiativeDTO.setGeneral(initiativeGeneralDTO);
    return initiativeDTO;
  }

  private InitiativeDTO createInitiativeDTOLowBudget(String organizationId, String initiativeId) {
    InitiativeDTO initiativeDTO = new InitiativeDTO();
    InitiativeGeneralDTO initiativeGeneralDTO =
        new InitiativeGeneralDTO(BigDecimal.valueOf(60), BigDecimal.valueOf(30));
    initiativeDTO.setInitiativeId(initiativeId);
    initiativeDTO.setOrganizationId(organizationId);
    initiativeDTO.setInitiativeName("initiativa1");
    initiativeDTO.setGeneral(initiativeGeneralDTO);
    return initiativeDTO;
  }
}
