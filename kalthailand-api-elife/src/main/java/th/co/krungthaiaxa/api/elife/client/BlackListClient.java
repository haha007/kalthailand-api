package th.co.krungthaiaxa.api.elife.client;

import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.elife.data.BlackListed;
import th.co.krungthaiaxa.api.elife.repository.BlackListedRepository;

import javax.inject.Inject;

@Service
public class BlackListClient {
    @Inject
    private BlackListedRepository blackListedRepository;

    public BlackListed findByIdNumber(String idNumber) {
        return blackListedRepository.findByIdNumber(idNumber);
    }

    public void setBlackListedRepository(BlackListedRepository blackListedRepository) {
        this.blackListedRepository = blackListedRepository;
    }
}
