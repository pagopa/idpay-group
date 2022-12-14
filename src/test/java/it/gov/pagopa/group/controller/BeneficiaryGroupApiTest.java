package it.gov.pagopa.group.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.group.connector.InitiativeService;
import it.gov.pagopa.group.constants.GroupConstants;
import it.gov.pagopa.group.dto.GroupUpdateDTO;
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
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Time;
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
    void uploadBeneficiaryGroupFile_ok() throws Exception{
        Group group = createGroupValid_ok();
        File file1 = new ClassPathResource("group" + File.separator + "ps_fiscal_code_groups_file_large_20.csv").getFile();
        FileInputStream inputFile = new FileInputStream( file1);
        MockMultipartFile file = new MockMultipartFile("file", file1.getName(), "text/csv", inputFile);

        when(initiativeService.getInitiative(group.getInitiativeId())).thenReturn(createInitiativeDTO(group.getOrganizationId(), group.getInitiativeId()));
        when(fileValidationService.rowFileCounterCheck(file)).thenReturn(20);
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

    @Test
    void uploadInvaliFormatFile_ko() throws Exception {
        GroupUpdateDTO groupUpdateDTO = createGroupUpdateDTONotValidFormatFile_ko();

        File file1 = new ClassPathResource("group" + File.separator + "ps_fiscal_code_groups_file_large_WrongCF.csv").getFile();

        //FIXME
        FileInputStream inputFile = new FileInputStream( file1);
        MockMultipartFile file = new MockMultipartFile("file", "file.json", "json", inputFile);

        when(initiativeService.getInitiative("A1")).thenReturn(createInitiativeDTO("O1", "A1"));

        MockMultipartHttpServletRequestBuilder builder =
                MockMvcRequestBuilders.multipart((BASE_URL + MessageFormat.format(PUT_GROUP_FILE, "O1", "A1")));
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

    @Test
    void uploadEmptyFile_ko() throws Exception{
        GroupUpdateDTO groupUpdateDTO = createGroupUpdateDTONotValidFormatFile_ko();

        File file1 = new ClassPathResource("group" + File.separator + "empty_file.csv").getFile();

        ;
        //FIXME
        FileInputStream inputFile = new FileInputStream( file1);
        MockMultipartFile file = new MockMultipartFile("file", "file.csv", "text/csv", inputFile);

        when(initiativeService.getInitiative("A1")).thenReturn(createInitiativeDTO("O1", "A1"));

        MockMultipartHttpServletRequestBuilder builder =
                MockMvcRequestBuilders.multipart((BASE_URL + MessageFormat.format(PUT_GROUP_FILE, "O1", "A1")));
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


    @Test
    void uploadFileCfTooLarge_ko() throws Exception{
        Group group = createGroupValid_ok();

        InitiativeDTO initiativeDTO = createInitiativeDTOLowBudget(group.getOrganizationId(), group.getInitiativeId());

        //FIXME
        File file1 = new ClassPathResource("group" + File.separator + "ps_fiscal_code_groups_file_large_WrongCF.csv").getFile();

        FileInputStream inputFile = new FileInputStream( file1);
        MockMultipartFile file = new MockMultipartFile("file", "file.csv", "text/csv", inputFile);

        when(initiativeService.getInitiative(initiativeDTO.getInitiativeId())).thenReturn(initiativeDTO);
        when(fileValidationService.rowFileCounterCheck(file)).thenReturn(-34);

        MockMultipartHttpServletRequestBuilder builder =
                MockMvcRequestBuilders.multipart((BASE_URL + MessageFormat.format(PUT_GROUP_FILE, "O1", "A1")));
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

    @Test
    void uploadInvalidBeneficiaryNumberCfFile_ko() throws Exception {
        Group group = createGroupValid_ok();

        InitiativeDTO initiativeDTO = createInitiativeDTOLowBudget(group.getOrganizationId(), group.getInitiativeId());

        //FIXME
        File file1 = new ClassPathResource("group" + File.separator + "ps_fiscal_code_groups_file_large_20.csv").getFile();

        FileInputStream inputFile = new FileInputStream( file1);
        MockMultipartFile file = new MockMultipartFile("file", "file.csv", "text/csv", inputFile);

        when(initiativeService.getInitiative(initiativeDTO.getInitiativeId())).thenReturn(initiativeDTO);
        when(fileValidationService.rowFileCounterCheck(file)).thenReturn(20);

        MockMultipartHttpServletRequestBuilder builder =
                MockMvcRequestBuilders.multipart((BASE_URL + MessageFormat.format(PUT_GROUP_FILE, "O1", "A1")));
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

    @Test
    void uploadFileExceptionFailed_ko() throws Exception{

        Group group = createGroupValid_ok();
        File file1 = new ClassPathResource("group" + File.separator + "ps_fiscal_code_groups_file_large_20.csv").getFile();
        FileInputStream inputFile = new FileInputStream(file1);
        MockMultipartFile file = new MockMultipartFile("file", anyString(), "text/csv", inputFile);

        when(initiativeService.getInitiative(group.getInitiativeId())).thenReturn(createInitiativeDTO(group.getOrganizationId(), group.getInitiativeId()));
        when(fileValidationService.rowFileCounterCheck(file)).thenThrow(new IOException());
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
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
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

    private GroupUpdateDTO createGroupUpdateDTONotValidFormatFile_ko(){
        GroupUpdateDTO group = new GroupUpdateDTO();
        group.setStatus("KO");
        group.setErrorKey(GroupConstants.Status.KOkeyMessage.INVALID_FILE_FORMAT);
        group.setElabTimeStamp(LocalDateTime.now());
        return group;
    }

    private GroupUpdateDTO createGroupUpdateDTONotValidEmptyFile_ko(){
        GroupUpdateDTO group = new GroupUpdateDTO();
        group.setStatus("KO");
        group.setErrorKey(GroupConstants.Status.KOkeyMessage.INVALID_FILE_EMPTY);
        group.setElabTimeStamp(LocalDateTime.now());
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
    private InitiativeDTO createInitiativeDTOLowBudget(String organizationId, String initiativeId){
        InitiativeDTO initiativeDTO = new InitiativeDTO();
        InitiativeGeneralDTO initiativeGeneralDTO = new InitiativeGeneralDTO(BigDecimal.valueOf(60), BigDecimal.valueOf(30));
        initiativeDTO.setInitiativeId(initiativeId);
        initiativeDTO.setOrganizationId(organizationId);
        initiativeDTO.setInitiativeName("initiativa1");
        initiativeDTO.setGeneral(initiativeGeneralDTO);
        return initiativeDTO;
    }
}
