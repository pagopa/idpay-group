package it.gov.pagopa.group.service;

import it.gov.pagopa.group.model.Group;
import it.gov.pagopa.group.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.stream.Stream;

@Service
public class BeneficiaryGroupServiceImpl implements BeneficiaryGroupService {
    private final Path root = Paths.get("C:\\Users\\fpinsone\\Documents\\IdPay\\prova");

    @Autowired
    private GroupRepository groupRepository;

    @Override
    public void init() {
        try {
            Files.createDirectory(root);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize folder for upload!");
        }
    }

    @Override
    public void save(MultipartFile file, String initiativeId, String organizationId) { //TODO aggiungere initiativeId e organizationId ai parametri
        try {
            Files.copy(file.getInputStream(), this.root.resolve(file.getOriginalFilename() + "_" + LocalDateTime.now()));
            Group group = new Group();
            group.setGroupId(initiativeId+organizationId);
            group.setInitiativeId(initiativeId);
            group.setOrganizationId(organizationId);
            group.setStatus("OK");
            group.setCreationDate(LocalDateTime.now());
            group.setUpdateDate(LocalDateTime.now());
            group.setCreationUser("admin");
            group.setUpdateUser("admin");
            group.setBeneficiaryList(null); //TODO metodo per popolare la lista a partire dal file multipart
            groupRepository.insert(group);
        } catch (Exception e) {
            throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
        }
    }

    @Override
    public Resource load(String filename) {
        try {
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
    public void delete(String filename){
        try{
            Path file = root.resolve(filename);
            file.toFile().delete();
        } catch (Exception e) {
            throw new RuntimeException("Could not delete the file!");
        }
    }
    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(root.toFile());
    }

    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.root, 1).filter(path -> !path.equals(this.root)).map(this.root::relativize);
        } catch (IOException e) {
            throw new RuntimeException("Could not load the files!");
        }
    }
}
