package it.gov.pagopa.group.repository;

import it.gov.pagopa.group.model.Group;
import it.gov.pagopa.group.model.Group.Fields;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

public class GroupRepositoryExtendedImpl implements GroupRepositoryExtended{
    private final MongoTemplate mongoTemplate;

    public GroupRepositoryExtendedImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<Group> deletePaged(String initiativeId, int pageSize) {
        Pageable pageable = PageRequest.of(0, pageSize);
        return mongoTemplate.findAllAndRemove(
                Query.query(Criteria.where(Fields.initiativeId).is(initiativeId)).with(pageable),
                Group.class
        );
    }
}
