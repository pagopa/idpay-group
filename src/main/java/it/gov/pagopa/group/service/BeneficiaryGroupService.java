package it.gov.pagopa.group.service;

import it.gov.pagopa.group.model.Group;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.stream.Stream;

@Service
public interface BeneficiaryGroupService {

    void save(MultipartFile file, String initiativeId, String organizationId, String status);

    Resource load(String organizationId, String filename);

    void delete(String organizationId, String filename);



    Group getStatusByInitiativeId(String initiativeId, String organizationId);
}
