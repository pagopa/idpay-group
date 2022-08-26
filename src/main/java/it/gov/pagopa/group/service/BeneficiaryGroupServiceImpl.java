package it.gov.pagopa.group.service;

import it.gov.pagopa.group.model.Group;
import it.gov.pagopa.group.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

    @Scheduled(fixedRate = 2000, initialDelay = 4000)
    public void scheduleGroupCheck() throws IOException {
        List<Group> groups = groupRepository.findAll();
        for (int i = 0; i < groups.size(); i++){
            if (groups.get(i).getStatus().equals("VALIDATED")){
                Resource file = load(groups.get(i).getFileName());
                cfAnonymizer(file);
                groups.get(i).setBeneficiaryList(createCfStringList(file));
                groupRepository.save(groups.get(i));
            }
        }

    }

    public void cfAnonymizer(Resource file){

    }

    @Override
    public void save(MultipartFile file, String initiativeId, String organizationId) {
        try {
            Files.copy(file.getInputStream(), this.root.resolve(file.getOriginalFilename()));
            Group group = new Group();
            group.setGroupId(initiativeId+organizationId);
            group.setInitiativeId(initiativeId);
            group.setOrganizationId(organizationId);
            group.setStatus("VALIDATED");
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

    private List<String> createCfStringList(Resource file) throws IOException {
        BufferedReader br;
        List<String> list = new ArrayList<String>();
        String line;
        InputStream is = file.getInputStream();
        br = new BufferedReader(new InputStreamReader(is));
        while ((line = br.readLine()) != null) {
            list.add(line);
        }
        return list;
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
    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.root, 1).filter(path -> !path.equals(this.root)).map(this.root::relativize);
        } catch (IOException e) {
            throw new RuntimeException("Could not load the files!");
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
}
