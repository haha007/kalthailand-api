package th.co.krungthaiaxa.elife.api.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.elife.api.model.Quote;

@Repository
public interface QuoteRepository extends PagingAndSortingRepository<Quote, String> {
    Quote findByQuoteId(String quoteId);
}
