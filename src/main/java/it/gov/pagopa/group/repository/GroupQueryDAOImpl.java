package it.gov.pagopa.group.repository;

import it.gov.pagopa.group.constants.GroupConstants;
import it.gov.pagopa.group.model.Group;
import it.gov.pagopa.group.model.Group.Fields;
import it.gov.pagopa.group.model.GroupUserWhitelist;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

@Repository
public class GroupQueryDAOImpl implements GroupQueryDAO {

  @Autowired
  private MongoTemplate mongoTemplate;

  @Override
  public Group findFirstByStatusAndUpdate(String status) {
    Query query = new Query(Criteria.where("status").is(GroupConstants.Status.VALIDATED));
    Update update = new Update().set("status", GroupConstants.Status.PROCESSING);
    return mongoTemplate.update(Group.class)
        .matching(query)
        .apply(update)
        .withOptions(FindAndModifyOptions.options().returnNew(true))
        .findAndModifyValue();
  }

  @Override
  public void setStatusOk(String initiativeId, int beneficiariesReached) {
    Query query = new Query(Criteria.where(Fields.initiativeId).is(initiativeId));
    Update update = new Update().set(Fields.status, GroupConstants.Status.OK)
        .set(Fields.beneficiariesReached, beneficiariesReached);
    mongoTemplate.updateFirst(query, update, Group.class);
  }

  @Override
  public void setGroupForException(String initiativeId, String exceptionMessage,
      LocalDateTime elabDateTime, int retry) {
    Query query = new Query(Criteria.where(Fields.initiativeId).is(initiativeId));
    Update update = new Update().set(Fields.exceptionMessage, exceptionMessage)
        .set(Fields.elabDateTime, elabDateTime).set(Fields.retry, retry)
        .unset(Fields.beneficiariesReached);
    mongoTemplate.updateFirst(query, update, Group.class);
  }

  @Override
  public void removeWhitelistByGroupId(String groupId) {
    Query query = new Query(Criteria.where(Fields.groupId).is(groupId)).limit(1000);
    boolean deletedAll = false;
    while (!deletedAll) {
      List<String> toDelete = mongoTemplate.find(query, GroupUserWhitelist.class).stream()
          .map(GroupUserWhitelist::getId).toList();
      long count = mongoTemplate.remove(
          new Query(Criteria.where(GroupUserWhitelist.Fields.id).in(toDelete)),
          GroupUserWhitelist.class).getDeletedCount();
      deletedAll = count == 0;
    }
  }

  @Override
  public void pushBeneficiaryList(List<GroupUserWhitelist> beneficiaryList) {
    mongoTemplate.insertAll(beneficiaryList);
  }
}
