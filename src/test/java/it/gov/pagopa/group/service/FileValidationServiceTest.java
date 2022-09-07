package it.gov.pagopa.group.service;

import it.gov.pagopa.group.repository.GroupRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.FileInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;


@WebMvcTest(value = {
        FileValidationService.class})
@Slf4j
public class FileValidationServiceTest {

    private final static int EXPECTED_CSV_FILE_MAX_ROW_COUNTER = 20;
    private final static int EXPECTED_CSV_FILE_INVALID_ROW_COUNTER = -34;

    @Autowired
    FileValidationService fileValidationService;

    @MockBean
    GroupRepository groupRepository;


    @Test
    void rowFileCounterCheck_validFile_ok() throws Exception{
        File file1 = new ClassPathResource("group" + File.separator + "ps_fiscal_code_groups_file_large_20.csv").getFile();
        FileInputStream inputFile = new FileInputStream( file1);
        MockMultipartFile file = new MockMultipartFile("file", file1.getName(), "text/csv", inputFile);

        assertEquals(EXPECTED_CSV_FILE_MAX_ROW_COUNTER, fileValidationService.rowFileCounterCheck(file));
    }
    @Test
    void rowFileCounterCheck_InvalidFile_ko() throws Exception{
        File file1 = new ClassPathResource("group" + File.separator + "ps_fiscal_code_groups_file_large_WrongCF.csv").getFile();
        FileInputStream inputFile = new FileInputStream( file1);
        MockMultipartFile file = new MockMultipartFile("file", file1.getName(), "text/csv", inputFile);

        assertEquals(EXPECTED_CSV_FILE_INVALID_ROW_COUNTER, fileValidationService.rowFileCounterCheck(file));
    }
}
