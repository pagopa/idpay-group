package it.gov.pagopa.group.service;

import it.gov.pagopa.group.connector.notification.NotificationConnector;
import it.gov.pagopa.group.connector.pdv.PdvEncryptRestConnector;
import it.gov.pagopa.group.constants.GroupConstants;
import it.gov.pagopa.group.dto.FiscalCodeTokenizedDTO;
import it.gov.pagopa.group.dto.PiiDTO;
import it.gov.pagopa.group.exception.BeneficiaryGroupException;
import it.gov.pagopa.group.model.Group;
import it.gov.pagopa.group.model.GroupUserWhitelist;
import it.gov.pagopa.group.repository.GroupQueryDAO;
import it.gov.pagopa.group.repository.GroupRepository;
import it.gov.pagopa.group.repository.GroupUserWhitelistRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class BeneficiaryGroupServiceImpl implements BeneficiaryGroupService {

  public static final String KEY_SEPARATOR = "_";

  private final String rootPath;
  private final boolean isFilesOnStorageToBeDeleted;
  private final GroupRepository groupRepository;
  private final GroupUserWhitelistRepository groupUserWhitelistRepository;
  private final PdvEncryptRestConnector pdvEncryptRestConnector;
  private final GroupQueryDAO groupQueryDAO;
  private final Clock clock;
  private final NotificationConnector notificationConnector;
  private final int whitelistMaxSize;

  public BeneficiaryGroupServiceImpl(
      @Value("${file.storage.path}") String rootPath,
      @Value("${file.storage.deletion}") boolean isFilesOnStorageToBeDeleted,
      @Value("${file.whitelist.document.max-size}") int whitelistMaxSize,
      GroupRepository groupRepository,
      GroupUserWhitelistRepository groupUserWhitelistRepository,
      PdvEncryptRestConnector pdvEncryptRestConnector,
      GroupQueryDAO groupQueryDAO,
      Clock clock,
      NotificationConnector notificationConnector) {
    this.rootPath = rootPath;
    this.isFilesOnStorageToBeDeleted = isFilesOnStorageToBeDeleted;
    this.groupRepository = groupRepository;
    this.groupUserWhitelistRepository = groupUserWhitelistRepository;
    this.pdvEncryptRestConnector = pdvEncryptRestConnector;
    this.groupQueryDAO = groupQueryDAO;
    this.clock = clock;
    this.notificationConnector = notificationConnector;
    this.whitelistMaxSize = whitelistMaxSize;
  }


  private void init(String organizationId) {
    try {
      Path root = Paths.get(rootPath + File.separator + organizationId);
      Files.createDirectory(root);
    } catch (IOException e) {
      throw new RuntimeException("Could not initialize folder for upload!");
    }
  }

  @Scheduled(fixedRate = 10000, initialDelay = 130000)
  public void scheduleValidatedGroup() throws IOException {
    boolean anonymizationDone = false;
    Group group = groupQueryDAO.findFirstByStatusAndUpdate(GroupConstants.Status.VALIDATED);
//TODO check for null?       if(groupOptional.isPresent()) {
    if (null != group) {
      String fileName = group.getFileName();
      log.info(
          "[GROUP_SCHEDULING] [ANONYMIZER] Found beneficiary's group for {} with status {} on Organization {}",
          fileName, GroupConstants.Status.VALIDATED, group.getOrganizationId());
      Resource file = load(group.getOrganizationId(), fileName);
      List<String> anonymousCFlist = null;
      try {
        anonymousCFlist = cfAnonymizer(file);
        pushBeneficiaryListToDb(anonymousCFlist, group.getGroupId(), group.getInitiativeId());
        groupQueryDAO.setStatusOk(group.getInitiativeId(), anonymousCFlist.size());
        anonymizationDone = true;
      } catch (Exception e) {
        groupQueryDAO.setGroupForException(group.getInitiativeId(), e.getMessage(),
            LocalDateTime.now(clock), 1);
        groupQueryDAO.removeWhitelistByGroupId(group.getGroupId());
      }
      if (isFilesOnStorageToBeDeleted && anonymizationDone) {
        delete(group.getOrganizationId(), fileName);
      }
    }
  }

  @Scheduled(fixedRate = 15000, initialDelay = 140000)
  public void scheduleProcKoGroup() throws IOException {
    boolean anonymizationDone = false;
    Optional<Group> groupOptional = groupRepository.findFirstByStatusAndRetryLessThan(
        GroupConstants.Status.PROC_KO, 3);
    if (groupOptional.isPresent()) {
      Group group = groupOptional.get();
      String fileName = group.getFileName();
      log.info(
          "[GROUP_SCHEDULING] [ANONYMIZER] Found beneficiary's group for {} with status {} on Organization {}",
          fileName, GroupConstants.Status.PROC_KO, group.getOrganizationId());
      Resource file = load(group.getOrganizationId(), fileName);
      List<String> anonymousCFlist = null;
      try {
        log.info("Retry to communicate with PDV num: {}", group.getRetry() + 1);
        anonymousCFlist = cfAnonymizer(file);
        pushBeneficiaryListToDb(anonymousCFlist, group.getGroupId(), group.getInitiativeId());
        groupQueryDAO.setStatusOk(group.getInitiativeId(), anonymousCFlist.size());
        anonymizationDone = true;
      } catch (Exception e) {
        groupQueryDAO.setGroupForException(group.getInitiativeId(), e.getMessage(),
            LocalDateTime.now(clock), group.getRetry() + 1);
        groupQueryDAO.removeWhitelistByGroupId(group.getGroupId());
      }
      if (isFilesOnStorageToBeDeleted && (anonymizationDone || group.getRetry() >= 3)) {
        delete(group.getOrganizationId(), fileName);
      }
    }
  }

  private void pushBeneficiaryListToDb(List<String> anonymousCFList, String groupId,
      String initiativeId) {

    long start = System.currentTimeMillis();
    int size = anonymousCFList.size();

    int chunkNumber = size / whitelistMaxSize;
    int lastChunkSize = size % whitelistMaxSize;

    List<GroupUserWhitelist> whitelist = new ArrayList<>();
    log.info(
        "[GROUP_SCHEDULING] [ANONYMIZER] Pushing beneficiary list to database [rows: {}]", size);

    for (int i = 0; i < chunkNumber; i++) {
      GroupUserWhitelist groupUserWhitelist = new GroupUserWhitelist(null, groupId, initiativeId,
          anonymousCFList.subList(i * whitelistMaxSize, (i + 1) * whitelistMaxSize));
      whitelist.add(groupUserWhitelist);
    }

    if(lastChunkSize != 0){
      GroupUserWhitelist groupUserWhitelist = new GroupUserWhitelist(null, groupId, initiativeId,
          anonymousCFList.subList(chunkNumber * whitelistMaxSize, lastChunkSize));
      whitelist.add(groupUserWhitelist);
    }

    groupQueryDAO.pushBeneficiaryList(whitelist);

    long end = System.currentTimeMillis();

    log.info("[GROUP_SCHEDULING] [ANONYMIZER] Time spent pushing {} rows to db: {}", size,
        end - start);
  }

  private List<String> cfAnonymizer(Resource file)
      throws Exception {
    List<String> anonymousCFlist = new ArrayList<>();
    String line;
    InputStream is = file.getInputStream();
    try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
      long before = System.currentTimeMillis();
      int lineCounter = 0;
      while ((line = br.readLine()) != null) {
        lineCounter++;
        FiscalCodeTokenizedDTO fiscalCodeTokenizedDTO = pdvEncryptRestConnector.putPii(
            PiiDTO.builder().pii(line.toUpperCase()).build());
        anonymousCFlist.add(fiscalCodeTokenizedDTO.getToken());
      }
      long after = System.currentTimeMillis();
      log.info("[ANONYMIZER] Time to finish {} PDV calls: {} ms", lineCounter, after - before);
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
      String groupId = initiativeId + KEY_SEPARATOR + organizationId;
      Files.createDirectories(root);
      Files.copy(file.getInputStream(), root.resolve(file.getOriginalFilename()),
          StandardCopyOption.REPLACE_EXISTING);
      Group group = new Group();
      group.setGroupId(groupId);
      group.setInitiativeId(initiativeId);
      group.setOrganizationId(organizationId);
      group.setStatus(status);
      group.setFileName(file.getOriginalFilename());
      group.setCreationDate(LocalDateTime.now(clock));
      group.setUpdateDate(LocalDateTime.now(clock));
      group.setCreationUser("admin"); //TODO recuperare info da apim
      group.setUpdateUser("admin"); //TODO recuperare info da apim
      group.setBeneficiariesReached(null);
      groupQueryDAO.removeWhitelistByGroupId(groupId);
      groupRepository.save(group);
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
      if (resource.exists() || resource.isReadable()) {
        return resource;
      } else {
        throw new RuntimeException("Could not read the file!");
      }
    } catch (MalformedURLException e) {
      log.error("[READ_FILE_GROUP] - Could not read the file: " + filename, e);
      throw new RuntimeException(e);
    }
  }

  @Override
  public void delete(String organizationId, String filename) {
    try {
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
            MessageFormat.format(GroupConstants.Exception.NotFound.NO_GROUP_FOR_INITIATIVE_ID,
                initiativeId),
            HttpStatus.NOT_FOUND));
  }

  @Override
  public boolean getCitizenStatusByCitizenToken(String initiativeId, String citizenToken) {
    List<String> beneficiaryList = groupUserWhitelistRepository.findByInitiativeId(initiativeId)
        .stream().map(GroupUserWhitelist::getUserId).flatMap(List::stream).toList();
    if (CollectionUtils.isEmpty(beneficiaryList)) {
      throw new BeneficiaryGroupException(GroupConstants.Exception.NotFound.CODE,
          MessageFormat.format(
              GroupConstants.Exception.NotFound.NO_BENEFICIARY_LIST_PROVIDED_FOR_INITIATIVE_ID,
              initiativeId),
          HttpStatus.NOT_FOUND);
    }
    return beneficiaryList.stream().anyMatch(beneficiary -> beneficiary.equals(citizenToken));
  }

  @Override
  public void sendInitiativeNotificationForCitizen(String initiativeId, String initiativeName,
      String serviceId) {
    log.info("[NOTIFY_ALLOWED_CITIZEN] - [DB] Getting Group with allowed beneficiaries");
    List<String> beneficiaryTokenizedList = groupUserWhitelistRepository.findByInitiativeId(
        initiativeId).stream().map(GroupUserWhitelist::getUserId).flatMap(List::stream).toList();
    if (CollectionUtils.isEmpty(beneficiaryTokenizedList)) {
      throw new BeneficiaryGroupException(GroupConstants.Exception.NotFound.CODE,
          MessageFormat.format(
              GroupConstants.Exception.NotFound.NO_BENEFICIARY_LIST_PROVIDED_FOR_INITIATIVE_ID,
              initiativeId),
          HttpStatus.NOT_FOUND);
    }
    log.debug("[NOTIFY_ALLOWED_CITIZEN] - Getting of beneficiaries from Group -> DONE");
    notificationConnector.sendAllowedCitizen(beneficiaryTokenizedList, initiativeId, initiativeName,
        serviceId);
  }

}
