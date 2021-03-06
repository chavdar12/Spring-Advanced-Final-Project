package bg.softuni.tradezone.service.impl;

import bg.softuni.tradezone.model.entity.Role;
import bg.softuni.tradezone.model.enums.RoleName;
import bg.softuni.tradezone.model.service.RoleServiceModel;
import bg.softuni.tradezone.repository.RoleRepository;
import bg.softuni.tradezone.service.base.RoleService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class RoleServiceImpl implements RoleService {

    private static final List<Role> roles = List.of(new Role(RoleName.ROLE_USER),
                                                    new Role(RoleName.ROLE_MODERATOR),
                                                    new Role(RoleName.ROLE_ADMIN),
                                                    new Role(RoleName.ROLE_ROOT));

    private final RoleRepository roleRepository;
    private final ModelMapper mapper;

    @Override
    public void seed() {
        roleRepository.saveAll(roles);
    }

    @Override
    public boolean rolesAreSeeded() {
        return roleRepository.count() != 0;
    }

    @Override
    public List<RoleServiceModel> getAllExceptRoot() {
        return roleRepository.findAllByNameNotLike(RoleName.ROLE_ROOT).stream()
                .map(x -> mapper.map(x, RoleServiceModel.class))
                .collect(Collectors.toList());
    }
}
