package it.gov.pagopa.group.utils;

import org.apache.commons.lang3.RandomStringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class CFGenerator {

    private final static String CF_LIST = "cfList";
    private final static String CF_LIST_WRONG = "cfListWrong";
    private final static String PATH_DIRECTORY = "src/test/resources/group";
    private final static String NAME_DIRECTORY = "fiscal_code";
    private final static String EXT_FILE = ".csv";
    private final static String CF_TOWN_CODE = "X000";
    private final static String AVAILABLE_LETTERS = "ABCDEHLMPRST";

    public static Map<String, File> generateTempFile() {
        Path cfListPath, cfListWrongPath;
        File cfListFile, cfListWrongFile;
        Map<String, File> fileMap = new HashMap<>();
        try {
            Path path = Files.createTempDirectory(Paths.get(PATH_DIRECTORY), NAME_DIRECTORY);
            path.toFile().deleteOnExit();
            cfListPath = Files.createTempFile(path, CF_LIST, EXT_FILE);
            cfListWrongPath = Files.createTempFile(path, CF_LIST_WRONG, EXT_FILE);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }

        if (cfListPath != null) {
            cfListFile = cfListPath.toFile();
            cfListFile.deleteOnExit();
            List<String> cfList = CFGenerator.CFGeneratorList();

            try {
                FileWriter writer = new FileWriter(cfListFile);
                for (String cf : cfList) {
                    writer.write(cf + "\n");
                }
                fileMap.put(CF_LIST, cfListFile);
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage());
            }
        }

        if (cfListWrongPath != null) {
            cfListWrongFile = cfListWrongPath.toFile();
            cfListWrongFile.deleteOnExit();
            List<String> cfListWrong = CFGenerator.CFGeneratorList();

            try {
                FileWriter writer = new FileWriter(cfListWrongFile);
                for (int i = 0; i < cfListWrong.size(); i++) {
                    writer.write(cfListWrong.get(i) + "\n");
                    if (i==2) {
                        writer.write("\n");
                    }
                }
                fileMap.put(CF_LIST_WRONG, cfListWrongFile);
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage());
            }
        }

        return fileMap;
    }

    public static ArrayList<String> CFGeneratorList() {
        ArrayList<String> CFList = new ArrayList<>();
        Random rand = new Random();
        String availableLetters = AVAILABLE_LETTERS;

        for (int i = 0; i < 5; i++) {
            String cf = "";
            //nameSurname
            cf+= RandomStringUtils.randomAlphabetic(6);
            //year
            cf+= String.valueOf(rand.nextInt(90) + 10);
            //month
            cf+= String.valueOf(availableLetters.charAt(rand.nextInt(availableLetters.length())));
            //day
            cf+= String.valueOf(rand.nextInt(90) + 10);
            //town
            cf+= CF_TOWN_CODE;
            //cin
            cf+= RandomStringUtils.randomAlphabetic(1);
            CFList.add(cf.toUpperCase());
        }
        return CFList;
    }
}
