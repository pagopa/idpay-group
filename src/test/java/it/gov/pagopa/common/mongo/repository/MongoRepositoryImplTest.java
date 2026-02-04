package it.gov.pagopa.common.mongo.repository;

import it.gov.pagopa.group.model.GroupUserWhitelist;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.repository.query.MongoEntityInformation;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MongoRepositoryImplTest {

    @Mock
    MongoOperations mongoOperations;

    @Mock
    MongoEntityInformation<GroupUserWhitelist, String> entityInformation;

    MongoRepositoryImpl<GroupUserWhitelist, String> repository;

    @BeforeEach
    void setUp() {
        repository = new MongoRepositoryImpl<>(entityInformation, mongoOperations);

        when(entityInformation.getJavaType()).thenReturn(GroupUserWhitelist.class);
        when(entityInformation.getCollectionName()).thenReturn("testCollection");
    }

    @Test
    void findById_shouldReturnEntity_whenFound() {

        GroupUserWhitelist entity = new GroupUserWhitelist();
        entity.setId("ID");

        when(mongoOperations.find(any(), any(), any()))
                .thenReturn(List.of(entity));

        Optional<GroupUserWhitelist> result = repository.findById("ID");

        assertTrue(result.isPresent());
        assertEquals("ID", result.get().getId());
    }

    @Test
    void findById_shouldReturnEmpty_whenNotFound() {

        when(mongoOperations.find(any(), any(), any()))
                .thenReturn(List.of());

        Optional<GroupUserWhitelist> result = repository.findById("ID");

        assertTrue(result.isEmpty());
    }
}
