package it.gov.pagopa.group.service;

import it.gov.pagopa.group.connector.pdv.PdvEncryptRestConnector;
import it.gov.pagopa.group.constants.GroupConstants;
import it.gov.pagopa.group.exception.BeneficiaryGroupException;
import it.gov.pagopa.group.model.Group;
import it.gov.pagopa.group.repository.GroupQueryDAO;
import it.gov.pagopa.group.repository.GroupRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;

import java.io.File;
import java.io.FileInputStream;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@WebMvcTest(value = {
        BeneficiaryGroupService.class})
@Slf4j
@TestPropertySource(properties = {
        "file.storage.path=output/tmp/group",
        "file.storage.deletion=false"
})
class BeneficiaryGroupServiceTest {

    private static final String FISCAL_CODE_TOKENIZED = "FISCAL_CODE_TOKENIZED";

    @Autowired
    BeneficiaryGroupService beneficiaryGroupService;

    @MockBean
    GroupRepository groupRepository;
    @MockBean
    GroupQueryDAO groupQueryDAO;
    @MockBean
    PdvEncryptRestConnector encryptRestConnector;


    @Test
    void saveValidFile_ok() throws Exception{
        File file1 = new ClassPathResource("group" + File.separator + "ps_fiscal_code_groups_file_large_20.csv").getFile();
        FileInputStream inputFile = new FileInputStream( file1);
        MockMultipartFile file = new MockMultipartFile("file", file1.getName(), "text/csv", inputFile);
        Group group = createGroupValid_ok();

        beneficiaryGroupService.save(file, group.getInitiativeId(), group.getOrganizationId(), group.getStatus());

        verify(groupRepository, times(1)).save(any());
    }
    @Test
    void saveInvalidFileException_ko() throws Exception{
        try {
            File file1 = new ClassPathResource("group" + File.separator + "ps_fiscal_code_groups_file_large_20.csv").getFile();
            FileInputStream inputFile = new FileInputStream( file1);
            MockMultipartFile file = new MockMultipartFile("file", file1.getName(), "text/csv", inputFile);
            beneficiaryGroupService.save(file, anyString(), anyString(), anyString());
        } catch (RuntimeException e){
            log.info("InitiativeException: " + e.getMessage());
            assertEquals("Could not store the file. Error: ", e.getMessage().subSequence(0, 33));
            assertTrue(e.getMessage().startsWith("Could not store the file. Error: "));
        }
    }

    @Test
    void whenBeneficiaryListContainCitizenToken_thenServiceReturnTrue() {
        Group group = new Group();
        List<String> beneficiaryList = new ArrayList<>();
        beneficiaryList.add(FISCAL_CODE_TOKENIZED);
        group.setBeneficiaryList(beneficiaryList);
        //Instruct the Repo Mock to return Dummy Item
        when(groupRepository.getBeneficiaryList(anyString(), anyString())).thenReturn(Optional.of(group));

        //Try to call the Real Service (which is using the instructed Repo)
        boolean citizenStatusByCitizenToken = beneficiaryGroupService.getCitizenStatusByCitizenToken(anyString(), anyString(), FISCAL_CODE_TOKENIZED);

        //Check the equality of the results
        assertEquals(true, citizenStatusByCitizenToken);

        // you are expecting repo to be called once with correct param
        verify(groupRepository).getBeneficiaryList(anyString(), anyString());
    }

    @Test
    void whenBeneficiaryListNotContainCitizenToken_thenServiceReturnFalse() {
        Group group = new Group();
        List<String> beneficiaryList = new ArrayList<>();
        beneficiaryList.add(FISCAL_CODE_TOKENIZED);
        group.setBeneficiaryList(beneficiaryList);
        //Instruct the Repo Mock to return Dummy Item
        when(groupRepository.getBeneficiaryList(anyString(), anyString())).thenReturn(Optional.of(group));

        //Try to call the Real Service (which is using the instructed Repo)
        boolean citizenStatusByCitizenToken = beneficiaryGroupService.getCitizenStatusByCitizenToken(anyString(), anyString(), "NotPresent");

        //Check the equality of the results
        assertEquals(false, citizenStatusByCitizenToken);

        // you are expecting repo to be called once with correct param
        verify(groupRepository).getBeneficiaryList(anyString(), anyString());
    }

    @Test
    void whenBeneficiaryListIsNull_thenServiceBeneficiaryListNotFound() {
        Group group = new Group();
        //Instruct the Repo Mock to return Dummy Item
        when(groupRepository.getBeneficiaryList(anyString(), anyString())).thenReturn(Optional.of(group));

        //Try to call the Real Service (which is using the instructed Repo)
        try {
            beneficiaryGroupService.getCitizenStatusByCitizenToken("Id1", "Ente1", FISCAL_CODE_TOKENIZED);
        } catch (BeneficiaryGroupException e) {
            log.info("BeneficiaryGroupException: " + e.getCode());
            assertEquals(HttpStatus.NOT_FOUND, e.getHttpStatus());
            assertEquals(GroupConstants.Exception.NotFound.CODE, e.getCode());
            assertEquals(MessageFormat.format(GroupConstants.Exception.NotFound.NO_BENEFICIARY_LIST_PROVIDED_FOR_INITIATIVE_ID, "Id1"), e.getMessage());

            // you are expecting repo to be called once with correct param
            verify(groupRepository).getBeneficiaryList(anyString(), anyString());
        }
    }

    @Test
    void whenGroupsNotFound_thenServiceNotFound() {
        //Instruct the Repo Mock to return Dummy Item
        when(groupRepository.getBeneficiaryList(anyString(), anyString())).thenThrow(
                new BeneficiaryGroupException(
                        GroupConstants.Exception.NotFound.CODE,
                        MessageFormat.format(GroupConstants.Exception.NotFound.NO_GROUP_FOR_INITIATIVE_ID, "Id1"),
                        HttpStatus.NOT_FOUND)
        );

        //Try to call the Real Service (which is using the instructed Repo)
        try {
            beneficiaryGroupService.getCitizenStatusByCitizenToken(anyString(), anyString(), FISCAL_CODE_TOKENIZED);
        } catch (BeneficiaryGroupException e) {
            log.info("BeneficiaryGroupException: " + e.getCode());
            assertEquals(HttpStatus.NOT_FOUND, e.getHttpStatus());
            assertEquals(GroupConstants.Exception.NotFound.CODE, e.getCode());
            assertEquals(MessageFormat.format(GroupConstants.Exception.NotFound.NO_GROUP_FOR_INITIATIVE_ID, "Id1"), e.getMessage());

            // you are expecting repo to be called once with correct param
            verify(groupRepository).getBeneficiaryList(anyString(), anyString());
        }
    }

    private Group createGroupValid_ok(){
        Group group = new Group();
        group.setGroupId("A1_O1");
        group.setInitiativeId("A1");
        group.setOrganizationId("O1");
        group.setFileName("ps_fiscal_code_groups_file_large_20.csv");
        group.setStatus("VALIDATED");
        group.setExceptionMessage(null);
        group.setElabDateTime(LocalDateTime.now());
        group.setCreationDate(LocalDateTime.now());
        group.setUpdateDate(LocalDateTime.now());
        group.setCreationUser("admin");
        group.setUpdateUser("admin");
        group.setBeneficiaryList(null);
        return group;
    }
}
