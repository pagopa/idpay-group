package it.gov.pagopa.group.controller;

import it.gov.pagopa.group.connector.InitiativeService;
import it.gov.pagopa.group.model.Group;
import it.gov.pagopa.group.service.BeneficiaryGroupService;
import it.gov.pagopa.group.service.FileValidationService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@WebMvcTest(value = {
        BeneficiaryGroup.class}, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@Slf4j
public class BeneficiaryGroupApiTest {
    @MockBean
    private BeneficiaryGroupService beneficiaryGroupService;

    @MockBean
    private InitiativeService initiativeService;

    @MockBean
    private FileValidationService fileValidationService;

    @Autowired
    protected MockMvc mvc;

    @Value("${storage.file.path}")
    private String rootPath;

    private static final String ORGANIZATION_ID_PLACEHOLDER = "{0}";
    private static final String INITIATIVE_ID_PLACEHOLDER = "{0}";
    private static final String BASE_URL = "http://localhost:8080/idpay";
    private static final String GET_GROUP_STATUS_URL = "/organization/" + ORGANIZATION_ID_PLACEHOLDER + "/initiative/" + INITIATIVE_ID_PLACEHOLDER + "/group";

    @Test
    void getGroupStatus_ok() throws Exception{
        Group group = createGroupValid_ok();

        when(beneficiaryGroupService.getStatusByInitiativeId(anyString())).thenReturn(group);

        mvc.perform(
                MockMvcRequestBuilders.get(BASE_URL + MessageFormat.format(GET_GROUP_STATUS_URL, group.getOrganizationId(), group.getInitiativeId()))
                        .contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andDo(print())
                        .andReturn();
    }
    @Test
    void getGroupStatus_groupNull_ko() throws Exception{
        Group group = null;

        when(beneficiaryGroupService.getStatusByInitiativeId(anyString())).thenReturn(group);

        mvc.perform(
                        MockMvcRequestBuilders.get(BASE_URL + MessageFormat.format(GET_GROUP_STATUS_URL, "A1", "A1"))
                                .contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andDo(print())
                .andReturn();
    }

    @Test
    void uploadBeneficiaryGroupFile_ok() throws Exception{
        Group group = createGroupValid_ok();

        MultipartFile multipartFile = new MultipartFile() {
            @Override
            public String getName() {
                return "file";
            }

            @Override
            public String getOriginalFilename() {
                return "file.csv";
            }

            @Override
            public String getContentType() {
                return null;
            }

            @Override
            public boolean isEmpty() {
                return true;
            }

            @Override
            public long getSize() {
                return 0;
            }

            @Override
            public byte[] getBytes() throws IOException {
                return new byte[0];
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return null;
            }

            @Override
            public void transferTo(File dest) throws IOException, IllegalStateException {

            }
        };


        doNothing().when(beneficiaryGroupService).save(multipartFile, group.getOrganizationId(), group.getInitiativeId(), group.getStatus());

        mvc.perform(MockMvcRequestBuilders.put(BASE_URL + MessageFormat.format(GET_GROUP_STATUS_URL, group.getOrganizationId(), group.getInitiativeId()))
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.MULTIPART_FORM_DATA))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                                .andDo(print())
                                        .andReturn();
    }


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

    private Group createGroupNotValid_ko(){
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
