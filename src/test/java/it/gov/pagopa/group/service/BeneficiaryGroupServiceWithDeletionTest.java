package it.gov.pagopa.group.service;

import it.gov.pagopa.group.connector.notification.NotificationConnector;
import it.gov.pagopa.group.connector.pdv.PdvEncryptRestConnector;
import it.gov.pagopa.group.constants.GroupConstants;
import it.gov.pagopa.group.dto.FiscalCodeTokenizedDTO;
import it.gov.pagopa.group.dto.PiiDTO;
import it.gov.pagopa.group.model.Group;
import it.gov.pagopa.group.repository.GroupQueryDAO;
import it.gov.pagopa.group.repository.GroupRepository;
import it.gov.pagopa.group.repository.GroupUserWhitelistRepository;
import it.gov.pagopa.group.util.CFGenerator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@WebMvcTest(value = {BeneficiaryGroupService.class})
@Slf4j
@TestPropertySource(
        properties = {"file.storage.path=output/tmp/group", "file.storage.deletion=true"})
class BeneficiaryGroupServiceWithDeletionTest {

    @Autowired BeneficiaryGroupService beneficiaryGroupService;
    @Autowired BeneficiaryGroupServiceImpl beneficiaryGroupServiceImpl;
    @MockBean GroupRepository groupRepository;
    @MockBean GroupQueryDAO groupQueryDAO;
    @MockBean PdvEncryptRestConnector encryptRestConnector;
    @MockBean NotificationConnector notificationConnector;
    @MockBean GroupUserWhitelistRepository groupUserWhitelistRepository;
    @MockBean private Clock clock;
    private static final LocalDate LOCAL_DATE = LocalDate.of(2022, 1, 1);
    private static File cfListFile;
    private final static String CF_LIST = "cfList";

    @BeforeAll
    static void initTempFile() {
        Map<String, File> fileMap = CFGenerator.generateTempFile();
        cfListFile = fileMap.get(CF_LIST);
    }

    @BeforeEach
    void initMocks() throws IOException {
        fixCLock();
        saveValidFile_ok();
    }

    void saveValidFile_ok() throws IOException {
        FileInputStream inputFile = new FileInputStream(cfListFile);
        MockMultipartFile file = new MockMultipartFile("file",
                cfListFile.getName().replaceAll("\\d", ""),
                "text/csv",
                inputFile);
        Group group = createGroupValid_ok();
        beneficiaryGroupService.save(
                file, group.getInitiativeId(), group.getOrganizationId(), group.getStatus());
    }

    void fixCLock() {
        // tell your tests to return the specified LOCAL_DATE when calling LocalDate.now(clock)
        // field that will contain the fixed clock
        Clock fixedClock =
                Clock.fixed(
                        LOCAL_DATE.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
    }

    @Test
    void scheduledValidatedGroupTest_ok() throws Exception {
        Group group = createGroupValid_ok();
        FiscalCodeTokenizedDTO fiscalCodeTokenizedDTO = new FiscalCodeTokenizedDTO();
        fiscalCodeTokenizedDTO.setToken("token");
        when(groupQueryDAO.findFirstByStatusAndUpdate(GroupConstants.Status.VALIDATED))
                .thenReturn(group);
        when(encryptRestConnector.putPii(PiiDTO.builder().pii(any()).build()))
                .thenReturn(fiscalCodeTokenizedDTO);
        beneficiaryGroupServiceImpl.scheduleValidatedGroup();
        verify(groupQueryDAO, times(1)).setStatusOk(anyString(), anyInt());
    }

    @Test
    void scheduledProcKoGroup_ok() throws Exception {
        Group group = createGroupValidWithProcKo_ok();
        FiscalCodeTokenizedDTO fiscalCodeTokenizedDTO = new FiscalCodeTokenizedDTO();
        fiscalCodeTokenizedDTO.setToken("token");
        when(groupRepository.findFirstByStatusAndRetryLessThan(GroupConstants.Status.PROC_KO, 3))
                .thenReturn(Optional.of(group));
        when(encryptRestConnector.putPii(PiiDTO.builder().pii(any()).build()))
                .thenReturn(fiscalCodeTokenizedDTO);
        beneficiaryGroupServiceImpl.scheduleProcKoGroup();
        verify(groupQueryDAO, times(1)).setStatusOk(anyString(), anyInt());
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
