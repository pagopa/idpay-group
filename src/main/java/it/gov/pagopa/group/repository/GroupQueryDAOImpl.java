package it.gov.pagopa.group.repository;

import it.gov.pagopa.group.constants.GroupConstants;
import it.gov.pagopa.group.model.Group;
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
}
