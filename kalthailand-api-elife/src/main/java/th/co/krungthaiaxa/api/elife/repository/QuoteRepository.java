package th.co.krungthaiaxa.api.elife.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import th.co.krungthaiaxa.api.elife.model.Quote;

@Repository
public interface QuoteRepository extends PagingAndSortingRepository<Quote, String> {
    Quote findByQuoteId(String quoteId);
}
