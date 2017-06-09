package th.co.krungthaiaxa.api.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import th.co.krungthaiaxa.api.auth.data.Role;
import th.co.krungthaiaxa.api.auth.repository.RoleRepository;

/**
 * @author tuong.le on 5/23/17.
 */
@Service
public class RoleService {
    private final RoleRepository roleRepository;

    @Autowired
    public RoleService(final RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Role getAuthorityById(final String roleId) {
        return roleRepository.findOne(roleId);
    }

    public Role createAuthority(final Role role) {
        return roleRepository.save(role);
    }

    public Page<Role> getAllRoles(final Pageable pageable) {
        return roleRepository.findAll(pageable);
    }
}
