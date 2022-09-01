package it.gov.pagopa.group.service;

import it.gov.pagopa.group.model.Group;
import it.gov.pagopa.group.repository.GroupRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;

@WebMvcTest(value = {
        BeneficiaryGroupService.class})
@Slf4j
public class BeneficiaryGroupServiceTest {

    @MockBean
    GroupRepository groupRepository;

    @MockBean
    BeneficiaryGroupService beneficiaryGroupService;







    private Group createGroupValid_ok(){
        Group group = new Group();
        group.setGroupId("A1_O1");
        group.setInitiativeId("A1");
        group.setOrganizationId("O1");
        group.setFileName("test.csv");
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
