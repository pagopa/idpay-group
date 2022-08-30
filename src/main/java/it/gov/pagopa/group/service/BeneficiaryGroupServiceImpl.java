package it.gov.pagopa.group.service;

import it.gov.pagopa.group.model.Group;
import it.gov.pagopa.group.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

@Service
public class BeneficiaryGroupServiceImpl implements BeneficiaryGroupService {

    @Value("${storage.file.path}")
    private String rootPath;

    @Autowired
    private GroupRepository groupRepository;

    @Override
    public void init() {
        try {
            Path root = Paths.get(rootPath);
            Files.createDirectory(root);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize folder for upload!");
        }
    }

    @Scheduled(fixedRate = 2000, initialDelay = 4000)
    public void scheduleGroupCheck() throws IOException {
        List<Group> groups = groupRepository.findGroupsByStatus("VALIDATED");
        for (Group group : groups){
            String fileName = group.getFileName();
            Resource file = load(fileName);
            List<String> anonymousCFlist = null;
            try {
                anonymousCFlist = cfAnonymizer(file);
            } catch (Exception e) {

            }
            group.setBeneficiaryList(anonymousCFlist);
            group.setStatus("OK");
            groupRepository.save(group);
            delete(fileName);
        }
    }

    public List<String> cfAnonymizer(Resource file){
        return null;
    }

    @Override
    public void save(MultipartFile file, String initiativeId, String organizationId, String status) {
        try {
            Path root = Paths.get(rootPath);
            Files.copy(file.getInputStream(), root.resolve(file.getOriginalFilename()));
            Group group = new Group();
            group.setGroupId(initiativeId+organizationId);
            group.setInitiativeId(initiativeId);
            group.setOrganizationId(organizationId);
            group.setStatus(status);
            group.setFileName(file.getOriginalFilename());
            group.setCreationDate(LocalDateTime.now());
            group.setUpdateDate(LocalDateTime.now());
            group.setCreationUser("admin"); //TODO recuperare info da apim
            group.setUpdateUser("admin"); //TODO recuperare info da apim
            group.setBeneficiaryList(null); //TODO Aggiungere anonimizzazione
            groupRepository.insert(group);
        } catch (Exception e) {
            throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
        }
    }

    @Override
    public Resource load(String filename) {
        try {
            Path root = Paths.get(rootPath);
            Path file = root.resolve(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read the file!");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    @Override
    public Stream<Path> loadAll() {
        try {
            Path root = Paths.get(rootPath);
            return Files.walk(root, 1).filter(path -> !path.equals(root)).map(root::relativize);
        } catch (IOException e) {
            throw new RuntimeException("Could not load the files!");
        }
    }

    @Override
    public void delete(String filename){
        try{
            Path root = Paths.get(rootPath);
            Path file = root.resolve(filename);
            file.toFile().delete();
        } catch (Exception e) {
            throw new RuntimeException("Could not delete the file!");
        }
    }
    @Override
    public void deleteAll() {
        Path root = Paths.get(rootPath);
        FileSystemUtils.deleteRecursively(root.toFile());
    }

    @Override
    public Group getStatusByInitiativeId(String initiativeId){
        return groupRepository.getStatus(initiativeId);
    }
}
