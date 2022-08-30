package it.gov.pagopa.group.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileValidationService {

    int rowFileCounterCheck(MultipartFile file) throws IOException;
}
