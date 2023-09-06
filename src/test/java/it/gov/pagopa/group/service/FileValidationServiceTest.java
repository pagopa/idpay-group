package it.gov.pagopa.group.service;

import it.gov.pagopa.group.repository.GroupRepository;
import it.gov.pagopa.group.utils.CFGenerator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;

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

    private final static int EXPECTED_CSV_FILE_MAX_ROW_COUNTER = 5;
    private final static int EXPECTED_CSV_FILE_INVALID_ROW_COUNTER = -4;

    private static final String FISCAL_CODE_TOKENIZED = "FISCAL_CODE_TOKENIZED";

    // Some fixed date to make your tests
    private final static LocalDate LOCAL_DATE = LocalDate.of(2022, 1, 1);

    private static File cfListFile, cfListWrongFile;
    private final static String CF_LIST = "cfList";
    private final static String CF_LIST_WRONG = "cfListWrong";

    @BeforeAll
    static void initTempFile() {
        Map<String, File> fileMap = CFGenerator.generateTempFile();
        cfListFile = fileMap.get(CF_LIST);
        cfListWrongFile = fileMap.get(CF_LIST_WRONG);
    }

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
        FileInputStream inputFile = new FileInputStream(cfListFile);
        MockMultipartFile file = new MockMultipartFile("file",
                cfListFile.getName().replaceAll("\\d", ""),
                "text/csv",
                inputFile);
        assertTrue(fileValidationService.rowFileCounterCheck(file)>0);
        assertEquals(EXPECTED_CSV_FILE_MAX_ROW_COUNTER, fileValidationService.rowFileCounterCheck(file));
    }
    @Test
    void rowFileCounterCheck_whenFileIsNotValid() throws Exception{
        FileInputStream inputFile = new FileInputStream(cfListWrongFile);
        MockMultipartFile file = new MockMultipartFile("file",
                cfListWrongFile.getName().replaceAll("\\d", ""),
                "text/csv",
                inputFile);
        assertTrue(fileValidationService.rowFileCounterCheck(file)<0);
        assertEquals(EXPECTED_CSV_FILE_INVALID_ROW_COUNTER, fileValidationService.rowFileCounterCheck(file));
    }
}
