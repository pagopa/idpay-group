package it.gov.pagopa.group.service;

import it.gov.pagopa.group.model.Group;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public interface BeneficiaryGroupService {

    void init();

    void save(MultipartFile file, String initiativeId, String organizationId);

    Resource load(String filename);

    void delete(String filename);

    void deleteAll();

    Stream<Path> loadAll();
}
