package it.gov.pagopa.group.service;

import it.gov.pagopa.group.connector.notification.NotificationConnector;
import it.gov.pagopa.group.connector.pdv.PdvEncryptRestConnector;
import it.gov.pagopa.group.constants.GroupConstants;
import it.gov.pagopa.group.constants.GroupConstants.Status;
import it.gov.pagopa.group.dto.FiscalCodeTokenizedDTO;
import it.gov.pagopa.group.dto.PiiDTO;
import it.gov.pagopa.group.dto.event.QueueCommandOperationDTO;
import it.gov.pagopa.group.exception.*;
import it.gov.pagopa.group.model.Group;
import it.gov.pagopa.group.model.GroupUserWhitelist;
import it.gov.pagopa.group.repository.GroupQueryDAO;
import it.gov.pagopa.group.repository.GroupRepository;
import it.gov.pagopa.group.repository.GroupUserWhitelistRepository;
import it.gov.pagopa.group.utils.AuditUtilities;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
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
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.*;

import static it.gov.pagopa.group.constants.GroupConstants.ExceptionCode.*;
import static it.gov.pagopa.group.constants.GroupConstants.ExceptionMessage.*;

@Service
@Slf4j
public class BeneficiaryGroupServiceImpl implements BeneficiaryGroupService {
  private final AuditUtilities auditUtilities;
  @Value("${app.delete.paginationSize}")
  private int pageSize;
  @Value("${app.delete.delayTime}")
  private long delay;
  public static final String KEY_SEPARATOR = "_";
  private final String rootPath;
  private final boolean isFilesOnStorageToBeDeleted;
  private final GroupRepository groupRepository;
  private final GroupUserWhitelistRepository groupUserWhitelistRepository;
  private final PdvEncryptRestConnector pdvEncryptRestConnector;
  private final GroupQueryDAO groupQueryDAO;
  private final Clock clock;
  private final NotificationConnector notificationConnector;

  public BeneficiaryGroupServiceImpl(
          AuditUtilities auditUtilities, @Value("${file.storage.path}") String rootPath,
          @Value("${file.storage.deletion}") boolean isFilesOnStorageToBeDeleted,
          GroupRepository groupRepository,
          GroupUserWhitelistRepository groupUserWhitelistRepository,
          PdvEncryptRestConnector pdvEncryptRestConnector,
          GroupQueryDAO groupQueryDAO,
          Clock clock,
          NotificationConnector notificationConnector) {
    this.auditUtilities = auditUtilities;
    this.rootPath = rootPath;
    this.isFilesOnStorageToBeDeleted = isFilesOnStorageToBeDeleted;
    this.groupRepository = groupRepository;
    this.groupUserWhitelistRepository = groupUserWhitelistRepository;
    this.pdvEncryptRestConnector = pdvEncryptRestConnector;
    this.groupQueryDAO = groupQueryDAO;
    this.clock = clock;
    this.notificationConnector = notificationConnector;
  }

  private void init(String organizationId) {
    try {
      Path root = Paths.get(rootPath + File.separator + organizationId);
      Files.createDirectory(root);
    } catch (IOException e) {
      throw new FolderInitializeException(GROUP_FOLDER_INITIALIZE_ERROR, GROUP_FOLDER_INITIALIZE_NOT_POSSIBLE);
    }
  }

  @Scheduled(fixedRate = 10000, initialDelay = 130000)
  public void scheduleValidatedGroup() throws IOException {
    boolean anonymizationDone = false;
    Group group = groupQueryDAO.findFirstByStatusAndUpdate(GroupConstants.Status.VALIDATED);
    // TODO check for null?       if(groupOptional.isPresent()) {
    if (null != group) {
      groupQueryDAO.removeWhitelistByGroupId(group.getGroupId());
      String fileName = group.getFileName();
      log.info(
          "[GROUP_SCHEDULING] [ANONYMIZER] Found beneficiary's group for {} with status {} on Organization {}",
          fileName,
          GroupConstants.Status.VALIDATED,
          group.getOrganizationId());
      Resource file = load(group.getOrganizationId(), fileName);
      List<GroupUserWhitelist> anonymousCFlist = null;
      try {
        anonymousCFlist = cfAnonymizer(file, group.getGroupId(), group.getInitiativeId());
        pushBeneficiaryListToDb(anonymousCFlist);
        groupQueryDAO.setStatusOk(group.getInitiativeId(), anonymousCFlist.size());
        anonymizationDone = true;
      } catch (Exception e) {
        groupQueryDAO.setGroupForException(
            group.getInitiativeId(), e.getMessage(), LocalDateTime.now(clock), 1);
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
    Optional<Group> groupOptional =
        groupRepository.findFirstByStatusAndRetryLessThan(GroupConstants.Status.PROC_KO, 3);
    if (groupOptional.isPresent()) {
      Group group = groupOptional.get();
      String fileName = group.getFileName();
      groupQueryDAO.removeWhitelistByGroupId(group.getGroupId());
      log.info(
          "[GROUP_SCHEDULING] [ANONYMIZER] Found beneficiary's group for {} with status {} on Organization {}",
          fileName,
          GroupConstants.Status.PROC_KO,
          group.getOrganizationId());
      Resource file = load(group.getOrganizationId(), fileName);
      List<GroupUserWhitelist> anonymousCFlist = null;
      try {
        log.info("Retry to communicate with PDV num: {}", group.getRetry() + 1);
        anonymousCFlist = cfAnonymizer(file, group.getGroupId(), group.getInitiativeId());
        pushBeneficiaryListToDb(anonymousCFlist);
        groupQueryDAO.setStatusOk(group.getInitiativeId(), anonymousCFlist.size());
        anonymizationDone = true;
      } catch (Exception e) {
        groupQueryDAO.setGroupForException(
            group.getInitiativeId(),
            e.getMessage(),
            LocalDateTime.now(clock),
            group.getRetry() + 1);
        groupQueryDAO.removeWhitelistByGroupId(group.getGroupId());
      }
      if (isFilesOnStorageToBeDeleted && (anonymizationDone || group.getRetry() >= 3)) {
        delete(group.getOrganizationId(), fileName);
      }
    }
  }

  private void pushBeneficiaryListToDb(List<GroupUserWhitelist> anonymousCFList) {

    long start = System.currentTimeMillis();
    int size = anonymousCFList.size();

    log.info(
        "[GROUP_SCHEDULING] [ANONYMIZER] Pushing beneficiary list to database [rows: {}]", size);

    groupQueryDAO.pushBeneficiaryList(anonymousCFList);

    long end = System.currentTimeMillis();

    log.info(
        "[GROUP_SCHEDULING] [ANONYMIZER] Time spent pushing {} rows to db: {}", size, end - start);
  }

  private List<GroupUserWhitelist> cfAnonymizer(Resource file, String groupId, String initiativeId)
      throws Exception {
    List<GroupUserWhitelist> anonymousCFlist = new ArrayList<>();
    String line;
    InputStream is = file.getInputStream();
    try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
      long before = System.currentTimeMillis();
      int lineCounter = 0;
      while ((line = br.readLine()) != null) {
        lineCounter++;
        FiscalCodeTokenizedDTO fiscalCodeTokenizedDTO =
            pdvEncryptRestConnector.putPii(PiiDTO.builder().pii(line.toUpperCase()).build());
        anonymousCFlist.add(
            new GroupUserWhitelist(null, groupId, initiativeId, fiscalCodeTokenizedDTO.getToken()));
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
      Files.copy(
          file.getInputStream(),
          root.resolve(Objects.requireNonNull(file.getOriginalFilename())),
          StandardCopyOption.REPLACE_EXISTING);
      Group group = new Group();
      group.setGroupId(groupId);
      group.setInitiativeId(initiativeId);
      group.setOrganizationId(organizationId);
      group.setStatus(status);
      group.setFileName(file.getOriginalFilename());
      group.setCreationDate(LocalDateTime.now(clock));
      group.setUpdateDate(LocalDateTime.now(clock));
      group.setCreationUser("admin"); // TODO recuperare info da apim
      group.setUpdateUser("admin"); // TODO recuperare info da apim
      group.setBeneficiariesReached(null);
      groupRepository.save(group);
    } catch (Exception e) {
      throw new FileSaveException(GROUP_SAVE_FILE_FAILED, GROUP_SAVE_NOT_POSSIBLE);
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
        throw new FileLoadException(GROUP_LOAD_FILE_ERROR, GROUP_READ_NOT_POSSIBLE);
      }
    } catch (MalformedURLException e) {
      log.error("[READ_FILE_GROUP] - Could not read the file: " + filename, e);
      throw new FileLoadException(GROUP_LOAD_FILE_ERROR, GROUP_READ_NOT_POSSIBLE);
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
      throw new FileDeleteException(GROUP_DELETE_FILE_FAILED, GROUP_DELETE_NOT_POSSIBLE);
    }
  }

  @Override
  public Group getStatusByInitiativeId(String initiativeId, String organizationId) {
    return groupRepository
        .getStatus(initiativeId, organizationId)
        .orElseThrow(() -> new GroupNotFoundException(NOT_FOUND,
                String.format(GROUP_NOT_FOUND_FOR_INITIATIVE, initiativeId)));
  }

  @Override
  public boolean getCitizenStatusByCitizenToken(String initiativeId, String citizenToken) {
    List<String> beneficiaryList =
        groupUserWhitelistRepository.findByInitiativeId(initiativeId).stream()
            .map(GroupUserWhitelist::getUserId)
            .toList();
    if (CollectionUtils.isEmpty(beneficiaryList)) {
      throw new BeneficiaryListNotProvidedException(GROUP_BENEFICIARY_LIST_NOT_PROVIDED,
              String.format(NOT_BENEFICIARY_LIST_PROVIDED_FOR_INITIATIVE, initiativeId));
    }
    return beneficiaryList.stream().anyMatch(beneficiary -> beneficiary.equals(citizenToken));
  }

  @Override
  public void sendInitiativeNotificationForCitizen(
      String initiativeId, String initiativeName, String serviceId) {
    log.info("[NOTIFY_ALLOWED_CITIZEN] - [DB] Getting Group with allowed beneficiaries");
    List<String> beneficiaryTokenizedList =
        groupUserWhitelistRepository.findByInitiativeId(initiativeId).stream()
            .map(GroupUserWhitelist::getUserId)
            .toList();
    if (CollectionUtils.isEmpty(beneficiaryTokenizedList)) {
      throw new BeneficiaryListNotProvidedException(GROUP_BENEFICIARY_LIST_NOT_PROVIDED,
              String.format(NOT_BENEFICIARY_LIST_PROVIDED_FOR_INITIATIVE, initiativeId));
    }
    log.debug("[NOTIFY_ALLOWED_CITIZEN] - Getting of beneficiaries from Group -> DONE");
    notificationConnector.sendAllowedCitizen(
        beneficiaryTokenizedList, initiativeId, initiativeName, serviceId);
  }

  @Override
  public void setStatusToValidated(String initiativeId) {
    Group group =
        groupRepository
            .findByInitiativeIdAndStatusIn(initiativeId, List.of(Status.DRAFT, Status.OK))
            .orElseThrow(
                () -> new GroupNotFoundOrNotValidStatusException(GROUP_NOT_FOUND_OR_STATUS_NOT_VALID,
                        String.format(GROUP_NOT_FOUND_FOR_INITIATIVE_OR_STATUS_NOT_VALID, initiativeId)));
    group.setStatus(Status.VALIDATED);
    groupRepository.save(group);
  }

  @Override
  public void processCommand(QueueCommandOperationDTO queueCommandOperationDTO) {
    long startTime = System.currentTimeMillis();
    if (GroupConstants.OPERATION_TYPE_DELETE_INITIATIVE.equals(queueCommandOperationDTO.getOperationType())) {
      deleteGroupRepo(queueCommandOperationDTO);
      deleteGroupWhitelistRepo(queueCommandOperationDTO);
      performanceLog(startTime, "DELETE_INITIATIVE");
    }
  }

  private void deleteGroupRepo(QueueCommandOperationDTO queueCommandOperationDTO) {

    List<Group> deletedOperation = new ArrayList<>();
    List<Group> fetchedGroups;

    do {
      fetchedGroups = groupRepository.deletePaged(queueCommandOperationDTO.getEntityId(),
              pageSize);

      deletedOperation.addAll(fetchedGroups);

      try {
        TimeUnit.MILLISECONDS.sleep(delay);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        log.error("An error has occurred while waiting {}", e.getMessage());
      }

    } while (fetchedGroups.size() == pageSize);

    log.info("[DELETE_INITIATIVE] Deleted initiative {} from collection: group",
            queueCommandOperationDTO.getEntityId());

    deletedOperation.forEach(group -> auditUtilities.logDeleteGroupOperation(queueCommandOperationDTO.getEntityId(),
            group.getFileName()));
  }

  private void deleteGroupWhitelistRepo(QueueCommandOperationDTO queueCommandOperationDTO) {

    List<GroupUserWhitelist> deletedOperation = new ArrayList<>();
    List<GroupUserWhitelist> fetchedGroups;


    do {
      fetchedGroups = groupUserWhitelistRepository.deletePaged(queueCommandOperationDTO.getEntityId(),
              pageSize);

      deletedOperation.addAll(fetchedGroups);

      try {
        TimeUnit.MILLISECONDS.sleep(delay);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        log.error("An error has occurred while waiting {}", e.getMessage());
      }

    } while (fetchedGroups.size() == pageSize);

    log.info("[DELETE_INITIATIVE] Deleted initiative {} from collection: group_user_whitelist",
            queueCommandOperationDTO.getEntityId());

    deletedOperation.forEach(groupUser -> auditUtilities.logDeleteGroupWhitelistOperation(groupUser.getUserId(),
            queueCommandOperationDTO.getEntityId()));
  }

  private void performanceLog(long startTime, String service) {
    log.info(
            "[PERFORMANCE_LOG] [{}] Time occurred to perform business logic: {} ms",
            service,
            System.currentTimeMillis() - startTime);
  }
}
