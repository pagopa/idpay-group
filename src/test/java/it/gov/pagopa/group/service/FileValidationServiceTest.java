package it.gov.pagopa.group.service;

import it.gov.pagopa.group.repository.GroupRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;


import it.gov.pagopa.group.model.Group;
import it.gov.pagopa.group.service.BeneficiaryGroupService;
import it.gov.pagopa.group.service.FileValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;


@WebMvcTest(value = {
        FileValidationService.class})
@Slf4j
public class FileValidationServiceTest {

    @Autowired
    FileValidationService fileValidationService;

    @MockBean
    GroupRepository groupRepository;


    @Test
    void rowFileCounterCheck_validFile_ok() throws Exception{
        File file1 = new ClassPathResource("group" + File.separator + "ps_fiscal_code_groups_file_large_20.csv").getFile();
        FileInputStream inputFile = new FileInputStream( file1);
        MockMultipartFile file = new MockMultipartFile("file", file1.getName(), "text/csv", inputFile);

        assertEquals(fileValidationService.rowFileCounterCheck(file), 20);
    }
    @Test
    void rowFileCounterCheck_InvalidFile_ko() throws Exception{
        File file1 = new ClassPathResource("group" + File.separator + "ps_fiscal_code_groups_file_large_WrongCF.csv").getFile();
        FileInputStream inputFile = new FileInputStream( file1);
        MockMultipartFile file = new MockMultipartFile("file", file1.getName(), "text/csv", inputFile);

        assertEquals(fileValidationService.rowFileCounterCheck(file), -34);
    }
}
