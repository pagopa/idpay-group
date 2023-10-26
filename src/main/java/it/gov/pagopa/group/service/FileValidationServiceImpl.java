package it.gov.pagopa.group.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Service
public class FileValidationServiceImpl implements FileValidationService {

    public static final String FISCAL_CODE_REGEX = "^([A-Za-z]{6}[0-9lmnpqrstuvLMNPQRSTUV]{2}[abcdehlmprstABCDEHLMPRST][0-9lmnpqrstuvLMNPQRSTUV]{2}[A-Za-z][0-9lmnpqrstuvLMNPQRSTUV]{3}[A-Za-z])$";

    @Override
    public int rowFileCounterCheck(MultipartFile file) throws IOException {
        BufferedReader br;
        int counter = 0;
        String line;
        InputStream is = file.getInputStream();
        br = new BufferedReader(new InputStreamReader(is));
        while ((line = br.readLine()) != null) {
            counter++;
            if (!checkCf(line)) {
                return -counter;
            }
        }
        return counter;
    }

    private boolean checkCf(String cf){
        return cf.matches(FISCAL_CODE_REGEX);

    }
}
