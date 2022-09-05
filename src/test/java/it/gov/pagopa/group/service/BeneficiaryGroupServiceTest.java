package it.gov.pagopa.group.service;

import it.gov.pagopa.group.connector.pdv.EncryptRestConnector;
import it.gov.pagopa.group.model.Group;
import it.gov.pagopa.group.repository.GroupRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;

import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@WebMvcTest(value = {
        BeneficiaryGroupService.class})
@Slf4j
@TestPropertySource(properties = {
        "storage.file.path=output/tmp/group",
        "storage.file.deletion=false"
})
public class BeneficiaryGroupServiceTest {

    @Autowired
    BeneficiaryGroupService beneficiaryGroupService;

    @MockBean
    GroupRepository groupRepository;

    @MockBean
    EncryptRestConnector encryptRestConnector;


    @Test
    void saveValidFile_ok() throws Exception{
        File file1 = new ClassPathResource("group" + File.separator + "ps_fiscal_code_groups_file_large_20.csv").getFile();
        FileInputStream inputFile = new FileInputStream( file1);
        MockMultipartFile file = new MockMultipartFile("file", file1.getName(), "text/csv", inputFile);
        Group group = createGroupValid_ok();

        beneficiaryGroupService.save(file, group.getInitiativeId(), group.getOrganizationId(), group.getStatus());

        verify(groupRepository, times(1)).save(any());
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
