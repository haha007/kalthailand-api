package th.co.krungthaiaxa.api.elife.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.elife.model.LineBC;
import th.co.krungthaiaxa.api.elife.repository.LineBCRepository;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Created by SantiLik on 4/4/2016.
 */

@Service
public class LineBCService {

    private final static Logger logger = LoggerFactory.getLogger(LineBCService.class);
    @Inject
    private LineBCRepository lineBCRepository;

    public Optional<LineBC> getLineBCInfo(String mid){

        if(isBlank(mid)){
            return Optional.empty();
        }

        if(logger.isDebugEnabled()){
            logger.debug(String.format("[%1$s] .....","getLineBCInfo"));
            logger.debug(String.format("MID is %1$s",mid));
        }

        Optional<List<Map<String,Object>>> data = lineBCRepository.getLineBC(mid);

        if(!data.isPresent()){
            return Optional.empty();
        }else{
            LineBC lineBC = new LineBC();
            lineBC.setDob((String)data.get().get(0).get("dob"));
            lineBC.setEmail((String)data.get().get(0).get("email"));
            lineBC.setFirstName((String)data.get().get(0).get("first_name"));
            lineBC.setLastName((String)data.get().get(0).get("last_name"));
            lineBC.setMobile((String)data.get().get(0).get("mobile"));
            lineBC.setPid((String)data.get().get(0).get("pid"));
            return Optional.of(lineBC);
        }

    }

}
