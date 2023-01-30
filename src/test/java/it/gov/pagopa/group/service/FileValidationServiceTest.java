package it.gov.pagopa.group.service;

import it.gov.pagopa.group.repository.GroupRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;


@WebMvcTest(value = {
        FileValidationService.class})
@Slf4j
class FileValidationServiceTest {

    @Autowired
    FileValidationService fileValidationService;

    @MockBean
    GroupRepository groupRepository;
    //Mock your clock bean
    @MockBean
    private Clock clock;

    private final static int EXPECTED_CSV_FILE_MAX_ROW_COUNTER = 20;
    private final static int EXPECTED_CSV_FILE_INVALID_ROW_COUNTER = -34;

    private static final String FISCAL_CODE_TOKENIZED = "FISCAL_CODE_TOKENIZED";

    // Some fixed date to make your tests
    private final static LocalDate LOCAL_DATE = LocalDate.of(2022, 1, 1);

    @BeforeEach
    void initMocks() {
        //tell your tests to return the specified LOCAL_DATE when calling LocalDate.now(clock)
        //field that will contain the fixed clock
        Clock fixedClock = Clock.fixed(LOCAL_DATE.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
    }


    @Test
    void rowFileCounterCheck_whenFileIsValid() throws Exception{
        File file1 = new ClassPathResource("group" + File.separator + "ps_fiscal_code_groups_file_large_20.csv").getFile();
        FileInputStream inputFile = new FileInputStream( file1);
        MockMultipartFile file = new MockMultipartFile("file", file1.getName(), "text/csv", inputFile);
        assertTrue(fileValidationService.rowFileCounterCheck(file)>0);
        assertEquals(EXPECTED_CSV_FILE_MAX_ROW_COUNTER, fileValidationService.rowFileCounterCheck(file));
    }
    @Test
    void rowFileCounterCheck_whenFileIsNotValid() throws Exception{
        File file1 = new ClassPathResource("group" + File.separator + "ps_fiscal_code_groups_file_large_WrongCF.csv").getFile();
        FileInputStream inputFile = new FileInputStream( file1);
        MockMultipartFile file = new MockMultipartFile("file", file1.getName(), "text/csv", inputFile);
        assertTrue(fileValidationService.rowFileCounterCheck(file)<0);
        assertEquals(EXPECTED_CSV_FILE_INVALID_ROW_COUNTER, fileValidationService.rowFileCounterCheck(file));
    }
}
