package th.co.krungthaiaxa.api.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.auth.data.Role;
import th.co.krungthaiaxa.api.auth.repository.RoleRepository;

/**
 * @author tuong.le on 5/23/17.
 */
@Service
public class RoleService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RoleService.class);
    private final RoleRepository roleRepository;

    @Autowired
    public RoleService(final RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Role getAuthorityByName(final String authorityName) {
        return roleRepository.findOne(authorityName);
    }

    public Role createAuthority(final Role role) {
        return roleRepository.save(role);
    }
}
