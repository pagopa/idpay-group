package it.gov.pagopa.group.service;

import it.gov.pagopa.group.connector.pdv.EncryptRestConnector;
import it.gov.pagopa.group.constants.GroupConstants;
import it.gov.pagopa.group.dto.FiscalCodeTokenizedDTO;
import it.gov.pagopa.group.dto.PiiDTO;
import it.gov.pagopa.group.exception.BeneficiaryGroupException;
import it.gov.pagopa.group.model.Group;
import it.gov.pagopa.group.repository.GroupRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@Slf4j
public class BeneficiaryGroupServiceImpl implements BeneficiaryGroupService {

    public static final String KEY_SEPARATOR = "_";
    @Value("${storage.file.path}")
    private String rootPath;
    @Value("${storage.file.deletion}")
    private boolean isFilesOnStorageToBeDeleted;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private EncryptRestConnector encryptRestConnector;


    private void init(String organizationId) {
        try {
            Path root = Paths.get(rootPath + File.separator + organizationId);
            Files.createDirectory(root);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize folder for upload!");
        }
    }

    @Scheduled(fixedRate = 4000, initialDelay = 4000)
    public void scheduleValidatedGroup() throws IOException {
        boolean anonymizationDone = false;
        Optional<Group> groupOptional = groupRepository.findFirstByStatus(GroupConstants.Status.VALIDATED); //FIXME change it in findAndUpdate with Mongo
        if(groupOptional.isPresent()) {
            Group group = groupOptional.get();
            group.setStatus(GroupConstants.Status.PROCESSING); //FIXME Da rimuovere. Vedere poco sopra
            groupRepository.save(group);
            String fileName = group.getFileName();
            log.info("[GROUP_SCHEDULING] [ANONYMIZER] Found beneficiary's group for {} with status {} on Organization {}", fileName, "VALIDATED", group.getOrganizationId());
            Resource file = load(group.getOrganizationId(), fileName);
            List<String> anonymousCFlist = null;
            try {
                anonymousCFlist = cfAnonymizer(file);
                group.setBeneficiaryList(anonymousCFlist);
                group.setStatus("OK");
                anonymizationDone = true;
            } catch (Exception e) {
                group.setExceptionMessage(e.getMessage());
                group.setElabDateTime(LocalDateTime.now());
                group.setRetry(1);
                group.setStatus(GroupConstants.Status.PROC_KO);
            }
            groupRepository.save(group);
            if(isFilesOnStorageToBeDeleted && anonymizationDone) {
                delete(group.getOrganizationId(), fileName);
            }
        }
    }

    @Scheduled(fixedRate = 10000, initialDelay = 8000)
    public void scheduleProcKoGroup() throws IOException {
        boolean anonymizationDone = false;
        Optional<Group> groupOptional = groupRepository.findFirstByStatusAndRetryLessThan(GroupConstants.Status.PROC_KO, 3);
        if(groupOptional.isPresent()) {
            Group group = groupOptional.get();
            String fileName = group.getFileName();
            log.info("[GROUP_SCHEDULING] [ANONYMIZER] Found beneficiary's group for {} with status {} on Organization {}", fileName, "PROC_KO", group.getOrganizationId());
            Resource file = load(group.getOrganizationId(), fileName);
            List<String> anonymousCFlist = null;
            try {
                log.info("Retry to communicate with PDV num: {}", group.getRetry()+1);
                anonymousCFlist = cfAnonymizer(file);
                group.setBeneficiaryList(anonymousCFlist);
                group.setStatus("OK");
                anonymizationDone = true;
            } catch (Exception e) {
                group.setExceptionMessage(e.getMessage());
                group.setElabDateTime(LocalDateTime.now());
                group.setRetry(group.getRetry()+1);
                group.setStatus("PROC_KO");
            }
            groupRepository.save(group);
            if(isFilesOnStorageToBeDeleted && (anonymizationDone || group.getRetry() >= 3)) {
                delete(group.getOrganizationId(), fileName);
            }
        }
    }

    public List<String> cfAnonymizer(Resource file) throws Exception {
        List<String> anonymousCFlist = new ArrayList<>();
        String line;
        InputStream is = file.getInputStream();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            long before=System.currentTimeMillis();
            int lineCounter = 0;
            while ((line = br.readLine()) != null) {
                lineCounter++;
                FiscalCodeTokenizedDTO fiscalCodeTokenizedDTO = encryptRestConnector.putPii(PiiDTO.builder().pii(line).build());
                anonymousCFlist.add(fiscalCodeTokenizedDTO.getToken());
            }
            long after=System.currentTimeMillis();
            log.info("[ANONYMIZER] Time to finish {} PDV calls: {} ms", lineCounter, after-before);
        } catch (Exception e) {
            log.error("[ANONYMIZER] - General exception: {}", e.getMessage());
            throw e;
        }
        return anonymousCFlist;
    }

    @Override
    public void save(MultipartFile file, String initiativeId, String organizationId, String status) {
        try {
            Path root = Paths.get(rootPath + File.separator + organizationId);
            Files.createDirectories(root);
            Files.copy(file.getInputStream(), root.resolve(file.getOriginalFilename()), StandardCopyOption.REPLACE_EXISTING);
            Group group = new Group();
            group.setGroupId(initiativeId + KEY_SEPARATOR + organizationId);
            group.setInitiativeId(initiativeId);
            group.setOrganizationId(organizationId);
            group.setStatus(status);
            group.setFileName(file.getOriginalFilename());
            group.setCreationDate(LocalDateTime.now());
            group.setUpdateDate(LocalDateTime.now());
            group.setCreationUser("admin"); //TODO recuperare info da apim
            group.setUpdateUser("admin"); //TODO recuperare info da apim
            group.setBeneficiaryList(null);
            groupRepository.save(group);
            Exception e = new Exception();
        } catch (Exception e) {
            throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
        }
    }
    @Override
    public Resource load(String organizationId, String filename) {
        try {
            Path root = Paths.get(rootPath + File.separator + organizationId);
            Resource resource = new UrlResource(root.toUri());
            if (!resource.exists()) {
                init(organizationId);
            }
            Path file = root.resolve(filename);
            resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()){
                return resource;
            }
            else
                throw new RuntimeException("Could not read the file!");
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    @Override
    public void delete(String organizationId, String filename) {
        try{
            Path root = Paths.get(rootPath + File.separator + organizationId);
            Path file = root.resolve(filename);
            Files.delete(file);
        } catch (Exception e) {
            log.error("[UPLOAD_FILE_GROUP] - Could not delete the file: " + filename, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Group getStatusByInitiativeId(String initiativeId, String organizationId) {

        return groupRepository.getStatus(initiativeId, organizationId)
                .orElseThrow(() -> new BeneficiaryGroupException(GroupConstants.Exception.NotFound.CODE,
                        MessageFormat.format(GroupConstants.Exception.BadRequest.NO_GROUP_FOR_INITIATIVE_ID, initiativeId),
                        HttpStatus.BAD_REQUEST));
    }

}
