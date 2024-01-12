package it.gov.pagopa.group.config;

import it.gov.pagopa.common.web.exception.ServiceException;
import it.gov.pagopa.group.exception.GroupNotFoundOrNotValidStatusException;
import it.gov.pagopa.group.exception.GroupNotFoundException;
import it.gov.pagopa.group.exception.InitiativeStatusNotValidException;
import it.gov.pagopa.group.exception.BeneficiaryListNotProvidedException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class ServiceExceptionConfig {
    @Bean
    public Map<Class<? extends ServiceException>, HttpStatus> serviceExceptionMapper() {
        Map<Class<? extends ServiceException>, HttpStatus> exceptionMap = new HashMap<>();

        // NotFound
        exceptionMap.put(BeneficiaryListNotProvidedException.class, HttpStatus.NOT_FOUND);
        exceptionMap.put(GroupNotFoundException.class, HttpStatus.NOT_FOUND);
        exceptionMap.put(GroupNotFoundOrNotValidStatusException.class, HttpStatus.NOT_FOUND);

        // UnprocessableEntityException
        exceptionMap.put(InitiativeStatusNotValidException.class, HttpStatus.UNPROCESSABLE_ENTITY);

        return exceptionMap;
    }
}
