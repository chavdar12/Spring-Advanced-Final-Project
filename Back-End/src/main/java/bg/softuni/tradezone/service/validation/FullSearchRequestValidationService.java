package bg.softuni.tradezone.service.validation;

import bg.softuni.tradezone.model.rest.search.FullSearchRequest;
import org.springframework.stereotype.Service;

@Service
public class FullSearchRequestValidationService implements ValidationService<FullSearchRequest> {

    @Override
    public boolean isValid(FullSearchRequest searchRequest) {
        return fieldsArePresent(searchRequest);
    }

    private boolean fieldsArePresent(FullSearchRequest searchRequest) {
        return searchRequest.getSearch() != null && searchRequest.getMax() != null
               && searchRequest.getMin() != null && searchRequest.getCategory() != null
               && searchRequest.getOrder() != null && searchRequest.getPage() != null
               && searchRequest.getSortBy() != null && searchRequest.getCondition() != null;
    }
}
