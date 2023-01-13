package it.gov.pagopa.group.repository;

import it.gov.pagopa.group.constants.GroupConstants;
import it.gov.pagopa.group.constants.GroupConstants.Status;
import it.gov.pagopa.group.model.Group;
import it.gov.pagopa.group.model.Group.Fields;
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
  public void setStatusOk(String initiativeId) {
    Query query = new Query(Criteria.where(Fields.initiativeId).is(initiativeId));
    Update update = new Update().set(Fields.status, GroupConstants.Status.OK);
    mongoTemplate.updateFirst(query, update, Group.class);
  }

  @Override
  public void setGroupForException(String initiativeId, String exceptionMessage,
      LocalDateTime elabDateTime, int retry) {
    Query query = new Query(Criteria.where(Fields.initiativeId).is(initiativeId));
    Update update = new Update().set(Fields.exceptionMessage, exceptionMessage)
        .set(Fields.elabDateTime, elabDateTime).set(Fields.retry, retry)
        .set(Fields.status, Status.PROC_KO).unset(Fields.beneficiaryList);
    mongoTemplate.updateFirst(query, update, Group.class);
  }

  @Override
  public void pushBeneficiaryList(String initiativeId, List<String> beneficiaryList) {
    Query query = new Query(Criteria.where(Fields.initiativeId).is(initiativeId));
    Update update = new Update().push("beneficiaryList").each(beneficiaryList);
    mongoTemplate.updateFirst(query, update, Group.class);
  }
}
