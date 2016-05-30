package th.co.krungthaiaxa.api.elife.client;

import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.elife.repository.LineBCRepository;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class LineBCClient {
    @Inject
    private LineBCRepository lineBCRepository;

    public Optional<List<Map<String, Object>>> getLineBC(String mid) {
        return lineBCRepository.getLineBC(mid);
    }

    public void setLineBCRepository(LineBCRepository lineBCRepository) {
        this.lineBCRepository = lineBCRepository;
    }
}
