package it.gov.pagopa.group.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.group.connector.InitiativeService;
import it.gov.pagopa.group.dto.InitiativeDTO;
import it.gov.pagopa.group.dto.InitiativeGeneralDTO;
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
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
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

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${storage.file.path}")
    private String rootPath;

    private static final String ORGANIZATION_ID_PLACEHOLDER = "{0}";
    private static final String INITIATIVE_ID_PLACEHOLDER = "{1}";
    private static final String BASE_URL = "http://localhost:8080/idpay";
    private static final String GET_GROUP_STATUS_URL = "/organization/" + ORGANIZATION_ID_PLACEHOLDER + "/initiative/" + INITIATIVE_ID_PLACEHOLDER + "/status";

    private static final String PUT_GROUP_FILE = "/organization/" + ORGANIZATION_ID_PLACEHOLDER + "/initiative/" + INITIATIVE_ID_PLACEHOLDER + "/upload";
    @Test
    void getGroupStatus_ok() throws Exception{
        Group group = createGroupValid_ok();

        when(beneficiaryGroupService.getStatusByInitiativeId(anyString(), anyString())).thenReturn(group);

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

        when(beneficiaryGroupService.getStatusByInitiativeId(anyString(),anyString())).thenReturn(group);

        mvc.perform(
                        MockMvcRequestBuilders.get(BASE_URL + MessageFormat.format(GET_GROUP_STATUS_URL, "A1", "A1"))
                                .contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(print())
                .andReturn();

    }

    @Test
    void uploadBeneficiaryGroupFile_ok() throws Exception{
        Group group = createGroupValid_ok();

        FileInputStream inputFile = new FileInputStream( "C:/Users/fpinsone/Documents/IdPay/file.csv");
        MockMultipartFile file = new MockMultipartFile("file", "file.csv", "text/csv", inputFile);
//
//
//        byte[] byteFile = inputFile.readAllBytes();
//        //when(beneficiaryGroupService.save(file, group.getOrganizationId(), group.getInitiativeId(), group.getStatus())).thenReturn(group);
//
////        mvc.perform(MockMvcRequestBuilders.put(BASE_URL + MessageFormat.format(PUT_GROUP_FILE, group.getOrganizationId(), group.getInitiativeId()))
////                        .contentType(MediaType.MULTIPART_FORM_DATA)
////                                .accept(MediaType.MULTIPART_FORM_DATA))
////                        .andExpect(MockMvcResultMatchers.status().isOk())
////                                .andDo(print())
////                                        .andReturn();
//
////        mvc.perform(MockMvcRequestBuilders.multipart(BASE_URL + MessageFormat.format(PUT_GROUP_FILE, group.getOrganizationId(), group.getInitiativeId()))
////                        .file(file)
////                        .param("some-random", "4"))
////                .andExpect(MockMvcResultMatchers.status().isOk())
////                .andDo(print())
////                .andReturn();
//
////        mvc.perform(MockMvcRequestBuilders.multipart(BASE_URL + MessageFormat.format(PUT_GROUP_FILE, group.getOrganizationId(), group.getInitiativeId())).file(file))
////                .andExpect(MockMvcResultMatchers.status().isOk());
//
//        mvc.perform(MockMvcRequestBuilders.put(BASE_URL + MessageFormat.format(PUT_GROUP_FILE, group.getOrganizationId(), group.getInitiativeId()))
//                .contentType(MediaType.MULTIPART_FORM_DATA)
//                .content(objectMapper.writeValueAsBytes(byteFile))
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(MockMvcResultMatchers.status().isOk())
//                .andDo(print())
//                .andReturn();

//        MockMultipartFile file = new MockMultipartFile("data", "dummy.csv",
//                "text/plain", "Some dataset...".getBytes());

        when(initiativeService.getInitiative(group.getInitiativeId())).thenReturn(createInitiativeDTO(group.getOrganizationId(), group.getInitiativeId()));

        MockMultipartHttpServletRequestBuilder builder =
                MockMvcRequestBuilders.multipart((BASE_URL + MessageFormat.format(PUT_GROUP_FILE, group.getOrganizationId(), group.getInitiativeId())));
        builder.with(new RequestPostProcessor() {
            @Override
            public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
                request.setMethod("PUT");
                return request;
            }
        });
        mvc.perform(builder
                        .file(file))
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

    private InitiativeDTO createInitiativeDTO(String organizationId, String initiativeId){
        InitiativeDTO initiativeDTO = new InitiativeDTO();
        InitiativeGeneralDTO initiativeGeneralDTO = new InitiativeGeneralDTO(BigDecimal.valueOf(90000), BigDecimal.valueOf(900));
        initiativeDTO.setInitiativeId(initiativeId);
        initiativeDTO.setOrganizationId(organizationId);
        initiativeDTO.setInitiativeName("initiativa1");
        initiativeDTO.setGeneral(initiativeGeneralDTO);
        return initiativeDTO;
    }
}
