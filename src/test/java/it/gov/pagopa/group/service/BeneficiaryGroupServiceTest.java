package it.gov.pagopa.group.service;

import it.gov.pagopa.group.connector.notification.NotificationConnector;
import it.gov.pagopa.group.connector.pdv.PdvEncryptRestConnector;
import it.gov.pagopa.group.constants.GroupConstants;
import it.gov.pagopa.group.constants.GroupConstants.Status;
import it.gov.pagopa.group.dto.FiscalCodeTokenizedDTO;
import it.gov.pagopa.group.dto.PiiDTO;
import it.gov.pagopa.group.exception.BeneficiaryGroupException;
import it.gov.pagopa.group.model.Group;
import it.gov.pagopa.group.model.GroupUserWhitelist;
import it.gov.pagopa.group.repository.GroupQueryDAO;
import it.gov.pagopa.group.repository.GroupRepository;
import it.gov.pagopa.group.repository.GroupUserWhitelistRepository;
import it.gov.pagopa.group.util.CFGenerator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@WebMvcTest(value = {BeneficiaryGroupService.class})
@Slf4j
@TestPropertySource(
    properties = {"file.storage.path=output/tmp/group", "file.storage.deletion=false"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BeneficiaryGroupServiceTest {

  @Autowired BeneficiaryGroupService beneficiaryGroupService;
  @Autowired BeneficiaryGroupServiceImpl beneficiaryGroupServiceImpl;

  @MockBean GroupRepository groupRepository;
  @MockBean GroupQueryDAO groupQueryDAO;
  @MockBean PdvEncryptRestConnector encryptRestConnector;
  @MockBean NotificationConnector notificationConnector;
  @MockBean GroupUserWhitelistRepository groupUserWhitelistRepository;

  // Mock your clock bean
  @MockBean private Clock clock;

  private static final String FISCAL_CODE_TOKENIZED = "FISCAL_CODE_TOKENIZED";

  // Some fixed date to make your tests
  private static final LocalDate LOCAL_DATE = LocalDate.of(2022, 1, 1);

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
  @Order(1)
  void saveValidFile_ok() throws Exception {
    FileInputStream inputFile = new FileInputStream(cfListFile);
    MockMultipartFile file = new MockMultipartFile("file",
            cfListFile.getName().replaceAll("\\d", ""),
            "text/csv",
            inputFile);
    Group group = createGroupValid_ok();

    beneficiaryGroupService.save(
        file, group.getInitiativeId(), group.getOrganizationId(), group.getStatus());

    verify(groupRepository, times(1)).save(any());
  }

  @Test
  void scheduledValidatedGroupTest_ok() throws Exception {
    Group group = createGroupValid_ok();
    FiscalCodeTokenizedDTO fiscalCodeTokenizedDTO = new FiscalCodeTokenizedDTO();
    fiscalCodeTokenizedDTO.setToken("token");
    when(groupQueryDAO.findFirstByStatusAndUpdate(Status.VALIDATED))
            .thenReturn(group);
    when(encryptRestConnector.putPii(PiiDTO.builder().pii(any()).build()))
            .thenReturn(fiscalCodeTokenizedDTO);
    beneficiaryGroupServiceImpl.scheduleValidatedGroup();
    verify(groupQueryDAO, times(1)).setStatusOk(anyString(), anyInt());
  }

  @Test
  void scheduledValidatedGroupTest_exceptionCfAnonymizer() throws Exception {
    Group group = createGroupValid_ok();
    when(groupQueryDAO.findFirstByStatusAndUpdate(GroupConstants.Status.VALIDATED))
            .thenReturn(group);
    when(encryptRestConnector.putPii(PiiDTO.builder().pii(any()).build()))
            .thenReturn(null);
    beneficiaryGroupServiceImpl.scheduleValidatedGroup();
    verify(groupQueryDAO, times(1))
            .setGroupForException(anyString(), anyString(), any(LocalDateTime.class), anyInt());
    verify(groupQueryDAO, times(2))
            .removeWhitelistByGroupId(anyString());
  }

  @Test
  void scheduledValidatedGroupTest_null() throws Exception {
    when(groupQueryDAO.findFirstByStatusAndUpdate(GroupConstants.Status.VALIDATED))
        .thenReturn(null);
    beneficiaryGroupServiceImpl.scheduleValidatedGroup();
    verify(groupQueryDAO, times(0)).setStatusOk(anyString(), anyInt());
  }

  @Test
  void scheduledProcKoGroup_ok() throws Exception {
    Group group = createGroupValidWithProcKo_ok();
    FiscalCodeTokenizedDTO fiscalCodeTokenizedDTO = new FiscalCodeTokenizedDTO();
    fiscalCodeTokenizedDTO.setToken("token");
    when(groupRepository.findFirstByStatusAndRetryLessThan(Status.PROC_KO, 3))
            .thenReturn(Optional.of(group));
    when(encryptRestConnector.putPii(PiiDTO.builder().pii(any()).build()))
            .thenReturn(fiscalCodeTokenizedDTO);
    beneficiaryGroupServiceImpl.scheduleProcKoGroup();
    verify(groupQueryDAO, times(1)).setStatusOk(anyString(), anyInt());
  }

  @Test
  void scheduledProcKoGroup_exceptionCfAnonymizer() throws Exception {
    Group group = createGroupValidWithProcKo_ok();
    when(groupRepository.findFirstByStatusAndRetryLessThan(Status.PROC_KO, 3))
            .thenReturn(Optional.of(group));
    when(groupQueryDAO.findFirstByStatusAndUpdate(Status.PROC_KO))
            .thenReturn(group);
    when(encryptRestConnector.putPii(PiiDTO.builder().pii(any()).build()))
            .thenReturn(null);
    beneficiaryGroupServiceImpl.scheduleProcKoGroup();
    verify(groupQueryDAO, times(1))
            .setGroupForException(anyString(), anyString(), any(LocalDateTime.class), anyInt());
    verify(groupQueryDAO, times(2))
            .removeWhitelistByGroupId(anyString());
  }

  @Test
  void loadFileNotExisting() throws Exception {
    String organizationId = "test";
    String fileName = "test.csv";
    try {
      beneficiaryGroupServiceImpl.load(organizationId, fileName);
    } catch (RuntimeException e) {
      Path root = Paths.get("output/tmp/group" + File.separator + organizationId);
      Files.delete(root);
      assertEquals("Could not read the file!", e.getMessage());
    }
  }

  @Test
  void scheduledProcKoGroup_null() throws Exception {
    when(groupRepository.findFirstByStatusAndRetryLessThan(Status.PROC_KO, 3))
        .thenReturn(Optional.empty());
    beneficiaryGroupServiceImpl.scheduleProcKoGroup();
    verify(groupQueryDAO, times(0)).setStatusOk(anyString(), anyInt());
  }

  @Test
  void saveInvalidFileException_ko() throws Exception {
    try {
      FileInputStream inputFile = new FileInputStream(cfListWrongFile);
      MockMultipartFile file = new MockMultipartFile("file",
              cfListWrongFile.getName().replaceAll("\\d", ""),
              "text/csv",
              inputFile);
      beneficiaryGroupService.save(file, anyString(), anyString(), anyString());
    } catch (RuntimeException e) {
      log.info("InitiativeException: " + e.getMessage());
      assertEquals("Could not store the file. Error: ", e.getMessage().subSequence(0, 33));
      assertTrue(e.getMessage().startsWith("Could not store the file. Error: "));
    }
  }

  @Test
  void whenBeneficiaryListContainCitizenToken_thenServiceReturnTrue() {
    List<GroupUserWhitelist> beneficiaryList = new ArrayList<>();
    beneficiaryList.add(new GroupUserWhitelist(null, "idG", "idI", FISCAL_CODE_TOKENIZED));
    // Instruct the Repo Mock to return Dummy Item
    when(groupUserWhitelistRepository.findByInitiativeId(anyString())).thenReturn(beneficiaryList);

    // Try to call the Real Service (which is using the instructed Repo)
    boolean citizenStatusByCitizenToken =
        beneficiaryGroupService.getCitizenStatusByCitizenToken(anyString(), FISCAL_CODE_TOKENIZED);

    // Check the equality of the results
    assertTrue(citizenStatusByCitizenToken);

    // you are expecting repo to be called once with correct param
    verify(groupUserWhitelistRepository).findByInitiativeId(anyString());
  }

  @Test
  void whenBeneficiaryListNotContainCitizenToken_thenServiceReturnFalse() {
    List<GroupUserWhitelist> beneficiaryList = new ArrayList<>();
    beneficiaryList.add(new GroupUserWhitelist(null, "idG", "idI", FISCAL_CODE_TOKENIZED));
    // Instruct the Repo Mock to return Dummy Item
    when(groupUserWhitelistRepository.findByInitiativeId(anyString())).thenReturn(beneficiaryList);

    // Try to call the Real Service (which is using the instructed Repo)
    boolean citizenStatusByCitizenToken =
        beneficiaryGroupService.getCitizenStatusByCitizenToken(anyString(), "NotPresent");

    // Check the equality of the results
    assertFalse(citizenStatusByCitizenToken);

    // you are expecting repo to be called once with correct param
    verify(groupUserWhitelistRepository).findByInitiativeId(anyString());
  }

  @Test
  void whenBeneficiaryListIsNull_thenServiceBeneficiaryListNotFound() {
    // Instruct the Repo Mock to return Dummy Item
    when(groupUserWhitelistRepository.findByInitiativeId(anyString())).thenReturn(List.of());

    // Try to call the Real Service (which is using the instructed Repo)
    try {
      beneficiaryGroupService.getCitizenStatusByCitizenToken("Id1", FISCAL_CODE_TOKENIZED);
    } catch (BeneficiaryGroupException e) {
      log.info("BeneficiaryGroupException: " + e.getCode());
      assertEquals(HttpStatus.NOT_FOUND, e.getHttpStatus());
      assertEquals(GroupConstants.Exception.NotFound.CODE, e.getCode());
      assertEquals(
          MessageFormat.format(
              GroupConstants.Exception.NotFound.NO_BENEFICIARY_LIST_PROVIDED_FOR_INITIATIVE_ID,
              "Id1"),
          e.getMessage());

      // you are expecting repo to be called once with correct param
      verify(groupUserWhitelistRepository).findByInitiativeId(anyString());
    }
  }

  @Test
  void getStatusByInitiativeId_ok() {
    Group group = createGroupValid_ok();

    when(groupRepository.getStatus("idI", "idG")).thenReturn(Optional.of(group));

    try {
      Group actual = beneficiaryGroupService.getStatusByInitiativeId("idI", "idG");
      assertEquals(group, actual);
    } catch (BeneficiaryGroupException e) {
      fail();
    }
  }

  @Test
  void getStatusByInitiativeId_not_found() {

    when(groupRepository.getStatus("idI", "idG")).thenReturn(Optional.empty());

    try {
      Group actual = beneficiaryGroupService.getStatusByInitiativeId("idI", "idG");
    } catch (BeneficiaryGroupException e) {
      assertEquals(HttpStatus.NOT_FOUND, e.getHttpStatus());
    }
  }

  @Test
  void sendInitiativeNotificationForCitizen_ok() {
    List<GroupUserWhitelist> beneficiaryList = new ArrayList<>();
    beneficiaryList.add(new GroupUserWhitelist(null, "idG", "idI", FISCAL_CODE_TOKENIZED));
    when(groupUserWhitelistRepository.findByInitiativeId(anyString())).thenReturn(beneficiaryList);

    try {
      beneficiaryGroupService.sendInitiativeNotificationForCitizen("id1", "name", "service");
    } catch (BeneficiaryGroupException e) {
      fail();
    }

    verify(notificationConnector, times(1))
        .sendAllowedCitizen(any(), anyString(), anyString(), anyString());
  }

  @Test
  void sendInitiativeNotificationForCitizen_not_found() {
    when(groupUserWhitelistRepository.findByInitiativeId(anyString())).thenReturn(List.of());

    try {
      beneficiaryGroupService.sendInitiativeNotificationForCitizen("id1", "name", "service");
    } catch (BeneficiaryGroupException e) {
      assertEquals(HttpStatus.NOT_FOUND, e.getHttpStatus());
    }

    verify(notificationConnector, times(0))
        .sendAllowedCitizen(any(), anyString(), anyString(), anyString());
  }

  @Test
  void setStatusToValidated_ok() {
    Group group = createGroupValid_ok();
    when(groupRepository.findByInitiativeIdAndStatus(anyString(), eq(GroupConstants.Status.DRAFT)))
        .thenReturn(Optional.of(group));

    beneficiaryGroupService.setStatusToValidated("idI");

    verify(groupRepository, times(1)).save(group);
  }

  @Test
  void setStatusToValidated_not_found() {
    when(groupRepository.findByInitiativeIdAndStatus(anyString(), eq(GroupConstants.Status.DRAFT)))
        .thenReturn(Optional.empty());

    try {
      beneficiaryGroupService.setStatusToValidated("idI");
    } catch (BeneficiaryGroupException e) {
      assertEquals(HttpStatus.NOT_FOUND, e.getHttpStatus());
    }

    verify(groupRepository, times(0)).save(any());
  }

  private Group createGroupValid_ok() {
    Group group = new Group();
    group.setGroupId("A1_O1");
    group.setInitiativeId("A1");
    group.setOrganizationId("O1");
    group.setFileName("cfList.csv");
    group.setStatus("VALIDATED");
    group.setExceptionMessage(null);
    group.setElabDateTime(LocalDateTime.now(clock));
    group.setCreationDate(LocalDateTime.now(clock));
    group.setUpdateDate(LocalDateTime.now(clock));
    group.setCreationUser("admin");
    group.setUpdateUser("admin");
    return group;
  }

  private Group createGroupValidWithProcKo_ok() {
    Group group = new Group();
    group.setGroupId("A1_O1");
    group.setInitiativeId("A1");
    group.setOrganizationId("O1");
    group.setFileName("cfList.csv");
    group.setStatus("PROC_KO");
    group.setExceptionMessage(null);
    group.setElabDateTime(LocalDateTime.now(clock));
    group.setCreationDate(LocalDateTime.now(clock));
    group.setUpdateDate(LocalDateTime.now(clock));
    group.setCreationUser("admin");
    group.setUpdateUser("admin");
    return group;
  }
}
